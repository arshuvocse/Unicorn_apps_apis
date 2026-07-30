package com.creatrix.unicorn.Interface;

import com.creatrix.unicorn.Activity.Doctor.TourePlan.Model.TourPlanReq;
import com.creatrix.unicorn.Activity.Doctor.VisitPlan.Model.VisitplanModel;
import com.creatrix.unicorn.Model.MonthDate;
import com.creatrix.unicorn.Model.TourPlanViewModel;

import java.util.List;

public interface IVisitPlan {
    interface  Presenter{
        void getVisitPlanDataByEmpId(List<MonthDate> aMondateList,boolean is_Entry,int month, int year, int empId);
        void saveVisitPlanPerdayByEmpId(List<VisitplanModel> vpl);

    }
    interface View{
       // void OnVisitPlanDataGet(List<TourPlanViewModel> aList);
        void OnArreangList(List<MonthDate> aMondateList,boolean is_Entry, List<VisitplanModel> aTpLIst);
        void OnFailour(String msg);
        void OnSuccessVPPDay(String msg);

    }
}
