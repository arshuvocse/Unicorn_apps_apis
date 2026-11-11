-- Place Order: return delivery address (tblCustMaster.Addrees2) alongside billing address (Address).
-- Re-runnable: CREATE OR ALTER.
CREATE OR ALTER PROCEDURE [dbo].[sp_webapi_GetCustomerbyUser]
    @empId INT
AS
    BEGIN

    DECLARE @roleType NVARCHAR(10);

    SELECT @roleType = CAST(UR.RoleTypeId AS NVARCHAR(10))
    FROM dbo.tblUser U WITH (NOLOCK)
    LEFT JOIN dbo.tbl_UserRoleInfo UR WITH (NOLOCK)
        ON UR.UserRoleID = U.UserRoleID
    WHERE U.EmpInfoId = @empId;

     SELECT distinct  isnull(D.CustomerBsPTag,'') CustomerBsPTag,  isnull(D.CustomerBsPCode,'') CustomerBsPCode, 00 SerialNo,  D.MarketCode MarketCode, D.CustomerTypeId, D.ProgramTypeId, D.SMCTypeId, 0 as CustomerCheck,format(tbInvo.InvoiceDate,'dd MMMM, yyyy') Note,  D.GroupId,D.RegionId, D.AreaId, D.TerritoryId, D.SubTerritoryId,D.MarketId,  D.CustomerMasterId ,
                D.CustomerName ,
                D.CustomerCode ,
                D.Address ,
                CMA.Addrees2 ,
                D.MarketName ,
                D.CellNo ,
                D.Type AS CustomerType ,
                D.CustomerStation AS 'CustomerStation' ,
                NULL AS 'Route' ,
                0 AS 'Balance' ,
                0 AS 'CreditLimit' ,
                NULL AS 'Route' FROM dbo.View_CustomerMaster D with (nolock)

                    LEFT JOIN dbo.tblCustMaster CMA WITH (NOLOCK) ON CMA.CustomerMasterId = D.CustomerMasterId
                    left join (
                select max(InvoiceDate) InvoiceDate, tblInvoice.CustomerMasterId from tblInvoice with (nolock)   group by CustomerMasterId) tbInvo on  tbInvo.CustomerMasterId=D.CustomerMasterId

            WHERE
    (
        -- RoleType 1
        @roleType = '1'
        AND
        (
            D.SalesPersonId = @empId
            OR ',' + ISNULL(D.TaggedSalesPersonIds,'') + ',' LIKE '%,' + CAST(@empId AS NVARCHAR(10)) + ',%'
        )
    )
       OR
    (
        -- RoleType 2
        @roleType = '2'
        AND
        (
            D.SalesPersonId = @empId
            OR ',' + ISNULL(D.TaggedSalesPersonIds,'') + ',' LIKE '%,' + CAST(@empId AS NVARCHAR(10)) + ',%'
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
        @roleType = '3'
        AND D.SalesPersonId IS NOT NULL
    )
    OR
    (
        -- DCR area lock turned off for this employee (tbl_DcrAreaLockSetup): all doctors/customers, like RoleType 3
        EXISTS (SELECT 1 FROM dbo.tbl_DcrAreaLockSetup L WITH (NOLOCK) WHERE L.EmpInfoId = @empId AND L.IsLockOff = 1)
        AND D.SalesPersonId IS NOT NULL
    );





            end
GO
