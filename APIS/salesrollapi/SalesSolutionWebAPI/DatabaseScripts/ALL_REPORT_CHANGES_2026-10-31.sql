-- Combined script: Reports + DCR chamber location changes (2026-10-31).
-- Run in SSMS / sqlcmd against SalesDisDB_Unicorn_DB. Each part is re-runnable.
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

-- ===== sp_WebAPI_GetAttendanceData_New_allTeam.sql =====
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

-- ===== sp_Webapi_SalesOrderReport_CustomerFilter.sql =====
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

-- ===== sp_WebAPI_Get_TargetAchievement_SubTerritory.sql =====
-- Target & Achievement report (app: Reports > "Target & Achievement").
-- Target  : tbl_Target_SubTerritoryMonthly (client-supplied, one row per year/month/sub-territory).
-- Sales   : same basis as sp_WebAPI_Get_TargetAChivementReport SalesValue: delivered invoices
--           (DeliveryInvoiceStatus Full/Partial) dated by tblInvoice.UpdateDate,
--           SUM(TotalPrice - DiscountAmount) - SUM(AdjustmentAmount); sub-territory from the order, else the customer.
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
	st AS (
		SELECT st.SubTerritoryId, st.SubTerritoryCode, st.SubTerritoryName, tr.TerritoryName
		FROM dbo.tblSubTerritory st WITH (NOLOCK)
		INNER JOIN dbo.tblTerritory tr WITH (NOLOCK) ON tr.TerritoryId = st.TerritoryId
		INNER JOIN dbo.tblArea ar WITH (NOLOCK) ON ar.AreaId = tr.AreaId
		INNER JOIN dbo.tblRegion rg WITH (NOLOCK) ON rg.RegionId = ar.RegionId
		WHERE @allData = 1
		   OR EXISTS (SELECT 1 FROM emp e
		              WHERE e.TerritoryId = tr.TerritoryId OR e.AreaId = ar.AreaId
		                 OR e.RegionId = rg.RegionId OR e.GroupId = rg.GroupId)
	),
	tg AS (
		SELECT SubTerritoryCode, SUM(TargetAmt) TargetAmt
		FROM dbo.tbl_Target_SubTerritoryMonthly WITH (NOLOCK)
		WHERE TargetYear = @year AND TargetMonth = @month AND IsActive = 1
		  AND (@selectedEmpId = 0 OR EmployeeCode = @selCode)
		GROUP BY SubTerritoryCode
	),
	sl AS (
		SELECT COALESCE(NULLIF(o.SubTerritoryId, 0), NULLIF(c.SubTerritoryId, 0)) SubTerritoryId,
		       SUM(ID.TotalPrice - ID.DiscountAmount) - SUM(ISNULL(ID.AdjustmentAmount, 0)) SalesAmt
		FROM dbo.tblInvoice inv WITH (NOLOCK)
		INNER JOIN dbo.tblInvoiceDetail ID WITH (NOLOCK) ON ID.InvoiceId = inv.InvoiceId
		INNER JOIN dbo.tblOrder o WITH (NOLOCK) ON o.OrderId = inv.OrderId
		LEFT JOIN dbo.tblCustMaster c WITH (NOLOCK) ON c.CustomerMasterId = o.CustomerMasterId
		WHERE inv.DeliveryInvoiceStatus IN ('Full', 'Partial')
		  AND inv.UpdateDate >= @from
		  AND inv.UpdateDate <  @to
		  AND (@selectedEmpId = 0 OR o.EntryBy IN (SELECT UserId FROM dbo.tblUser WITH (NOLOCK) WHERE EmpInfoId = @selectedEmpId))
		GROUP BY COALESCE(NULLIF(o.SubTerritoryId, 0), NULLIF(c.SubTerritoryId, 0))
	)
	SELECT ROW_NUMBER() OVER (ORDER BY st.SubTerritoryCode) AS SerialNo,
	       st.SubTerritoryCode,
	       st.SubTerritoryName,
	       st.TerritoryName,
	       CAST(ISNULL(tg.TargetAmt, 0) AS DECIMAL(18,2)) AS TargetAmt,
	       CAST(ISNULL(sl.SalesAmt, 0) AS DECIMAL(18,2)) AS SalesAmt,
	       CAST(CASE WHEN ISNULL(tg.TargetAmt, 0) = 0 THEN 0
	                 ELSE ISNULL(sl.SalesAmt, 0) * 100.0 / tg.TargetAmt END AS DECIMAL(18,2)) AS AchvPercent,
	       CAST(ISNULL(tg.TargetAmt, 0) - ISNULL(sl.SalesAmt, 0) AS DECIMAL(18,2)) AS BalanceAmt
	FROM st
	LEFT JOIN tg ON tg.SubTerritoryCode = st.SubTerritoryCode
	LEFT JOIN sl ON sl.SubTerritoryId = st.SubTerritoryId
	WHERE tg.SubTerritoryCode IS NOT NULL OR sl.SubTerritoryId IS NOT NULL
	ORDER BY st.SubTerritoryCode
END
GO

-- ===== sp_Webapi_Get_DcrListById.sql =====
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

-- ===== dcr_chamber_location_fill.sql =====
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

