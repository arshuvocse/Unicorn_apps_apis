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
