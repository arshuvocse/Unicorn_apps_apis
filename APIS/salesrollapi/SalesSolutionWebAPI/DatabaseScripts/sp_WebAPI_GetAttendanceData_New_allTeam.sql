USE [SalesDisDB_Unicorn_DB]
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
