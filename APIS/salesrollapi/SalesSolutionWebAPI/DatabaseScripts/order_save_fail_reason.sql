USE [SalesDisDB_Unicorn_DB]
GO
/*
  Why did sp_webapi_SaveOrderMaster return no OrderId? It just returns nothing for its guards, and the app
  then showed "The order could not be placed. Please review the products/campaign". The order API calls this
  only after such a failed save, to tell the user the real reason.
  Guards checked (same as sp_webapi_SaveOrderMaster): same customer ordered in the last 5 minutes (double tap /
  retry after a slow network: the first order WAS saved), inactive user/employee, unknown customer.
  Read-only. Re-runnable. Columns are checked with COL_LENGTH first, so a schema difference never breaks it.
*/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE OR ALTER PROCEDURE dbo.sp_webapi_Get_OrderSaveFailReason
    @empId INT,
    @CustomerCode NVARCHAR(50)
AS
BEGIN
    SET NOCOUNT ON;
    DECLARE @orderCode NVARCHAR(100) = NULL, @found BIT = 0;

    IF COL_LENGTH('dbo.tblOrder', 'ServerDateTime') IS NOT NULL AND COL_LENGTH('dbo.tblOrder', 'OrderCode') IS NOT NULL
        EXEC sp_executesql
            N'SELECT TOP (1) @code = OrderCode FROM dbo.tblOrder WITH (NOLOCK)
              WHERE CustomerCode = @cc AND ServerDateTime >= DATEADD(MINUTE, -6, GETDATE())
              ORDER BY OrderId DESC',
            N'@cc NVARCHAR(50), @code NVARCHAR(100) OUTPUT', @cc = @CustomerCode, @code = @orderCode OUTPUT;
    IF @orderCode IS NOT NULL
    BEGIN
        SELECT N'This customer''s order was already submitted a few minutes ago (' + @orderCode
             + N'). Please check your order list; do not submit it again.' AS Reason;
        RETURN;
    END

    IF COL_LENGTH('dbo.tblUser', 'UserStatus') IS NOT NULL AND COL_LENGTH('dbo.tblEmpGeneralInfo', 'EmployeeStatus') IS NOT NULL
    BEGIN
        EXEC sp_executesql
            N'IF EXISTS (SELECT 1 FROM dbo.tblUser u WITH (NOLOCK)
                         INNER JOIN dbo.tblEmpGeneralInfo e WITH (NOLOCK) ON e.EmpInfoId = u.EmpInfoId
                         WHERE u.EmpInfoId = @emp AND u.UserStatus = ''Active'' AND e.EmployeeStatus = ''Active'')
                  SET @ok = 1',
            N'@emp INT, @ok BIT OUTPUT', @emp = @empId, @ok = @found OUTPUT;
        IF @found = 0
        BEGIN
            SELECT N'Your user/employee account is not active. Please contact the Admin department.' AS Reason;
            RETURN;
        END
    END

    IF NOT EXISTS (SELECT 1 FROM dbo.tblCustMaster WITH (NOLOCK) WHERE CustomerCode = @CustomerCode)
    BEGIN
        SELECT N'Customer ' + ISNULL(@CustomerCode, N'') + N' was not found. Please sync customers and try again.' AS Reason;
        RETURN;
    END

    SELECT CAST(NULL AS NVARCHAR(500)) AS Reason; -- none of the known guards: the API keeps its generic message
END
GO
