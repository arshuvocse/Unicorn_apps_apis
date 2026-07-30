IF OBJECT_ID('dbo.sp_Webapi_Save_Customer', 'P') IS NULL
    EXEC('CREATE PROCEDURE [dbo].[sp_Webapi_Save_Customer] AS BEGIN SET NOCOUNT ON; END');
GO

ALTER PROCEDURE [dbo].[sp_Webapi_Save_Customer]
    @name NVARCHAR(MAX) = NULL,
    @address NVARCHAR(MAX) = NULL,
    @phone NVARCHAR(MAX) = NULL,
    @conPerson NVARCHAR(MAX) = NULL,
    @CustomerBSPCode NVARCHAR(MAX) = NULL,
    @termOfPayment NVARCHAR(50) = NULL,
    @isFromApp BIT = NULL,
    @marketId INT = 0,
    @CustomerTypeId INT = NULL,
    @ProgramTypeId INT = NULL,
    @SMCTypeId INT = NULL,
    @empId INT = NULL,
    @VoterID NVARCHAR(MAX) = NULL,
    @TradeLicense NVARCHAR(MAX) = NULL,
    @Reamrks NVARCHAR(MAX) = NULL,
    @Latitude NVARCHAR(MAX) = NULL,
    @Longitude NVARCHAR(MAX) = NULL,
    @StreetAddress NVARCHAR(MAX) = NULL
AS
BEGIN
    SET NOCOUNT ON;

    DECLARE @userid INT,
            @Id INT,
            @CountData INT,
            @CustomerBsPCodeJust VARCHAR(50) = NULL,
            @ComUnitName NVARCHAR(MAX),
            @ComUnitCode NVARCHAR(MAX),
            @NSMStationTypeId INT,
            @DZSMStationTypeId INT,
            @DCId INT,
            @StationTypeId INT,
            @GroupId INT,
            @RegionId INT,
            @AreaId INT,
            @TerritoryId INT,
            @SubTerritoryId INT,
            @divId INT,
            @disId INT,
            @thanaId INT,
            @Role NVARCHAR(MAX),
            @EntryTime TIME(7) = CAST(GETDATE() AS TIME),
            @entrydate DATETIME = GETDATE();

    SELECT @userid = UserId
    FROM dbo.tblUser
    WHERE EmpInfoId = @empId;

    SELECT @CountData = ISNULL(COUNT(*), 0)
    FROM dbo.tblCustMaster
    WHERE CellNo = @phone
      AND [ActionStatus] <> '3';

    IF (@CountData > 0)
    BEGIN
        SELECT 0;
        RETURN;
    END;

    IF (ISNULL(@CustomerTypeId, 0) = 0)
    BEGIN
        SELECT TOP 1 @CustomerTypeId = CustomerTypeId
        FROM dbo.tblCustomerType
        WHERE IsDefault = 1;
    END;

    SELECT @Role = usR.RoleName
    FROM dbo.tblEmpGeneralInfo emp
    LEFT JOIN dbo.tblUser us ON us.EmpInfoId = emp.EmpInfoId
    LEFT JOIN dbo.tbl_UserRoleInfo usR ON usR.UserRoleID = us.UserRoleID
    WHERE us.UserId = @userid;

    IF (@marketId > 0)
    BEGIN
        SELECT @divId = div.DivisionId,
               @disId = dis.DistrictId,
               @thanaId = mr.ThanaId,
               @SubTerritoryId = sr.SubTerritoryId,
               @TerritoryId = tr.TerritoryId,
               @AreaId = ar.AreaId,
               @RegionId = rg.RegionId,
               @GroupId = rg.GroupId
        FROM dbo.tblmarket mr WITH (NOLOCK)
        INNER JOIN dbo.tblSubTerritory sr WITH (NOLOCK) ON sr.SubTerritoryId = mr.SubTerritoryId
        INNER JOIN dbo.tblTerritory tr WITH (NOLOCK) ON sr.TerritoryId = tr.TerritoryId
        INNER JOIN dbo.tblArea ar WITH (NOLOCK) ON ar.AreaId = tr.AreaId
        INNER JOIN dbo.tblRegion rg WITH (NOLOCK) ON ar.RegionId = rg.RegionId
        LEFT JOIN dbo.tbl_Thana tha WITH (NOLOCK) ON mr.ThanaId = tha.ThanaId
        LEFT JOIN dbo.tbl_District dis WITH (NOLOCK) ON dis.DistrictId = tha.district_id
        LEFT JOIN dbo.tbl_Division div WITH (NOLOCK) ON dis.DivisionId = div.DivisionId
        WHERE mr.MarketId = @marketId;

        SELECT @StationTypeId = StationTypeId
        FROM dbo.tblMarketStationDetail
        WHERE MarketId = @marketId
          AND UserRoleID = 1;

        SELECT @NSMStationTypeId = StationTypeId
        FROM dbo.tblMarketStationDetail
        WHERE MarketId = @marketId
          AND UserRoleID = 2;

        SELECT @DZSMStationTypeId = StationTypeId
        FROM dbo.tblMarketStationDetail
        WHERE MarketId = @marketId
          AND UserRoleID = 3;

        SELECT @DCId = masR.DCId,
               @ComUnitName = cunit.ComUnitName,
               @ComUnitCode = cunit.ComUnitCode
        FROM dbo.tblDcWiseTerritoryMaster masR
        INNER JOIN dbo.tblDcWiseTerritoryDetail dtl ON masR.DcWiseTerritoryMasterId = dtl.DcWiseTerritoryMasterId
        INNER JOIN dbo.tblCompanyUnit cunit ON masR.DCId = cunit.ComUnitId
        WHERE dtl.TerritoryId = @TerritoryId;
    END;

    IF (@GroupId IS NULL)
    BEGIN
        IF (@Role = 'MIO')
            SELECT @GroupId = GroupId, @AreaId = AreaId, @TerritoryId = TerritoryId, @RegionId = RegionId
            FROM dbo.View_webapi_FieldForce
            WHERE MIOEmpId = @empId;

        IF (@Role = 'ASM')
            SELECT @GroupId = GroupId, @AreaId = AreaId, @TerritoryId = TerritoryId, @RegionId = RegionId
            FROM dbo.View_webapi_FieldForce
            WHERE ASMEMPId = @empId;

        IF (@Role = 'RSM')
            SELECT @GroupId = GroupId, @AreaId = AreaId, @TerritoryId = TerritoryId, @RegionId = RegionId
            FROM dbo.View_webapi_FieldForce
            WHERE RSMEMPId = @empId;

        IF (@Role = 'NSM')
            SELECT @GroupId = GroupId, @AreaId = AreaId, @TerritoryId = TerritoryId, @RegionId = RegionId
            FROM dbo.View_webapi_FieldForce
            WHERE NSMEMPId = @empId;
    END;

    INSERT INTO dbo.tblCustMaster
    (
        CustomerName,
        Address,
        CellNo,
        ConPerson,
        TermOfPayment,
        IsActive,
        CustomerTypeId,
        CreateBy,
        CreateDate,
        IsVatApplicable,
        [ActionStatus],
        [VoterID],
        [TradeLicense],
        Reamrks,
        OwnerName,
        GroupId,
        RegionId,
        AreaId,
        TerritoryId,
        SubTerritoryId,
        MarketId,
        Latitude,
        Longitude,
        StreetAddress,
        ProgramTypeId,
        StationTypeId,
        DivisionId,
        DistrictId,
        ThanaId,
        CategoryId,
        FixedCustomer,
        NSMStationTypeId,
        DZSMStationTypeId,
        SMCTypeId,
        CustomerBsPCode,
        CustomerBsPCodeInfo,
        SalesPersonId
    )
    VALUES
    (
        UPPER(@name),
        UPPER(@address),
        @phone,
        UPPER(@conPerson),
        @termOfPayment,
        0,
        NULLIF(@CustomerTypeId, 0),
        @userid,
        GETDATE(),
        0,
        '0',
        UPPER(@VoterID),
        UPPER(@TradeLicense),
        @Reamrks,
        UPPER(@conPerson),
        @GroupId,
        @RegionId,
        @AreaId,
        @TerritoryId,
        @SubTerritoryId,
        ISNULL(@marketId, 0),
        @Latitude,
        @Longitude,
        @StreetAddress,
        NULLIF(@ProgramTypeId, 0),
        @StationTypeId,
        @divId,
        @disId,
        @thanaId,
        1,
        0,
        @NSMStationTypeId,
        @DZSMStationTypeId,
        NULLIF(@SMCTypeId, 0),
        @CustomerBsPCodeJust,
        @CustomerBSPCode,
        @empId
    );

    SET @Id = CONVERT(INT, SCOPE_IDENTITY());

    EXECUTE dbo.sp_webapi_SaveCustomerAppLog
        @CustomerApprovalId = 0,
        @Date = @entrydate,
        @FromEmpId = @empId,
        @ToEmpId = 0,
        @TableId = @Id,
        @Status = N'Posted',
        @Comments = N'',
        @Type = N'Customer',
        @Step = 1,
        @GroupId = @GroupId,
        @RegionId = @RegionId,
        @AreaId = @AreaId,
        @TerritoryId = @TerritoryId,
        @ToGroupId = 0,
        @ToRegionId = 0,
        @ToAreaId = 0,
        @ToTerritoryId = 0,
        @EntryByS = @empId,
        @EntryDateS = @entrydate,
        @EntryTimeS = @EntryTime,
        @ApproveByS = NULL,
        @ApproveDateS = NULL,
        @ApproveTimeS = NULL,
        @EntryByApp = @empId,
        @EntryDateApp = @entrydate,
        @EntryTimeApp = @EntryTime,
        @ApproveByApp = NULL,
        @ApproveDateApp = NULL,
        @ApproveTimeApp = NULL,
        @MenuId = 302;

    SELECT @Id;
END;
GO

IF OBJECT_ID('dbo.sp_Webapi_Update_Customer', 'P') IS NULL
    EXEC('CREATE PROCEDURE [dbo].[sp_Webapi_Update_Customer] AS BEGIN SET NOCOUNT ON; END');
GO

ALTER PROCEDURE [dbo].[sp_Webapi_Update_Customer]
    @id INT,
    @name NVARCHAR(MAX) = NULL,
    @address NVARCHAR(MAX) = NULL,
    @phone NVARCHAR(MAX) = NULL,
    @conPerson NVARCHAR(MAX) = NULL,
    @termOfPayment NVARCHAR(50) = NULL,
    @isFromApp BIT = NULL,
    @marketId INT = NULL,
    @CustomerTypeId INT = NULL,
    @ProgramTypeId INT = NULL,
    @empId INT = NULL,
    @CustomerBSPCode NVARCHAR(MAX) = NULL,
    @VoterID NVARCHAR(MAX) = NULL,
    @TradeLicense NVARCHAR(MAX) = NULL,
    @Reamrks NVARCHAR(MAX) = NULL,
    @Latitude NVARCHAR(MAX) = NULL,
    @Longitude NVARCHAR(MAX) = NULL,
    @StreetAddress NVARCHAR(MAX) = NULL,
    @SMCTypeId INT = NULL,
    @DoctorAddress NVARCHAR(MAX) = NULL
AS
BEGIN
    SET NOCOUNT ON;

    DECLARE @userid INT,
            @GroupId INT,
            @RegionId INT,
            @AreaId INT,
            @TerritoryId INT,
            @SubTerritoryId INT;

    SELECT @userid = UserId
    FROM dbo.tblUser
    WHERE EmpInfoId = @empId;

    IF (ISNULL(@marketId, 0) > 0)
    BEGIN
        SELECT @SubTerritoryId = sr.SubTerritoryId,
               @TerritoryId = tr.TerritoryId,
               @AreaId = ar.AreaId,
               @RegionId = rg.RegionId,
               @GroupId = rg.GroupId
        FROM dbo.tblmarket mr WITH (NOLOCK)
        INNER JOIN dbo.tblSubTerritory sr WITH (NOLOCK) ON sr.SubTerritoryId = mr.SubTerritoryId
        INNER JOIN dbo.tblTerritory tr WITH (NOLOCK) ON sr.TerritoryId = tr.TerritoryId
        INNER JOIN dbo.tblArea ar WITH (NOLOCK) ON ar.AreaId = tr.AreaId
        INNER JOIN dbo.tblRegion rg WITH (NOLOCK) ON ar.RegionId = rg.RegionId
        WHERE mr.MarketId = @marketId;
    END;

    UPDATE dbo.tblCustMaster
    SET CustomerName = UPPER(@name),
        Address = UPPER(@address),
        CellNo = @phone,
        ConPerson = UPPER(@conPerson),
        TermOfPayment = @termOfPayment,
        UpdateBy = @userid,
        UpdateDate = GETDATE(),
        [VoterID] = UPPER(@VoterID),
        [TradeLicense] = UPPER(@TradeLicense),
        Reamrks = @Reamrks,
        OwnerName = UPPER(@conPerson),
        GroupId = CASE WHEN ISNULL(@marketId, 0) > 0 THEN @GroupId ELSE GroupId END,
        RegionId = CASE WHEN ISNULL(@marketId, 0) > 0 THEN @RegionId ELSE RegionId END,
        AreaId = CASE WHEN ISNULL(@marketId, 0) > 0 THEN @AreaId ELSE AreaId END,
        TerritoryId = CASE WHEN ISNULL(@marketId, 0) > 0 THEN @TerritoryId ELSE TerritoryId END,
        SubTerritoryId = CASE WHEN ISNULL(@marketId, 0) > 0 THEN @SubTerritoryId ELSE SubTerritoryId END,
        MarketId = ISNULL(@marketId, 0),
        CustomerTypeId = CASE WHEN ISNULL(@CustomerTypeId, 0) > 0 THEN @CustomerTypeId ELSE CustomerTypeId END,
        ProgramTypeId = NULLIF(@ProgramTypeId, 0),
        SMCTypeId = NULLIF(@SMCTypeId, 0),
        Latitude = NULLIF(LTRIM(RTRIM(@Latitude)), ''),
        Longitude = NULLIF(LTRIM(RTRIM(@Longitude)), ''),
        StreetAddress = NULLIF(LTRIM(RTRIM(@StreetAddress)), ''),
        CustomerBsPCodeInfo = @CustomerBSPCode
    WHERE CustomerMasterId = @id;
END;
GO
