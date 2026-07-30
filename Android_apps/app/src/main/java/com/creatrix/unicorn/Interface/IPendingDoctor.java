package com.creatrix.unicorn.Interface;

import com.creatrix.unicorn.Activity.Customer.Pending.CustomerARModel;
import com.creatrix.unicorn.Activity.Doctor.Pending.DoctorARModel;
import com.creatrix.unicorn.Model.Customer;

import java.util.List;

public interface IPendingDoctor {
    interface Presenter{
        void GetDoctorReport(int empId);
    }

    interface View{
        void OnSuccessDoctorReport(List<DoctorARModel> aList);
        void  OnErrorReport(String msg);
    }
}
