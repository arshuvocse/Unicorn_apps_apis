package com.creatrix.unicorn.Activity.Attendance.Model;

public class ButtonRP {
    String PunchInBtn;
    String PunchOUTBtn;
    Integer ShiftId;
    String ShiftInTime;
    String ShiftOutTime;
    String ShiftText;

    public String getPunchInBtn() {
        return PunchInBtn;
    }

    public void setPunchInBtn(String punchInBtn) {
        PunchInBtn = punchInBtn;
    }

    public String getPunchOUTBtn() {
        return PunchOUTBtn;
    }

    public void setPunchOUTBtn(String punchOUTBtn) {
        PunchOUTBtn = punchOUTBtn;
    }

    public Integer getShiftId() {
        return ShiftId;
    }

    public void setShiftId(Integer shiftId) {
        ShiftId = shiftId;
    }

    public String getShiftInTime() {
        return ShiftInTime;
    }

    public void setShiftInTime(String shiftInTime) {
        ShiftInTime = shiftInTime;
    }

    public String getShiftOutTime() {
        return ShiftOutTime;
    }

    public void setShiftOutTime(String shiftOutTime) {
        ShiftOutTime = shiftOutTime;
    }

    public String getShiftText() {
        return ShiftText;
    }

    public void setShiftText(String shiftText) {
        ShiftText = shiftText;
    }
}
