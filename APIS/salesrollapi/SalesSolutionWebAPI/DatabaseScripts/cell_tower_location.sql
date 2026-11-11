-- Cell tower positions learned from the reps' own GPS fixes, so a device with no GPS (indoors, weak
-- sky view) can still be placed from its serving tower without any paid geolocation key.
-- Written by api/Tracking/LearnCell, read by api/Tracking/ResolveLocation. Re-runnable.
IF OBJECT_ID('dbo.tbl_CellTowerLocation', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.tbl_CellTowerLocation
    (
        RadioType      VARCHAR(8) NOT NULL,   -- gsm | wcdma | lte | nr
        Mcc            INT        NOT NULL,
        Mnc            INT        NOT NULL,
        Lac            INT        NOT NULL,   -- LAC (gsm/wcdma) or TAC (lte/nr)
        CellId         BIGINT     NOT NULL,
        Latitude       FLOAT      NOT NULL,
        Longitude      FLOAT      NOT NULL,
        AccuracyMeters FLOAT      NOT NULL,   -- radius covering the samples seen so far
        SampleCount    INT        NOT NULL,
        UpdatedAt      DATETIME   NOT NULL,
        CONSTRAINT PK_tbl_CellTowerLocation PRIMARY KEY (RadioType, Mcc, Mnc, Lac, CellId)
    );
END
GO

-- Upserts one tower from a GPS-confirmed fix. The stored point is the running mean of all samples;
-- AccuracyMeters grows to the farthest sample seen, so it reports the real spread, not the GPS accuracy.
CREATE OR ALTER PROCEDURE [dbo].[sp_Webapi_Save_CellTowerLocation]
    @RadioType VARCHAR(8),
    @Mcc INT,
    @Mnc INT,
    @Lac INT,
    @CellId BIGINT,
    @Latitude FLOAT,
    @Longitude FLOAT,
    @AccuracyMeters FLOAT
AS
BEGIN
    SET NOCOUNT ON;

    DECLARE @lat FLOAT, @lng FLOAT, @acc FLOAT, @n INT;

    SELECT @lat = Latitude, @lng = Longitude, @acc = AccuracyMeters, @n = SampleCount
    FROM dbo.tbl_CellTowerLocation
    WHERE RadioType = @RadioType AND Mcc = @Mcc AND Mnc = @Mnc AND Lac = @Lac AND CellId = @CellId;

    IF @n IS NULL
    BEGIN
        INSERT INTO dbo.tbl_CellTowerLocation
            (RadioType, Mcc, Mnc, Lac, CellId, Latitude, Longitude, AccuracyMeters, SampleCount, UpdatedAt)
        VALUES
            (@RadioType, @Mcc, @Mnc, @Lac, @CellId, @Latitude, @Longitude, @AccuracyMeters, 1, GETDATE());
        RETURN;
    END

    -- Distance of the new sample from the mean we hold; the radius must cover it.
    DECLARE @spread FLOAT =
        geography::Point(@lat, @lng, 4326).STDistance(geography::Point(@Latitude, @Longitude, 4326));

    -- Cap the weight of history so a tower that was mapped wrong once can still be corrected.
    DECLARE @weight INT = CASE WHEN @n > 50 THEN 50 ELSE @n END;

    UPDATE dbo.tbl_CellTowerLocation
    SET Latitude       = @lat + (@Latitude - @lat) / (@weight + 1),
        Longitude      = @lng + (@Longitude - @lng) / (@weight + 1),
        AccuracyMeters = CASE WHEN @spread + @AccuracyMeters > @acc THEN @spread + @AccuracyMeters ELSE @acc END,
        SampleCount    = @n + 1,
        UpdatedAt      = GETDATE()
    WHERE RadioType = @RadioType AND Mcc = @Mcc AND Mnc = @Mnc AND Lac = @Lac AND CellId = @CellId;
END
GO

-- Looks up one tower. Returns no row when the tower has never been seen with GPS.
CREATE OR ALTER PROCEDURE [dbo].[sp_Webapi_Get_CellTowerLocation]
    @RadioType VARCHAR(8),
    @Mcc INT,
    @Mnc INT,
    @Lac INT,
    @CellId BIGINT
AS
BEGIN
    SET NOCOUNT ON;

    SELECT Latitude, Longitude, AccuracyMeters, SampleCount
    FROM dbo.tbl_CellTowerLocation WITH (NOLOCK)
    WHERE RadioType = @RadioType AND Mcc = @Mcc AND Mnc = @Mnc AND Lac = @Lac AND CellId = @CellId;
END
GO

-- Self-check of the running average and the radius. Rolled back, so it leaves nothing behind;
-- it raises an error if the arithmetic is wrong. Safe to run on any server.
BEGIN TRANSACTION;

EXEC [dbo].[sp_Webapi_Save_CellTowerLocation] 'lte', 470, 1, 100, 999999999, 23.800000, 90.400000, 10;
-- Second sample ~100 m north of the first.
EXEC [dbo].[sp_Webapi_Save_CellTowerLocation] 'lte', 470, 1, 100, 999999999, 23.800900, 90.400000, 10;

DECLARE @lat FLOAT, @acc FLOAT, @n INT;
SELECT @lat = Latitude, @acc = AccuracyMeters, @n = SampleCount
FROM dbo.tbl_CellTowerLocation
WHERE RadioType = 'lte' AND Mcc = 470 AND Mnc = 1 AND Lac = 100 AND CellId = 999999999;

IF @n <> 2
    RAISERROR('tbl_CellTowerLocation self-check: SampleCount %d, expected 2', 16, 1, @n);
-- Mean of 23.800000 and 23.800900.
IF @lat NOT BETWEEN 23.800440 AND 23.800460
    RAISERROR('tbl_CellTowerLocation self-check: mean latitude not averaged', 16, 1);
-- ~100 m spread + the 10 m fix accuracy. RAISERROR cannot substitute a float, hence the text.
DECLARE @accText VARCHAR(32) = CONVERT(VARCHAR(32), @acc);
IF @acc NOT BETWEEN 100 AND 130
    RAISERROR('tbl_CellTowerLocation self-check: radius %s, expected ~110 m', 16, 1, @accText);

ROLLBACK TRANSACTION;
GO
