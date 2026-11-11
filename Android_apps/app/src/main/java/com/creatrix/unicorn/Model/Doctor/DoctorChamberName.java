package com.creatrix.unicorn.Model.Doctor;

import com.google.gson.annotations.SerializedName;

public class DoctorChamberName {
    int pk;
    
    @SerializedName(value="chemberId", alternate={"ChemberId", "chamberId", "ChamberId"})
    int ChemberId;
    
    @SerializedName(value="chemberName", alternate={"ChemberName", "chamberName", "ChamberName"})
    String ChemberName;
    
    @SerializedName(value="doctorId", alternate={"DoctorId"})
    int DoctorId;

    @SerializedName(value="shift", alternate={"Shift"})
    String Shift;

    @SerializedName(value="sittingTime", alternate={"SittingTime"})
    String SittingTime;

    @SerializedName(value="address", alternate={"Address"})
    String Address;

    @SerializedName(value="institutionId", alternate={"InstitutionId"})
    Integer InstitutionId;

    @SerializedName(value="institutionName", alternate={"InstitutionName"})
    String InstitutionName;

    @SerializedName(value="lat", alternate={"Lat"})
    String Lat;

    @SerializedName(value="lon", alternate={"Lon"})
    String Lon;

    public DoctorChamberName() {
    }

    public int getChemberId() {
        return ChemberId;
    }

    public void setChemberId(int chemberId) {
        ChemberId = chemberId;
    }

    public String getChemberName() {
        return ChemberName;
    }

    public void setChemberName(String chemberName) {
        ChemberName = chemberName;
    }

    public int getDoctorId() {
        return DoctorId;
    }

    public void setDoctorId(int doctorId) {
        DoctorId = doctorId;
    }

    public int getPk() {
        return pk;
    }

    public String getShift() {
        return Shift;
    }

    public void setShift(String shift) {
        Shift = shift;
    }

    public String getSittingTime() {
        return SittingTime;
    }

    public void setSittingTime(String sittingTime) {
        SittingTime = sittingTime;
    }

    public String getAddress() {
        return Address;
    }

    public void setAddress(String address) {
        Address = address;
    }

    public Integer getInstitutionId() {
        return InstitutionId;
    }

    public void setInstitutionId(Integer institutionId) {
        InstitutionId = institutionId;
    }

    public String getInstitutionName() {
        return InstitutionName;
    }

    public void setInstitutionName(String institutionName) {
        InstitutionName = institutionName;
    }

    public String getLat() {
        return Lat;
    }

    public void setLat(String lat) {
        Lat = lat;
    }

    public String getLon() {
        return Lon;
    }

    public void setLon(String lon) {
        Lon = lon;
    }

    // Same rule as the server SP: a chamber with an empty Lat or Lon still needs its location
    public boolean hasLocation() {
        return Lat != null && !Lat.trim().isEmpty() && Lon != null && !Lon.trim().isEmpty();
    }

    public boolean isMorning() {
        return Shift != null && Shift.trim().equalsIgnoreCase("Morning");
    }

    public boolean isEvening() {
        return Shift != null && Shift.trim().equalsIgnoreCase("Evening");
    }

    @Override
    public String toString() {
        return ChemberName;
    }
}
