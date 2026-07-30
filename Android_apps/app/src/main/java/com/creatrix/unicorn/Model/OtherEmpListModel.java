package com.creatrix.unicorn.Model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class OtherEmpListModel {
    @SerializedName("EmpInfoId")
    @Expose
    private int EmpInfoId;

    @SerializedName("EmpMasterCode")
    @Expose
    private String EmpMasterCode;

    public int getEmpInfoId() {
        return EmpInfoId;
    }

    public void setEmpInfoId(int empInfoId) {
        EmpInfoId = empInfoId;
    }

    public String getEmpMasterCode() {
        return EmpMasterCode;
    }

    public void setEmpMasterCode(String empMasterCode) {
        EmpMasterCode = empMasterCode;
    }
}
