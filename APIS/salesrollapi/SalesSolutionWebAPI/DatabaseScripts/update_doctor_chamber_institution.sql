-- SUPERSEDED for sp_Webapi_Save_DcrInfo / sp_Webapi_Get_ChamberName: re-run dcr_area_lock_and_tourplan_gate.sql after this script, or the DCR area lock / tour plan gate is lost.
-- Migration script for Doctor Chamber and Institution
IF COL_LENGTH('dbo.tblDoctorChemberDetail', 'Shift') IS NULL
    ALTER TABLE dbo.tblDoctorChemberDetail ADD Shift NVARCHAR(50) NULL;

IF COL_LENGTH('dbo.tblDoctorChemberDetail', 'SittingTime') IS NULL
    ALTER TABLE dbo.tblDoctorChemberDetail ADD SittingTime NVARCHAR(100) NULL;

IF COL_LENGTH('dbo.tblDoctorChemberDetail', 'InstitutionId') IS NULL
    ALTER TABLE dbo.tblDoctorChemberDetail ADD InstitutionId INT NULL;
GO

-- 1. sp_Webapi_Save_DoctorChemberDetail (supports Shift, SittingTime, InstitutionId, Address)
CREATE OR ALTER PROCEDURE [dbo].[sp_Webapi_Save_DoctorChemberDetail]
    @pk INT,
    @ChamberTypeId INT = NULL,
    @Name NVARCHAR(MAX) = NULL,
    @Shift NVARCHAR(50) = NULL,
    @SittingTime NVARCHAR(100) = NULL,
    @InstitutionId INT = NULL,
    @Address NVARCHAR(MAX) = NULL
AS
BEGIN
    SET NOCOUNT ON;

    INSERT INTO dbo.tblDoctorChemberDetail
    (
        DoctorId,
        ChamberTypeId,
        Name,
        Shift,
        SittingTime,
        InstitutionId,
        Address
    )
    VALUES
    (
        @pk,
        NULLIF(@ChamberTypeId, 0),
        NULLIF(LTRIM(RTRIM(@Name)), ''),
        NULLIF(LTRIM(RTRIM(@Shift)), ''),
        NULLIF(LTRIM(RTRIM(@SittingTime)), ''),
        NULLIF(@InstitutionId, 0),
        NULLIF(LTRIM(RTRIM(@Address)), '')
    );
END
GO

-- 2. sp_Webapi_Get_DoctorChember_AppLog (returns Address)
CREATE OR ALTER PROCEDURE [dbo].[sp_Webapi_Get_DoctorChember_AppLog]
    @doctorId INT 
AS
BEGIN
    SET NOCOUNT ON;

    SELECT 
        dcd.ChamberTypeId,
        chm.ChamberName AS ChamberTypeName,
        dcd.Name AS ChamberName,
        ISNULL(dcd.Shift, '') AS Shift,
        ISNULL(dcd.SittingTime, '') AS SittingTime,
        dcd.InstitutionId,
        ISNULL(dcd.Address, '') AS Address
    FROM dbo.tblDoctorChemberDetail dcd
    LEFT JOIN dbo.tblDoctorChamber chm ON chm.ChamberId = dcd.ChamberTypeId
    WHERE dcd.DoctorId = @doctorId;
END
GO

-- Set all existing chambers without a shift to 'Morning'
UPDATE dbo.tblDoctorChemberDetail
SET Shift = 'Morning'
WHERE Shift IS NULL OR LTRIM(RTRIM(Shift)) = '';
GO

-- 3. sp_Webapi_Get_ChamberName (includes Shift, SittingTime, Address, InstitutionId for Master Sync)
CREATE OR ALTER PROCEDURE [dbo].[sp_Webapi_Get_ChamberName]
    @empid INT 
AS
BEGIN
    DECLARE @roleType NVARCHAR(10);

    SELECT @roleType = CAST(UR.RoleTypeId AS NVARCHAR(10))
    FROM dbo.tblUser U WITH (NOLOCK)
    LEFT JOIN dbo.tbl_UserRoleInfo UR WITH (NOLOCK) 
        ON UR.UserRoleID = U.UserRoleID
    WHERE U.EmpInfoId = @empId;

    SELECT DISTINCT 
        tblDoctorChemberDetail.ChemberId AS ChemberId,
        ISNULL(tblDoctorChemberDetail.Name, 'N/A') AS ChemberName, 
        tblDoctorChemberDetail.DoctorId,
        ISNULL(tblDoctorChemberDetail.Shift, '') AS Shift,
        ISNULL(tblDoctorChemberDetail.SittingTime, '') AS SittingTime,
        ISNULL(tblDoctorChemberDetail.Address, '') AS Address,
        tblDoctorChemberDetail.InstitutionId
    FROM dbo.tblDoctorChemberDetail WITH (NOLOCK)
    INNER JOIN dbo.tblDoctorMaster doc WITH (NOLOCK) ON doc.DoctorId = tblDoctorChemberDetail.DoctorId 
    WHERE (doc.IsActive = '1')   
      AND (
        (@roleType = '1' AND doc.SalesPersonId = @empId)
        OR
        (@roleType = '2' AND doc.SalesPersonId IN (
            SELECT DISTINCT EFI2.MIOEmpId
            FROM View_Webapi_EmployeeFieldForceInfo EFI2 WITH (NOLOCK)
            WHERE EFI2.TerritoryId IN (
                SELECT T.TerritoryId
                FROM dbo.tblTerritory T WITH (NOLOCK)
                WHERE T.AreaId IN (
                    SELECT DISTINCT EFI.AreaId
                    FROM View_Webapi_EmployeeFieldForceInfo EFI WITH (NOLOCK)
                    WHERE EFI.EmpInfoId = @empId
                )
            )
            AND EFI2.MIOEmpId IS NOT NULL
        ))
        OR
        (@roleType = '3' AND doc.SalesPersonId IS NOT NULL)
    );
END
GO
