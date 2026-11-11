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
