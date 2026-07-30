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

    @Override
    public String toString() {
        return ChemberName;
    }
}
