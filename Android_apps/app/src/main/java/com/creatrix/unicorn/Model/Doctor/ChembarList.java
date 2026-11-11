package com.creatrix.unicorn.Model.Doctor;

public class ChembarList {
    int ChamberTypeId;
    String Name;
    String Shift;
    String SittingTime;
    int InstitutionId;
    String InstitutionName;
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

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
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



    @Override
    public String toString() {
        return Name ;
    }
}
