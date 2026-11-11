USE [SalesDisDB_Unicorn_DB]
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
