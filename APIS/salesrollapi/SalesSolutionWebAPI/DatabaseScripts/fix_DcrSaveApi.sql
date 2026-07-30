SET XACT_ABORT ON;
GO

ALTER PROCEDURE [dbo].[sp_Webapi_Save_DcrInfo]
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

    SET @DcrDateValue = COALESCE(
        TRY_CONVERT(DATETIME, NULLIF(LTRIM(RTRIM(@dcrDate)), ''), 106),
        TRY_CONVERT(DATETIME, NULLIF(LTRIM(RTRIM(@dcrDate)), ''))
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

    IF NULLIF(LTRIM(RTRIM(@EntryDate_Apps)), '') IS NULL
    BEGIN
        SET @EntryDateAppsValue = @DcrDateValue;
    END;
    ELSE
    BEGIN
        SET @EntryDateAppsValue = COALESCE(
            TRY_CONVERT(DATETIME, LTRIM(RTRIM(@EntryDate_Apps)), 106),
            TRY_CONVERT(DATETIME, LTRIM(RTRIM(@EntryDate_Apps)))
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
