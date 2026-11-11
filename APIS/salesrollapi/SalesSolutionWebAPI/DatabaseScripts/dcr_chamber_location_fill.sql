USE [SalesDisDB_Unicorn_DB]
GO
/*
  DCR chamber location: the first DCR at a chamber stores the visit's GPS position as the
  chamber's Lat/Lon. Once a chamber has a location it is never changed by a DCR.
  Called by the API right after a successful DCR save (doctor DCR only; for CVR the ChamberId is a customer).
  Re-runnable.
*/
-- the live DB already has these columns; add them where they are missing (e.g. local copies)
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
