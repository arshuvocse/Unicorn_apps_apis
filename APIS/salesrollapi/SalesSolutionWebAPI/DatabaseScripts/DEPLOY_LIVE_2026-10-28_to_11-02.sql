/* =====================================================================================
   LIVE DEPLOY: every DB change from 2026-10-28 to 2026-11-02, one file.
   Target: SalesDisDB_Unicorn_DB (SQL Server 2016 SP1 or later; CREATE OR ALTER is used).

   BEFORE RUNNING
     1. Take a full backup of SalesDisDB_Unicorn_DB.
     2. Run off-peak: part 10 creates an index on tbl_DoctorTourPlanDetail.
     3. In SSMS turn on Query > SQLCMD Mode, then Execute (F5). Without SQLCMD mode the
        script refuses to run anything (stop-on-first-error needs it) and closes the
        connection; reconnect, turn SQLCMD Mode on, run again.

   WHAT IT DOES
     - Stops at the first error. Checks the database name and SQL Server version first.
     - Saves the current definition of every procedure/function it changes into
       dbo.tbl_DeployBackup (DeployTag = 'LIVE_2026-11-02') before changing anything.
     - No data is updated or deleted. Tables/columns/indexes are added only when missing.
       The only DROP is fn_Webapi_DcrAreaAllowed (replaced by the chamber geofence); it is
       backed up first.
     - Every part is re-runnable: if it stops halfway, fix the cause and run the whole file again.

   ROLLBACK (one object): SELECT Definition FROM dbo.tbl_DeployBackup
       WHERE DeployTag = 'LIVE_2026-11-02' AND ObjectName = 'dbo.<name>';
     Run that text with CREATE changed to CREATE OR ALTER. Definition NULL means the object
     is new in this deploy; to roll back, DROP it.
   ===================================================================================== */
:on error exit
:setvar SqlCmdMode "on"
GO
-- Without SQLCMD mode the line above is a syntax error and this check switches execution off.
IF '$(SqlCmdMode)' <> 'on'
BEGIN
    RAISERROR('Turn on SQLCMD Mode (Query > SQLCMD Mode) and run again. Nothing was changed.', 20, 1) WITH LOG; -- severity 20 drops the connection, so no later batch even compiles
    SET NOEXEC ON;                                -- fallback if the login may not use WITH LOG
END
GO
USE [SalesDisDB_Unicorn_DB]
GO
-- CREATE OR ALTER needs SQL Server 2016 SP1 (13.0.4001) or later.
IF DB_NAME() <> N'SalesDisDB_Unicorn_DB'
   OR CAST(PARSENAME(CAST(SERVERPROPERTY('ProductVersion') AS NVARCHAR(32)), 4) AS INT) < 13
   OR (CAST(PARSENAME(CAST(SERVERPROPERTY('ProductVersion') AS NVARCHAR(32)), 4) AS INT) = 13
       AND CAST(PARSENAME(CAST(SERVERPROPERTY('ProductVersion') AS NVARCHAR(32)), 2) AS INT) < 4001)
BEGIN
    RAISERROR('Wrong database or SQL Server older than 2016 SP1. Nothing was changed.', 20, 1) WITH LOG; -- severity 20 drops the connection, so no later batch even compiles
    SET NOEXEC ON;                                -- fallback if the login may not use WITH LOG
END
GO
-- Part 10 (sp_Webapi_Get_ChamberName) reads columns added by update_doctor_chamber_institution.sql (2026-10-27).
IF COL_LENGTH('dbo.tblDoctorChemberDetail', 'Shift') IS NULL
   OR COL_LENGTH('dbo.tblDoctorChemberDetail', 'SittingTime') IS NULL
   OR COL_LENGTH('dbo.tblDoctorChemberDetail', 'InstitutionId') IS NULL
BEGIN
    RAISERROR('tblDoctorChemberDetail lacks Shift/SittingTime/InstitutionId. Run update_doctor_chamber_institution.sql first. Nothing was changed.', 20, 1) WITH LOG; -- severity 20 drops the connection, so no later batch even compiles
    SET NOEXEC ON;                                -- fallback if the login may not use WITH LOG
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

-- ===== 0. Backup of current definitions =====
IF OBJECT_ID(N'dbo.tbl_DeployBackup', N'U') IS NULL
    CREATE TABLE dbo.tbl_DeployBackup
    (
        BackupId   INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_tbl_DeployBackup PRIMARY KEY,
        DeployTag  NVARCHAR(50)  NOT NULL,
        ObjectName NVARCHAR(256) NOT NULL,
        Definition NVARCHAR(MAX) NULL,      -- NULL = object did not exist before this deploy
        BackupDate DATETIME      NOT NULL CONSTRAINT DF_tbl_DeployBackup_BackupDate DEFAULT (GETDATE())
    );
GO
-- Re-running the file keeps the first backup (the true pre-deploy state).
INSERT INTO dbo.tbl_DeployBackup (DeployTag, ObjectName, Definition)
SELECT N'LIVE_2026-11-02', o.ObjectName, OBJECT_DEFINITION(OBJECT_ID(o.ObjectName))
FROM (VALUES
    (N'dbo.fn_Webapi_DcrAreaAllowed'),
    (N'dbo.fn_Webapi_DcrGateMessage'),
    (N'dbo.fn_Webapi_IsAllDataRole'),
    (N'dbo.sp_WebAPI_GetAttendanceData_New'),
    (N'dbo.sp_WebAPI_Get_TargetAchievement_SubTerritory'),
    (N'dbo.sp_Webapi_Check_DcrGate'),
    (N'dbo.sp_Webapi_GetCustWiseOrderReport_L'),
    (N'dbo.sp_Webapi_GetCustWiseSalesReport_L'),
    (N'dbo.sp_Webapi_GetProductWiseOrderReport_L'),
    (N'dbo.sp_Webapi_GetProductWiseSalesReport_L'),
    (N'dbo.sp_Webapi_Get_CellTowerLocation'),
    (N'dbo.sp_Webapi_Get_ChamberName'),
    (N'dbo.sp_Webapi_Get_DayWiseTourPlan'),
    (N'dbo.sp_Webapi_Get_DcrAreaLock'),
    (N'dbo.sp_Webapi_Get_DcrListById'),
    (N'dbo.sp_Webapi_Get_TourPlanReport'),
    (N'dbo.sp_Webapi_Save_CellTowerLocation'),
    (N'dbo.sp_Webapi_Save_ChamberLock'),
    (N'dbo.sp_Webapi_Save_DayWiseTourPlan'),
    (N'dbo.sp_Webapi_Save_DcrAreaLock'),
    (N'dbo.sp_Webapi_Save_DcrInfo'),
    (N'dbo.sp_Webapi_Set_ChamberLocationIfEmpty')
) o(ObjectName)
WHERE NOT EXISTS (SELECT 1 FROM dbo.tbl_DeployBackup b
                  WHERE b.DeployTag = N'LIVE_2026-11-02' AND b.ObjectName = o.ObjectName);
GO

-- ===== 1. fn_Webapi_IsAllDataRole.sql =====
-- 1 when the employee's user role sees every employee's data in app reports (Target & Achievement, Tour Plan).
-- RoleTypeId: 3 DSM/DZSM, 4 Regional Head/NSM, 5 Admin/Pharma Head, 8 DIC/SubDepot, 9 MIS, 11 Distribution Head, 14 Head of NSM.
-- Re-runnable.
-- SELECT dbo.fn_Webapi_IsAllDataRole(0)  -- Admin user (EmpInfoId 0) -> 1
CREATE OR ALTER FUNCTION [dbo].[fn_Webapi_IsAllDataRole] (@empId INT)
RETURNS BIT
AS
BEGIN
	RETURN CASE WHEN EXISTS (
		SELECT 1
		FROM dbo.tblUser u
		INNER JOIN dbo.tbl_UserRoleInfo r ON r.UserRoleID = u.UserRoleID
		WHERE u.EmpInfoId = @empId
		  AND r.RoleTypeId IN (3, 4, 5, 8, 9, 11, 14)
	) THEN 1 ELSE 0 END
END
GO

GO

-- ===== 2. cell_tower_location.sql =====
-- Cell tower positions learned from the reps' own GPS fixes, so a device with no GPS (indoors, weak
-- sky view) can still be placed from its serving tower without any paid geolocation key.
-- Written by api/Tracking/LearnCell, read by api/Tracking/ResolveLocation. Re-runnable.
IF OBJECT_ID('dbo.tbl_CellTowerLocation', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.tbl_CellTowerLocation
    (
        RadioType      VARCHAR(8) NOT NULL,   -- gsm | wcdma | lte | nr
        Mcc            INT        NOT NULL,
        Mnc            INT        NOT NULL,
        Lac            INT        NOT NULL,   -- LAC (gsm/wcdma) or TAC (lte/nr)
        CellId         BIGINT     NOT NULL,
        Latitude       FLOAT      NOT NULL,
        Longitude      FLOAT      NOT NULL,
        AccuracyMeters FLOAT      NOT NULL,   -- radius covering the samples seen so far
        SampleCount    INT        NOT NULL,
        UpdatedAt      DATETIME   NOT NULL,
        CONSTRAINT PK_tbl_CellTowerLocation PRIMARY KEY (RadioType, Mcc, Mnc, Lac, CellId)
    );
END
GO

-- Upserts one tower from a GPS-confirmed fix. The stored point is the running mean of all samples;
-- AccuracyMeters grows to the farthest sample seen, so it reports the real spread, not the GPS accuracy.
CREATE OR ALTER PROCEDURE [dbo].[sp_Webapi_Save_CellTowerLocation]
    @RadioType VARCHAR(8),
    @Mcc INT,
    @Mnc INT,
    @Lac INT,
    @CellId BIGINT,
    @Latitude FLOAT,
    @Longitude FLOAT,
    @AccuracyMeters FLOAT
AS
BEGIN
    SET NOCOUNT ON;

    DECLARE @lat FLOAT, @lng FLOAT, @acc FLOAT, @n INT;

    SELECT @lat = Latitude, @lng = Longitude, @acc = AccuracyMeters, @n = SampleCount
    FROM dbo.tbl_CellTowerLocation
    WHERE RadioType = @RadioType AND Mcc = @Mcc AND Mnc = @Mnc AND Lac = @Lac AND CellId = @CellId;

    IF @n IS NULL
    BEGIN
        INSERT INTO dbo.tbl_CellTowerLocation
            (RadioType, Mcc, Mnc, Lac, CellId, Latitude, Longitude, AccuracyMeters, SampleCount, UpdatedAt)
        VALUES
            (@RadioType, @Mcc, @Mnc, @Lac, @CellId, @Latitude, @Longitude, @AccuracyMeters, 1, GETDATE());
        RETURN;
    END

    -- Distance of the new sample from the mean we hold; the radius must cover it.
    DECLARE @spread FLOAT =
        geography::Point(@lat, @lng, 4326).STDistance(geography::Point(@Latitude, @Longitude, 4326));

    -- Cap the weight of history so a tower that was mapped wrong once can still be corrected.
    DECLARE @weight INT = CASE WHEN @n > 50 THEN 50 ELSE @n END;

    UPDATE dbo.tbl_CellTowerLocation
    SET Latitude       = @lat + (@Latitude - @lat) / (@weight + 1),
        Longitude      = @lng + (@Longitude - @lng) / (@weight + 1),
        AccuracyMeters = CASE WHEN @spread + @AccuracyMeters > @acc THEN @spread + @AccuracyMeters ELSE @acc END,
        SampleCount    = @n + 1,
        UpdatedAt      = GETDATE()
    WHERE RadioType = @RadioType AND Mcc = @Mcc AND Mnc = @Mnc AND Lac = @Lac AND CellId = @CellId;
END
GO

-- Looks up one tower. Returns no row when the tower has never been seen with GPS.
CREATE OR ALTER PROCEDURE [dbo].[sp_Webapi_Get_CellTowerLocation]
    @RadioType VARCHAR(8),
    @Mcc INT,
    @Mnc INT,
    @Lac INT,
    @CellId BIGINT
AS
BEGIN
    SET NOCOUNT ON;

    SELECT Latitude, Longitude, AccuracyMeters, SampleCount
    FROM dbo.tbl_CellTowerLocation WITH (NOLOCK)
    WHERE RadioType = @RadioType AND Mcc = @Mcc AND Mnc = @Mnc AND Lac = @Lac AND CellId = @CellId;
END
GO

-- Self-check of the running average and the radius. Rolled back, so it leaves nothing behind;
-- it raises an error if the arithmetic is wrong. Safe to run on any server.
BEGIN TRANSACTION;

EXEC [dbo].[sp_Webapi_Save_CellTowerLocation] 'lte', 470, 1, 100, 999999999, 23.800000, 90.400000, 10;
-- Second sample ~100 m north of the first.
EXEC [dbo].[sp_Webapi_Save_CellTowerLocation] 'lte', 470, 1, 100, 999999999, 23.800900, 90.400000, 10;

DECLARE @lat FLOAT, @acc FLOAT, @n INT;
SELECT @lat = Latitude, @acc = AccuracyMeters, @n = SampleCount
FROM dbo.tbl_CellTowerLocation
WHERE RadioType = 'lte' AND Mcc = 470 AND Mnc = 1 AND Lac = 100 AND CellId = 999999999;

IF @n <> 2
    RAISERROR('tbl_CellTowerLocation self-check: SampleCount %d, expected 2', 16, 1, @n);
-- Mean of 23.800000 and 23.800900.
IF @lat NOT BETWEEN 23.800440 AND 23.800460
    RAISERROR('tbl_CellTowerLocation self-check: mean latitude not averaged', 16, 1);
-- ~100 m spread + the 10 m fix accuracy. RAISERROR cannot substitute a float, hence the text.
DECLARE @accText VARCHAR(32) = CONVERT(VARCHAR(32), @acc);
IF @acc NOT BETWEEN 100 AND 130
    RAISERROR('tbl_CellTowerLocation self-check: radius %s, expected ~110 m', 16, 1, @accText);

ROLLBACK TRANSACTION;
GO

GO

-- ===== 3. sp_WebAPI_GetAttendanceData_New_allTeam.sql =====
GO
/*
  Attendance Report (app Reports -> Attendance Report), "All" sales person option.
  New optional @allTeam (default 0): existing callers keep the single-employee behaviour.
  @allTeam = 1 returns @empId plus everyone under him (same hierarchy rule as the other app reports;
  all-data roles see every employee) and adds EmpName so the app can label each row.
  Re-runnable (ALTER).
*/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
ALTER PROCEDURE [dbo].[sp_WebAPI_GetAttendanceData_New]
    @empId INT ,
    @fromDate  nvarchar(max)= NULL,
    @toDate  nvarchar(max)= NULL,
    @allTeam BIT = 0
AS
BEGIN
    SET NOCOUNT ON;

    SELECT @empId AS EmpInfoId INTO #emp;
    IF (@allTeam = 1)
    BEGIN
        INSERT INTO #emp (EmpInfoId)
        SELECT EmpInfoId FROM dbo.View_Webapi_EmployeeFieldForceInfo WITH (NOLOCK)
        WHERE MIOEmpId = @empId OR ASMEMPId = @empId OR RSMEMPId = @empId OR NSMEMPId = @empId
        UNION
        SELECT EmpInfoId FROM dbo.tblEmpGeneralInfo WITH (NOLOCK)
        WHERE dbo.fn_Webapi_IsAllDataRole(@empId) = 1
        EXCEPT
        SELECT EmpInfoId FROM #emp;
    END

    SELECT DISTINCT
        att1.AttendanceId,
        att1.EmpInfoId,
        ISNULL(eg.EmpName, '') AS EmpName,
        CONVERT(NVARCHAR(50), att1.AttendanceDate, 107) AS AttendanceDate,
        CASE WHEN att1.AttType = 1 THEN sst.ShiftShortName ELSE '' END AS PunchInShiftName,
        CASE WHEN att1.AttType = 1 THEN att1.PunchInTime   ELSE '' END AS PunchInTime,
        CASE WHEN att1.AttType = 1 THEN att1.PInLat        ELSE '' END AS PInLat,
        CASE WHEN att1.AttType = 1 THEN att1.PInLog        ELSE '' END AS PInLog,
        CASE WHEN att1.AttType = 1 THEN att1.AttAddress    ELSE '' END AS PInAttAddress,
        CASE WHEN att1.AttType = 2 THEN sst.ShiftShortName ELSE '' END AS PunchOutShiftName,
        CASE WHEN att1.AttType = 2 THEN att1.PunchInTime   ELSE '' END AS PunchOutTime,
        CASE WHEN att1.AttType = 2 THEN att1.PInLat        ELSE '' END AS POutLat,
        CASE WHEN att1.AttType = 2 THEN att1.PInLog        ELSE '' END AS POutLong,
        CASE WHEN att1.AttType = 2 THEN att1.AttAddress    ELSE '' END AS POutAttAddress
    FROM dbo.tblMarketAttendance_Master_webapi att1
    INNER JOIN #emp e ON e.EmpInfoId = att1.EmpInfoId
    LEFT JOIN tbl_Shift sst ON sst.ShiftId = att1.ShiftId
    LEFT JOIN dbo.tblEmpGeneralInfo eg WITH (NOLOCK) ON eg.EmpInfoId = att1.EmpInfoId
    WHERE CONVERT(DATE, att1.AttendanceDate) BETWEEN @fromDate AND @toDate
    ORDER BY EmpName, att1.AttendanceId ASC;

    DROP TABLE #emp;
END
GO

GO

-- ===== 4. sp_Webapi_SalesOrderReport_CustomerFilter.sql =====
GO
/*
  Sales Summary / Order Summary reports (app Reports menu), Customer-wise and Product-wise tabs.
  - New optional @CustomerId (default NULL = all customers) on all four SPs: filters tblOrder.CustomerMasterId.
  - Product-wise SPs: the employee filter now matches @empId in any hierarchy column (same as the
    customer-wise SPs) instead of the caller's @Role, so a manager can pick one subordinate from the
    Sales Person dropdown. @Role is kept for compatibility but no longer used.
  Re-runnable (ALTER).
*/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
ALTER PROCEDURE [dbo].[sp_Webapi_GetCustWiseOrderReport_L]
    @empId int = null,
    @Role  nvarchar(max)=null,
    @Month  int=null,
    @Year   int=null,
    @ProviderType  int=null,
    @CustomerType  int=null,
    @CustomerId int=null
AS
BEGIN
    DECLARE @params NVARCHAR(max)='  '
    IF(@Month IS NOT NULL)
        SET @params=@params+ ' AND MONTH(convert(Date,  D.SubmissionDate))='''+CAST(@Month AS NVARCHAR(max))+''''
    IF(@Year IS NOT NULL )
        SET @params=@params+ ' AND   year(convert(Date,  D.SubmissionDate))= '''+CAST(@Year AS NVARCHAR(max))+''''
    IF(@ProviderType IS NOT NULL )
        SET @params=@params+ ' AND  D.ProgramTypeId='''+CAST(@ProviderType AS NVARCHAR(max))+''''
    IF(@CustomerType IS NOT NULL )
        SET @params=@params+ ' AND  D.CustTypeId='''+CAST(@CustomerType AS NVARCHAR(max))+''''
    IF(ISNULL(@CustomerId,0) > 0)
        SET @params=@params+ ' AND  D.CustomerMasterId='+CAST(@CustomerId AS NVARCHAR(max))
    DECLARE @Q NVARCHAR(MAX)
    SET @Q='SELECT ROW_NUMBER() Over (Order by D.CustomerCode) As [SN],  ISNULL(Count(D.CustomerMasterId),0) ordered_qty, D.CustomerCode+ '' : '' + D.CustomerName customer_name,
    convert(decimal(18,2),iSNULL(SUM(D.GrossValue-D.TotalDiscount),0)) as ordered_value
FROM    dbo.tblOrder D with (nolock)
left join tblUser u with (nolock) on D.EntryBy=u.UserId
left join tblEmpGeneralInfo emp with (nolock) on emp.EmpInfoId=u.EmpInfoId
inner join View_Webapi_EmployeeFieldForceInfo fs on emp.EmpInfoId=fs.EmpInfoId
WHERE   IsFromApp = 1  and (fs.MIOEmpId='+convert(nvarchar(max),@empId)+' or fs.ASMEMPId='+convert(nvarchar(max),@empId)+' or fs.RSMEMPId='+convert(nvarchar(max),@empId)+' or fs.NSMEMPId='+convert(nvarchar(max),@empId)+')
  '  +@params+'
group by D.CustomerCode, D.CustomerName
'
    EXEC sys.sp_executesql @Q
END
GO

ALTER PROCEDURE [dbo].[sp_Webapi_GetCustWiseSalesReport_L]
    @empId int = null,
    @Role  nvarchar(max)=null,
    @Month  int=null,
    @Year   int=null,
    @ProviderType  int=null,
    @CustomerType  int=null,
    @CustomerId int=null
AS
BEGIN
    DECLARE @params NVARCHAR(max)='  '
    IF(@Month IS NOT NULL)
        SET @params=@params+ ' AND MONTH(convert(Date, A.InvoiceDate))='''+CAST(@Month AS NVARCHAR(max))+''''
    IF(@Year IS NOT NULL )
        SET @params=@params+ ' AND year(convert(Date, A.InvoiceDate))= '''+CAST(@Year AS NVARCHAR(max))+''''
    IF(@ProviderType IS NOT NULL )
        SET @params=@params+ ' AND  D.ProgramTypeId='''+CAST(@ProviderType AS NVARCHAR(max))+''''
    IF(@CustomerType IS NOT NULL )
        SET @params=@params+ ' AND  D.CustTypeId='''+CAST(@CustomerType AS NVARCHAR(max))+''''
    IF(ISNULL(@CustomerId,0) > 0)
        SET @params=@params+ ' AND  D.CustomerMasterId='+CAST(@CustomerId AS NVARCHAR(max))
    DECLARE @Q NVARCHAR(MAX)
    SET @Q='SELECT ROW_NUMBER() Over (Order by D.CustomerCode) As [SN],  ISNULL(Count(D.CustomerMasterId),0) ordered_qty, D.CustomerCode+ '' : '' + D.CustomerName customer_name,
    convert(decimal(18,2),iSNULL(SUM(D.GrossValue-D.TotalDiscount),0)) as ordered_value
FROM    dbo.tblOrder D with (nolock)
inner join dbo.tblInvoice A with (nolock) on A.OrderId=D.OrderId
left join tblUser u with (nolock) on D.EntryBy=u.UserId
left join tblEmpGeneralInfo emp with (nolock) on emp.EmpInfoId=u.EmpInfoId
inner join View_Webapi_EmployeeFieldForceInfo fs on emp.EmpInfoId=fs.EmpInfoId
WHERE   IsFromApp = 1  and (fs.MIOEmpId='+convert(nvarchar(max),@empId)+' or fs.ASMEMPId='+convert(nvarchar(max),@empId)+' or fs.RSMEMPId='+convert(nvarchar(max),@empId)+' or fs.NSMEMPId='+convert(nvarchar(max),@empId)+')
  '  +@params+'
group by D.CustomerCode, D.CustomerName
'
    EXEC sys.sp_executesql @Q
END
GO

ALTER PROCEDURE [dbo].[sp_Webapi_GetProductWiseOrderReport_L]
    @empId int = null,
    @Role  nvarchar(max)=null,
    @Month  int=null,
    @Year   int=null,
    @ProviderType  int=null,
    @CustomerType  int=null,
    @CustomerId int=null
AS
BEGIN
    DECLARE @params NVARCHAR(max)='  '
    IF(@Month IS NOT NULL)
        SET @params=@params+ ' AND MONTH(convert(Date,  D.SubmissionDate))='''+CAST(@Month AS NVARCHAR(max))+''''
    IF(@Year IS NOT NULL )
        SET @params=@params+ ' AND   year(convert(Date,  D.SubmissionDate))= '''+CAST(@Year AS NVARCHAR(max))+''''
    IF(@ProviderType IS NOT NULL )
        SET @params=@params+ ' AND  D.ProgramTypeId='''+CAST(@ProviderType AS NVARCHAR(max))+''''
    IF(@CustomerType IS NOT NULL )
        SET @params=@params+ ' AND  D.CustTypeId='''+CAST(@CustomerType AS NVARCHAR(max))+''''
    IF(ISNULL(@CustomerId,0) > 0)
        SET @params=@params+ ' AND  D.CustomerMasterId='+CAST(@CustomerId AS NVARCHAR(max))
    DECLARE @Q NVARCHAR(MAX)
    SET @Q='select  ROW_NUMBER() Over (Order by pro.ProductCode) As [SN], order_times,  pro.ProductCode+'' : ''+pro.ProductName product_name,   (tblDtl.Quantity) ordered_qty, ordered_value from tblProduct pro
inner join (select ISNULL(count(D.OrderId),0) order_times, convert(decimal(18,2),iSNULL(SUM(dtl.TotalTradePrice-dtl.DiscountAmount),0)) as ordered_value,  SUM(dtl.Quantity) Quantity, dtl.ProductId from tblOrderDetail dtl
inner join  dbo.tblOrder D  with (nolock) on dtl.OrderId=D.OrderId
left join tblUser u with (nolock) on D.EntryBy=u.UserId
left join tblEmpGeneralInfo emp with (nolock) on emp.EmpInfoId=u.EmpInfoId
inner join View_Webapi_EmployeeFieldForceInfo fs on emp.EmpInfoId=fs.EmpInfoId
WHERE  IsFromApp = 1  and (fs.MIOEmpId='+convert(nvarchar(max),@empId)+' or fs.ASMEMPId='+convert(nvarchar(max),@empId)+' or fs.RSMEMPId='+convert(nvarchar(max),@empId)+' or fs.NSMEMPId='+convert(nvarchar(max),@empId)+')
  '  +@params+'
 group by  ProductId) tblDtl on tblDtl.ProductId=pro.ProductId
'
    EXEC sys.sp_executesql @Q
END
GO

ALTER PROCEDURE [dbo].[sp_Webapi_GetProductWiseSalesReport_L]
    @empId int = null,
    @Role  nvarchar(max)=null,
    @Month  int=null,
    @Year   int=null,
    @ProviderType  int=null,
    @CustomerType  int=null,
    @CustomerId int=null
AS
BEGIN
    DECLARE @params NVARCHAR(max)='  '
    IF(@Month IS NOT NULL)
        SET @params=@params+ ' AND MONTH(convert(Date,A.InvoiceDate))='''+CAST(@Month AS NVARCHAR(max))+''''
    IF(@Year IS NOT NULL )
        SET @params=@params+ ' AND   year(convert(Date,A.InvoiceDate))= '''+CAST(@Year AS NVARCHAR(max))+''''
    IF(@ProviderType IS NOT NULL )
        SET @params=@params+ ' AND  D.ProgramTypeId='''+CAST(@ProviderType AS NVARCHAR(max))+''''
    IF(@CustomerType IS NOT NULL )
        SET @params=@params+ ' AND  D.CustTypeId='''+CAST(@CustomerType AS NVARCHAR(max))+''''
    IF(ISNULL(@CustomerId,0) > 0)
        SET @params=@params+ ' AND  D.CustomerMasterId='+CAST(@CustomerId AS NVARCHAR(max))
    DECLARE @Q NVARCHAR(MAX)
    SET @Q='select  ROW_NUMBER() Over (Order by pro.ProductCode) As [SN], order_times,  pro.ProductCode+'' : ''+pro.ProductName product_name,   (tblDtl.Quantity) ordered_qty, ordered_value from tblProduct pro
inner join (select ISNULL(count(D.OrderId),0) order_times,  convert(decimal(18,2),iSNULL(SUM(dtl.TotalTradePrice-dtl.DiscountAmount),0)) as ordered_value,  SUM(dtl.Quantity) Quantity, dtl.ProductId from tblOrderDetail dtl
inner join  dbo.tblOrder D  with (nolock) on dtl.OrderId=D.OrderId
inner join  dbo.tblInvoice A  with (nolock) on A.OrderId=D.OrderId
left join tblUser u with (nolock) on D.EntryBy=u.UserId
left join tblEmpGeneralInfo emp with (nolock) on emp.EmpInfoId=u.EmpInfoId
inner join View_Webapi_EmployeeFieldForceInfo fs on emp.EmpInfoId=fs.EmpInfoId
WHERE  IsFromApp = 1  and (fs.MIOEmpId='+convert(nvarchar(max),@empId)+' or fs.ASMEMPId='+convert(nvarchar(max),@empId)+' or fs.RSMEMPId='+convert(nvarchar(max),@empId)+' or fs.NSMEMPId='+convert(nvarchar(max),@empId)+')
  '  +@params+'
 group by  ProductId) tblDtl on tblDtl.ProductId=pro.ProductId
'
    EXEC sys.sp_executesql @Q
END
GO

GO

-- ===== 5. sp_WebAPI_Get_TargetAchievement_SubTerritory.sql =====
-- Target & Achievement report (app: Reports > "Target & Achievement").
-- Target  : tbl_Target_SubTerritoryMonthly (client-supplied, one row per year/month/sub-territory).
-- Sales   : same basis as sp_WebAPI_Get_TargetAChivementReport SalesValue: delivered invoices
--           (DeliveryInvoiceStatus Full/Partial) dated by tblInvoice.UpdateDate,
--           SUM(TotalPrice - DiscountAmount) - SUM(AdjustmentAmount); sub-territory from the order, else the customer,
--           else one "(no sub-territory)" row per territory of the order's entry employee (target 0).
-- Works with an empty target table: rows then come from sales only.
-- Access  : dbo.fn_Webapi_IsAllDataRole(@empId) = 1 -> every sub-territory; others -> own hierarchy only.
-- Roll-up : sub-territory -> territory (MIO) -> area (ASM) -> region (RSM) -> group (NSM),
--           resolved through View_Webapi_EmployeeFieldForceInfo like sp_WebAPI_Get_TargetAChivementReport.
-- Re-runnable: table is created only if missing; SP uses CREATE OR ALTER.

IF OBJECT_ID('dbo.tbl_Target_SubTerritoryMonthly', 'U') IS NULL
BEGIN
	CREATE TABLE [dbo].[tbl_Target_SubTerritoryMonthly](
		[TargetId] int IDENTITY(1,1) NOT NULL PRIMARY KEY,
		[TargetYear] int NOT NULL,
		[TargetMonth] int NOT NULL,
		[SubTerritoryCode] nvarchar(50) NOT NULL,
		[EmployeeCode] nvarchar(50) NULL,
		[TargetAmt] numeric(18,2) NOT NULL DEFAULT 0,
		[IsActive] bit NOT NULL DEFAULT 1,
		[EntryDate] datetime NOT NULL DEFAULT getdate(),
		CONSTRAINT [UQ_Target_SubTerr_Month] UNIQUE (TargetYear, TargetMonth, SubTerritoryCode))
END
GO

-- Sample data (NOT executed; SubTerritoryCode must exist in tblSubTerritory.SubTerritoryCode):
-- INSERT INTO dbo.tbl_Target_SubTerritoryMonthly (TargetYear, TargetMonth, SubTerritoryCode, EmployeeCode, TargetAmt)
-- VALUES (2026, 10, '51520', 'E0101', 250000.00),
--        (2026, 10, '51521', 'E0102', 180000.00);

-- EXEC sp_WebAPI_Get_TargetAchievement_SubTerritory @empId = 7, @month = 10, @year = 2026
CREATE OR ALTER PROCEDURE [dbo].[sp_WebAPI_Get_TargetAchievement_SubTerritory]
	@empId INT,
	@month INT,
	@year  INT,
	@selectedEmpId INT = 0   -- app Sales Person filter: 0 = everyone in @empId's hierarchy, else that employee only
AS
BEGIN
	SET NOCOUNT ON;

	DECLARE @from DATE = DATEFROMPARTS(@year, @month, 1);
	DECLARE @to   DATE = DATEADD(MONTH, 1, @from);
	DECLARE @allData BIT = dbo.fn_Webapi_IsAllDataRole(@empId);
	-- targets are stored per EmployeeCode (= tblEmpGeneralInfo.EmpMasterCode); sales per order entry user
	DECLARE @selCode NVARCHAR(50) = (SELECT TOP 1 EmpMasterCode FROM dbo.tblEmpGeneralInfo WITH (NOLOCK) WHERE EmpInfoId = @selectedEmpId);

	;WITH emp AS (
		SELECT TerritoryId, AreaId, RegionId, GroupId
		FROM dbo.View_Webapi_EmployeeFieldForceInfo
		WHERE EmpInfoId = @empId
	),
	terr AS (
		SELECT tr.TerritoryId, tr.TerritoryName
		FROM dbo.tblTerritory tr WITH (NOLOCK)
		-- LEFT: a territory without an area/region (seen in the Unicorn DB) must still match on TerritoryId
		LEFT JOIN dbo.tblArea ar WITH (NOLOCK) ON ar.AreaId = tr.AreaId
		LEFT JOIN dbo.tblRegion rg WITH (NOLOCK) ON rg.RegionId = ar.RegionId
		WHERE @allData = 1
		   OR EXISTS (SELECT 1 FROM emp e
		              WHERE e.TerritoryId = tr.TerritoryId OR e.AreaId = ar.AreaId
		                 OR e.RegionId = rg.RegionId OR e.GroupId = rg.GroupId)
	),
	st AS (
		SELECT st.SubTerritoryId, st.SubTerritoryCode, st.SubTerritoryName, t.TerritoryName
		FROM dbo.tblSubTerritory st WITH (NOLOCK)
		INNER JOIN terr t ON t.TerritoryId = st.TerritoryId
	),
	tg AS (
		SELECT SubTerritoryCode, SUM(TargetAmt) TargetAmt
		FROM dbo.tbl_Target_SubTerritoryMonthly WITH (NOLOCK)
		WHERE TargetYear = @year AND TargetMonth = @month AND IsActive = 1
		  AND (@selectedEmpId = 0 OR EmployeeCode = @selCode)
		GROUP BY SubTerritoryCode
	),
	-- EmpTerritoryId: territory of the employee who entered the order, used when the sale has no sub-territory
	sl AS (
		SELECT COALESCE(NULLIF(o.SubTerritoryId, 0), NULLIF(c.SubTerritoryId, 0)) SubTerritoryId,
		       ef.TerritoryId EmpTerritoryId,
		       SUM(ID.TotalPrice - ID.DiscountAmount) - SUM(ISNULL(ID.AdjustmentAmount, 0)) SalesAmt
		FROM dbo.tblInvoice inv WITH (NOLOCK)
		INNER JOIN dbo.tblInvoiceDetail ID WITH (NOLOCK) ON ID.InvoiceId = inv.InvoiceId
		INNER JOIN dbo.tblOrder o WITH (NOLOCK) ON o.OrderId = inv.OrderId
		LEFT JOIN dbo.tblCustMaster c WITH (NOLOCK) ON c.CustomerMasterId = o.CustomerMasterId
		LEFT JOIN dbo.tblUser u WITH (NOLOCK) ON u.UserId = o.EntryBy
		OUTER APPLY (SELECT TOP (1) v.TerritoryId FROM dbo.View_Webapi_EmployeeFieldForceInfo v WITH (NOLOCK)
		             WHERE v.EmpInfoId = u.EmpInfoId) ef
		WHERE inv.DeliveryInvoiceStatus IN ('Full', 'Partial')
		  AND inv.UpdateDate >= @from
		  AND inv.UpdateDate <  @to
		  AND (@selectedEmpId = 0 OR o.EntryBy IN (SELECT UserId FROM dbo.tblUser WITH (NOLOCK) WHERE EmpInfoId = @selectedEmpId))
		GROUP BY COALESCE(NULLIF(o.SubTerritoryId, 0), NULLIF(c.SubTerritoryId, 0)), ef.TerritoryId
	),
	slSub AS (
		SELECT SubTerritoryId, SUM(SalesAmt) SalesAmt FROM sl WHERE SubTerritoryId IS NOT NULL GROUP BY SubTerritoryId
	),
	-- Fallback: orders/customers without a sub-territory (the norm in the Unicorn DB) are reported per territory,
	-- so achievement still shows; there is no sub-territory target to compare them with.
	slNoSub AS (
		SELECT EmpTerritoryId, SUM(SalesAmt) SalesAmt FROM sl WHERE SubTerritoryId IS NULL GROUP BY EmpTerritoryId
	),
	rpt AS (
		SELECT 0 AS SortGroup, st.SubTerritoryCode, st.SubTerritoryName, st.TerritoryName,
		       ISNULL(tg.TargetAmt, 0) TargetAmt, ISNULL(s.SalesAmt, 0) SalesAmt
		FROM st
		LEFT JOIN tg ON tg.SubTerritoryCode = st.SubTerritoryCode
		LEFT JOIN slSub s ON s.SubTerritoryId = st.SubTerritoryId
		WHERE tg.SubTerritoryCode IS NOT NULL OR s.SubTerritoryId IS NOT NULL
		UNION ALL
		-- the app shows "SubTerritoryCode : SubTerritoryName", so the territory name goes in the code column
		SELECT 1, ISNULL(t.TerritoryName, N'No territory'), N'(no sub-territory)', ISNULL(t.TerritoryName, N''),
		       0, n.SalesAmt
		FROM slNoSub n
		LEFT JOIN terr t ON t.TerritoryId = n.EmpTerritoryId
		-- access: the territory must be visible to @empId; sales with no territory at all only for all-data roles
		WHERE t.TerritoryId IS NOT NULL OR (@allData = 1 AND n.EmpTerritoryId IS NULL)
	)
	SELECT ROW_NUMBER() OVER (ORDER BY SortGroup, SubTerritoryCode) AS SerialNo,
	       SubTerritoryCode,
	       SubTerritoryName,
	       TerritoryName,
	       CAST(TargetAmt AS DECIMAL(18,2)) AS TargetAmt,
	       CAST(SalesAmt AS DECIMAL(18,2)) AS SalesAmt,
	       CAST(CASE WHEN TargetAmt = 0 THEN 0 ELSE SalesAmt * 100.0 / TargetAmt END AS DECIMAL(18,2)) AS AchvPercent,
	       CAST(TargetAmt - SalesAmt AS DECIMAL(18,2)) AS BalanceAmt
	FROM rpt
	ORDER BY SerialNo
END
GO

GO

-- ===== 6. sp_Webapi_Get_DcrListById.sql =====
GO
/*
  DCR List (app Reports > "DCR List"). Replaces the version in fix_Reports_CustWiseSales_DcrList.sql.
  New optional params (old callers unchanged):
    @doctorId INT = 0  -> 0 = all doctors, else only that doctor
    @allTeam  BIT = 0  -> 1 = @empId plus everyone in his hierarchy (app Sales Person "All")
  Also: static SQL instead of string-built dynamic SQL, and an empty FromDt/ToDt now means
  "no date filter" (it used to become 01-Jan-1900 and return nothing).
  Re-runnable (CREATE OR ALTER).
*/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE OR ALTER PROCEDURE [dbo].[sp_Webapi_Get_DcrListById]
    @empId NVARCHAR(50),
    @FromDt NVARCHAR(MAX) = NULL,
    @ToDt NVARCHAR(MAX) = NULL,
    @providertype NVARCHAR(MAX) = NULL,
    @pharmatype NVARCHAR(MAX) = NULL,
    @doctortype NVARCHAR(MAX) = NULL,
    @doctorId INT = 0,
    @allTeam BIT = 0
AS
BEGIN
    SET NOCOUNT ON;
    DECLARE @emp INT = TRY_CONVERT(INT, @empId);
    DECLARE @from DATE = TRY_CONVERT(DATE, NULLIF(@FromDt, ''));
    DECLARE @to   DATE = ISNULL(TRY_CONVERT(DATE, NULLIF(@ToDt, '')), @from);
    DECLARE @prov INT = ISNULL(TRY_CONVERT(INT, NULLIF(@providertype, '')), 0);
    DECLARE @pharma INT = ISNULL(TRY_CONVERT(INT, NULLIF(@pharmatype, '')), 0);
    DECLARE @dtype INT = ISNULL(TRY_CONVERT(INT, NULLIF(@doctortype, '')), 0);

    -- users whose DCRs are shown: the employee, or (All) the employee + his hierarchy
    DECLARE @users TABLE (UserId INT PRIMARY KEY);
    INSERT INTO @users (UserId)
    SELECT DISTINCT u.UserId
    FROM dbo.tblUser u WITH (NOLOCK)
    WHERE u.EmpInfoId = @emp
       OR (@allTeam = 1 AND u.EmpInfoId IN (
                SELECT v.EmpInfoId FROM dbo.View_Webapi_EmployeeFieldForceInfo v WITH (NOLOCK)
                WHERE v.MIOEmpId = @emp OR v.ASMEMPId = @emp OR v.RSMEMPId = @emp OR v.NSMEMPId = @emp));

    SELECT  A.DcrId,
            FORMAT(A.DcrDate, 'MMMM dd, yyyy hh:mm tt') AS DcrDate,
            B.TourTypeName,
            C.Name AS ChamberAddress,
            D.EmpName,
            A.IsApproved,
            M.DoctorName AS Name,
            A.ApprovalStatus
    FROM    dbo.tbl_DCRInfo A
            INNER JOIN @users us ON us.UserId = A.EntryBy
            LEFT JOIN dbo.tbl_TourPlanType B ON B.TourTypeId = A.TourTypeId
            LEFT JOIN dbo.tblDoctorChemberDetail C ON C.ChemberId = A.ChemberId
            LEFT JOIN dbo.tblUser U2 ON U2.UserId = A.EntryBy
            LEFT JOIN dbo.tblEmpGeneralInfo D ON D.EmpInfoId = U2.EmpInfoId
            LEFT JOIN dbo.tblDoctorMaster M ON M.DoctorId = A.DoctorId
    WHERE (@from IS NULL OR CONVERT(DATE, A.DcrDate) BETWEEN @from AND @to)
      AND (@prov = 0 OR CONVERT(INT, A.DoctorProgramypeId) = @prov)
      AND (@pharma = 0 OR CONVERT(INT, A.SmcTypeId_DCR) = @pharma)
      AND (@dtype = 0 OR CONVERT(INT, M.Doctortypeid) = @dtype)
      AND (ISNULL(@doctorId, 0) = 0 OR A.DoctorId = @doctorId)
    ORDER BY A.DcrDate DESC, A.DcrId DESC
    OPTION (RECOMPILE);
END
GO

GO

-- ===== 7. dcr_chamber_location_fill.sql =====
GO
/*
  DCR chamber location: the first DCR at a chamber stores the visit's GPS position as the
  chamber's Lat/Lon. Once a chamber has a location it is never changed by a DCR.
  Called by the API right after a successful DCR save (doctor DCR only; for CVR the ChamberId is a customer).
  Re-runnable.
*/
-- the live DB already has these columns; add them where they are missing (e.g. local copies)
IF COL_LENGTH('dbo.tblDoctorChemberDetail', 'isLock') IS NULL
    ALTER TABLE dbo.tblDoctorChemberDetail ADD isLock BIT NULL;
IF COL_LENGTH('dbo.tblDoctorChemberDetail', 'Lat') IS NULL
    ALTER TABLE dbo.tblDoctorChemberDetail ADD Lat NVARCHAR(MAX) NULL;
IF COL_LENGTH('dbo.tblDoctorChemberDetail', 'Lon') IS NULL
    ALTER TABLE dbo.tblDoctorChemberDetail ADD Lon NVARCHAR(MAX) NULL;
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
-- Returns Updated = 1 when the chamber's empty location was filled, 0 when it already had one
-- (or the incoming position is missing / zero).
CREATE OR ALTER PROCEDURE dbo.sp_Webapi_Set_ChamberLocationIfEmpty
    @chamberId INT,
    @Latitude  NVARCHAR(50),
    @Longitude NVARCHAR(50)
AS
BEGIN
    SET NOCOUNT ON;
    DECLARE @lat DECIMAL(9,6) = TRY_CONVERT(DECIMAL(9,6), NULLIF(LTRIM(RTRIM(@Latitude)), ''));
    DECLARE @lon DECIMAL(9,6) = TRY_CONVERT(DECIMAL(9,6), NULLIF(LTRIM(RTRIM(@Longitude)), ''));

    -- no usable fix (missing, unparsable, or 0,0 from a GPS that was not ready): leave the chamber alone
    IF @lat IS NULL OR @lon IS NULL OR (@lat = 0 AND @lon = 0)
    BEGIN
        SELECT CAST(0 AS BIT) AS Updated;
        RETURN;
    END

    UPDATE dbo.tblDoctorChemberDetail
    SET Lat = LTRIM(RTRIM(@Latitude)),
        Lon = LTRIM(RTRIM(@Longitude))
    WHERE ChemberId = @chamberId
      AND (NULLIF(LTRIM(RTRIM(Lat)), '') IS NULL OR NULLIF(LTRIM(RTRIM(Lon)), '') IS NULL);

    SELECT CAST(CASE WHEN @@ROWCOUNT > 0 THEN 1 ELSE 0 END AS BIT) AS Updated;
END
GO

GO

-- ===== 8. sp_Webapi_Save_DayWiseTourPlan.sql =====
GO

/****** Object:  Table [dbo].[tbl_DayWiseTourPlanMaster] ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[tbl_DayWiseTourPlanMaster]') AND type in (N'U'))
BEGIN
CREATE TABLE [dbo].[tbl_DayWiseTourPlanMaster](
    [TourPlanMasterId] [int] IDENTITY(1,1) NOT NULL,
    [EmpInfoId] [int] NOT NULL,
    [TourPlanDate] [date] NOT NULL,
    [IsFinalSubmit] [bit] NOT NULL DEFAULT ((0)),
    [CreatedDate] [datetime] NOT NULL DEFAULT (getdate()),
    [UpdatedDate] [datetime] NULL,
    CONSTRAINT [PK_tbl_DayWiseTourPlanMaster] PRIMARY KEY CLUSTERED 
    (
        [TourPlanMasterId] ASC
    )
)
END
GO

/****** Object:  Table [dbo].[tbl_DayWiseTourPlanCustomerDetail] ******/
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[tbl_DayWiseTourPlanCustomerDetail]') AND type in (N'U'))
BEGIN
CREATE TABLE [dbo].[tbl_DayWiseTourPlanCustomerDetail](
    [CustomerDetailId] [int] IDENTITY(1,1) NOT NULL,
    [TourPlanMasterId] [int] NOT NULL,
    [CustomerMasterId] [int] NOT NULL,
    [CreatedDate] [datetime] NOT NULL DEFAULT (getdate()),
    CONSTRAINT [PK_tbl_DayWiseTourPlanCustomerDetail] PRIMARY KEY CLUSTERED 
    (
        [CustomerDetailId] ASC
    )
)
END
GO

/****** Object:  Table [dbo].[tbl_DayWiseTourPlanDoctorDetail] ******/
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[tbl_DayWiseTourPlanDoctorDetail]') AND type in (N'U'))
BEGIN
CREATE TABLE [dbo].[tbl_DayWiseTourPlanDoctorDetail](
    [DoctorDetailId] [int] IDENTITY(1,1) NOT NULL,
    [TourPlanMasterId] [int] NOT NULL,
    [DoctorId] [int] NOT NULL,
    [CreatedDate] [datetime] NOT NULL DEFAULT (getdate()),
    CONSTRAINT [PK_tbl_DayWiseTourPlanDoctorDetail] PRIMARY KEY CLUSTERED 
    (
        [DoctorDetailId] ASC
    )
)
END
GO

-- Chamber-wise tour plan: NULL = doctor-wise (any chamber of the doctor; also all older rows).
IF COL_LENGTH(N'dbo.tbl_DayWiseTourPlanDoctorDetail', N'ChamberId') IS NULL
    ALTER TABLE [dbo].[tbl_DayWiseTourPlanDoctorDetail] ADD [ChamberId] [int] NULL;
GO

/****** Object:  StoredProcedure [dbo].[sp_Webapi_Save_DayWiseTourPlan] ******/
CREATE OR ALTER PROCEDURE [dbo].[sp_Webapi_Save_DayWiseTourPlan]
    @EmpInfoId INT,
    @TourPlanDate DATE,
    @IsFinalSubmit BIT = 1,
    @CustomerIds VARCHAR(MAX) = '', -- Comma separated customer IDs e.g. '101,102'
    @DoctorIds VARCHAR(MAX) = '',    -- Comma separated doctor IDs e.g. '201,202' (doctor-wise)
    @DoctorChamberIds VARCHAR(MAX) = '' -- Comma separated doctorId:chamberId e.g. '201:55,202:0' (0 = doctor-wise); used instead of @DoctorIds when given
AS
BEGIN
    SET NOCOUNT ON;
    BEGIN TRANSACTION;
    BEGIN TRY
        DECLARE @TourPlanMasterId INT;

        -- Check if master record exists for this employee and date
        SELECT @TourPlanMasterId = TourPlanMasterId 
        FROM [dbo].[tbl_DayWiseTourPlanMaster]
        WHERE [EmpInfoId] = @EmpInfoId AND [TourPlanDate] = @TourPlanDate;

        IF (@TourPlanMasterId IS NULL OR @TourPlanMasterId = 0)
        BEGIN
            INSERT INTO [dbo].[tbl_DayWiseTourPlanMaster] ([EmpInfoId], [TourPlanDate], [IsFinalSubmit], [CreatedDate])
            VALUES (@EmpInfoId, @TourPlanDate, @IsFinalSubmit, GETDATE());

            SET @TourPlanMasterId = SCOPE_IDENTITY();
        END
        ELSE
        BEGIN
            UPDATE [dbo].[tbl_DayWiseTourPlanMaster]
            SET [IsFinalSubmit] = @IsFinalSubmit,
                [UpdatedDate] = GETDATE()
            WHERE [TourPlanMasterId] = @TourPlanMasterId;

            -- Delete old details for update
            DELETE FROM [dbo].[tbl_DayWiseTourPlanCustomerDetail] WHERE [TourPlanMasterId] = @TourPlanMasterId;
            DELETE FROM [dbo].[tbl_DayWiseTourPlanDoctorDetail] WHERE [TourPlanMasterId] = @TourPlanMasterId;
        END

        -- Insert Customer Details
        IF (@CustomerIds IS NOT NULL AND LEN(@CustomerIds) > 0)
        BEGIN
            INSERT INTO [dbo].[tbl_DayWiseTourPlanCustomerDetail] ([TourPlanMasterId], [CustomerMasterId], [CreatedDate])
            SELECT @TourPlanMasterId, CAST(Item AS INT), GETDATE()
            FROM dbo.DelimitedSplit8K(@CustomerIds, ',')
            WHERE LTRIM(RTRIM(Item)) <> '';
        END

        -- Insert Doctor Details
        IF (@DoctorChamberIds IS NOT NULL AND LEN(@DoctorChamberIds) > 0)
        BEGIN
            INSERT INTO [dbo].[tbl_DayWiseTourPlanDoctorDetail] ([TourPlanMasterId], [DoctorId], [ChamberId], [CreatedDate])
            SELECT @TourPlanMasterId,
                   CAST(LEFT(v, CHARINDEX(':', v + ':') - 1) AS INT),
                   NULLIF(TRY_CAST(SUBSTRING(v, CHARINDEX(':', v + ':') + 1, 20) AS INT), 0),
                   GETDATE()
            FROM (SELECT LTRIM(RTRIM(Item)) AS v FROM dbo.DelimitedSplit8K(@DoctorChamberIds, ',')) x
            WHERE v <> '';
        END
        ELSE IF (@DoctorIds IS NOT NULL AND LEN(@DoctorIds) > 0)
        BEGIN
            INSERT INTO [dbo].[tbl_DayWiseTourPlanDoctorDetail] ([TourPlanMasterId], [DoctorId], [CreatedDate])
            SELECT @TourPlanMasterId, CAST(Item AS INT), GETDATE()
            FROM dbo.DelimitedSplit8K(@DoctorIds, ',')
            WHERE LTRIM(RTRIM(Item)) <> '';
        END

        COMMIT TRANSACTION;
        SELECT 1 AS IsSuccess, 'Day-wise Tour Plan saved successfully.' AS Message, @TourPlanMasterId AS TourPlanMasterId;
    END TRY
    BEGIN CATCH
        ROLLBACK TRANSACTION;
        SELECT 0 AS IsSuccess, ERROR_MESSAGE() AS Message, 0 AS TourPlanMasterId;
    END CATCH
END
GO

/****** Object:  StoredProcedure [dbo].[sp_Webapi_Get_DayWiseTourPlan] ******/
-- Saved day-wise plan of one month, so the app can show earlier selections again.
-- Type 'C' = customer (Id = CustomerMasterId), 'D' = doctor (Id = DoctorId, ChamberId 0 = doctor-wise).
CREATE OR ALTER PROCEDURE [dbo].[sp_Webapi_Get_DayWiseTourPlan]
    @EmpInfoId INT,
    @Month INT,
    @Year INT
AS
BEGIN
    SET NOCOUNT ON;
    DECLARE @From DATE = DATEFROMPARTS(@Year, @Month, 1);
    DECLARE @To DATE = EOMONTH(@From);

    SELECT CONVERT(VARCHAR(10), m.TourPlanDate, 23) AS TourPlanDate, 'C' AS Type, cd.CustomerMasterId AS Id, 0 AS ChamberId
    FROM [dbo].[tbl_DayWiseTourPlanMaster] m
    INNER JOIN [dbo].[tbl_DayWiseTourPlanCustomerDetail] cd ON cd.TourPlanMasterId = m.TourPlanMasterId
    WHERE m.EmpInfoId = @EmpInfoId AND m.TourPlanDate BETWEEN @From AND @To
    UNION ALL
    SELECT CONVERT(VARCHAR(10), m.TourPlanDate, 23), 'D', dd.DoctorId, ISNULL(dd.ChamberId, 0)
    FROM [dbo].[tbl_DayWiseTourPlanMaster] m
    INNER JOIN [dbo].[tbl_DayWiseTourPlanDoctorDetail] dd ON dd.TourPlanMasterId = m.TourPlanMasterId
    WHERE m.EmpInfoId = @EmpInfoId AND m.TourPlanDate BETWEEN @From AND @To;
END
GO

GO

-- ===== 9. sp_Webapi_Get_TourPlanReport.sql =====
GO
/*
  Tour Plan Report (app Reports menu -> "Tour Plan Report").
  Reads what the app's tour plan entry screens save:
    - Tour Plan (market / other visit)  : tbl_TourPlanMaster + tbl_TourPlanInfo (+ tblTPMarketDetail, tblTPCustomerDetail, tbl_TourPlanPurpose)
    - Doctor/Customer Tour Plan          : tbl_DoctorTourPlanMaster + tbl_DoctorTourPlanDetail
    - Day-wise Tour Plan (if deployed)   : tbl_DayWiseTourPlanMaster + tbl_DayWiseTourPlanDoctorDetail / CustomerDetail
  Hierarchy: same rule as other app reports (View_Webapi_EmployeeFieldForceInfo MIO/ASM/RSM/NSM EmpId = @empId);
             all-data roles (dbo.fn_Webapi_IsAllDataRole) see every employee.
  Date params accept the app's "d-MMM-yyyy" text; empty = current month.
  Re-runnable (CREATE OR ALTER needs SQL 2016 SP1+; DB compat level does not matter).
*/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE OR ALTER PROCEDURE [dbo].[sp_Webapi_Get_TourPlanReport]
    @empId INT,
    @selectedEmpId INT = 0,
    @FromDt NVARCHAR(50) = NULL,
    @ToDt NVARCHAR(50) = NULL,
    @doctorId INT = 0,   -- 0 = all; otherwise only entries that include this doctor
    @customerId INT = 0  -- 0 = all; otherwise only entries that include this customer
AS
BEGIN
    SET NOCOUNT ON;
    SET @doctorId = ISNULL(@doctorId, 0);
    SET @customerId = ISNULL(@customerId, 0);
    DECLARE @all BIT = CASE WHEN @doctorId = 0 AND @customerId = 0 THEN 1 ELSE 0 END;

    DECLARE @From DATE = CASE WHEN ISNULL(@FromDt, '') = '' THEN DATEADD(DAY, 1, EOMONTH(GETDATE(), -1)) ELSE CONVERT(DATE, @FromDt) END;
    DECLARE @To DATE = CASE WHEN ISNULL(@ToDt, '') = '' THEN EOMONTH(GETDATE()) ELSE CONVERT(DATE, @ToDt) END;
    DECLARE @ToNext DATE = DATEADD(DAY, 1, @To);

    -- logged-in employee + everyone under him
    SELECT @empId AS EmpInfoId INTO #emp
    UNION
    SELECT EmpInfoId FROM dbo.View_Webapi_EmployeeFieldForceInfo WITH (NOLOCK)
    WHERE MIOEmpId = @empId OR ASMEMPId = @empId OR RSMEMPId = @empId OR NSMEMPId = @empId
    UNION
    SELECT EmpInfoId FROM dbo.tblEmpGeneralInfo WITH (NOLOCK)
    WHERE dbo.fn_Webapi_IsAllDataRole(@empId) = 1;

    -- a specific employee may only be picked from the allowed set
    IF (ISNULL(@selectedEmpId, 0) > 0)
        DELETE FROM #emp WHERE EmpInfoId <> @selectedEmpId;

    CREATE TABLE #rpt (
        EmpInfoId INT, TourPlanDate DATE, VisitType NVARCHAR(50), Shift NVARCHAR(20),
        DoctorCustomer NVARCHAR(MAX), AreaName NVARCHAR(MAX), MarketName NVARCHAR(MAX),
        Purpose NVARCHAR(MAX), Objective NVARCHAR(MAX), ApprovalStatus NVARCHAR(50));

    /* 1. Tour Plan (market / other visit) */
    SELECT t.TourPlanId, t.EmpInfoId, CONVERT(DATE, t.TourPlanDate) TourPlanDate, t.IsMarketVisit, t.IsOtherVisit,
           t.IsMorning, t.IsEvening, t.AreaName, t.MarketName, t.MarketNameEnd, t.Objective, t.TPId,
           m.ApprovalStatus, m.IsFinalSubmit
    INTO #tp
    FROM dbo.tbl_TourPlanInfo t WITH (NOLOCK)
    INNER JOIN dbo.tbl_TourPlanMaster m WITH (NOLOCK) ON m.TPMaster = t.TPMaster
    INNER JOIN #emp e ON e.EmpInfoId = t.EmpInfoId
    WHERE t.TourPlanDate >= @From AND t.TourPlanDate < @ToNext
      AND (@all = 1 OR (@customerId > 0 AND EXISTS (SELECT 1 FROM dbo.tblTPCustomerDetail cd WITH (NOLOCK)
                                                    WHERE cd.TourPlanId = t.TourPlanId AND cd.CustomerMasterId = @customerId)));

    SELECT md.TourPlanId, mk.MarketName INTO #tpMarket
    FROM dbo.tblTPMarketDetail md WITH (NOLOCK)
    INNER JOIN #tp ON #tp.TourPlanId = md.TourPlanId
    INNER JOIN dbo.tblMarket mk WITH (NOLOCK) ON mk.MarketId = md.MarketId;

    SELECT cd.TourPlanId, c.CustomerName INTO #tpCust
    FROM dbo.tblTPCustomerDetail cd WITH (NOLOCK)
    INNER JOIN #tp ON #tp.TourPlanId = cd.TourPlanId
    INNER JOIN dbo.tblCustMaster c WITH (NOLOCK) ON c.CustomerMasterId = cd.CustomerMasterId;

    INSERT INTO #rpt
    SELECT t.EmpInfoId, t.TourPlanDate,
           CASE WHEN t.IsOtherVisit = 1 THEN 'Other Visit' ELSE 'Market Visit' END,
           CASE WHEN t.IsMorning = 1 THEN 'Morning' WHEN t.IsEvening = 1 THEN 'Evening' ELSE '' END,
           STUFF((SELECT ', ' + x.CustomerName FROM #tpCust x WHERE x.TourPlanId = t.TourPlanId FOR XML PATH(''), TYPE).value('.', 'nvarchar(max)'), 1, 2, ''),
           t.AreaName,
           ISNULL(NULLIF(ISNULL(t.MarketName, '') + CASE WHEN ISNULL(t.MarketNameEnd, '') NOT IN ('', ISNULL(t.MarketName, '')) THEN ' - ' + t.MarketNameEnd ELSE '' END, ''),
                  STUFF((SELECT ', ' + x.MarketName FROM #tpMarket x WHERE x.TourPlanId = t.TourPlanId FOR XML PATH(''), TYPE).value('.', 'nvarchar(max)'), 1, 2, '')),
           p.TPName, t.Objective,
           CASE m.s WHEN '3' THEN 'Rejected' WHEN '2' THEN 'Approved' WHEN '1' THEN 'Verified'
                    ELSE CASE WHEN ISNULL(t.IsFinalSubmit, 0) = 1 THEN 'Pending' ELSE 'Not Submitted' END END
    FROM #tp t
    CROSS APPLY (SELECT ISNULL(t.ApprovalStatus, '0') s) m
    LEFT JOIN dbo.tbl_TourPlanPurpose p WITH (NOLOCK) ON p.TPId = t.TPId;

    /* 2. Doctor / Customer Tour Plan: one row per employee, date, type and market */
    SELECT d.EmpInfoId, CONVERT(DATE, d.TourPlanDate) TourPlanDate, ISNULL(d.Type_DV, 'D') Type_DV,
           ISNULL(d.MarketName_DV, '') MarketName, d.AreaName_DV, d.DoctorName_DV, dm.ApprovalStatus, dm.IsFinalSubmit
    INTO #dtp
    FROM dbo.tbl_DoctorTourPlanMaster dm WITH (NOLOCK)
    INNER JOIN #emp e ON e.EmpInfoId = dm.EmpInfoId
    INNER JOIN dbo.tbl_DoctorTourPlanDetail d WITH (NOLOCK) ON d.DocTPMaster = dm.DocTPMaster
    WHERE dm.YearValue * 100 + dm.MonthValue BETWEEN YEAR(@From) * 100 + MONTH(@From) AND YEAR(@To) * 100 + MONTH(@To)
      AND d.TourPlanDate >= @From AND d.TourPlanDate < @ToNext
      -- Type_DV 'C' rows keep the customer id in DoctorId
      AND (@all = 1 OR (ISNULL(d.Type_DV, 'D') = 'C' AND d.DoctorId = @customerId) OR (ISNULL(d.Type_DV, 'D') <> 'C' AND d.DoctorId = @doctorId));

    INSERT INTO #rpt
    SELECT g.EmpInfoId, g.TourPlanDate,
           CASE WHEN g.Type_DV = 'C' THEN 'Customer Tour Plan' ELSE 'Doctor Tour Plan' END, '',
           STUFF((SELECT ', ' + x.DoctorName_DV FROM #dtp x
                  WHERE x.EmpInfoId = g.EmpInfoId AND x.TourPlanDate = g.TourPlanDate AND x.Type_DV = g.Type_DV AND x.MarketName = g.MarketName
                  FOR XML PATH(''), TYPE).value('.', 'nvarchar(max)'), 1, 2, ''),
           g.AreaName, g.MarketName, '', '',
           CASE g.ApprovalStatus WHEN '3' THEN 'Rejected' WHEN '2' THEN 'Approved' WHEN '1' THEN 'Verified'
                ELSE CASE WHEN g.IsFinalSubmit = 1 THEN 'Pending' ELSE 'Not Submitted' END END
    FROM (
        SELECT EmpInfoId, TourPlanDate, Type_DV, MarketName, MAX(AreaName_DV) AreaName,
               MAX(ApprovalStatus) ApprovalStatus, MAX(CONVERT(INT, ISNULL(IsFinalSubmit, 0))) IsFinalSubmit
        FROM #dtp
        GROUP BY EmpInfoId, TourPlanDate, Type_DV, MarketName
    ) g;

    /* 3. Day-wise Tour Plan (tables come from sp_Webapi_Save_DayWiseTourPlan.sql; skipped where not deployed)
          one row per plan, shift and kind (Doctor / Customer), names one per line: doctors take the shift of their chamber
          (tblDoctorChemberDetail, the app's chamber list), customers have none */
    CREATE TABLE #dw (TourPlanMasterId INT, EmpInfoId INT, TourPlanDate DATE, IsFinalSubmit BIT, Shift NVARCHAR(20), Kind NVARCHAR(20), Name NVARCHAR(MAX));
    IF OBJECT_ID('dbo.tbl_DayWiseTourPlanMaster') IS NOT NULL
        EXEC sp_executesql N'
        INSERT INTO #dw
        SELECT m.TourPlanMasterId, m.EmpInfoId, m.TourPlanDate, m.IsFinalSubmit, ISNULL(ch.Shift, ''''), ''Doctor'',
               dm.DoctorName + ISNULL('' ('' + NULLIF(ch.Name, ''N/A'') + '')'', '''')
        FROM dbo.tbl_DayWiseTourPlanMaster m
        INNER JOIN #emp e ON e.EmpInfoId = m.EmpInfoId
        INNER JOIN dbo.tbl_DayWiseTourPlanDoctorDetail dd ON dd.TourPlanMasterId = m.TourPlanMasterId
        INNER JOIN dbo.tblDoctorMaster dm WITH (NOLOCK) ON dm.DoctorId = dd.DoctorId
        LEFT JOIN dbo.tblDoctorChemberDetail ch WITH (NOLOCK) ON ch.ChemberId = dd.ChamberId AND ch.DoctorId = dd.DoctorId
        WHERE m.TourPlanDate >= @From AND m.TourPlanDate <= @To AND (@all = 1 OR dd.DoctorId = @doctorId)
        UNION ALL
        SELECT m.TourPlanMasterId, m.EmpInfoId, m.TourPlanDate, m.IsFinalSubmit, '''', ''Customer'', c.CustomerName
        FROM dbo.tbl_DayWiseTourPlanMaster m
        INNER JOIN #emp e ON e.EmpInfoId = m.EmpInfoId
        INNER JOIN dbo.tbl_DayWiseTourPlanCustomerDetail cd ON cd.TourPlanMasterId = m.TourPlanMasterId
        INNER JOIN dbo.tblCustMaster c WITH (NOLOCK) ON c.CustomerMasterId = cd.CustomerMasterId
        WHERE m.TourPlanDate >= @From AND m.TourPlanDate <= @To AND (@all = 1 OR cd.CustomerMasterId = @customerId)',
        N'@From DATE, @To DATE, @all BIT, @doctorId INT, @customerId INT', @From, @To, @all, @doctorId, @customerId;

    INSERT INTO #rpt
    SELECT g.EmpInfoId, g.TourPlanDate, g.Kind, g.Shift,
           STUFF((SELECT CHAR(10) + x.Name FROM #dw x WHERE x.TourPlanMasterId = g.TourPlanMasterId AND x.Shift = g.Shift AND x.Kind = g.Kind
                  ORDER BY x.Name FOR XML PATH(''), TYPE).value('.', 'nvarchar(max)'), 1, 1, ''),
           '', '', '', '',
           CASE WHEN g.IsFinalSubmit = 1 THEN 'Submitted' ELSE 'Not Submitted' END
    FROM (SELECT DISTINCT TourPlanMasterId, EmpInfoId, TourPlanDate, IsFinalSubmit, Shift, Kind FROM #dw) g;

    SELECT r.EmpInfoId, ISNULL(eg.EmpMasterCode, '') EmpCode, ISNULL(eg.EmpName, '') EmpName,
           CONVERT(VARCHAR(10), r.TourPlanDate, 23) TourPlanDate,
           r.VisitType, ISNULL(r.Shift, '') Shift, ISNULL(r.DoctorCustomer, '') DoctorCustomer,
           ISNULL(r.AreaName, '') AreaName, ISNULL(r.MarketName, '') MarketName,
           ISNULL(r.Purpose, '') Purpose, ISNULL(r.Objective, '') Objective, r.ApprovalStatus
    FROM #rpt r
    LEFT JOIN dbo.tblEmpGeneralInfo eg WITH (NOLOCK) ON eg.EmpInfoId = r.EmpInfoId
    -- the app groups rows as day > shift > visit type, so keep that order
    ORDER BY r.TourPlanDate, eg.EmpName, CASE r.Shift WHEN 'Morning' THEN 0 WHEN 'Evening' THEN 1 ELSE 2 END,
             CASE r.VisitType WHEN 'Doctor' THEN 0 WHEN 'Customer' THEN 1 ELSE 2 END, r.VisitType;
END
GO

GO

-- ===== 10. dcr_area_lock_and_tourplan_gate.sql =====
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

GO

-- ===== Done: verify =====
SELECT b.ObjectName,
       CASE WHEN OBJECT_ID(b.ObjectName) IS NULL THEN 'absent' ELSE 'present' END AS NowState,
       CASE WHEN b.Definition IS NULL THEN 'new' ELSE 'changed' END AS DeployAction,
       o.modify_date
FROM dbo.tbl_DeployBackup b
LEFT JOIN sys.objects o ON o.object_id = OBJECT_ID(b.ObjectName)
WHERE b.DeployTag = N'LIVE_2026-11-02'
ORDER BY b.ObjectName;
PRINT 'Deploy LIVE_2026-11-02 finished.';
GO
-- Leave the SSMS window usable if a guard switched execution off.
SET NOEXEC OFF;
GO
