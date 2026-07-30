CREATE OR ALTER PROCEDURE [dbo].[sp_Webapi_Get_AttendanceInfo]
    @empId INT
AS
BEGIN
    SET NOCOUNT ON;

    DECLARE @Today DATE = CONVERT(DATE, GETDATE());
    DECLARE @CurrentTime TIME(7) = CONVERT(TIME(7), GETDATE());

    IF EXISTS
    (
        SELECT 1
        FROM dbo.tblEmpwiseShiftDeclare sd
        INNER JOIN dbo.tbl_Shift sft ON sd.ShiftId = sft.ShiftId
        WHERE sft.IsActive = 1
          AND sd.EmpInfoId = @empId
    )
    BEGIN
        ;WITH ShiftStatus AS
        (
            SELECT
                sd.EmpInfoId,
                sd.ShiftId,
                sft.ShiftInTime,
                sft.ShiftOutTime,
                sft.ShiftText,
                CASE WHEN EXISTS
                (
                    SELECT 1
                    FROM dbo.tblMarketAttendance_Master_webapi att WITH (NOLOCK)
                    WHERE att.EmpInfoId = sd.EmpInfoId
                      AND att.ShiftId = sd.ShiftId
                      AND att.AttType = 1
                      AND CONVERT(DATE, att.AttendanceDate) = @Today
                ) THEN 1 ELSE 0 END AS HasPunchIn,
                CASE WHEN EXISTS
                (
                    SELECT 1
                    FROM dbo.tblMarketAttendance_Master_webapi att WITH (NOLOCK)
                    WHERE att.EmpInfoId = sd.EmpInfoId
                      AND att.ShiftId = sd.ShiftId
                      AND att.AttType = 2
                      AND CONVERT(DATE, att.AttendanceDate) = @Today
                ) THEN 1 ELSE 0 END AS HasPunchOut
            FROM dbo.tblEmpwiseShiftDeclare sd
            INNER JOIN dbo.tbl_Shift sft ON sd.ShiftId = sft.ShiftId
            WHERE sft.IsActive = 1
              AND sd.EmpInfoId = @empId
        ),
        SelectedShift AS
        (
            SELECT TOP (1)
                EmpInfoId,
                ShiftId,
                ShiftInTime,
                ShiftOutTime,
                ShiftText,
                HasPunchIn,
                HasPunchOut
            FROM ShiftStatus
            ORDER BY
                CASE
                    WHEN HasPunchIn = 1 AND HasPunchOut = 0 THEN 0
                    WHEN HasPunchIn = 0
                         AND
                         (
                             (ShiftInTime <= ShiftOutTime
                              AND @CurrentTime >= ShiftInTime
                              AND @CurrentTime <= ShiftOutTime)
                             OR
                             (ShiftInTime > ShiftOutTime
                              AND (@CurrentTime >= ShiftInTime
                                   OR @CurrentTime <= ShiftOutTime))
                         ) THEN 1
                    WHEN HasPunchIn = 0 THEN 2
                    ELSE 3
                END,
                CASE
                    WHEN HasPunchIn = 1 AND HasPunchOut = 0 THEN ShiftInTime
                    ELSE NULL
                END,
                CASE
                    WHEN HasPunchIn = 0 THEN ShiftInTime
                    ELSE NULL
                END,
                CASE
                    WHEN HasPunchIn = 1 AND HasPunchOut = 1 THEN ShiftInTime
                    ELSE NULL
                END DESC,
                ShiftOutTime,
                ShiftId
        )
        SELECT
            CASE
                WHEN HasPunchIn = 0 THEN 'ON'
                ELSE 'OFF'
            END AS PunchInBtn,
            CASE
                WHEN HasPunchIn = 1 AND HasPunchOut = 0 THEN 'ON'
                ELSE 'OFF'
            END AS PunchOUTBtn,
            ShiftId,
            CONVERT(VARCHAR(8), ShiftInTime, 108) AS ShiftInTime,
            CONVERT(VARCHAR(8), ShiftOutTime, 108) AS ShiftOutTime,
            ShiftText
        FROM SelectedShift;

        RETURN;
    END;

    -- Backward-compatible behavior for employees without shift declarations.
    SELECT
        CASE WHEN punchIn.AttType = 1 THEN 'OFF' ELSE 'ON' END AS PunchInBtn,
        CASE
            WHEN punchIn.AttType = 1 AND punchOut.AttType IS NULL THEN 'ON'
            ELSE 'OFF'
        END AS PunchOUTBtn,
        emp.ShiftId,
        CONVERT(VARCHAR(8), sft.ShiftInTime, 108) AS ShiftInTime,
        CONVERT(VARCHAR(8), sft.ShiftOutTime, 108) AS ShiftOutTime,
        sft.ShiftText
    FROM dbo.tblEmpGeneralInfo emp WITH (NOLOCK)
    LEFT JOIN dbo.tbl_Shift sft WITH (NOLOCK)
        ON sft.ShiftId = emp.ShiftId
    OUTER APPLY
    (
        SELECT TOP (1) att.AttType
        FROM dbo.tblMarketAttendance_Master_webapi att WITH (NOLOCK)
        WHERE att.EmpInfoId = @empId
          AND att.AttType = 1
          AND CONVERT(DATE, att.AttendanceDate) = @Today
    ) punchIn
    OUTER APPLY
    (
        SELECT TOP (1) att.AttType
        FROM dbo.tblMarketAttendance_Master_webapi att WITH (NOLOCK)
        WHERE att.EmpInfoId = @empId
          AND att.AttType = 2
          AND CONVERT(DATE, att.AttendanceDate) = @Today
    ) punchOut
    WHERE emp.EmpInfoId = @empId;
END;
