CREATE OR ALTER PROCEDURE [dbo].[sp_Webapi_Get_DoctorList]
    @empId INT
AS
BEGIN
    SET NOCOUNT ON;
    DECLARE @roleType NVARCHAR(10);
    -- Get RoleType of employee
    SELECT @roleType = CAST(UR.RoleTypeId AS NVARCHAR(10))
    FROM dbo.tblUser U WITH (NOLOCK)
    LEFT JOIN dbo.tbl_UserRoleInfo UR WITH (NOLOCK) 
        ON UR.UserRoleID = U.UserRoleID
    WHERE U.EmpInfoId = @empId;

    SELECT DISTINCT     ISNULL(DM.Longitude , '') Longitude, ISNULL(DM.Latitude, '') Latitude, ISNULL(DM.StreetAddress, '') StreetAddress, 
        ISNULL(DM.MarketCode, 'N/A') AS MarketCode,
        DM.DoctorId,
        ISNULL(DM.DoctorCode, '') AS DoctorCode,
        DM.DoctorName,
        ISNULL(DM.DocContact, 'N/A') AS DocContact,
        ISNULL(DM.DoctorTypeName, 'N/A') AS DoctorTypeName,
        ISNULL(DM.ProgramTypeName, 'N/A') AS ProgramTypeName,
        ISNULL(DM.ChemberName, 'N/A') AS ChemberName,
        ISNULL(DM.GroupId,0) GroupId,
        ISNULL(DM.RegionId,0) RegionId,
        ISNULL(DM.AreaId,0) AreaId,
        ISNULL(DM.TerritoryId,0) TerritoryId,
        ISNULL(DM.SubTerritoryId,0) SubTerritoryId,
       ISNULL( DM.MarketId,0) MarketId,
        ISNULL(DM.MarketName, 'N/A') AS MarketName,
        ISNULL(DM.SMCTypeId, 0) AS SMCTypeId,
        ISNULL(DM.SMCType, 'N/A') AS SMCType,
        ISNULL(DM.ProgramTypeId, 0) AS ProgramTypeId,
        ISNULL(DM.DoctorTypeId, 0) AS DoctorTypeId
    FROM View_DoctorMaster DM WITH (NOLOCK)
    WHERE
    (
        -- RoleType 1: Own doctor list + TaggedSalesPersonIds এ থাকলে
        @roleType = '1'
        AND 
        (
            DM.SalesPersonId = @empId
            OR ',' + ISNULL(DM.TaggedSalesPersonIds,'') + ',' LIKE '%,' + CAST(@empId AS NVARCHAR(10)) + ',%'
        )
    )
    OR
    (
        -- RoleType 2: Area wise all MIO doctor list + নিজের own list
        @roleType = '2'
        AND
        (
            DM.SalesPersonId = @empId
            OR ',' + ISNULL(DM.TaggedSalesPersonIds,'') + ',' LIKE '%,' + CAST(@empId AS NVARCHAR(10)) + ',%'
            OR
            DM.SalesPersonId IN
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
        AND DM.SalesPersonId is not null
    )
    OR
    (
        -- DCR area lock turned off for this employee (tbl_DcrAreaLockSetup): all doctors/customers, like RoleType 3
        EXISTS (SELECT 1 FROM dbo.tbl_DcrAreaLockSetup L WITH (NOLOCK) WHERE L.EmpInfoId = @empId AND L.IsLockOff = 1)
        AND DM.SalesPersonId IS NOT NULL
    );
END
GO
