package com.creatrix.unicorn.Interface;

import com.creatrix.unicorn.Model.MilageClaimReport;
import com.creatrix.unicorn.Model.MileageClaimSM;
import com.creatrix.unicorn.Model.Transport;

import java.util.List;

public interface IMileageClaim {
    interface Presenter{
        void GetTransportList();
        void SaveMileageClaim(MileageClaimSM aInfo);
        void GetMileageList(int empId);
    }
    interface View{
        void onTransportListGet(List<Transport> aList);
        void onSaveSuccess(String message);
        void onSaveError(String message);
        void onMileageListGet(List<MilageClaimReport> aList);
    }
}
