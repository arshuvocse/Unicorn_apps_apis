USE [SalesDisDB_Unicorn_DB]
GO
/*
  Chamber location (map icon on ADD DCR) - all DB changes in one re-runnable script.
  1) tblDoctorChemberDetail: Lat / Lon / isLock columns (already present in the live DB; added only if missing)
  2) sp_Webapi_Set_ChamberLocationIfEmpty: fills Lat/Lon only when the chamber has none yet
  3) sp_Webapi_Get_ChamberName: offline chamber sync now also returns Lat and Lon (the only change to an existing SP)
*/
IF COL_LENGTH('dbo.tblDoctorChemberDetail', 'isLock') IS NULL
    ALTER TABLE dbo.tblDoctorChemberDetail ADD isLock BIT NULL;
IF COL_LENGTH('dbo.tblDoctorChemberDetail', 'Lat') IS NULL
    ALTER TABLE dbo.tblDoctorChemberDetail ADD Lat NVARCHAR(MAX) NULL;
IF COL_LENGTH('dbo.tblDoctorChemberDetail', 'Lon') IS NULL
    ALTER TABLE dbo.tblDoctorChemberDetail ADD Lon NVARCHAR(MAX) NULL;
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
-- Returns Updated = 1 when the chamber's empty location was filled, 0 when it already had one
-- (or the incoming position is missing / zero).
CREATE OR ALTER PROCEDURE dbo.sp_Webapi_Set_ChamberLocationIfEmpty
    @chamberId INT,
    @Latitude  NVARCHAR(50),
    @Longitude NVARCHAR(50)
AS
BEGIN
    SET NOCOUNT ON;
    DECLARE @lat DECIMAL(9,6) = TRY_CONVERT(DECIMAL(9,6), NULLIF(LTRIM(RTRIM(@Latitude)), ''));
    DECLARE @lon DECIMAL(9,6) = TRY_CONVERT(DECIMAL(9,6), NULLIF(LTRIM(RTRIM(@Longitude)), ''));

    -- no usable fix (missing, unparsable, or 0,0 from a GPS that was not ready): leave the chamber alone
    IF @lat IS NULL OR @lon IS NULL OR (@lat = 0 AND @lon = 0)
    BEGIN
        SELECT CAST(0 AS BIT) AS Updated;
        RETURN;
    END

    UPDATE dbo.tblDoctorChemberDetail
    SET Lat = LTRIM(RTRIM(@Latitude)),
        Lon = LTRIM(RTRIM(@Longitude))
    WHERE ChemberId = @chamberId
      AND (NULLIF(LTRIM(RTRIM(Lat)), '') IS NULL OR NULLIF(LTRIM(RTRIM(Lon)), '') IS NULL);

    SELECT CAST(CASE WHEN @@ROWCOUNT > 0 THEN 1 ELSE 0 END AS BIT) AS Updated;
END
GO
-- Offline chamber sync for AddDCRActivity: unchanged area rule, plus every active doctor's chambers
-- when the employee's area lock is off. Lat/Lon added to the result.
CREATE OR ALTER PROCEDURE [dbo].[sp_Webapi_Get_ChamberName]
	@empid INT
AS
BEGIN
	DECLARE @roleType NVARCHAR(10);
	DECLARE @lockOff BIT = ISNULL((SELECT IsLockOff FROM dbo.tbl_DcrAreaLockSetup WITH (NOLOCK) WHERE EmpInfoId = @empid), 0);

    -- Get RoleType of employee
    SELECT @roleType = CAST(UR.RoleTypeId AS NVARCHAR(10))
    FROM dbo.tblUser U WITH (NOLOCK)
    LEFT JOIN dbo.tbl_UserRoleInfo UR WITH (NOLOCK)
        ON UR.UserRoleID = U.UserRoleID
    WHERE U.EmpInfoId = @empId;

	SELECT distinct
        tblDoctorChemberDetail.ChemberId AS ChemberId,
        ISNULL(tblDoctorChemberDetail.Name, 'N/A') AS ChemberName,
        tblDoctorChemberDetail.DoctorId,
        ISNULL(tblDoctorChemberDetail.Shift, '') AS Shift,
        ISNULL(tblDoctorChemberDetail.SittingTime, '') AS SittingTime,
        ISNULL(tblDoctorChemberDetail.Address, '') AS Address,
        tblDoctorChemberDetail.InstitutionId,
        tblDoctorChemberDetail.Lat,
        tblDoctorChemberDetail.Lon
    FROM dbo.tblDoctorChemberDetail WITH (NOLOCK)
	INNER JOIN dbo.tblDoctorMaster doc WITH (NOLOCK) ON doc.DoctorId = tblDoctorChemberDetail.DoctorId
	WHERE (doc.IsActive = '1')
	AND
	(
		@lockOff = 1
		OR
		(
			-- RoleType 1: Own doctor list + TaggedSalesPersonIds
			@roleType = '1'
			AND
			(
				doc.SalesPersonId = @empId
				OR ',' + ISNULL(doc.TaggedSalesPersonIds,'') + ',' LIKE '%,' + CAST(@empId AS NVARCHAR(10)) + ',%'
			)
		)
		OR
		(
			-- RoleType 2: Area wise all MIO doctor list + own list
			@roleType = '2'
			AND
			(
				doc.SalesPersonId = @empId
				OR ',' + ISNULL(doc.TaggedSalesPersonIds,'') + ',' LIKE '%,' + CAST(@empId AS NVARCHAR(10)) + ',%'
				OR
				doc.SalesPersonId IN
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
			AND doc.SalesPersonId IS NOT NULL
		)
	);
END
GO
