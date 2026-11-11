-- Attendance punch-in time restriction (per employee). Re-runnable.
-- Employees with no active row are unrestricted. Removing a restriction = IsActive 0.
IF OBJECT_ID('dbo.tbl_AttendancePunchWindow', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.tbl_AttendancePunchWindow
    (
        Id          INT IDENTITY(1,1) PRIMARY KEY,
        EmpInfoId   INT      NOT NULL,
        PunchInFrom TIME(0)  NOT NULL,
        PunchInTo   TIME(0)  NOT NULL,
        IsActive    BIT      NOT NULL DEFAULT 1,
        EntryBy     INT      NULL,
        EntryDate   DATETIME NOT NULL DEFAULT GETDATE(),
        UpdateBy    INT      NULL,
        UpdateDate  DATETIME NULL
    );
    CREATE UNIQUE INDEX UX_tbl_AttendancePunchWindow_EmpInfoId ON dbo.tbl_AttendancePunchWindow (EmpInfoId);
END
GO

-- Returns one row: IsRestricted 0 = no limit; IsAllowedNow evaluated on server time.
CREATE OR ALTER PROCEDURE dbo.sp_Webapi_Get_AttendancePunchWindow
    @empId INT
AS
BEGIN
    SET NOCOUNT ON;
    -- Truncate to the minute so the end time includes its whole last minute (same as the app check).
    DECLARE @Now TIME(0) = CAST(CONVERT(VARCHAR(5), GETDATE(), 108) AS TIME(0));

    SELECT
        @empId AS EmpInfoId,
        CONVERT(VARCHAR(5), w.PunchInFrom, 108) AS PunchInFrom,
        CONVERT(VARCHAR(5), w.PunchInTo, 108) AS PunchInTo,
        CAST(CASE WHEN w.EmpInfoId IS NULL THEN 0 ELSE 1 END AS BIT) AS IsRestricted,
        CAST(CASE
            WHEN w.EmpInfoId IS NULL THEN 1
            WHEN w.PunchInFrom <= w.PunchInTo AND @Now BETWEEN w.PunchInFrom AND w.PunchInTo THEN 1
            WHEN w.PunchInFrom > w.PunchInTo AND (@Now >= w.PunchInFrom OR @Now <= w.PunchInTo) THEN 1 -- overnight window
            ELSE 0 END AS BIT) AS IsAllowedNow
    FROM (SELECT 1 AS x) d
    LEFT JOIN dbo.tbl_AttendancePunchWindow w ON w.EmpInfoId = @empId AND w.IsActive = 1;
END
GO

CREATE OR ALTER PROCEDURE dbo.sp_Webapi_Save_AttendancePunchWindow
    @empId INT,
    @PunchInFrom TIME(0),
    @PunchInTo TIME(0),
    @EntryBy INT = NULL
AS
BEGIN
    SET NOCOUNT ON;
    IF EXISTS (SELECT 1 FROM dbo.tbl_AttendancePunchWindow WHERE EmpInfoId = @empId)
        UPDATE dbo.tbl_AttendancePunchWindow
        SET PunchInFrom = @PunchInFrom, PunchInTo = @PunchInTo, IsActive = 1,
            UpdateBy = @EntryBy, UpdateDate = GETDATE()
        WHERE EmpInfoId = @empId;
    ELSE
        INSERT INTO dbo.tbl_AttendancePunchWindow (EmpInfoId, PunchInFrom, PunchInTo, IsActive, EntryBy, EntryDate)
        VALUES (@empId, @PunchInFrom, @PunchInTo, 1, @EntryBy, GETDATE());
END
GO

CREATE OR ALTER PROCEDURE dbo.sp_Webapi_Remove_AttendancePunchWindow
    @empId INT,
    @EntryBy INT = NULL
AS
BEGIN
    SET NOCOUNT ON;
    UPDATE dbo.tbl_AttendancePunchWindow
    SET IsActive = 0, UpdateBy = @EntryBy, UpdateDate = GETDATE()
    WHERE EmpInfoId = @empId AND IsActive = 1;
END
GO
