USE [SalesDisDB_Unicorn_DB]
GO
/*
  Order submit location: where the sales person was when the order was submitted from the app.
  Saved by the order API (api/OrderProcess/MakeOrder) right after the order master/detail save.
  Own column names (Submit*) so they cannot clash with any existing tblOrder column.
  Run BEFORE publishing the order API. Re-runnable; adds nullable columns only, no data changes.
*/
IF COL_LENGTH('dbo.tblOrder', 'SubmitLat') IS NULL
    ALTER TABLE dbo.tblOrder ADD SubmitLat NVARCHAR(50) NULL;
IF COL_LENGTH('dbo.tblOrder', 'SubmitLon') IS NULL
    ALTER TABLE dbo.tblOrder ADD SubmitLon NVARCHAR(50) NULL;
IF COL_LENGTH('dbo.tblOrder', 'SubmitAddress') IS NULL
    ALTER TABLE dbo.tblOrder ADD SubmitAddress NVARCHAR(500) NULL;
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
-- Missing / unparsable / out-of-range / 0,0 positions are ignored, so a bad fix never overwrites a good one.
-- An empty call (e.g. manager edit from the approval screen) keeps the original submit location.
CREATE OR ALTER PROCEDURE dbo.sp_webapi_SaveOrderLocation
    @OrderId INT,
    @Latitude NVARCHAR(50) = NULL,
    @Longitude NVARCHAR(50) = NULL,
    @StreetAddress NVARCHAR(500) = NULL
AS
BEGIN
    SET NOCOUNT ON;
    DECLARE @lat FLOAT = TRY_CONVERT(FLOAT, NULLIF(LTRIM(RTRIM(@Latitude)), ''));
    DECLARE @lon FLOAT = TRY_CONVERT(FLOAT, NULLIF(LTRIM(RTRIM(@Longitude)), ''));

    IF @lat IS NULL OR @lon IS NULL OR @lat NOT BETWEEN -90 AND 90 OR @lon NOT BETWEEN -180 AND 180
       OR (@lat = 0 AND @lon = 0)
        RETURN;

    UPDATE dbo.tblOrder
    SET SubmitLat = LTRIM(RTRIM(@Latitude)),
        SubmitLon = LTRIM(RTRIM(@Longitude)),
        SubmitAddress = COALESCE(NULLIF(LTRIM(RTRIM(@StreetAddress)), ''), SubmitAddress)
    WHERE OrderId = @OrderId;
END
GO
