package com.creatrix.unicorn.Interface;

import com.creatrix.unicorn.Model.Dashboard_SummeryVM;
import com.creatrix.unicorn.Model.Notice;

import java.util.List;

public interface IMioDashboard {

    interface  Presenter{
        void getDashboardSummeryData(int empId,String currentDate);
        void getTodaySummery(int empId,String currentDate);

    }
    interface  View{
        void onDashboardSummeryDataBind(Dashboard_SummeryVM aData);
        void onTodaySummeryDataBind(Dashboard_SummeryVM aData);
        void onError(String message);
    }
}
