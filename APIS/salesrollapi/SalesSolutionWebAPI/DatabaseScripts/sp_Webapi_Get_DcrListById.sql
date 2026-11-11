USE [SalesDisDB_Unicorn_DB]
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
