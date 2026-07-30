package com.creatrix.unicorn.Interface;

import com.creatrix.unicorn.Activity.Doctor.TourePlan.Model.TPDetailsModel;
import com.creatrix.unicorn.Activity.Doctor.TourePlan.Model.TourPlanReq;
import com.creatrix.unicorn.Model.Attendance;
import com.creatrix.unicorn.Model.MonthDate;
import com.creatrix.unicorn.Model.TerritoryViewModel;
import com.creatrix.unicorn.Model.TourPlanViewModel;

import java.util.List;

public interface ITourplan {
    interface  Presenter{
        void getTourPlanDailyByEmpId(int month, int year, int empId,String date);
        void getTourPlanDataByEmpId(List<MonthDate> aMondateList,boolean is_Entry, int month, int year, int empId);
        void saveTourPlanPerdayByEmpId(TourPlanReq tppmList);

    }
    interface View{
        //void OnTourPlanDataGet(List<TPDetailsModel> aList);
        void OnTourPlanDataGet(List<TourPlanViewModel> aList);
        void OnTourPlanDailyDataGet(List<MonthDate> aList);
        void OnArreangList(List<MonthDate> aMondateList,boolean is_Entry, List<TourPlanViewModel> aTpLIst);
        void OnFailour(String msg);
        void OnSuccessTPPDay(String msg);

    }
}
