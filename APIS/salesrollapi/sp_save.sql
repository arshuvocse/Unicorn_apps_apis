
CREATE   PROCEDURE [dbo].[sp_Webapi_Save_DoctorEntry]
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
        Longitude,
        SalesPersonId
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
        NULLIF(LTRIM(RTRIM(@Longitude)), ''),
        @EmpId
    );

    SELECT @doctorId = SCOPE_IDENTITY();

    DECLARE @Id INT;
    SET @Id = @doctorId;

    DECLARE @Role NVARCHAR(MAX);
    SELECT @Role = usR.RoleName, @empId = emp.EmpInfoId
    FROM dbo.tblEmpGeneralInfo emp
    LEFT JOIN tblUser us ON us.EmpInfoId = emp.EmpInfoId
    LEFT JOIN tbl_UserRoleInfo usR ON usR.UserRoleID = us.UserRoleID
    WHERE us.EmpInfoId = @EmpId;

    DECLARE @ToEmpId1 INT;
    DECLARE @GroupId1 INT;
    DECLARE @RegionId1 INT;
    DECLARE @AreaId1 INT;
    DECLARE @TerrId1 INT;

    DECLARE @EntryTime TIME(7) = CAST(GETDATE() AS TIME);

    IF (@Role = 'MIO')
    BEGIN
        SELECT @GroupId1 = GroupId, @AreaId1 = AreaId, @TerrId1 = TerritoryId, @RegionId1 = RegionId 
        FROM dbo.View_webapi_FieldForce WHERE MIOEmpId = @empId;
    END
    IF (@Role = 'ASM')
    BEGIN
        SELECT @GroupId1 = GroupId, @AreaId1 = AreaId, @TerrId1 = TerritoryId, @RegionId1 = RegionId 
        FROM dbo.View_webapi_FieldForce WHERE ASMEMPId = @empId;
    END
    IF (@Role = 'RSM')
    BEGIN
        SELECT @GroupId1 = GroupId, @AreaId1 = AreaId, @TerrId1 = TerritoryId, @RegionId1 = RegionId 
        FROM dbo.View_webapi_FieldForce WHERE RSMEMPId = @empId;
    END
    IF (@Role = 'NSM')
    BEGIN
        SELECT @GroupId1 = GroupId, @AreaId1 = AreaId, @TerrId1 = TerritoryId, @RegionId1 = RegionId 
        FROM dbo.View_webapi_FieldForce WHERE NSMEMPId = @empId;
    END

    DECLARE @EntryDate DATETIME = GETDATE();

    EXECUTE dbo.sp_webapi_SaveDoctorAppLog 
        @DoctorApprovalId = 0,
        @Date = @EntryDate,
        @FromEmpId = @empId,
        @ToEmpId = 0,
        @TableId = @Id,
        @Status = N'Posted',
        @Comments = N'',
        @Type = N'Doctor',
        @Step = 1,
        @GroupId = @GroupId1,
        @RegionId = @RegionId1,
        @AreaId = @AreaId1,
        @TerritoryId = @TerrId1,
        @ToGroupId = 0,
        @ToRegionId = 0,
        @ToAreaId = 0,
        @ToTerritoryId = 0,
        @EntryByS = @empId,
        @EntryDateS = @EntryDate,
        @EntryTimeS = @EntryTime,
        @ApproveByS = NULL,
        @ApproveDateS = NULL,
        @ApproveTimeS = NULL,
        @EntryByApp = @empId,
        @EntryDateApp = @EntryDate,
        @EntryTimeApp = @EntryTime,
        @ApproveByApp = NULL,
        @ApproveDateApp = NULL,
        @ApproveTimeApp = NULL,
        @MenuId = 303;

    SELECT @doctorId;
END
