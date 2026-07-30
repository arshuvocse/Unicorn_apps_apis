package com.creatrix.unicorn.Interface;


import com.creatrix.unicorn.Activity.Approval.DWSP.Model.ADWSPDate;
import com.creatrix.unicorn.Activity.Approval.DWSP.Model.DWSPApprovalData;
import com.creatrix.unicorn.Activity.Approval.TourPlan.Model.TPApprovalSaveBody;
import com.creatrix.unicorn.Activity.Approval.TourPlan.Model.TourPlanApprovalData;
import com.creatrix.unicorn.Activity.DWSP.Model.DWSPApprovalSaveBody;

import java.util.List;
import java.util.Map;

public interface IDWSPApproval {
    interface Presenter{
        void getDWSPApprovalList(String pram, Map<String,String> mapparam);
        void SaveDWSPApproval(DWSPApprovalSaveBody aInfo);
    }
    interface  View{
        void onGetDWSPApprovalList(DWSPApprovalData aData);
        void onSaveSuccess(String message);
        void onError(String message);
    }
}