package com.creatrix.unicorn.Activity.Doctor.Approval.Model;

import androidx.annotation.Keep;

@Keep
public class ChembarTypeWithName {
    int ChamberTypeId;
    String ChamberTypeName;
    String ChamberName;
    String Shift;
    String SittingTime;
    String Address;

    public String getAddress() {
        return Address;
    }

    public void setAddress(String address) {
        Address = address;
    }

    public int getChamberTypeId() {
        return ChamberTypeId;
    }

    public void setChamberTypeId(int chamberTypeId) {
        ChamberTypeId = chamberTypeId;
    }

    public String getChamberTypeName() {
        return ChamberTypeName;
    }

    public void setChamberTypeName(String chamberTypeName) {
        ChamberTypeName = chamberTypeName;
    }

    public String getChamberName() {
        return ChamberName;
    }

    public void setChamberName(String chamberName) {
        ChamberName = chamberName;
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
}
