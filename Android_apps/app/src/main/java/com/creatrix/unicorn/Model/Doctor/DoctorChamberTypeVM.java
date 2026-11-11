package com.creatrix.unicorn.Model.Doctor;

public class DoctorChamberTypeVM {
    int pk;
    int ChamberTypeId;
    String ChamberTypeName;
    String ChamberName;
    String Shift;
    String SittingTime;
    int InstitutionId;
    String InstitutionName;
    String Address;

    public int getPk() {
        return pk;
    }

    public void setPk(int pk) {
        this.pk = pk;
    }

    public DoctorChamberTypeVM(int chamberTypeId, String chamberTypeName, String chamberName) {
        ChamberTypeId = chamberTypeId;
        ChamberTypeName = chamberTypeName;
        ChamberName = chamberName;
    }

    public DoctorChamberTypeVM(int chamberTypeId, String chamberTypeName, String chamberName, String shift, String sittingTime) {
        ChamberTypeId = chamberTypeId;
        ChamberTypeName = chamberTypeName;
        ChamberName = chamberName;
        Shift = shift;
        SittingTime = sittingTime;
    }

    public DoctorChamberTypeVM(int chamberTypeId, String chamberTypeName, String chamberName, String shift, String sittingTime, String address) {
        ChamberTypeId = chamberTypeId;
        ChamberTypeName = chamberTypeName;
        ChamberName = chamberName;
        Shift = shift;
        SittingTime = sittingTime;
        Address = address;
    }

    public DoctorChamberTypeVM(int chamberTypeId, String chamberTypeName, String chamberName, String shift, String sittingTime, int institutionId, String institutionName) {
        ChamberTypeId = chamberTypeId;
        ChamberTypeName = chamberTypeName;
        ChamberName = chamberName;
        Shift = shift;
        SittingTime = sittingTime;
        InstitutionId = institutionId;
        InstitutionName = institutionName;
    }

    public DoctorChamberTypeVM(int chamberTypeId, String chamberTypeName, String chamberName, String shift, String sittingTime, int institutionId, String institutionName, String address) {
        ChamberTypeId = chamberTypeId;
        ChamberTypeName = chamberTypeName;
        ChamberName = chamberName;
        Shift = shift;
        SittingTime = sittingTime;
        InstitutionId = institutionId;
        InstitutionName = institutionName;
        Address = address;
    }

    public DoctorChamberTypeVM() {
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

    public int getInstitutionId() {
        return InstitutionId;
    }

    public void setInstitutionId(int institutionId) {
        InstitutionId = institutionId;
    }

    public String getInstitutionName() {
        return InstitutionName;
    }

    public void setInstitutionName(String institutionName) {
        InstitutionName = institutionName;
    }

    public String getAddress() {
        return Address;
    }

    public void setAddress(String address) {
        Address = address;
    }
/* public int getChamberTypeId() {
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
    }*/

    @Override
    public String toString() {
        return ChamberTypeName;
    }
}

