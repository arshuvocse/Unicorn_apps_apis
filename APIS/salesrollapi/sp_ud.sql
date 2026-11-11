
CREATE   PROCEDURE [dbo].[sp_Webapi_UD_DoctorEntry]
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

    IF @DoctorAddress IS NULL OR LTRIM(RTRIM(@DoctorAddress)) = ''
    BEGIN
        RAISERROR('Doctor Address is required.', 16, 1);
        RETURN;
    END

    IF @InstitutionSTr IS NULL OR LTRIM(RTRIM(@InstitutionSTr)) = '' OR LTRIM(RTRIM(@InstitutionSTr)) = '----'
    BEGIN
        RAISERROR('Doctor Institution is required.', 16, 1);
        RETURN;
    END

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

    DELETE FROM dbo.tblDoctorSpecialDayDetail WHERE DoctorId = @DoctorId;
    DELETE FROM dbo.tblDoctorContactDetail WHERE DoctorId = @DoctorId;
    DELETE FROM dbo.tblDoctorDegreeDetail WHERE DoctorId = @DoctorId;
    DELETE FROM dbo.tblDoctorBrandDetail WHERE DoctorId = @DoctorId;
    DELETE FROM dbo.tblDoctorSpecialityDetail WHERE DoctorId = @DoctorId;
    DELETE FROM dbo.tblDoctorInstitutionDetail WHERE DoctorId = @DoctorId;
    DELETE FROM dbo.tblDoctorProgramTypeDetail WHERE DoctorId = @DoctorId;
    DELETE FROM dbo.tblDoctorChemberDetail WHERE DoctorId = @DoctorId;
END
