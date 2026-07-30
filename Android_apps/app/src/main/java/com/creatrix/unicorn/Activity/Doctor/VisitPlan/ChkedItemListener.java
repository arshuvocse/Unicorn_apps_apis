package com.creatrix.unicorn.Activity.Doctor.VisitPlan;

import com.creatrix.unicorn.Model.Doctor.DoctorListViewModel;

import java.util.List;

public interface ChkedItemListener {
    void ckdItemName(List<DoctorListViewModel> st,int Pos);
    void unckdItemName(List<DoctorListViewModel> st,int Pos);
}
