package com.creatrix.unicorn.Activity.Attendance.Model;

import com.google.gson.annotations.SerializedName;

public class ShiftCategory {
    @SerializedName("ShiftCategory")
    private String shiftCategory;

    @SerializedName("ModalShow")
    private String modalShow;

    public String getShiftCategory() {
        return shiftCategory;
    }

    public void setShiftCategory(String shiftCategory) {
        this.shiftCategory = shiftCategory;
    }

    public String getModalShow() {
        return modalShow;
    }

    public void setModalShow(String modalShow) {
        this.modalShow = modalShow;
    }
}
