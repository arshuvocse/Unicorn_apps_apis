USE [SalesDisDB_Unicorn_DB]
GO

/****** Object:  Table [dbo].[tbl_DayWiseTourPlanMaster] ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[tbl_DayWiseTourPlanMaster]') AND type in (N'U'))
BEGIN
CREATE TABLE [dbo].[tbl_DayWiseTourPlanMaster](
    [TourPlanMasterId] [int] IDENTITY(1,1) NOT NULL,
    [EmpInfoId] [int] NOT NULL,
    [TourPlanDate] [date] NOT NULL,
    [IsFinalSubmit] [bit] NOT NULL DEFAULT ((0)),
    [CreatedDate] [datetime] NOT NULL DEFAULT (getdate()),
    [UpdatedDate] [datetime] NULL,
    CONSTRAINT [PK_tbl_DayWiseTourPlanMaster] PRIMARY KEY CLUSTERED 
    (
        [TourPlanMasterId] ASC
    )
)
END
GO

/****** Object:  Table [dbo].[tbl_DayWiseTourPlanCustomerDetail] ******/
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[tbl_DayWiseTourPlanCustomerDetail]') AND type in (N'U'))
BEGIN
CREATE TABLE [dbo].[tbl_DayWiseTourPlanCustomerDetail](
    [CustomerDetailId] [int] IDENTITY(1,1) NOT NULL,
    [TourPlanMasterId] [int] NOT NULL,
    [CustomerMasterId] [int] NOT NULL,
    [CreatedDate] [datetime] NOT NULL DEFAULT (getdate()),
    CONSTRAINT [PK_tbl_DayWiseTourPlanCustomerDetail] PRIMARY KEY CLUSTERED 
    (
        [CustomerDetailId] ASC
    )
)
END
GO

/****** Object:  Table [dbo].[tbl_DayWiseTourPlanDoctorDetail] ******/
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[tbl_DayWiseTourPlanDoctorDetail]') AND type in (N'U'))
BEGIN
CREATE TABLE [dbo].[tbl_DayWiseTourPlanDoctorDetail](
    [DoctorDetailId] [int] IDENTITY(1,1) NOT NULL,
    [TourPlanMasterId] [int] NOT NULL,
    [DoctorId] [int] NOT NULL,
    [CreatedDate] [datetime] NOT NULL DEFAULT (getdate()),
    CONSTRAINT [PK_tbl_DayWiseTourPlanDoctorDetail] PRIMARY KEY CLUSTERED 
    (
        [DoctorDetailId] ASC
    )
)
END
GO

-- Chamber-wise tour plan: NULL = doctor-wise (any chamber of the doctor; also all older rows).
IF COL_LENGTH(N'dbo.tbl_DayWiseTourPlanDoctorDetail', N'ChamberId') IS NULL
    ALTER TABLE [dbo].[tbl_DayWiseTourPlanDoctorDetail] ADD [ChamberId] [int] NULL;
GO

/****** Object:  StoredProcedure [dbo].[sp_Webapi_Save_DayWiseTourPlan] ******/
CREATE OR ALTER PROCEDURE [dbo].[sp_Webapi_Save_DayWiseTourPlan]
    @EmpInfoId INT,
    @TourPlanDate DATE,
    @IsFinalSubmit BIT = 1,
    @CustomerIds VARCHAR(MAX) = '', -- Comma separated customer IDs e.g. '101,102'
    @DoctorIds VARCHAR(MAX) = '',    -- Comma separated doctor IDs e.g. '201,202' (doctor-wise)
    @DoctorChamberIds VARCHAR(MAX) = '' -- Comma separated doctorId:chamberId e.g. '201:55,202:0' (0 = doctor-wise); used instead of @DoctorIds when given
AS
BEGIN
    SET NOCOUNT ON;
    BEGIN TRANSACTION;
    BEGIN TRY
        DECLARE @TourPlanMasterId INT;

        -- Check if master record exists for this employee and date
        SELECT @TourPlanMasterId = TourPlanMasterId 
        FROM [dbo].[tbl_DayWiseTourPlanMaster]
        WHERE [EmpInfoId] = @EmpInfoId AND [TourPlanDate] = @TourPlanDate;

        IF (@TourPlanMasterId IS NULL OR @TourPlanMasterId = 0)
        BEGIN
            INSERT INTO [dbo].[tbl_DayWiseTourPlanMaster] ([EmpInfoId], [TourPlanDate], [IsFinalSubmit], [CreatedDate])
            VALUES (@EmpInfoId, @TourPlanDate, @IsFinalSubmit, GETDATE());

            SET @TourPlanMasterId = SCOPE_IDENTITY();
        END
        ELSE
        BEGIN
            UPDATE [dbo].[tbl_DayWiseTourPlanMaster]
            SET [IsFinalSubmit] = @IsFinalSubmit,
                [UpdatedDate] = GETDATE()
            WHERE [TourPlanMasterId] = @TourPlanMasterId;

            -- Delete old details for update
            DELETE FROM [dbo].[tbl_DayWiseTourPlanCustomerDetail] WHERE [TourPlanMasterId] = @TourPlanMasterId;
            DELETE FROM [dbo].[tbl_DayWiseTourPlanDoctorDetail] WHERE [TourPlanMasterId] = @TourPlanMasterId;
        END

        -- Insert Customer Details
        IF (@CustomerIds IS NOT NULL AND LEN(@CustomerIds) > 0)
        BEGIN
            INSERT INTO [dbo].[tbl_DayWiseTourPlanCustomerDetail] ([TourPlanMasterId], [CustomerMasterId], [CreatedDate])
            SELECT @TourPlanMasterId, CAST(Item AS INT), GETDATE()
            FROM dbo.DelimitedSplit8K(@CustomerIds, ',')
            WHERE LTRIM(RTRIM(Item)) <> '';
        END

        -- Insert Doctor Details
        IF (@DoctorChamberIds IS NOT NULL AND LEN(@DoctorChamberIds) > 0)
        BEGIN
            INSERT INTO [dbo].[tbl_DayWiseTourPlanDoctorDetail] ([TourPlanMasterId], [DoctorId], [ChamberId], [CreatedDate])
            SELECT @TourPlanMasterId,
                   CAST(LEFT(v, CHARINDEX(':', v + ':') - 1) AS INT),
                   NULLIF(TRY_CAST(SUBSTRING(v, CHARINDEX(':', v + ':') + 1, 20) AS INT), 0),
                   GETDATE()
            FROM (SELECT LTRIM(RTRIM(Item)) AS v FROM dbo.DelimitedSplit8K(@DoctorChamberIds, ',')) x
            WHERE v <> '';
        END
        ELSE IF (@DoctorIds IS NOT NULL AND LEN(@DoctorIds) > 0)
        BEGIN
            INSERT INTO [dbo].[tbl_DayWiseTourPlanDoctorDetail] ([TourPlanMasterId], [DoctorId], [CreatedDate])
            SELECT @TourPlanMasterId, CAST(Item AS INT), GETDATE()
            FROM dbo.DelimitedSplit8K(@DoctorIds, ',')
            WHERE LTRIM(RTRIM(Item)) <> '';
        END

        COMMIT TRANSACTION;
        SELECT 1 AS IsSuccess, 'Day-wise Tour Plan saved successfully.' AS Message, @TourPlanMasterId AS TourPlanMasterId;
    END TRY
    BEGIN CATCH
        ROLLBACK TRANSACTION;
        SELECT 0 AS IsSuccess, ERROR_MESSAGE() AS Message, 0 AS TourPlanMasterId;
    END CATCH
END
GO

/****** Object:  StoredProcedure [dbo].[sp_Webapi_Get_DayWiseTourPlan] ******/
-- Saved day-wise plan of one month, so the app can show earlier selections again.
-- Type 'C' = customer (Id = CustomerMasterId), 'D' = doctor (Id = DoctorId, ChamberId 0 = doctor-wise).
CREATE OR ALTER PROCEDURE [dbo].[sp_Webapi_Get_DayWiseTourPlan]
    @EmpInfoId INT,
    @Month INT,
    @Year INT
AS
BEGIN
    SET NOCOUNT ON;
    DECLARE @From DATE = DATEFROMPARTS(@Year, @Month, 1);
    DECLARE @To DATE = EOMONTH(@From);

    SELECT CONVERT(VARCHAR(10), m.TourPlanDate, 23) AS TourPlanDate, 'C' AS Type, cd.CustomerMasterId AS Id, 0 AS ChamberId
    FROM [dbo].[tbl_DayWiseTourPlanMaster] m
    INNER JOIN [dbo].[tbl_DayWiseTourPlanCustomerDetail] cd ON cd.TourPlanMasterId = m.TourPlanMasterId
    WHERE m.EmpInfoId = @EmpInfoId AND m.TourPlanDate BETWEEN @From AND @To
    UNION ALL
    SELECT CONVERT(VARCHAR(10), m.TourPlanDate, 23), 'D', dd.DoctorId, ISNULL(dd.ChamberId, 0)
    FROM [dbo].[tbl_DayWiseTourPlanMaster] m
    INNER JOIN [dbo].[tbl_DayWiseTourPlanDoctorDetail] dd ON dd.TourPlanMasterId = m.TourPlanMasterId
    WHERE m.EmpInfoId = @EmpInfoId AND m.TourPlanDate BETWEEN @From AND @To;
END
GO
