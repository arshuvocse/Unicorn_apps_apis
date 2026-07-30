CREATE OR ALTER PROCEDURE [dbo].[sp_WebApi_SaveProviderDropoutIntrigration]
    @programName NVARCHAR(50) = NULL,
    @providerCode NVARCHAR(50) = NULL,
    @providerName NVARCHAR(200) = NULL,
    @mobileNo NVARCHAR(30) = NULL,
    @nid NVARCHAR(30) = NULL,
    @email NVARCHAR(255) = NULL,
    @outlet NVARCHAR(200) = NULL,
    @dropoutReason NVARCHAR(500) = NULL,
    @insertedAt DATETIME2(7) = NULL
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;

    BEGIN TRY
        DECLARE @providerIDropoutIntrigrationd BIGINT;
        DECLARE @HasProviderDropoutIntegrationIdColumn BIT =
            CASE
                WHEN COL_LENGTH('dbo.tblProviderDropoutIntrigration', 'providerIDropoutIntrigrationd') IS NULL THEN 0
                ELSE 1
            END;
        DECLARE @CanAutoGenerateProviderDropoutIntegrationId BIT = 0;
        DECLARE @InsertedProviderDropoutIds TABLE
        (
            providerIDropoutIntrigrationd BIGINT NULL
        );

        IF @HasProviderDropoutIntegrationIdColumn = 1
        BEGIN
            SELECT
                @CanAutoGenerateProviderDropoutIntegrationId =
                    CASE
                        WHEN COLUMNPROPERTY(OBJECT_ID('dbo.tblProviderDropoutIntrigration'), 'providerIDropoutIntrigrationd', 'IsIdentity') = 1
                            THEN 1
                        WHEN EXISTS
                        (
                            SELECT 1
                            FROM sys.columns
                            WHERE object_id = OBJECT_ID('dbo.tblProviderDropoutIntrigration')
                              AND name = 'providerIDropoutIntrigrationd'
                              AND default_object_id <> 0
                        )
                            THEN 1
                        ELSE 0
                    END;
        END

        IF @HasProviderDropoutIntegrationIdColumn = 1
           AND @CanAutoGenerateProviderDropoutIntegrationId = 0
        BEGIN
            BEGIN TRANSACTION;

            SELECT
                @providerIDropoutIntrigrationd = ISNULL(MAX([providerIDropoutIntrigrationd]), 0) + 1
            FROM [dbo].[tblProviderDropoutIntrigration] WITH (UPDLOCK, HOLDLOCK);

            INSERT INTO [dbo].[tblProviderDropoutIntrigration]
            (
                [providerIDropoutIntrigrationd],
                [programName],
                [providerCode],
                [providerName],
                [mobileNo],
                [nid],
                [email],
                [outlet],
                [dropoutReason],
                [insertedAt]
            )
            OUTPUT INSERTED.[providerIDropoutIntrigrationd] INTO @InsertedProviderDropoutIds
            VALUES
            (
                @providerIDropoutIntrigrationd,
                @programName,
                @providerCode,
                @providerName,
                @mobileNo,
                @nid,
                @email,
                @outlet,
                @dropoutReason,
                ISNULL(@insertedAt, SYSDATETIME())
            );

            COMMIT TRANSACTION;
        END
        ELSE IF @HasProviderDropoutIntegrationIdColumn = 1
        BEGIN
            INSERT INTO [dbo].[tblProviderDropoutIntrigration]
            (
                [programName],
                [providerCode],
                [providerName],
                [mobileNo],
                [nid],
                [email],
                [outlet],
                [dropoutReason],
                [insertedAt]
            )
            OUTPUT INSERTED.[providerIDropoutIntrigrationd] INTO @InsertedProviderDropoutIds
            VALUES
            (
                @programName,
                @providerCode,
                @providerName,
                @mobileNo,
                @nid,
                @email,
                @outlet,
                @dropoutReason,
                ISNULL(@insertedAt, SYSDATETIME())
            );
        END
        ELSE
        BEGIN
            INSERT INTO [dbo].[tblProviderDropoutIntrigration]
            (
                [programName],
                [providerCode],
                [providerName],
                [mobileNo],
                [nid],
                [email],
                [outlet],
                [dropoutReason],
                [insertedAt]
            )
            VALUES
            (
                @programName,
                @providerCode,
                @providerName,
                @mobileNo,
                @nid,
                @email,
                @outlet,
                @dropoutReason,
                ISNULL(@insertedAt, SYSDATETIME())
            );
        END

        SELECT TOP (1)
            @providerIDropoutIntrigrationd = providerIDropoutIntrigrationd
        FROM @InsertedProviderDropoutIds;

        SELECT
            CAST(1 AS BIT) AS IsSuccess,
            N'inserted' AS [Action],
            N'Provider dropout info saved successfully.' AS [Message],
            CAST(NULL AS NVARCHAR(4000)) AS ErrorMessage,
            @providerIDropoutIntrigrationd AS providerIDropoutIntrigrationd;
    END TRY
    BEGIN CATCH
        IF XACT_STATE() <> 0
        BEGIN
            ROLLBACK TRANSACTION;
        END

        SELECT
            CAST(0 AS BIT) AS IsSuccess,
            CAST(NULL AS NVARCHAR(20)) AS [Action],
            N'Provider dropout info save failed.' AS [Message],
            ERROR_MESSAGE() AS ErrorMessage,
            CAST(NULL AS BIGINT) AS providerIDropoutIntrigrationd;
    END CATCH
END
