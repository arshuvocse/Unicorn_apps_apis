IF OBJECT_ID('dbo.tblProviderDropoutIntrigration', 'U') IS NULL
BEGIN
    THROW 50000, 'dbo.tblProviderDropoutIntrigration was not found.', 1;
END;

IF COL_LENGTH('dbo.tblProviderDropoutIntrigration', 'providerIDropoutIntrigrationd') IS NULL
BEGIN
    THROW 50001, 'Column providerIDropoutIntrigrationd was not found on dbo.tblProviderDropoutIntrigration.', 1;
END;

IF COLUMNPROPERTY(OBJECT_ID('dbo.tblProviderDropoutIntrigration'), 'providerIDropoutIntrigrationd', 'IsIdentity') = 1
BEGIN
    PRINT 'providerIDropoutIntrigrationd is already an IDENTITY column. No change applied.';
    RETURN;
END;

IF EXISTS
(
    SELECT 1
    FROM sys.columns
    WHERE object_id = OBJECT_ID('dbo.tblProviderDropoutIntrigration')
      AND name = 'providerIDropoutIntrigrationd'
      AND default_object_id <> 0
)
BEGIN
    PRINT 'providerIDropoutIntrigrationd already has a default constraint. No change applied.';
    RETURN;
END;

DECLARE @SequenceName SYSNAME = N'Seq_tblProviderDropoutIntrigration_providerIDropoutIntrigrationd';
DECLARE @DefaultConstraintName SYSNAME = N'DF_tblProviderDropoutIntrigration_providerIDropoutIntrigrationd';
DECLARE @NextValue BIGINT;
DECLARE @CreateSequenceSql NVARCHAR(MAX);
DECLARE @AddDefaultSql NVARCHAR(MAX);

SELECT @NextValue = ISNULL(MAX([providerIDropoutIntrigrationd]), 0) + 1
FROM dbo.tblProviderDropoutIntrigration;

IF OBJECT_ID(N'dbo.' + @SequenceName, 'SO') IS NULL
BEGIN
    SET @CreateSequenceSql =
        N'CREATE SEQUENCE dbo.' + QUOTENAME(@SequenceName) + N'
          AS BIGINT
          START WITH ' + CAST(@NextValue AS NVARCHAR(30)) + N'
          INCREMENT BY 1;';

    EXEC sys.sp_executesql @CreateSequenceSql;
END;
ELSE
BEGIN
    DECLARE @RestartSequenceSql NVARCHAR(MAX) =
        N'ALTER SEQUENCE dbo.' + QUOTENAME(@SequenceName) + N'
          RESTART WITH ' + CAST(@NextValue AS NVARCHAR(30)) + N';';

    EXEC sys.sp_executesql @RestartSequenceSql;
END;

SET @AddDefaultSql =
    N'ALTER TABLE dbo.tblProviderDropoutIntrigration
      ADD CONSTRAINT ' + QUOTENAME(@DefaultConstraintName) + N'
      DEFAULT (NEXT VALUE FOR dbo.' + QUOTENAME(@SequenceName) + N') FOR [providerIDropoutIntrigrationd];';

EXEC sys.sp_executesql @AddDefaultSql;

PRINT 'Auto-generated ID support added for dbo.tblProviderDropoutIntrigration.providerIDropoutIntrigrationd.';
