CREATE OR ALTER PROCEDURE [dbo].[sp_webapi_SavePunchTotalInfo]
    @attendaceDate DATETIME,
    @pInTime NVARCHAR(50) = NULL,
    @pInLat NVARCHAR(50) = NULL,
    @pInLong NVARCHAR(50) = NULL,
    @remarks NVARCHAR(MAX) = NULL,
    @empId INT,
    @EntryDate DATETIME,
    @AttType INT,
    @AttAddress NVARCHAR(MAX) = NULL,
    @ShiftId INT = NULL
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;

    DECLARE @Now DATETIME = GETDATE();
    DECLARE @EntryTime TIME(7) = CAST(GETDATE() AS TIME);
    DECLARE @AttendanceId INT = NULL;
    DECLARE @ExpectedShiftId INT = NULL;
    DECLARE @ExpectedAttType INT = NULL;
    DECLARE @UserRoleID INT = NULL;
    DECLARE @Role NVARCHAR(MAX) = NULL;
    DECLARE @GroupId INT = NULL;
    DECLARE @RegionId INT = NULL;
    DECLARE @AreaId INT = NULL;
    DECLARE @TerritoryId INT = NULL;
    DECLARE @SubTerritoryId INT = NULL;
    DECLARE @MarketId INT = NULL;
    DECLARE @GroupName NVARCHAR(MAX) = NULL;
    DECLARE @RegionName NVARCHAR(MAX) = NULL;
    DECLARE @AreaName NVARCHAR(MAX) = NULL;
    DECLARE @TerritoryName NVARCHAR(MAX) = NULL;
    DECLARE @SubTerritoryName NVARCHAR(MAX) = NULL;
    DECLARE @MarketName NVARCHAR(MAX) = NULL;
    DECLARE @GroupCode NVARCHAR(MAX) = NULL;
    DECLARE @RegionCode NVARCHAR(MAX) = NULL;
    DECLARE @AreaCode NVARCHAR(MAX) = NULL;
    DECLARE @TerritoryCode NVARCHAR(MAX) = NULL;
    DECLARE @SubTerritoryCode NVARCHAR(MAX) = NULL;
    DECLARE @MarketCode NVARCHAR(MAX) = NULL;

    IF @AttType NOT IN (1, 2)
    BEGIN
        SELECT CAST(-1 AS INT);
        RETURN;
    END;

    -- Resolve the next valid action from the employee's active shift sequence.
    IF EXISTS
    (
        SELECT 1
        FROM dbo.tblEmpwiseShiftDeclare sd
        INNER JOIN dbo.tbl_Shift s ON s.ShiftId = sd.ShiftId
        WHERE sd.EmpInfoId = @empId
          AND s.IsActive = 1
    )
    BEGIN
        ;WITH ShiftStatus AS
        (
            SELECT
                sd.ShiftId,
                s.ShiftInTime,
                s.ShiftOutTime,
                CASE WHEN EXISTS
                (
                    SELECT 1
                    FROM dbo.tblMarketAttendance_Master_webapi a WITH (UPDLOCK, HOLDLOCK)
                    WHERE a.EmpInfoId = @empId
                      AND a.ShiftId = sd.ShiftId
                      AND a.AttType = 1
                      AND CONVERT(DATE, a.AttendanceDate) = CONVERT(DATE, @Now)
                ) THEN 1 ELSE 0 END AS HasPunchIn,
                CASE WHEN EXISTS
                (
                    SELECT 1
                    FROM dbo.tblMarketAttendance_Master_webapi a WITH (UPDLOCK, HOLDLOCK)
                    WHERE a.EmpInfoId = @empId
                      AND a.ShiftId = sd.ShiftId
                      AND a.AttType = 2
                      AND CONVERT(DATE, a.AttendanceDate) = CONVERT(DATE, @Now)
                ) THEN 1 ELSE 0 END AS HasPunchOut
            FROM dbo.tblEmpwiseShiftDeclare sd
            INNER JOIN dbo.tbl_Shift s ON s.ShiftId = sd.ShiftId
            WHERE sd.EmpInfoId = @empId
              AND s.IsActive = 1
        )
        SELECT TOP (1)
            @ExpectedShiftId = ShiftId,
            @ExpectedAttType = CASE WHEN HasPunchIn = 0 THEN 1 ELSE 2 END
        FROM ShiftStatus
        WHERE NOT (HasPunchIn = 1 AND HasPunchOut = 1)
        ORDER BY
            CASE
                WHEN HasPunchIn = 1 AND HasPunchOut = 0 THEN 0
                WHEN HasPunchIn = 0
                     AND
                     (
                         (ShiftInTime <= ShiftOutTime
                          AND @EntryTime >= ShiftInTime
                          AND @EntryTime <= ShiftOutTime)
                         OR
                         (ShiftInTime > ShiftOutTime
                          AND (@EntryTime >= ShiftInTime
                               OR @EntryTime <= ShiftOutTime))
                     ) THEN 1
                ELSE 2
            END,
            ShiftInTime,
            ShiftOutTime,
            ShiftId;

        IF @ExpectedShiftId IS NULL
        BEGIN
            SELECT CAST(-4 AS INT);
            RETURN;
        END;

        IF @ShiftId IS NULL
            SET @ShiftId = @ExpectedShiftId;

        IF @ShiftId <> @ExpectedShiftId OR @AttType <> @ExpectedAttType
        BEGIN
            SELECT CAST(-2 AS INT);
            RETURN;
        END;
    END;

    IF @ShiftId IS NULL
    BEGIN
        SELECT @ShiftId = ShiftId
        FROM dbo.tblEmpGeneralInfo
        WHERE EmpInfoId = @empId;
    END;

    IF @ShiftId IS NULL
       OR (
            EXISTS
            (
                SELECT 1
                FROM dbo.tblEmpwiseShiftDeclare
                WHERE EmpInfoId = @empId
            )
            AND NOT EXISTS
            (
                SELECT 1
                FROM dbo.tblEmpwiseShiftDeclare sd
                INNER JOIN dbo.tbl_Shift s ON s.ShiftId = sd.ShiftId
                WHERE sd.EmpInfoId = @empId
                  AND sd.ShiftId = @ShiftId
                  AND s.IsActive = 1
            )
       )
    BEGIN
        SELECT CAST(-3 AS INT);
        RETURN;
    END;

    IF EXISTS
    (
        SELECT 1
        FROM dbo.tblMarketAttendance_Master_webapi WITH (UPDLOCK, HOLDLOCK)
        WHERE EmpInfoId = @empId
          AND ShiftId = @ShiftId
          AND AttType = @AttType
          AND CONVERT(DATE, AttendanceDate) = CONVERT(DATE, @Now)
    )
    BEGIN
        SELECT CAST(-3 AS INT);
        RETURN;
    END;

    IF @AttType = 2 AND NOT EXISTS
    (
        SELECT 1
        FROM dbo.tblMarketAttendance_Master_webapi WITH (UPDLOCK, HOLDLOCK)
        WHERE EmpInfoId = @empId
          AND ShiftId = @ShiftId
          AND AttType = 1
          AND CONVERT(DATE, AttendanceDate) = CONVERT(DATE, @Now)
    )
    BEGIN
        SELECT CAST(-2 AS INT);
        RETURN;
    END;

    SELECT
        @Role = ur.RoleName,
        @UserRoleID = u.UserRoleID
    FROM dbo.tblEmpGeneralInfo emp
    LEFT JOIN dbo.tblUser u ON u.EmpInfoId = emp.EmpInfoId
    LEFT JOIN dbo.tbl_UserRoleInfo ur ON ur.UserRoleID = u.UserRoleID
    WHERE emp.EmpInfoId = @empId;

    SELECT TOP (1)
        @GroupId = tp.GroupId,
        @RegionId = tp.RegionId,
        @AreaId = tp.AreaId,
        @TerritoryId = tp.TerritoryId,
        @SubTerritoryId = tp.SubTerritoryId,
        @MarketId = tp.MarketId,
        @GroupName = tp.GroupName,
        @RegionName = tp.RegionName,
        @AreaName = tp.AreaName,
        @TerritoryName = tp.TerritoryName,
        @SubTerritoryName = tp.SubTerritoryName,
        @MarketName = tp.MarketName,
        @GroupCode = tp.GroupCode_TP,
        @RegionCode = tp.RegionCode_TP,
        @AreaCode = tp.AreaCode_TP,
        @TerritoryCode = tp.TerritoryCode_TP,
        @SubTerritoryCode = tp.SubTerritoryCode_TP,
        @MarketCode = tp.MarketCode_TP
    FROM dbo.tbl_TourPlanInfo tp
    INNER JOIN dbo.tbl_TourPlanMaster tm ON tm.TpMaster = tp.TpMaster
    WHERE tm.ApprovalStatus = '2'
      AND tp.EmpInfoId = @empId
      AND tp.serialno = '1'
      AND CONVERT(DATE, tp.TourPlanDate) = CONVERT(DATE, @Now);

    INSERT INTO dbo.tblMarketAttendance_Master_webapi
    (
        EmpInfoId, PunchInTime, PInLat, PInLog, POutRemarks,
        AttendanceDate, PINCreatedDateTime, ShiftId, AttType,
        ApprovalStatus, UserRoleID, AttAddress, GroupId, RegionId,
        AreaId, TerritoryId, SubTerritoryId, MarketId, GroupName_Att,
        RegionName_Att, AreaName_Att, TerritoryName_Att,
        SubTerritoryName_Att, MarketName_Att, GroupCode_Att,
        RegionCode_Att, AreaCode_Att, TerritoryCode_Att,
        SubTerritoryCode_Att, MarketCode_Att
    )
    VALUES
    (
        @empId, FORMAT(@Now, 'hh:mm tt'), @pInLat, @pInLong, @remarks,
        @Now, ISNULL(@EntryDate, @Now), @ShiftId, @AttType,
        0, @UserRoleID, @AttAddress, @GroupId, @RegionId,
        @AreaId, @TerritoryId, @SubTerritoryId, @MarketId, @GroupName,
        @RegionName, @AreaName, @TerritoryName,
        @SubTerritoryName, @MarketName, @GroupCode,
        @RegionCode, @AreaCode, @TerritoryCode,
        @SubTerritoryCode, @MarketCode
    );

    SET @AttendanceId = SCOPE_IDENTITY();

    IF @Role = 'MIO'
        SELECT TOP (1) @GroupId = GroupId, @RegionId = RegionId,
            @AreaId = AreaId, @TerritoryId = TerritoryId
        FROM dbo.View_webapi_FieldForce WHERE MIOEmpId = @empId;
    ELSE IF @Role = 'ASM'
        SELECT TOP (1) @GroupId = GroupId, @RegionId = RegionId,
            @AreaId = AreaId, @TerritoryId = TerritoryId
        FROM dbo.View_webapi_FieldForce WHERE ASMEMPId = @empId;
    ELSE IF @Role = 'RSM'
        SELECT TOP (1) @GroupId = GroupId, @RegionId = RegionId,
            @AreaId = AreaId, @TerritoryId = TerritoryId
        FROM dbo.View_webapi_FieldForce WHERE RSMEMPId = @empId;
    ELSE IF @Role = 'NSM'
        SELECT TOP (1) @GroupId = GroupId, @RegionId = RegionId,
            @AreaId = AreaId, @TerritoryId = TerritoryId
        FROM dbo.View_webapi_FieldForce WHERE NSMEMPId = @empId;

    -- Keep the attendance identity as the first result consumed by ExecuteScalar.
    SELECT @AttendanceId;

    EXEC dbo.sp_webapi_SaveAppLog
        @ApprovalId = 0,
        @Date = @Now,
        @FromEmpId = @empId,
        @ToEmpId = 0,
        @TableId = @AttendanceId,
        @Status = N'Posted',
        @Comments = N'',
        @Type = N'Attendance',
        @Step = 1,
        @GroupId = @GroupId,
        @RegionId = @RegionId,
        @AreaId = @AreaId,
        @TerritoryId = @TerritoryId,
        @ToGroupId = 0,
        @ToRegionId = 0,
        @ToAreaId = 0,
        @ToTerritoryId = 0,
        @EntryByS = @empId,
        @EntryDateS = @EntryDate,
        @EntryTimeS = @EntryTime,
        @ApproveByS = NULL,
        @ApproveDateS = NULL,
        @ApproveTimeS = NULL,
        @EntryByApp = @empId,
        @EntryDateApp = @EntryDate,
        @EntryTimeApp = @EntryTime,
        @ApproveByApp = NULL,
        @ApproveDateApp = NULL,
        @ApproveTimeApp = NULL,
        @MenuId = 301;
END;
