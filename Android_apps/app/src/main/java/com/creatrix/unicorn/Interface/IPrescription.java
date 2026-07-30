package com.creatrix.unicorn.Interface;

import com.creatrix.unicorn.Model.Doctor.DoctorChamberName;
import com.creatrix.unicorn.Model.PrescriptionSM;
import com.creatrix.unicorn.Model.PrescriptionTYpe;
import com.creatrix.unicorn.Model.Product;

import java.util.List;

public interface IPrescription {
    interface Presenter{
        void GetPrescriptionType(int empId);
        void GetProducts(int empId);
        void GetChamberId(int docId);
        void SavePrescription(PrescriptionSM aPres);
        void SubmitPrescription(PrescriptionSM aPres);

    }
    interface View{
        void onPrescriptionTypeGet(List<PrescriptionTYpe> aList);
        void onProductGet(List<Product> aList);
        void onChamberGet(List<DoctorChamberName> cList);
        void onSaveSuccess(String message);
        void onSaveError(String message);
    }
}
