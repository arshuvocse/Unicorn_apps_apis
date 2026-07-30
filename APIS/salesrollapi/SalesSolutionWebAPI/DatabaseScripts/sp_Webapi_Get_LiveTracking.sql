CREATE OR ALTER PROCEDURE [dbo].[sp_Webapi_Get_LiveTracking]
    @EmpInfoId INT,
    @TrackDate DATE
AS
BEGIN
    SET NOCOUNT ON;

    SELECT
        PM.EmpMasterCode,
        PM.EmpName,
        dgs.DesigName,
        usRT.DisplayName,
        utrack.LatValue,
        utrack.LongValue,
        utrack.AddressName,
        utrack.TrackTime
    FROM dbo.tbl_UserTracking AS utrack
    LEFT JOIN dbo.tblEmpGeneralInfo AS PM WITH (NOLOCK)
        ON utrack.EmpInfoId = PM.EmpInfoId
    LEFT JOIN dbo.tblUser AS us WITH (NOLOCK)
        ON PM.EmpInfoId = us.EmpInfoId
    LEFT JOIN dbo.tblDesignation AS dgs WITH (NOLOCK)
        ON PM.DesignationId = dgs.DesignationId
    LEFT JOIN dbo.tbl_UserRoleInfo AS usR WITH (NOLOCK)
        ON usR.UserRoleId = us.UserRoleId
    LEFT JOIN dbo.tblRoleType AS usRT WITH (NOLOCK)
        ON usR.RoleTypeId = usRT.RoleTypeId
    WHERE utrack.EmpInfoId = @EmpInfoId
      AND utrack.TrackDate >= @TrackDate
      AND utrack.TrackDate < DATEADD(DAY, 1, @TrackDate)
    ORDER BY utrack.TrackDate, utrack.TrackTime;
END;
