package com.creatrix.unicorn.Interface;

import com.creatrix.unicorn.Activity.Doctor.DCR.NonEffectiveReason;
import com.creatrix.unicorn.Model.DcrSM;
import com.creatrix.unicorn.Model.Doctor.DoctorBrand;
import com.creatrix.unicorn.Model.Doctor.DoctorChamberName;
import com.creatrix.unicorn.Model.Doctor.DoctorChamberTypeVM;
import com.creatrix.unicorn.Model.Gift;
import com.creatrix.unicorn.Model.Product;
import com.creatrix.unicorn.Model.TourTypeViewModel;
import com.creatrix.unicorn.Model.UserByRole;
import com.creatrix.unicorn.Model.UserRole;

import java.util.List;

public interface IDCR {
    interface Presenter{
        void GetVisitType();
        void GetChamber(int doctorId);
        void GetCustomerChamber(int doctorId);
        void GetGiftProduct(String empId);
        void GetBrand();
        void SaveDCR(DcrSM aInfo);
        void GetUserRole();
        void GetUserByRoleId(int roleid/*,int empId*/);
        void GetNoneffective();
        void SaveChamberLocation(DoctorChamberName chamber, String lat, String lon);
    }

    interface View{
        void OnVisitTypeGet(List<TourTypeViewModel> aInfo);
       // void OnChamberGet(List<DoctorChamberTypeVM> aInfo);
        void OnChamberGet(List<DoctorChamberName> aInfo);
        void OnGiftProductGet(List<Gift> aInfo);
        void OnDoctorBrandGet(List<DoctorBrand> aInfo);
        void OnDcrSaveSuccess(String message);
        void OnDcrSaveError(String message);
        void onUserRoleGet(List<UserRole> aList);
        void onUserGet(List<UserByRole> aList);
        void onNoneffectiveGet(List<NonEffectiveReason> aList);
        void OnChamberLocationSaved(DoctorChamberName chamber, String lat, String lon);
        void OnChamberLocationError(String message);
    }
}
