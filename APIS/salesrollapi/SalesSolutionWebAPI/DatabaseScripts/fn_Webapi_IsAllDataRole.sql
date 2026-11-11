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
