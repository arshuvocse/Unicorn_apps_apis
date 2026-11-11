-- DCR area lock (chamber geofence, can be lifted per chamber) + DCR tour-plan gate.
-- Re-runnable. Run sp_Webapi_Save_DayWiseTourPlan.sql FIRST (creates the day-wise tour plan tables).
--
-- Area lock (doctor DCR only): once a chamber has a location (tblDoctorChemberDetail.Lat/Lon, set by
-- its first DCR), a DCR at that chamber must be submitted within 200 m of it. Lift the lock for one
-- chamber with sp_Webapi_Save_ChamberLock (isLock = 0); NULL or 1 = locked.
-- tbl_DcrAreaLockSetup (per employee, IsLockOff = 1) now only widens the doctor/customer/chamber
-- sync lists; it no longer blocks a DCR.
-- Tour plan gate (CVR): the customer must be in that day's day-wise tour plan.
-- Tour plan gate (doctor DCR): the doctor must be in that day's tour plan. A day-wise plan
-- row with ChamberId (chamber-wise) also pins the chamber; ChamberId NULL (doctor-wise / old rows)
-- allows any of the doctor's chambers. The old monthly plan (tbl_DoctorTourPlanDetail) still counts.
-- A doctor and a customer with the same code (DoctorCode = CustomerCode) count as one: planned as
-- either kind that day, a DCR or a CVR for it is allowed.

SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

IF OBJECT_ID(N'dbo.tbl_DcrAreaLockSetup', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.tbl_DcrAreaLockSetup(
        AreaLockId INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_tbl_DcrAreaLockSetup PRIMARY KEY,
        EmpInfoId INT NOT NULL CONSTRAINT UQ_tbl_DcrAreaLockSetup_Emp UNIQUE,
        IsLockOff BIT NOT NULL CONSTRAINT DF_tbl_DcrAreaLockSetup_IsLockOff DEFAULT (0),
        Remarks NVARCHAR(250) NULL,
        EntryBy INT NULL,
        EntryDate DATETIME NOT NULL CONSTRAINT DF_tbl_DcrAreaLockSetup_EntryDate DEFAULT (GETDATE()),
        UpdateBy INT NULL,
        UpdateDate DATETIME NULL
    );
END
GO

-- the geofence reads these (already in the live DB; added only where missing)
IF COL_LENGTH('dbo.tblDoctorChemberDetail', 'isLock') IS NULL
    ALTER TABLE dbo.tblDoctorChemberDetail ADD isLock BIT NULL;
IF COL_LENGTH('dbo.tblDoctorChemberDetail', 'Lat') IS NULL
    ALTER TABLE dbo.tblDoctorChemberDetail ADD Lat NVARCHAR(MAX) NULL;
IF COL_LENGTH('dbo.tblDoctorChemberDetail', 'Lon') IS NULL
    ALTER TABLE dbo.tblDoctorChemberDetail ADD Lon NVARCHAR(MAX) NULL;
GO

-- The tour-plan gate looks up the old monthly plan by employee + doctor + date on every DCR save.
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'IX_DocTPDetail_Emp_Doctor_Date' AND object_id = OBJECT_ID(N'dbo.tbl_DoctorTourPlanDetail'))
    CREATE NONCLUSTERED INDEX IX_DocTPDetail_Emp_Doctor_Date ON dbo.tbl_DoctorTourPlanDetail (EmpInfoId, DoctorId, TourPlanDate);
GO

-- Territory-based version of the lock, replaced by the chamber geofence below.
IF OBJECT_ID(N'dbo.fn_Webapi_DcrAreaAllowed') IS NOT NULL
    DROP FUNCTION dbo.fn_Webapi_DcrAreaAllowed;
GO

-- '' = DCR/CVR allowed, otherwise the reason shown to the user.
CREATE OR ALTER FUNCTION dbo.fn_Webapi_DcrGateMessage
(
    @empId INT,
    @doctorId INT,
    @chamberId INT,
    @dcrDate DATE,
    @Type NVARCHAR(10),   -- 'DCR' or 'CVR'
    @Latitude NVARCHAR(50),
    @Longitude NVARCHAR(50)
)
RETURNS NVARCHAR(500)
AS
BEGIN
    DECLARE @dateText NVARCHAR(20) = CONVERT(NVARCHAR(20), @dcrDate, 106);
    DECLARE @radiusM INT = 200;   -- DCR area lock: allowed distance from the chamber, in metres

    -- CVR: the "chamber" is the customer itself (DoctorId = ChamberId = CustomerMasterId); no area lock,
    -- but the customer must be in that day's tour plan, same as a doctor.
    IF @Type = 'CVR'
    BEGIN
        IF EXISTS (SELECT 1 FROM dbo.tbl_DayWiseTourPlanMaster M WITH (NOLOCK)
                   INNER JOIN dbo.tbl_DayWiseTourPlanCustomerDetail CD WITH (NOLOCK) ON CD.TourPlanMasterId = M.TourPlanMasterId
                   WHERE M.EmpInfoId = @empId AND M.TourPlanDate = @dcrDate AND CD.CustomerMasterId = @doctorId)
            RETURN N'';
        -- Same code planned as a doctor that day (one shop kept as both a customer and a doctor).
        IF EXISTS (SELECT 1 FROM dbo.tblCustMaster C WITH (NOLOCK)
                   INNER JOIN dbo.View_DoctorMaster D WITH (NOLOCK) ON D.DoctorCode = C.CustomerCode
                   INNER JOIN dbo.tbl_DayWiseTourPlanDoctorDetail DD WITH (NOLOCK) ON DD.DoctorId = D.DoctorId
                   INNER JOIN dbo.tbl_DayWiseTourPlanMaster M WITH (NOLOCK) ON M.TourPlanMasterId = DD.TourPlanMasterId
                   WHERE C.CustomerMasterId = @doctorId AND NULLIF(LTRIM(RTRIM(C.CustomerCode)), '') IS NOT NULL
                     AND M.EmpInfoId = @empId AND M.TourPlanDate = @dcrDate)
            RETURN N'';
        RETURN N'This customer is not in your tour plan for ' + @dateText + N'. Add the customer to your day-wise tour plan first.';
    END;

    IF ISNULL(@chamberId, 0) > 0
    BEGIN
        DECLARE @found BIT = 0, @locked BIT, @cLat FLOAT, @cLon FLOAT;
        SELECT @found = 1,
               @locked = ISNULL(isLock, 1),
               @cLat = TRY_CONVERT(FLOAT, NULLIF(LTRIM(RTRIM(Lat)), '')),
               @cLon = TRY_CONVERT(FLOAT, NULLIF(LTRIM(RTRIM(Lon)), ''))
        FROM dbo.tblDoctorChemberDetail WITH (NOLOCK)
        WHERE ChemberId = @chamberId AND DoctorId = @doctorId;

        IF @found = 0
            RETURN N'The selected chamber does not belong to this doctor.';

        -- Locked chamber with a usable saved location (none yet = this DCR sets it, see sp_Webapi_Set_ChamberLocationIfEmpty).
        IF @locked = 1
           AND @cLat BETWEEN -90 AND 90 AND @cLon BETWEEN -180 AND 180 AND NOT (@cLat = 0 AND @cLon = 0)
        BEGIN
            DECLARE @lat FLOAT = TRY_CONVERT(FLOAT, NULLIF(LTRIM(RTRIM(@Latitude)), ''));
            DECLARE @lon FLOAT = TRY_CONVERT(FLOAT, NULLIF(LTRIM(RTRIM(@Longitude)), ''));

            IF @lat IS NULL OR @lon IS NULL OR @lat NOT BETWEEN -90 AND 90 OR @lon NOT BETWEEN -180 AND 180 OR (@lat = 0 AND @lon = 0)
                RETURN N'DCR area lock: your current location was not received. Turn on GPS and try again.';

            DECLARE @distM FLOAT = geography::Point(@cLat, @cLon, 4326).STDistance(geography::Point(@lat, @lon, 4326));
            IF @distM > @radiusM
                RETURN N'DCR area lock: you are ' + CAST(CAST(ROUND(@distM, 0) AS INT) AS NVARCHAR(20))
                     + N' m away from this chamber (allowed ' + CAST(@radiusM AS NVARCHAR(10))
                     + N' m). Go to the chamber, or ask your admin to lift this chamber''s lock.';
        END;
    END;

    -- Day-wise tour plan rows for this doctor decide (chamber-wise when ChamberId is set).
    IF EXISTS (SELECT 1 FROM dbo.tbl_DayWiseTourPlanMaster M WITH (NOLOCK)
               INNER JOIN dbo.tbl_DayWiseTourPlanDoctorDetail DD WITH (NOLOCK) ON DD.TourPlanMasterId = M.TourPlanMasterId
               WHERE M.EmpInfoId = @empId AND M.TourPlanDate = @dcrDate AND DD.DoctorId = @doctorId)
    BEGIN
        IF NOT EXISTS (SELECT 1 FROM dbo.tbl_DayWiseTourPlanMaster M WITH (NOLOCK)
                       INNER JOIN dbo.tbl_DayWiseTourPlanDoctorDetail DD WITH (NOLOCK) ON DD.TourPlanMasterId = M.TourPlanMasterId
                       WHERE M.EmpInfoId = @empId AND M.TourPlanDate = @dcrDate AND DD.DoctorId = @doctorId
                         AND (DD.ChamberId IS NULL OR DD.ChamberId = @chamberId))
            RETURN N'This chamber is not in your tour plan for ' + @dateText + N'. Select the chamber planned for this doctor.';
        RETURN N'';
    END;

    -- Backwards compatibility: the old monthly tour plan.
    IF EXISTS (SELECT 1 FROM dbo.tbl_DoctorTourPlanDetail T WITH (NOLOCK)
               WHERE T.EmpInfoId = @empId AND T.DoctorId = @doctorId
                 AND T.TourPlanDate >= @dcrDate AND T.TourPlanDate < DATEADD(DAY, 1, @dcrDate))
        RETURN N'';

    -- Same code planned as a customer that day (one shop kept as both a customer and a doctor).
    IF EXISTS (SELECT 1 FROM dbo.View_DoctorMaster D WITH (NOLOCK)
               INNER JOIN dbo.tblCustMaster C WITH (NOLOCK) ON C.CustomerCode = D.DoctorCode
               INNER JOIN dbo.tbl_DayWiseTourPlanCustomerDetail CD WITH (NOLOCK) ON CD.CustomerMasterId = C.CustomerMasterId
               INNER JOIN dbo.tbl_DayWiseTourPlanMaster M WITH (NOLOCK) ON M.TourPlanMasterId = CD.TourPlanMasterId
               WHERE D.DoctorId = @doctorId AND NULLIF(LTRIM(RTRIM(D.DoctorCode)), '') IS NOT NULL
                 AND M.EmpInfoId = @empId AND M.TourPlanDate = @dcrDate)
        RETURN N'';

    RETURN N'This doctor is not in your tour plan for ' + @dateText + N'. Add the doctor to your day-wise tour plan first.';
END;
GO

-- Pre-check used by the DCR save API so the app can show the reason (isValiCheck + Msd).
CREATE OR ALTER PROCEDURE dbo.sp_Webapi_Check_DcrGate
    @doctorId INT,
    @chamberId INT = NULL,
    @sessionUser NVARCHAR(50),
    @dcrDate NVARCHAR(50),
    @Type NVARCHAR(MAX) = NULL,
    @Latitude NVARCHAR(50) = NULL,
    @Longitude NVARCHAR(50) = NULL
AS
BEGIN
    SET NOCOUNT ON;
    DECLARE @clean NVARCHAR(50) = REPLACE(LTRIM(RTRIM(@dcrDate)), 'Sept', 'Sep');
    DECLARE @date DATE = COALESCE(TRY_CONVERT(DATETIME, NULLIF(@clean, ''), 106), TRY_CONVERT(DATETIME, NULLIF(@clean, '')));

    IF @date IS NULL
    BEGIN
        SELECT N'' AS Msg; -- the save SP reports the invalid date
        RETURN;
    END;

    SELECT dbo.fn_Webapi_DcrGateMessage(
        TRY_CONVERT(INT, @sessionUser), @doctorId, @chamberId, @date,
        CASE WHEN LOWER(LTRIM(RTRIM(ISNULL(@Type, '')))) = 'ccradapter' THEN 'CVR' ELSE 'DCR' END,
        @Latitude, @Longitude) AS Msg;
END;
GO

-- Admin: lift a chamber's DCR area lock (@IsLock = 0) or put it back (@IsLock = 1).
CREATE OR ALTER PROCEDURE dbo.sp_Webapi_Save_ChamberLock
    @ChamberId INT,
    @IsLock BIT
AS
BEGIN
    SET NOCOUNT ON;
    UPDATE dbo.tblDoctorChemberDetail SET isLock = @IsLock WHERE ChemberId = @ChamberId;

    IF @@ROWCOUNT = 0
        SELECT 0 AS IsSuccess, N'Chamber not found.' AS Message;
    ELSE
        SELECT 1 AS IsSuccess,
               CASE WHEN @IsLock = 1 THEN N'Chamber DCR area lock turned ON.' ELSE N'Chamber DCR area lock lifted.' END AS Message;
END;
GO

-- Admin: area lock list (@EmpInfoId = 0 -> every employee that has a setup row) or one employee.
CREATE OR ALTER PROCEDURE dbo.sp_Webapi_Get_DcrAreaLock
    @EmpInfoId INT = 0
AS
BEGIN
    SET NOCOUNT ON;
    SELECT E.EmpInfoId, E.EmpMasterCode, E.EmpName,
           CAST(ISNULL(L.IsLockOff, 0) AS BIT) AS IsLockOff,
           L.Remarks, ISNULL(L.UpdateDate, L.EntryDate) AS LastChanged
    FROM dbo.tblEmpGeneralInfo E WITH (NOLOCK)
    LEFT JOIN dbo.tbl_DcrAreaLockSetup L WITH (NOLOCK) ON L.EmpInfoId = E.EmpInfoId
    WHERE (ISNULL(@EmpInfoId, 0) = 0 AND L.EmpInfoId IS NOT NULL)
       OR E.EmpInfoId = @EmpInfoId
    ORDER BY E.EmpName;
END;
GO

-- Admin: turn an employee's DCR area lock off (@IsLockOff = 1) or back on (@IsLockOff = 0).
CREATE OR ALTER PROCEDURE dbo.sp_Webapi_Save_DcrAreaLock
    @EmpInfoId INT,
    @IsLockOff BIT,
    @Remarks NVARCHAR(250) = NULL,
    @EntryBy INT = NULL
AS
BEGIN
    SET NOCOUNT ON;
    IF NOT EXISTS (SELECT 1 FROM dbo.tblEmpGeneralInfo WHERE EmpInfoId = @EmpInfoId)
    BEGIN
        SELECT 0 AS IsSuccess, N'Employee not found.' AS Message;
        RETURN;
    END;

    IF EXISTS (SELECT 1 FROM dbo.tbl_DcrAreaLockSetup WHERE EmpInfoId = @EmpInfoId)
        UPDATE dbo.tbl_DcrAreaLockSetup
        SET IsLockOff = @IsLockOff, Remarks = @Remarks, UpdateBy = @EntryBy, UpdateDate = GETDATE()
        WHERE EmpInfoId = @EmpInfoId;
    ELSE
        INSERT INTO dbo.tbl_DcrAreaLockSetup (EmpInfoId, IsLockOff, Remarks, EntryBy, EntryDate)
        VALUES (@EmpInfoId, @IsLockOff, @Remarks, @EntryBy, GETDATE());

    SELECT 1 AS IsSuccess,
           CASE WHEN @IsLockOff = 1 THEN N'DCR area lock turned OFF.' ELSE N'DCR area lock turned ON.' END AS Message;
END;
GO

-- Offline chamber sync for AddDCRActivity: unchanged area rule, plus every active doctor's chambers
-- when the employee's area lock is off.
CREATE OR ALTER PROCEDURE [dbo].[sp_Webapi_Get_ChamberName]
	@empid INT
AS
BEGIN
	DECLARE @roleType NVARCHAR(10);
	DECLARE @lockOff BIT = ISNULL((SELECT IsLockOff FROM dbo.tbl_DcrAreaLockSetup WITH (NOLOCK) WHERE EmpInfoId = @empid), 0);

    -- Get RoleType of employee
    SELECT @roleType = CAST(UR.RoleTypeId AS NVARCHAR(10))
    FROM dbo.tblUser U WITH (NOLOCK)
    LEFT JOIN dbo.tbl_UserRoleInfo UR WITH (NOLOCK)
        ON UR.UserRoleID = U.UserRoleID
    WHERE U.EmpInfoId = @empId;

	SELECT distinct
        tblDoctorChemberDetail.ChemberId AS ChemberId,
        ISNULL(tblDoctorChemberDetail.Name, 'N/A') AS ChemberName,
        tblDoctorChemberDetail.DoctorId,
        ISNULL(tblDoctorChemberDetail.Shift, '') AS Shift,
        ISNULL(tblDoctorChemberDetail.SittingTime, '') AS SittingTime,
        ISNULL(tblDoctorChemberDetail.Address, '') AS Address,
        tblDoctorChemberDetail.InstitutionId,
        tblDoctorChemberDetail.Lat,
        tblDoctorChemberDetail.Lon
    FROM dbo.tblDoctorChemberDetail WITH (NOLOCK)
	INNER JOIN dbo.tblDoctorMaster doc WITH (NOLOCK) ON doc.DoctorId = tblDoctorChemberDetail.DoctorId
	WHERE (doc.IsActive = '1')
	AND
	(
		@lockOff = 1
		OR
		(
			-- RoleType 1: Own doctor list + TaggedSalesPersonIds
			@roleType = '1'
			AND
			(
				doc.SalesPersonId = @empId
				OR ',' + ISNULL(doc.TaggedSalesPersonIds,'') + ',' LIKE '%,' + CAST(@empId AS NVARCHAR(10)) + ',%'
			)
		)
		OR
		(
			-- RoleType 2: Area wise all MIO doctor list + own list
			@roleType = '2'
			AND
			(
				doc.SalesPersonId = @empId
				OR ',' + ISNULL(doc.TaggedSalesPersonIds,'') + ',' LIKE '%,' + CAST(@empId AS NVARCHAR(10)) + ',%'
				OR
				doc.SalesPersonId IN
				(
					SELECT DISTINCT EFI2.MIOEmpId
					FROM View_Webapi_EmployeeFieldForceInfo EFI2 WITH (NOLOCK)
					WHERE EFI2.TerritoryId IN
					(
						SELECT T.TerritoryId
						FROM dbo.tblTerritory T WITH (NOLOCK)
						WHERE T.AreaId IN
						(
							SELECT DISTINCT EFI.AreaId
							FROM View_Webapi_EmployeeFieldForceInfo EFI WITH (NOLOCK)
							WHERE EFI.EmpInfoId = @empId
						)
					)
					AND EFI2.MIOEmpId IS NOT NULL
				)
			)
		)
		OR
		(
			-- RoleType 3: All doctor list
			@roleType = '3'
			AND doc.SalesPersonId IS NOT NULL
		)
	);
END
GO

-- DCR save: live definition + area lock / tour plan gate (THROW 50010 with the reason).
CREATE OR ALTER PROCEDURE [dbo].[sp_Webapi_Save_DcrInfo]
    @doctorId INT,
    @dcrDate NVARCHAR(50),
    @EntryTime NVARCHAR(50) = NULL,
    @visitTypeId INT = NULL,
    @chamberId INT = NULL,
    @sessionUser NVARCHAR(50),
    @remarks NVARCHAR(50) = NULL,
    @DocTPDetailsId INT = 0,
    @EntryDate_Apps NVARCHAR(50) = NULL,
    @Latitude NVARCHAR(MAX) = NULL,
    @Longitude NVARCHAR(MAX) = NULL,
    @StreetAddress NVARCHAR(MAX) = NULL,
    @Type NVARCHAR(MAX) = NULL
AS
BEGIN
    SET NOCOUNT ON;

    DECLARE @Id INT = 0;
    DECLARE @DcrDateValue DATETIME;
    DECLARE @EntryDateAppsValue DATETIME;
    DECLARE @EntryTimeValue TIME(7);

    ----------------------------------------------------------------
    -- Normalize non-standard 4-letter month abbreviations (e.g. "Sept" -> "Sep")
    -- so TRY_CONVERT with style 106 (dd MMM yyyy) doesn't fail.
    ----------------------------------------------------------------
    DECLARE @dcrDateClean NVARCHAR(50) = LTRIM(RTRIM(@dcrDate));
    DECLARE @EntryDateAppsClean NVARCHAR(50) = LTRIM(RTRIM(@EntryDate_Apps));

    SET @dcrDateClean = REPLACE(@dcrDateClean, 'Sept', 'Sep');
    SET @EntryDateAppsClean = REPLACE(@EntryDateAppsClean, 'Sept', 'Sep');

    SET @DcrDateValue = COALESCE(
        TRY_CONVERT(DATETIME, NULLIF(@dcrDateClean, ''), 106),
        TRY_CONVERT(DATETIME, NULLIF(@dcrDateClean, ''))
    );

    IF @DcrDateValue IS NULL
        THROW 50001, 'Invalid DcrDate. Expected format: dd-MMM-yyyy hh:mm tt.', 1;

    IF NULLIF(LTRIM(RTRIM(@EntryTime)), '') IS NOT NULL
    BEGIN
        SET @EntryTimeValue = TRY_CONVERT(TIME(7), LTRIM(RTRIM(@EntryTime)));

        IF @EntryTimeValue IS NULL
            THROW 50002, 'Invalid EntryTime. Expected format: hh:mm tt.', 1;

        SET @DcrDateValue = DATEADD(
            DAY,
            DATEDIFF(DAY, 0, @DcrDateValue),
            CONVERT(DATETIME, @EntryTimeValue)
        );
    END;
    ELSE
    BEGIN
        SET @EntryTimeValue = CONVERT(TIME(7), @DcrDateValue);
    END;

    IF NULLIF(@EntryDateAppsClean, '') IS NULL
    BEGIN
        SET @EntryDateAppsValue = @DcrDateValue;
    END;
    ELSE
    BEGIN
        SET @EntryDateAppsValue = COALESCE(
            TRY_CONVERT(DATETIME, @EntryDateAppsClean, 106),
            TRY_CONVERT(DATETIME, @EntryDateAppsClean)
        );

        IF @EntryDateAppsValue IS NULL
            THROW 50003, 'Invalid EntryDate_Apps. Expected format: dd-MMM-yyyy hh:mm tt.', 1;
    END;

    IF LOWER(LTRIM(RTRIM(ISNULL(@Type, '')))) = 'ccradapter'
        SET @Type = 'CVR';
    ELSE
        SET @Type = 'DCR';

    DECLARE @userId INT;

    SELECT @userId = UserId
    FROM dbo.tblUser
    WHERE EmpInfoId = TRY_CONVERT(INT, @sessionUser);

    IF @userId IS NULL
        THROW 50004, 'Invalid SessionUser or employee user mapping was not found.', 1;

    -- DCR area lock + tour plan gate (see dbo.fn_Webapi_DcrGateMessage).
    DECLARE @gateMsg NVARCHAR(500) = dbo.fn_Webapi_DcrGateMessage(
        TRY_CONVERT(INT, @sessionUser), @doctorId, @chamberId, CONVERT(DATE, @DcrDateValue), @Type,
        LEFT(@Latitude, 50), LEFT(@Longitude, 50));

    IF @gateMsg <> N''
        THROW 50010, @gateMsg, 1;

    DECLARE @existingId INT = 0;

    SELECT TOP (1) @existingId = ISNULL(c.DcrId, 0)
    FROM dbo.tbl_DCRInfo c WITH (UPDLOCK, HOLDLOCK)
    WHERE c.DoctorId = @doctorId
      AND CONVERT(DATE, c.DcrDate) = CONVERT(DATE, @DcrDateValue)
      AND c.TypeDcr = @Type
      AND ISNULL(c.ApprovalStatus, '') <> '2';

    IF ISNULL(@existingId, 0) = 0
    BEGIN
        IF ISNULL(@DocTPDetailsId, 0) > 0
        BEGIN
            UPDATE dbo.tbl_DoctorTourPlanDetail
            SET IsDcrDone = 1
            WHERE DocTPDetailsId = @DocTPDetailsId;
        END;

        INSERT INTO dbo.tbl_DCRInfo
        (
            DcrDate,
            TourTypeId,
            ChemberId,
            EntryBy,
            EntryDate,
            Remarks,
            DoctorId,
            DocTPDetailsId,
            EntryDate_Apps,
            ApprovalStatus,
            Latitude,
            Longitude,
            StreetAddress,
            TypeDcr
        )
        VALUES
        (
            @DcrDateValue,
            @visitTypeId,
            @chamberId,
            @userId,
            GETDATE(),
            NULLIF(LTRIM(RTRIM(@remarks)), ''),
            @doctorId,
            NULLIF(@DocTPDetailsId, 0),
            @EntryDateAppsValue,
            0,
            NULLIF(LTRIM(RTRIM(@Latitude)), ''),
            NULLIF(LTRIM(RTRIM(@Longitude)), ''),
            NULLIF(LTRIM(RTRIM(@StreetAddress)), ''),
            @Type
        );

        SET @Id = CONVERT(INT, SCOPE_IDENTITY());

        DECLARE @empId INT;
        DECLARE @Role NVARCHAR(MAX);

        SELECT
            @Role = usR.RoleName,
            @empId = emp.EmpInfoId
        FROM dbo.tblEmpGeneralInfo emp WITH (NOLOCK)
        LEFT JOIN dbo.tblUser us WITH (NOLOCK) ON us.EmpInfoId = emp.EmpInfoId
        LEFT JOIN dbo.tbl_UserRoleInfo usR WITH (NOLOCK) ON usR.UserRoleID = us.UserRoleID
        WHERE us.UserId = @userId;

        DECLARE
            @GroupId1 INT,
            @RegionId1 INT,
            @AreaId1 INT,
            @TerrId1 INT;

        IF @Role = 'MIO'
        BEGIN
            SELECT
                @GroupId1 = GroupId,
                @AreaId1 = AreaId,
                @TerrId1 = TerritoryId,
                @RegionId1 = RegionId
            FROM dbo.View_webapi_FieldForce WITH (NOLOCK)
            WHERE MIOEmpId = @empId;
        END;
        ELSE IF @Role = 'ASM'
        BEGIN
            SELECT
                @GroupId1 = GroupId,
                @AreaId1 = AreaId,
                @TerrId1 = TerritoryId,
                @RegionId1 = RegionId
            FROM dbo.View_webapi_FieldForce WITH (NOLOCK)
            WHERE ASMEMPId = @empId;
        END;
        ELSE IF @Role = 'RSM'
        BEGIN
            SELECT
                @GroupId1 = GroupId,
                @AreaId1 = AreaId,
                @TerrId1 = TerritoryId,
                @RegionId1 = RegionId
            FROM dbo.View_webapi_FieldForce WITH (NOLOCK)
            WHERE RSMEMPId = @empId;
        END;
        ELSE IF @Role = 'NSM'
        BEGIN
            SELECT
                @GroupId1 = GroupId,
                @AreaId1 = AreaId,
                @TerrId1 = TerritoryId,
                @RegionId1 = RegionId
            FROM dbo.View_webapi_FieldForce WITH (NOLOCK)
            WHERE NSMEMPId = @empId;
        END;

        EXECUTE dbo.sp_webapi_SaveDCRAppLog
            @DCRApprovalId = 0,
            @Date = @EntryDateAppsValue,
            @FromEmpId = @empId,
            @ToEmpId = 0,
            @TableId = @Id,
            @Status = N'Posted',
            @Comments = N'',
            @Type = N'DCR',
            @Step = 1,
            @GroupId = @GroupId1,
            @RegionId = @RegionId1,
            @AreaId = @AreaId1,
            @TerritoryId = @TerrId1,
            @ToGroupId = 0,
            @ToRegionId = 0,
            @ToAreaId = 0,
            @ToTerritoryId = 0,
            @EntryByS = @empId,
            @EntryDateS = @EntryDateAppsValue,
            @EntryTimeS = @EntryTimeValue,
            @ApproveByS = NULL,
            @ApproveDateS = NULL,
            @ApproveTimeS = NULL,
            @EntryByApp = @empId,
            @EntryDateApp = @EntryDateAppsValue,
            @EntryTimeApp = @EntryTimeValue,
            @ApproveByApp = NULL,
            @ApproveDateApp = NULL,
            @ApproveTimeApp = NULL,
            @MenuId = 382;
    END;

    SELECT ISNULL(@Id, 0);
END;
GO
