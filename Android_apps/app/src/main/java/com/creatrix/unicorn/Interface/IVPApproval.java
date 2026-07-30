package com.creatrix.unicorn.Interface;


import com.creatrix.unicorn.Activity.Approval.TourPlan.Model.TPApprovalSaveBody;
import com.creatrix.unicorn.Activity.Approval.TourPlan.Model.TourPlanApprovalData;
import com.creatrix.unicorn.Activity.Approval.VisitPlan.Model.VPApprovalSaveBody;
import com.creatrix.unicorn.Activity.Approval.VisitPlan.Model.VisitPlanApprovalData;

import java.util.List;
import java.util.Map;

public interface IVPApproval {
    interface Presenter{
        void getVPApprovalList(String pram, Map<String,String> mapparam);
        void SaveVPApproval(VPApprovalSaveBody aInfo);
    }
    interface  View{
        void onGetVPApprovalList(List<VisitPlanApprovalData> aList);
        void onSaveSuccess(String message);
        void onError(String message);
    }
}