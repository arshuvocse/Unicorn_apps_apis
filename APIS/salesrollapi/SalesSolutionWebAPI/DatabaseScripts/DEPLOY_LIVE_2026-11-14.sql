-- =====================================================================================
-- DEPLOY_LIVE_2026-11-14.sql
-- Attendance HTTP 500 fix + performance indexes, in one file.
--
-- How to run: SSMS -> select the Unicorn database -> open this file -> Execute (F5).
-- When to run: when few users are active. Creating an index briefly blocks saves on that table.
-- Safe to run more than once. It does not delete or change any data.
--
-- What it does:
--   0) Saves a copy of the two procedures it replaces (table dbo.zz_ProcBackup_20261114).
--   1) Replaces sp_webapi_SavePunchTotalInfo  (fixes the attendance HTTP 500).
--   2) Adds missing indexes on the approval-log, tracking, DCR-product and attendance tables.
--   3) Replaces sp_Get_UserLocationTracking   (same result, uses the new index).
-- How to undo: see the bottom of this file.
-- =====================================================================================
SET NOCOUNT ON;

-- Safety checks. If one fails, nothing below runs and nothing is changed.
IF OBJECT_ID('dbo.tblMarketAttendance_Master_webapi') IS NULL OR OBJECT_ID('dbo.sp_webapi_SavePunchTotalInfo') IS NULL
BEGIN
    RAISERROR('STOPPED: this is not the Unicorn database (attendance table or punch procedure not found). Nothing was changed.', 16, 1);
    SET NOEXEC ON;
END;

DECLARE @major INT = CAST(PARSENAME(CAST(SERVERPROPERTY('ProductVersion') AS VARCHAR(30)), 4) AS INT),
        @build INT = CAST(PARSENAME(CAST(SERVERPROPERTY('ProductVersion') AS VARCHAR(30)), 2) AS INT);
IF @major < 13 OR (@major = 13 AND @build < 4001)
BEGIN
    RAISERROR('STOPPED: this script needs SQL Server 2016 SP1 or newer. Nothing was changed.', 16, 1);
    SET NOEXEC ON;
END;
GO

-- 0) Keep the current text of the procedures that are about to be replaced (first run only).
IF OBJECT_ID('dbo.zz_ProcBackup_20261114') IS NULL
BEGIN
    SELECT p.name AS ProcName, m.definition AS OldDefinition, GETDATE() AS SavedAt
    INTO dbo.zz_ProcBackup_20261114
    FROM sys.procedures p
    JOIN sys.sql_modules m ON m.object_id = p.object_id
    WHERE p.name IN ('sp_webapi_SavePunchTotalInfo', 'sp_Get_UserLocationTracking');
    PRINT 'Step 0 done: old procedures saved in dbo.zz_ProcBackup_20261114';
END;
GO

-- 1) Punch procedure ------------------------------------------------------------------

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

    -- No BEGIN / ROLLBACK / COMMIT TRAN in this procedure: the API already runs it inside its own transaction.
    -- The live version did ROLLBACK TRAN on this path, which ended the API's transaction too, so the API's
    -- commit failed and the app got HTTP 500 instead of the "already provided attendance" message.
    IF EXISTS
    (
        SELECT 1
        FROM dbo.tblMarketAttendance_Master_webapi WITH (UPDLOCK, HOLDLOCK)
        WHERE EmpInfoId = @empId
          -- 10 minutes, matching the API message. Was 1 hour, which also blocked a double-shift
          -- employee from starting the second shift right after ending the first.
          AND PINCreatedDateTime BETWEEN DATEADD(MINUTE, -10, GETDATE())
                                     AND DATEADD(SECOND, 30, GETDATE())
    )
    BEGIN
        SELECT CAST(-1 AS INT);
        RETURN;
    END;

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
        @empId, FORMAT(@Now, 'hh:mm:ss tt'), @pInLat, @pInLong, @remarks,
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

GO
PRINT 'Step 1 done: sp_webapi_SavePunchTotalInfo replaced';
GO

-- 2) Indexes, 3) tracking procedure ---------------------------------------------------
-- Performance fix: missing indexes + one procedure rewrite.
-- Every lookup on these tables was reading the whole table (measured on a copy: 37,559 pages per
-- DCR approval-log lookup, 24,267 per order one, 7,022 per tracking read), which is what fills SQL Server memory.
-- Safe to re-run. Run off-peak: each CREATE INDEX briefly blocks writes to its table.

SET NOCOUNT ON;

-- 1) Approval logs: every save proc and every approval list looks rows up by TableId (+ Step).
DECLARE @t sysname, @sql nvarchar(max);
DECLARE logs CURSOR LOCAL FAST_FORWARD FOR
    SELECT name FROM sys.tables
    WHERE name IN ('tblPrescriptionApprovalLog', 'tblOrderApprovalLog', 'tblExpanseApprovalLog', 'tblTourPlanApprovalLog',
                   'tblDCRApprovalLog', 'tblVisitPlanApprovalLog', 'tblMileageApprovalLog', 'tblDWSPApprovalLog',
                   'tblApprovalLog', 'tblDoctorApprovalLog_New', 'tblCustomerApprovalLog', 'tblLeaveApprovalLog');
OPEN logs;
FETCH NEXT FROM logs INTO @t;
WHILE @@FETCH_STATUS = 0
BEGIN
    IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'IX_' + @t + '_TableId_Step' AND object_id = OBJECT_ID('dbo.' + @t))
    BEGIN
        SET @sql = N'CREATE NONCLUSTERED INDEX ' + QUOTENAME('IX_' + @t + '_TableId_Step') + N' ON dbo.' + QUOTENAME(@t)
                 + N' (TableId, Step) INCLUDE (FromEmpId, RoleTypeId, ToRoleTypeId);';
        EXEC sys.sp_executesql @sql;
        PRINT 'Created IX_' + @t + '_TableId_Step';
    END;
    FETCH NEXT FROM logs INTO @t;
END;
CLOSE logs;
DEALLOCATE logs;

-- 2) Live tracking / location history read one employee's rows by date.
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'IX_tbl_UserTracking_Emp_TrackDate' AND object_id = OBJECT_ID('dbo.tbl_UserTracking'))
    CREATE NONCLUSTERED INDEX IX_tbl_UserTracking_Emp_TrackDate
        ON dbo.tbl_UserTracking (EmpInfoId, TrackDate);

-- 3) DCR product rows are always fetched by their DCR (PrescriptionId).
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'IX_tbl_PrescriptionProductDetail_PrescriptionId' AND object_id = OBJECT_ID('dbo.tbl_PrescriptionProductDetail'))
    CREATE NONCLUSTERED INDEX IX_tbl_PrescriptionProductDetail_PrescriptionId
        ON dbo.tbl_PrescriptionProductDetail (PrescriptionId);

-- 4) Every punch and every attendance screen checks one employee's rows for today.
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'IX_tblMarketAttendance_Emp_Date' AND object_id = OBJECT_ID('dbo.tblMarketAttendance_Master_webapi'))
    CREATE NONCLUSTERED INDEX IX_tblMarketAttendance_Emp_Date
        ON dbo.tblMarketAttendance_Master_webapi (EmpInfoId, AttendanceDate) INCLUDE (ShiftId, AttType);
GO

-- 5) Same rows and columns as before; the date filter is now a range so the new index is used directly.
CREATE OR ALTER PROCEDURE [dbo].[sp_Get_UserLocationTracking]
    @empid INT,
    @trackDate DATETIME
AS
BEGIN
    SELECT  TrackingId,
            EmpInfoId,
            LatValue,
            LongValue,
            AddressName,
            CONVERT(varchar(15), CAST(Time AS TIME), 100) AS Time,
            TrackDate
    FROM dbo.tbl_UserTracking
    WHERE EmpInfoId = @empid
      -- Written on the parameter itself (no local variable) so SQL Server can size the range and pick the index.
      AND TrackDate >= CONVERT(DATE, @trackDate)
      AND TrackDate < DATEADD(DAY, 1, CONVERT(DATE, @trackDate));
END
GO

PRINT 'Step 3 done: sp_Get_UserLocationTracking replaced';
PRINT 'ALL DONE.';
GO
SET NOEXEC OFF;
GO

-- =====================================================================================
-- HOW TO UNDO (only if needed; select the lines you want, remove the "--", and run them)
--
-- Old procedure text:
--   SELECT ProcName, OldDefinition FROM dbo.zz_ProcBackup_20261114;
--   Copy OldDefinition into a new query window, change CREATE to ALTER, and execute it.
--
-- Remove the indexes:
--   DROP INDEX IX_tbl_UserTracking_Emp_TrackDate ON dbo.tbl_UserTracking;
--   DROP INDEX IX_tbl_PrescriptionProductDetail_PrescriptionId ON dbo.tbl_PrescriptionProductDetail;
--   DROP INDEX IX_tblMarketAttendance_Emp_Date ON dbo.tblMarketAttendance_Master_webapi;
--   DROP INDEX IX_tblPrescriptionApprovalLog_TableId_Step ON dbo.tblPrescriptionApprovalLog;
--   DROP INDEX IX_tblOrderApprovalLog_TableId_Step ON dbo.tblOrderApprovalLog;
--   (same pattern, IX_<table>_TableId_Step, for the other approval-log tables)
--
-- NOT part of this script (server setting, depends on the server's RAM): SQL Server memory limit.
--   SSMS -> right-click the server -> Properties -> Memory -> Maximum server memory.
-- =====================================================================================
