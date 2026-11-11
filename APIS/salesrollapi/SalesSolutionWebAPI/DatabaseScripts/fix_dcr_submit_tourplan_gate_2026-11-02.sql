/* =====================================================================================
   DCR / CVR submit fix (tour plan gate). Target: SalesDisDB_Unicorn_DB. Re-runnable.
   Only dbo.fn_Webapi_DcrGateMessage changes; no data is updated. No app or API change needed.

   Rules after this script:
     - Doctor DCR : the doctor must be in that day's day-wise plan (Doctor tab; a chamber-wise row
                    also pins the chamber) or in the old monthly plan.
     - Customer CVR: the customer must be in that day's day-wise plan (Customer tab).
     - A doctor and a customer with the same code (DoctorCode = CustomerCode, e.g. Apollo Surgical
       C000309) count as one: planned on either tab that day, both DCR and CVR are allowed.
     - Doctor DCR keeps the 200 m chamber area lock (tblDoctorChemberDetail.isLock = 0 lifts it).

   ROLLBACK: SELECT Definition FROM dbo.tbl_DeployBackup
             WHERE DeployTag = N'DCR_GATE_FIX_2026-11-02' AND ObjectName = N'dbo.fn_Webapi_DcrGateMessage';
             run that text with CREATE changed to CREATE OR ALTER.
   ===================================================================================== */
USE [SalesDisDB_Unicorn_DB]
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

-- The gate reads the day-wise tour plan tables; stop if they are missing.
IF OBJECT_ID(N'dbo.tbl_DayWiseTourPlanMaster', N'U') IS NULL
   OR OBJECT_ID(N'dbo.tbl_DayWiseTourPlanDoctorDetail', N'U') IS NULL
   OR OBJECT_ID(N'dbo.tbl_DayWiseTourPlanCustomerDetail', N'U') IS NULL
   OR COL_LENGTH(N'dbo.tbl_DayWiseTourPlanDoctorDetail', N'ChamberId') IS NULL
BEGIN
    RAISERROR('Day-wise tour plan tables are missing. Run sp_Webapi_Save_DayWiseTourPlan.sql first. Nothing was changed.', 16, 1);
    SET NOEXEC ON;
END
GO

-- Keep the current definition once (re-runs keep the first copy).
IF OBJECT_ID(N'dbo.tbl_DeployBackup', N'U') IS NULL
    CREATE TABLE dbo.tbl_DeployBackup
    (
        BackupId   INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_tbl_DeployBackup PRIMARY KEY,
        DeployTag  NVARCHAR(50)  NOT NULL,
        ObjectName NVARCHAR(256) NOT NULL,
        Definition NVARCHAR(MAX) NULL,
        BackupDate DATETIME      NOT NULL CONSTRAINT DF_tbl_DeployBackup_BackupDate DEFAULT (GETDATE())
    );
GO
INSERT INTO dbo.tbl_DeployBackup (DeployTag, ObjectName, Definition)
SELECT N'DCR_GATE_FIX_2026-11-02', N'dbo.fn_Webapi_DcrGateMessage', OBJECT_DEFINITION(OBJECT_ID(N'dbo.fn_Webapi_DcrGateMessage'))
WHERE NOT EXISTS (SELECT 1 FROM dbo.tbl_DeployBackup
                  WHERE DeployTag = N'DCR_GATE_FIX_2026-11-02' AND ObjectName = N'dbo.fn_Webapi_DcrGateMessage');
GO

-- '' = DCR/CVR allowed, otherwise the reason shown to the user.
CREATE OR ALTER FUNCTION dbo.fn_Webapi_DcrGateMessage
(
    @empId INT,
    @doctorId INT,
    @chamberId INT,
    @dcrDate DATE,
    @Type NVARCHAR(10),   -- 'DCR' or 'CVR'
    @Latitude NVARCHAR(50),
    @Longitude NVARCHAR(50)
)
RETURNS NVARCHAR(500)
AS
BEGIN
    DECLARE @dateText NVARCHAR(20) = CONVERT(NVARCHAR(20), @dcrDate, 106);
    DECLARE @radiusM INT = 200;   -- DCR area lock: allowed distance from the chamber, in metres

    -- CVR: the "chamber" is the customer itself (DoctorId = ChamberId = CustomerMasterId); no area lock,
    -- but the customer must be in that day's tour plan, same as a doctor.
    IF @Type = 'CVR'
    BEGIN
        IF EXISTS (SELECT 1 FROM dbo.tbl_DayWiseTourPlanMaster M WITH (NOLOCK)
                   INNER JOIN dbo.tbl_DayWiseTourPlanCustomerDetail CD WITH (NOLOCK) ON CD.TourPlanMasterId = M.TourPlanMasterId
                   WHERE M.EmpInfoId = @empId AND M.TourPlanDate = @dcrDate AND CD.CustomerMasterId = @doctorId)
            RETURN N'';
        -- Same code planned as a doctor that day (one shop kept as both a customer and a doctor).
        IF EXISTS (SELECT 1 FROM dbo.tblCustMaster C WITH (NOLOCK)
                   INNER JOIN dbo.View_DoctorMaster D WITH (NOLOCK) ON D.DoctorCode = C.CustomerCode
                   INNER JOIN dbo.tbl_DayWiseTourPlanDoctorDetail DD WITH (NOLOCK) ON DD.DoctorId = D.DoctorId
                   INNER JOIN dbo.tbl_DayWiseTourPlanMaster M WITH (NOLOCK) ON M.TourPlanMasterId = DD.TourPlanMasterId
                   WHERE C.CustomerMasterId = @doctorId AND NULLIF(LTRIM(RTRIM(C.CustomerCode)), '') IS NOT NULL
                     AND M.EmpInfoId = @empId AND M.TourPlanDate = @dcrDate)
            RETURN N'';
        RETURN N'This customer is not in your tour plan for ' + @dateText + N'. Add the customer to your day-wise tour plan first.';
    END;

    IF ISNULL(@chamberId, 0) > 0
    BEGIN
        DECLARE @found BIT = 0, @locked BIT, @cLat FLOAT, @cLon FLOAT;
        SELECT @found = 1,
               @locked = ISNULL(isLock, 1),
               @cLat = TRY_CONVERT(FLOAT, NULLIF(LTRIM(RTRIM(Lat)), '')),
               @cLon = TRY_CONVERT(FLOAT, NULLIF(LTRIM(RTRIM(Lon)), ''))
        FROM dbo.tblDoctorChemberDetail WITH (NOLOCK)
        WHERE ChemberId = @chamberId AND DoctorId = @doctorId;

        IF @found = 0
            RETURN N'The selected chamber does not belong to this doctor.';

        -- Locked chamber with a usable saved location (none yet = this DCR sets it, see sp_Webapi_Set_ChamberLocationIfEmpty).
        IF @locked = 1
           AND @cLat BETWEEN -90 AND 90 AND @cLon BETWEEN -180 AND 180 AND NOT (@cLat = 0 AND @cLon = 0)
        BEGIN
            DECLARE @lat FLOAT = TRY_CONVERT(FLOAT, NULLIF(LTRIM(RTRIM(@Latitude)), ''));
            DECLARE @lon FLOAT = TRY_CONVERT(FLOAT, NULLIF(LTRIM(RTRIM(@Longitude)), ''));

            IF @lat IS NULL OR @lon IS NULL OR @lat NOT BETWEEN -90 AND 90 OR @lon NOT BETWEEN -180 AND 180 OR (@lat = 0 AND @lon = 0)
                RETURN N'DCR area lock: your current location was not received. Turn on GPS and try again.';

            DECLARE @distM FLOAT = geography::Point(@cLat, @cLon, 4326).STDistance(geography::Point(@lat, @lon, 4326));
            IF @distM > @radiusM
                RETURN N'DCR area lock: you are ' + CAST(CAST(ROUND(@distM, 0) AS INT) AS NVARCHAR(20))
                     + N' m away from this chamber (allowed ' + CAST(@radiusM AS NVARCHAR(10))
                     + N' m). Go to the chamber, or ask your admin to lift this chamber''s lock.';
        END;
    END;

    -- Day-wise tour plan rows for this doctor decide (chamber-wise when ChamberId is set).
    IF EXISTS (SELECT 1 FROM dbo.tbl_DayWiseTourPlanMaster M WITH (NOLOCK)
               INNER JOIN dbo.tbl_DayWiseTourPlanDoctorDetail DD WITH (NOLOCK) ON DD.TourPlanMasterId = M.TourPlanMasterId
               WHERE M.EmpInfoId = @empId AND M.TourPlanDate = @dcrDate AND DD.DoctorId = @doctorId)
    BEGIN
        IF NOT EXISTS (SELECT 1 FROM dbo.tbl_DayWiseTourPlanMaster M WITH (NOLOCK)
                       INNER JOIN dbo.tbl_DayWiseTourPlanDoctorDetail DD WITH (NOLOCK) ON DD.TourPlanMasterId = M.TourPlanMasterId
                       WHERE M.EmpInfoId = @empId AND M.TourPlanDate = @dcrDate AND DD.DoctorId = @doctorId
                         AND (DD.ChamberId IS NULL OR DD.ChamberId = @chamberId))
            RETURN N'This chamber is not in your tour plan for ' + @dateText + N'. Select the chamber planned for this doctor.';
        RETURN N'';
    END;

    -- Backwards compatibility: the old monthly tour plan.
    IF EXISTS (SELECT 1 FROM dbo.tbl_DoctorTourPlanDetail T WITH (NOLOCK)
               WHERE T.EmpInfoId = @empId AND T.DoctorId = @doctorId
                 AND T.TourPlanDate >= @dcrDate AND T.TourPlanDate < DATEADD(DAY, 1, @dcrDate))
        RETURN N'';

    -- Same code planned as a customer that day (one shop kept as both a customer and a doctor).
    IF EXISTS (SELECT 1 FROM dbo.View_DoctorMaster D WITH (NOLOCK)
               INNER JOIN dbo.tblCustMaster C WITH (NOLOCK) ON C.CustomerCode = D.DoctorCode
               INNER JOIN dbo.tbl_DayWiseTourPlanCustomerDetail CD WITH (NOLOCK) ON CD.CustomerMasterId = C.CustomerMasterId
               INNER JOIN dbo.tbl_DayWiseTourPlanMaster M WITH (NOLOCK) ON M.TourPlanMasterId = CD.TourPlanMasterId
               WHERE D.DoctorId = @doctorId AND NULLIF(LTRIM(RTRIM(D.DoctorCode)), '') IS NOT NULL
                 AND M.EmpInfoId = @empId AND M.TourPlanDate = @dcrDate)
        RETURN N'';

    RETURN N'This doctor is not in your tour plan for ' + @dateText + N'. Add the doctor to your day-wise tour plan first.';
END;
GO

-- Check: Apollo Surgical (C000309) for EMP000009 on 02 Nov 2026. Both columns empty = DCR and CVR will save.
DECLARE @emp INT  = (SELECT EmpInfoId FROM dbo.tblEmpGeneralInfo WHERE EmpMasterCode = 'EMP000009');
DECLARE @cust INT = (SELECT TOP 1 CustomerMasterId FROM dbo.tblCustMaster WHERE CustomerCode = 'C000309');
DECLARE @doc INT  = (SELECT TOP 1 DoctorId FROM dbo.View_DoctorMaster WHERE DoctorCode = 'C000309');
SELECT @emp AS EmpInfoId, @doc AS DoctorId, @cust AS CustomerMasterId,
       dbo.fn_Webapi_DcrGateMessage(@emp, @doc, NULL, '2026-11-02', 'DCR', NULL, NULL) AS DoctorDcrMsg,
       dbo.fn_Webapi_DcrGateMessage(@emp, @cust, @cust, '2026-11-02', 'CVR', NULL, NULL) AS CustomerCvrMsg;
GO
SET NOEXEC OFF;
GO
