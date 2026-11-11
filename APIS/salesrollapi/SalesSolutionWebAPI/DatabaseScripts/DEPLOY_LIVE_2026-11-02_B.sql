/* =====================================================================================
   LIVE DEPLOY (part B, 2026-11-02): attendance punch window, order submit location,
   order save-fail reason, Target & Achievement fallback, customer list SP. One file.
   Target: SalesDisDB_Unicorn_DB (SQL Server 2016 SP1 or later; CREATE OR ALTER is used).
   Needed by the APIs published on 2026-11-02 (All_Publish\SalesAPI and \OrderAPI):
   run this BEFORE replacing the API folders on the server.

   BEFORE RUNNING
     1. Take a full backup of SalesDisDB_Unicorn_DB.
     2. In SSMS turn on Query > SQLCMD Mode, then Execute (F5). Without SQLCMD mode the
        script refuses to run anything and closes the connection; reconnect, turn SQLCMD
        Mode on, run again.

   WHAT IT DOES
     - Stops at the first error. Checks the database name and SQL Server version first.
     - Saves the current definition of every procedure it changes into dbo.tbl_DeployBackup
       (DeployTag = 'LIVE_2026-11-02B') before changing anything.
     - No data is updated or deleted. Tables/columns are added only when missing. No DROP.
     - Re-runnable: if it stops halfway, fix the cause and run the whole file again.
       Safe to run even if some parts were already run before (e.g. part 4 = part 5 of the
       earlier DEPLOY_LIVE_2026-10-28_to_11-02.sql).

   ROLLBACK (one object): SELECT Definition FROM dbo.tbl_DeployBackup
       WHERE DeployTag = 'LIVE_2026-11-02B' AND ObjectName = 'dbo.<name>';
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
    RAISERROR('Wrong database or SQL Server older than 2016 SP1. Nothing was changed.', 20, 1) WITH LOG;
    SET NOEXEC ON;
END
GO
-- Part 4 (Target report) needs fn_Webapi_IsAllDataRole from DEPLOY_LIVE_2026-10-28_to_11-02.sql.
IF OBJECT_ID(N'dbo.fn_Webapi_IsAllDataRole') IS NULL
BEGIN
    RAISERROR('dbo.fn_Webapi_IsAllDataRole is missing. Run DEPLOY_LIVE_2026-10-28_to_11-02.sql first. Nothing was changed.', 20, 1) WITH LOG;
    SET NOEXEC ON;
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
SELECT N'LIVE_2026-11-02B', o.ObjectName, OBJECT_DEFINITION(OBJECT_ID(o.ObjectName))
FROM (VALUES
    (N'dbo.sp_Webapi_Get_AttendancePunchWindow'),
    (N'dbo.sp_Webapi_Save_AttendancePunchWindow'),
    (N'dbo.sp_Webapi_Remove_AttendancePunchWindow'),
    (N'dbo.sp_webapi_SaveOrderLocation'),
    (N'dbo.sp_webapi_Get_OrderSaveFailReason'),
    (N'dbo.sp_WebAPI_Get_TargetAchievement_SubTerritory'),
    (N'dbo.sp_webapi_GetCustomerbyUser')
) o(ObjectName)
WHERE NOT EXISTS (SELECT 1 FROM dbo.tbl_DeployBackup b
                  WHERE b.DeployTag = N'LIVE_2026-11-02B' AND b.ObjectName = o.ObjectName);
GO
-- ===== 1. attendance_punch_window.sql =====
-- Attendance punch-in time restriction (per employee). Re-runnable.
-- Employees with no active row are unrestricted. Removing a restriction = IsActive 0.
IF OBJECT_ID('dbo.tbl_AttendancePunchWindow', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.tbl_AttendancePunchWindow
    (
        Id          INT IDENTITY(1,1) PRIMARY KEY,
        EmpInfoId   INT      NOT NULL,
        PunchInFrom TIME(0)  NOT NULL,
        PunchInTo   TIME(0)  NOT NULL,
        IsActive    BIT      NOT NULL DEFAULT 1,
        EntryBy     INT      NULL,
        EntryDate   DATETIME NOT NULL DEFAULT GETDATE(),
        UpdateBy    INT      NULL,
        UpdateDate  DATETIME NULL
    );
    CREATE UNIQUE INDEX UX_tbl_AttendancePunchWindow_EmpInfoId ON dbo.tbl_AttendancePunchWindow (EmpInfoId);
END
GO

-- Returns one row: IsRestricted 0 = no limit; IsAllowedNow evaluated on server time.
CREATE OR ALTER PROCEDURE dbo.sp_Webapi_Get_AttendancePunchWindow
    @empId INT
AS
BEGIN
    SET NOCOUNT ON;
    -- Truncate to the minute so the end time includes its whole last minute (same as the app check).
    DECLARE @Now TIME(0) = CAST(CONVERT(VARCHAR(5), GETDATE(), 108) AS TIME(0));

    SELECT
        @empId AS EmpInfoId,
        CONVERT(VARCHAR(5), w.PunchInFrom, 108) AS PunchInFrom,
        CONVERT(VARCHAR(5), w.PunchInTo, 108) AS PunchInTo,
        CAST(CASE WHEN w.EmpInfoId IS NULL THEN 0 ELSE 1 END AS BIT) AS IsRestricted,
        CAST(CASE
            WHEN w.EmpInfoId IS NULL THEN 1
            WHEN w.PunchInFrom <= w.PunchInTo AND @Now BETWEEN w.PunchInFrom AND w.PunchInTo THEN 1
            WHEN w.PunchInFrom > w.PunchInTo AND (@Now >= w.PunchInFrom OR @Now <= w.PunchInTo) THEN 1 -- overnight window
            ELSE 0 END AS BIT) AS IsAllowedNow
    FROM (SELECT 1 AS x) d
    LEFT JOIN dbo.tbl_AttendancePunchWindow w ON w.EmpInfoId = @empId AND w.IsActive = 1;
END
GO

CREATE OR ALTER PROCEDURE dbo.sp_Webapi_Save_AttendancePunchWindow
    @empId INT,
    @PunchInFrom TIME(0),
    @PunchInTo TIME(0),
    @EntryBy INT = NULL
AS
BEGIN
    SET NOCOUNT ON;
    IF EXISTS (SELECT 1 FROM dbo.tbl_AttendancePunchWindow WHERE EmpInfoId = @empId)
        UPDATE dbo.tbl_AttendancePunchWindow
        SET PunchInFrom = @PunchInFrom, PunchInTo = @PunchInTo, IsActive = 1,
            UpdateBy = @EntryBy, UpdateDate = GETDATE()
        WHERE EmpInfoId = @empId;
    ELSE
        INSERT INTO dbo.tbl_AttendancePunchWindow (EmpInfoId, PunchInFrom, PunchInTo, IsActive, EntryBy, EntryDate)
        VALUES (@empId, @PunchInFrom, @PunchInTo, 1, @EntryBy, GETDATE());
END
GO

CREATE OR ALTER PROCEDURE dbo.sp_Webapi_Remove_AttendancePunchWindow
    @empId INT,
    @EntryBy INT = NULL
AS
BEGIN
    SET NOCOUNT ON;
    UPDATE dbo.tbl_AttendancePunchWindow
    SET IsActive = 0, UpdateBy = @EntryBy, UpdateDate = GETDATE()
    WHERE EmpInfoId = @empId AND IsActive = 1;
END
GO
GO

-- ===== 2. order_submit_location.sql =====
/*
  Order submit location: where the sales person was when the order was submitted from the app.
  Saved by the order API (api/OrderProcess/MakeOrder) right after the order master/detail save.
  Own column names (Submit*) so they cannot clash with any existing tblOrder column.
  Run BEFORE publishing the order API. Re-runnable; adds nullable columns only, no data changes.
*/
IF COL_LENGTH('dbo.tblOrder', 'SubmitLat') IS NULL
    ALTER TABLE dbo.tblOrder ADD SubmitLat NVARCHAR(50) NULL;
IF COL_LENGTH('dbo.tblOrder', 'SubmitLon') IS NULL
    ALTER TABLE dbo.tblOrder ADD SubmitLon NVARCHAR(50) NULL;
IF COL_LENGTH('dbo.tblOrder', 'SubmitAddress') IS NULL
    ALTER TABLE dbo.tblOrder ADD SubmitAddress NVARCHAR(500) NULL;
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
-- Missing / unparsable / out-of-range / 0,0 positions are ignored, so a bad fix never overwrites a good one.
-- An empty call (e.g. manager edit from the approval screen) keeps the original submit location.
CREATE OR ALTER PROCEDURE dbo.sp_webapi_SaveOrderLocation
    @OrderId INT,
    @Latitude NVARCHAR(50) = NULL,
    @Longitude NVARCHAR(50) = NULL,
    @StreetAddress NVARCHAR(500) = NULL
AS
BEGIN
    SET NOCOUNT ON;
    DECLARE @lat FLOAT = TRY_CONVERT(FLOAT, NULLIF(LTRIM(RTRIM(@Latitude)), ''));
    DECLARE @lon FLOAT = TRY_CONVERT(FLOAT, NULLIF(LTRIM(RTRIM(@Longitude)), ''));

    IF @lat IS NULL OR @lon IS NULL OR @lat NOT BETWEEN -90 AND 90 OR @lon NOT BETWEEN -180 AND 180
       OR (@lat = 0 AND @lon = 0)
        RETURN;

    UPDATE dbo.tblOrder
    SET SubmitLat = LTRIM(RTRIM(@Latitude)),
        SubmitLon = LTRIM(RTRIM(@Longitude)),
        SubmitAddress = COALESCE(NULLIF(LTRIM(RTRIM(@StreetAddress)), ''), SubmitAddress)
    WHERE OrderId = @OrderId;
END
GO
GO

-- ===== 3. order_save_fail_reason.sql =====
/*
  Why did sp_webapi_SaveOrderMaster return no OrderId? It just returns nothing for its guards, and the app
  then showed "The order could not be placed. Please review the products/campaign". The order API calls this
  only after such a failed save, to tell the user the real reason.
  Guards checked (same as sp_webapi_SaveOrderMaster): same customer ordered in the last 5 minutes (double tap /
  retry after a slow network: the first order WAS saved), inactive user/employee, unknown customer.
  Read-only. Re-runnable. Columns are checked with COL_LENGTH first, so a schema difference never breaks it.
*/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE OR ALTER PROCEDURE dbo.sp_webapi_Get_OrderSaveFailReason
    @empId INT,
    @CustomerCode NVARCHAR(50)
AS
BEGIN
    SET NOCOUNT ON;
    DECLARE @orderCode NVARCHAR(100) = NULL, @found BIT = 0;

    IF COL_LENGTH('dbo.tblOrder', 'ServerDateTime') IS NOT NULL AND COL_LENGTH('dbo.tblOrder', 'OrderCode') IS NOT NULL
        EXEC sp_executesql
            N'SELECT TOP (1) @code = OrderCode FROM dbo.tblOrder WITH (NOLOCK)
              WHERE CustomerCode = @cc AND ServerDateTime >= DATEADD(MINUTE, -6, GETDATE())
              ORDER BY OrderId DESC',
            N'@cc NVARCHAR(50), @code NVARCHAR(100) OUTPUT', @cc = @CustomerCode, @code = @orderCode OUTPUT;
    IF @orderCode IS NOT NULL
    BEGIN
        SELECT N'This customer''s order was already submitted a few minutes ago (' + @orderCode
             + N'). Please check your order list; do not submit it again.' AS Reason;
        RETURN;
    END

    IF COL_LENGTH('dbo.tblUser', 'UserStatus') IS NOT NULL AND COL_LENGTH('dbo.tblEmpGeneralInfo', 'EmployeeStatus') IS NOT NULL
    BEGIN
        EXEC sp_executesql
            N'IF EXISTS (SELECT 1 FROM dbo.tblUser u WITH (NOLOCK)
                         INNER JOIN dbo.tblEmpGeneralInfo e WITH (NOLOCK) ON e.EmpInfoId = u.EmpInfoId
                         WHERE u.EmpInfoId = @emp AND u.UserStatus = ''Active'' AND e.EmployeeStatus = ''Active'')
                  SET @ok = 1',
            N'@emp INT, @ok BIT OUTPUT', @emp = @empId, @ok = @found OUTPUT;
        IF @found = 0
        BEGIN
            SELECT N'Your user/employee account is not active. Please contact the Admin department.' AS Reason;
            RETURN;
        END
    END

    IF NOT EXISTS (SELECT 1 FROM dbo.tblCustMaster WITH (NOLOCK) WHERE CustomerCode = @CustomerCode)
    BEGIN
        SELECT N'Customer ' + ISNULL(@CustomerCode, N'') + N' was not found. Please sync customers and try again.' AS Reason;
        RETURN;
    END

    SELECT CAST(NULL AS NVARCHAR(500)) AS Reason; -- none of the known guards: the API keeps its generic message
END
GO
GO

-- ===== 4. sp_WebAPI_Get_TargetAchievement_SubTerritory.sql =====
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

-- ===== 5. sp_webapi_GetCustomerbyUser.sql =====
-- Customer list (app Customer Directory / Place Order). Live had the older version without:
--   * Addrees2 (delivery address; the 2026-11-02 Sales API also works without it), and
--   * the DCR area lock-off rule (tbl_DcrAreaLockSetup.IsLockOff = 1 -> all customers, like RoleType 3),
--     whose table came with DEPLOY_LIVE_2026-10-28_to_11-02.sql.
-- Built with dynamic SQL: if tblCustMaster has no Addrees2 column, the SP returns NULL for it
-- instead of failing to compile.
IF OBJECT_ID(N'dbo.tbl_DcrAreaLockSetup', N'U') IS NULL OR COL_LENGTH('dbo.tbl_DcrAreaLockSetup', 'IsLockOff') IS NULL
BEGIN
    RAISERROR('dbo.tbl_DcrAreaLockSetup.IsLockOff is missing. Run DEPLOY_LIVE_2026-10-28_to_11-02.sql first. Parts 1-4 are done; part 5 was not changed.', 16, 1);
    SET NOEXEC ON;
END
GO
DECLARE @addr NVARCHAR(100) = CASE WHEN COL_LENGTH('dbo.tblCustMaster', 'Addrees2') IS NOT NULL
                                   THEN N'CMA.Addrees2' ELSE N'CAST(NULL AS NVARCHAR(500))' END;
DECLARE @sql NVARCHAR(MAX) = N'CREATE OR ALTER PROCEDURE [dbo].[sp_webapi_GetCustomerbyUser]
    @empId INT
AS
    BEGIN

    DECLARE @roleType NVARCHAR(10);

    SELECT @roleType = CAST(UR.RoleTypeId AS NVARCHAR(10))
    FROM dbo.tblUser U WITH (NOLOCK)
    LEFT JOIN dbo.tbl_UserRoleInfo UR WITH (NOLOCK)
        ON UR.UserRoleID = U.UserRoleID
    WHERE U.EmpInfoId = @empId;

     SELECT distinct  isnull(D.CustomerBsPTag,'''') CustomerBsPTag,  isnull(D.CustomerBsPCode,'''') CustomerBsPCode, 00 SerialNo,  D.MarketCode MarketCode, D.CustomerTypeId, D.ProgramTypeId, D.SMCTypeId, 0 as CustomerCheck,format(tbInvo.InvoiceDate,''dd MMMM, yyyy'') Note,  D.GroupId,D.RegionId, D.AreaId, D.TerritoryId, D.SubTerritoryId,D.MarketId,  D.CustomerMasterId ,
                D.CustomerName ,
                D.CustomerCode ,
                D.Address ,
                {ADDR} AS Addrees2 ,
                D.MarketName ,
                D.CellNo ,
                D.Type AS CustomerType ,
                D.CustomerStation AS ''CustomerStation'' ,
                NULL AS ''Route'' ,
                0 AS ''Balance'' ,
                0 AS ''CreditLimit'' ,
                NULL AS ''Route'' FROM dbo.View_CustomerMaster D with (nolock)

                    LEFT JOIN dbo.tblCustMaster CMA WITH (NOLOCK) ON CMA.CustomerMasterId = D.CustomerMasterId
                    left join (
                select max(InvoiceDate) InvoiceDate, tblInvoice.CustomerMasterId from tblInvoice with (nolock)   group by CustomerMasterId) tbInvo on  tbInvo.CustomerMasterId=D.CustomerMasterId

            WHERE
    (
        -- RoleType 1
        @roleType = ''1''
        AND
        (
            D.SalesPersonId = @empId
            OR '','' + ISNULL(D.TaggedSalesPersonIds,'''') + '','' LIKE ''%,'' + CAST(@empId AS NVARCHAR(10)) + '',%''
        )
    )
       OR
    (
        -- RoleType 2
        @roleType = ''2''
        AND
        (
            D.SalesPersonId = @empId
            OR '','' + ISNULL(D.TaggedSalesPersonIds,'''') + '','' LIKE ''%,'' + CAST(@empId AS NVARCHAR(10)) + '',%''
            OR
            D.SalesPersonId IN
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
        -- RoleType 3
        @roleType = ''3''
        AND D.SalesPersonId IS NOT NULL
    )
    OR
    (
        -- DCR area lock turned off for this employee (tbl_DcrAreaLockSetup): all doctors/customers, like RoleType 3
        EXISTS (SELECT 1 FROM dbo.tbl_DcrAreaLockSetup L WITH (NOLOCK) WHERE L.EmpInfoId = @empId AND L.IsLockOff = 1)
        AND D.SalesPersonId IS NOT NULL
    );





            end';
SET @sql = REPLACE(@sql, N'{ADDR}', @addr);
EXEC sys.sp_executesql @sql;
GO
-- ===== Done: verify (every row must show Exists = 1) =====
SELECT o.ObjectName, CASE WHEN OBJECT_ID(o.ObjectName) IS NULL THEN 0 ELSE 1 END AS [Exists]
FROM (VALUES
    (N'dbo.tbl_AttendancePunchWindow'),
    (N'dbo.sp_Webapi_Get_AttendancePunchWindow'),
    (N'dbo.sp_Webapi_Save_AttendancePunchWindow'),
    (N'dbo.sp_Webapi_Remove_AttendancePunchWindow'),
    (N'dbo.sp_webapi_SaveOrderLocation'),
    (N'dbo.sp_webapi_Get_OrderSaveFailReason'),
    (N'dbo.tbl_Target_SubTerritoryMonthly'),
    (N'dbo.sp_WebAPI_Get_TargetAchievement_SubTerritory'),
    (N'dbo.sp_webapi_GetCustomerbyUser')
) o(ObjectName)
UNION ALL
SELECT N'tblOrder.SubmitLat/SubmitLon/SubmitAddress',
       CASE WHEN COL_LENGTH('dbo.tblOrder','SubmitLat') IS NOT NULL AND COL_LENGTH('dbo.tblOrder','SubmitLon') IS NOT NULL
                 AND COL_LENGTH('dbo.tblOrder','SubmitAddress') IS NOT NULL THEN 1 ELSE 0 END;
GO
SET NOEXEC OFF;
GO