package com.creatrix.unicorn.Activity.Attendance.Model;

import com.google.gson.annotations.SerializedName;

public class SaveShiftCategoryRequest {
    @SerializedName("EmpId")
    private int empId;

    @SerializedName("ShiftCategory")
    private String shiftCategory;

    public SaveShiftCategoryRequest(int empId, String shiftCategory) {
        this.empId = empId;
        this.shiftCategory = shiftCategory;
    }

    public int getEmpId() {
        return empId;
    }

    public void setEmpId(int empId) {
        this.empId = empId;
    }

    public String getShiftCategory() {
        return shiftCategory;
    }

    public void setShiftCategory(String shiftCategory) {
        this.shiftCategory = shiftCategory;
    }
}
