IF COL_LENGTH('dbo.tblDoctorMaster', 'Latitude') IS NULL
    ALTER TABLE dbo.tblDoctorMaster ADD Latitude NVARCHAR(50) NULL;
GO

IF COL_LENGTH('dbo.tblDoctorMaster', 'Longitude') IS NULL
    ALTER TABLE dbo.tblDoctorMaster ADD Longitude NVARCHAR(50) NULL;
GO

IF COL_LENGTH('dbo.tblDoctorMaster', 'StreetAddress') IS NULL
    ALTER TABLE dbo.tblDoctorMaster ADD StreetAddress NVARCHAR(MAX) NULL;
GO

IF OBJECT_ID('dbo.sp_Webapi_UpdateDoctorLocation', 'P') IS NULL
    EXEC('CREATE PROCEDURE [dbo].[sp_Webapi_UpdateDoctorLocation] AS BEGIN SET NOCOUNT ON; END');
GO

ALTER PROCEDURE [dbo].[sp_Webapi_UpdateDoctorLocation]
    @DoctorId INT,
    @EmpId INT = NULL,
    @Latitude NVARCHAR(50) = NULL,
    @Longitude NVARCHAR(50) = NULL,
    @StreetAddress NVARCHAR(MAX) = NULL
AS
BEGIN
    SET NOCOUNT ON;

    DECLARE @UpdateUserId INT;

    SELECT @UpdateUserId = UserId
    FROM dbo.tblUser
    WHERE EmpInfoId = @EmpId;

    UPDATE dbo.tblDoctorMaster
    SET Latitude = NULLIF(LTRIM(RTRIM(@Latitude)), ''),
        Longitude = NULLIF(LTRIM(RTRIM(@Longitude)), ''),
        StreetAddress = NULLIF(LTRIM(RTRIM(@StreetAddress)), ''),
        UpdateBy = ISNULL(@UpdateUserId, UpdateBy),
        UpdateDate = GETDATE()
    WHERE DoctorId = @DoctorId;
END
GO
