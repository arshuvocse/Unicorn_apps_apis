package com.creatrix.unicorn.Model;

public class Report_AttendanceViewModel {

    private int AttendanceId;
    private String EmpName;

    public String getEmpName() {
        return EmpName;
    }
    private String AttendanceDate;
    private String AttendanceType;
    private String AttendanceStatus;
    private String AttendanceText;
    private String AttendanceTime;

    private String PunchInShiftName;
    private String PunchInTime;
    private String PInLat;
    private String PInLog;
    private String PInAttAddress;

    private String PunchOutShiftName;
    private String PunchOutTime;
    private String POutLat;
    private String POutLong;
    private String POutAttAddress;

    public int getAttendanceId() {
        return AttendanceId;
    }

    public void setAttendanceId(int attendanceId) {
        AttendanceId = attendanceId;
    }

    public String getAttendanceDate() {
        return AttendanceDate;
    }

    public void setAttendanceDate(String attendanceDate) {
        AttendanceDate = attendanceDate;
    }

    public String getAttendanceType() {
        return AttendanceType;
    }

    public void setAttendanceType(String attendanceType) {
        AttendanceType = attendanceType;
    }

    public String getAttendanceStatus() {
        return AttendanceStatus;
    }

    public void setAttendanceStatus(String attendanceStatus) {
        AttendanceStatus = attendanceStatus;
    }

    public String getAttendanceText() {
        return AttendanceText;
    }

    public void setAttendanceText(String attendanceText) {
        AttendanceText = attendanceText;
    }

    public String getAttendanceTime() {
        return AttendanceTime;
    }

    public void setAttendanceTime(String attendanceTime) {
        AttendanceTime = attendanceTime;
    }

    public String getPunchInShiftName() {
        return PunchInShiftName;
    }

    public void setPunchInShiftName(String punchInShiftName) {
        PunchInShiftName = punchInShiftName;
    }

    public String getPunchInTime() {
        return PunchInTime;
    }

    public void setPunchInTime(String punchInTime) {
        PunchInTime = punchInTime;
    }

    public String getPInLat() {
        return PInLat;
    }

    public void setPInLat(String PInLat) {
        this.PInLat = PInLat;
    }

    public String getPInLog() {
        return PInLog;
    }

    public void setPInLog(String PInLog) {
        this.PInLog = PInLog;
    }

    public String getPInAttAddress() {
        return PInAttAddress;
    }

    public void setPInAttAddress(String PInAttAddress) {
        this.PInAttAddress = PInAttAddress;
    }

    public String getPunchOutShiftName() {
        return PunchOutShiftName;
    }

    public void setPunchOutShiftName(String punchOutShiftName) {
        PunchOutShiftName = punchOutShiftName;
    }

    public String getPunchOutTime() {
        return PunchOutTime;
    }

    public void setPunchOutTime(String punchOutTime) {
        PunchOutTime = punchOutTime;
    }

    public String getPOutLat() {
        return POutLat;
    }

    public void setPOutLat(String POutLat) {
        this.POutLat = POutLat;
    }

    public String getPOutLong() {
        return POutLong;
    }

    public void setPOutLong(String POutLong) {
        this.POutLong = POutLong;
    }

    public String getPOutAttAddress() {
        return POutAttAddress;
    }

    public void setPOutAttAddress(String POutAttAddress) {
        this.POutAttAddress = POutAttAddress;
    }
}
