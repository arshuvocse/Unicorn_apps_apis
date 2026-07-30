SET XACT_ABORT ON;
GO

IF COL_LENGTH('dbo.tblDoctorMaster', 'Latitude') IS NULL
    ALTER TABLE dbo.tblDoctorMaster ADD Latitude NVARCHAR(50) NULL;
GO

IF COL_LENGTH('dbo.tblDoctorMaster', 'Longitude') IS NULL
    ALTER TABLE dbo.tblDoctorMaster ADD Longitude NVARCHAR(50) NULL;
GO

ALTER PROCEDURE [dbo].[sp_Webapi_Save_DoctorEntry]
    @DoctorId INT = 0,
    @DoctorName NVARCHAR(MAX),
    @DoctorAddress NVARCHAR(MAX) = NULL,
    @DegreeStr NVARCHAR(MAX) = NULL,
    @BrandStr NVARCHAR(MAX) = NULL,
    @SpecialityStr NVARCHAR(MAX) = NULL,
    @DoctorTypeStr NVARCHAR(MAX) = NULL,
    @InstitutionSTr NVARCHAR(MAX) = NULL,
    @SessionUser INT = NULL,
    @EmpId INT = NULL,
    @Latitude NVARCHAR(50) = NULL,
    @Longitude NVARCHAR(50) = NULL,
    @ContactTypeId INT = NULL,
    @SpecialDayId INT = NULL,
    @DoctorCategoryId INT = NULL,
    @StationTypeId INT = NULL,
    @ProgramTypeId INT = NULL,
    @SMCTypeId INT = NULL,
    @DoctorTypeId INT = NULL,
    @DesignationId INT = NULL
AS
BEGIN
    SET NOCOUNT ON;

    DECLARE @NewDoctorId INT;
    DECLARE @EntryEmpId INT = COALESCE(NULLIF(@EmpId, 0), NULLIF(@SessionUser, 0));
    DECLARE @EntryUserId INT;

    SELECT @EntryUserId = UserId
    FROM dbo.tblUser
    WHERE EmpInfoId = @EntryEmpId;

    INSERT INTO dbo.tblDoctorMaster
    (
        DoctorName,
        DoctorCode,
        DoctorAddress,
        IsActive,
        EntryBy,
        EntryDate,
        IsFromApp,
        DesignationId,
        ApprovalStatus,
        DoctorCategoryId,
        StationTypeId,
        ProgramTypeId,
        SMCTypeId,
        DoctorTypeId,
        Latitude,
        Longitude
    )
    VALUES
    (
        LTRIM(RTRIM(@DoctorName)),
        NULL,
        NULLIF(LTRIM(RTRIM(@DoctorAddress)), ''),
        0,
        @EntryUserId,
        GETDATE(),
        1,
        NULLIF(@DesignationId, 0),
        '0',
        NULLIF(@DoctorCategoryId, 0),
        NULLIF(@StationTypeId, 0),
        NULLIF(@ProgramTypeId, 0),
        NULLIF(@SMCTypeId, 0),
        NULLIF(@DoctorTypeId, 0),
        NULLIF(LTRIM(RTRIM(@Latitude)), ''),
        NULLIF(LTRIM(RTRIM(@Longitude)), '')
    );

    SET @NewDoctorId = CONVERT(INT, SCOPE_IDENTITY());
    SELECT @NewDoctorId;
END;
GO

ALTER PROCEDURE [dbo].[sp_Webapi_UD_DoctorEntry]
    @DoctorId INT,
    @DoctorName NVARCHAR(MAX),
    @DoctorAddress NVARCHAR(MAX) = NULL,
    @DegreeStr NVARCHAR(MAX) = NULL,
    @BrandStr NVARCHAR(MAX) = NULL,
    @SpecialityStr NVARCHAR(MAX) = NULL,
    @DoctorTypeStr NVARCHAR(MAX) = NULL,
    @InstitutionSTr NVARCHAR(MAX) = NULL,
    @SessionUser INT = NULL,
    @EmpId INT = NULL,
    @Latitude NVARCHAR(50) = NULL,
    @Longitude NVARCHAR(50) = NULL,
    @ContactTypeId INT = NULL,
    @SpecialDayId INT = NULL,
    @DoctorCategoryId INT = NULL,
    @StationTypeId INT = NULL,
    @ProgramTypeId INT = NULL,
    @SMCTypeId INT = NULL,
    @DoctorTypeId INT = NULL,
    @DesignationId INT = NULL
AS
BEGIN
    SET NOCOUNT ON;

    IF NOT EXISTS (SELECT 1 FROM dbo.tblDoctorMaster WHERE DoctorId = @DoctorId)
        THROW 50001, 'Doctor not found.', 1;

    DECLARE @UpdateEmpId INT = COALESCE(NULLIF(@EmpId, 0), NULLIF(@SessionUser, 0));
    DECLARE @UpdateUserId INT;

    SELECT @UpdateUserId = UserId
    FROM dbo.tblUser
    WHERE EmpInfoId = @UpdateEmpId;

    UPDATE dbo.tblDoctorMaster
    SET DoctorName = LTRIM(RTRIM(@DoctorName)),
        DoctorAddress = NULLIF(LTRIM(RTRIM(@DoctorAddress)), ''),
        UpdateBy = @UpdateUserId,
        UpdateDate = GETDATE(),
        DesignationId = NULLIF(@DesignationId, 0),
        DoctorCategoryId = NULLIF(@DoctorCategoryId, 0),
        StationTypeId = NULLIF(@StationTypeId, 0),
        ProgramTypeId = NULLIF(@ProgramTypeId, 0),
        SMCTypeId = NULLIF(@SMCTypeId, 0),
        DoctorTypeId = NULLIF(@DoctorTypeId, 0),
        Latitude = NULLIF(LTRIM(RTRIM(@Latitude)), ''),
        Longitude = NULLIF(LTRIM(RTRIM(@Longitude)), '')
    WHERE DoctorId = @DoctorId;

    -- Detail rows are recreated by DoctorDAL from the JSON arrays/CSV fields.
    DELETE FROM dbo.tblDoctorSpecialDayDetail WHERE DoctorId = @DoctorId;
    DELETE FROM dbo.tblDoctorContactDetail WHERE DoctorId = @DoctorId;
    DELETE FROM dbo.tblDoctorDegreeDetail WHERE DoctorId = @DoctorId;
    DELETE FROM dbo.tblDoctorBrandDetail WHERE DoctorId = @DoctorId;
    DELETE FROM dbo.tblDoctorSpecialityDetail WHERE DoctorId = @DoctorId;
    DELETE FROM dbo.tblDoctorInstitutionDetail WHERE DoctorId = @DoctorId;
    DELETE FROM dbo.tblDoctorProgramTypeDetail WHERE DoctorId = @DoctorId;
    DELETE FROM dbo.tblDoctorChemberDetail WHERE DoctorId = @DoctorId;
END;
GO
