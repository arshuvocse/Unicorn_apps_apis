package com.creatrix.unicorn.Interface;
import com.creatrix.unicorn.Activity.Approval.Order.OrderApprovalData;
import com.creatrix.unicorn.Activity.Approval.Order.OrderApprovalSaveBody;
import com.creatrix.unicorn.Model.ExpenseClaimMaster;

import java.util.List;
import java.util.Map;



public interface IOrderApproval {
    interface  Presenter{
        void getOrderApprovalList(String pram,Map<String,String> mapparam);
        void SaveOrderApproval(OrderApprovalSaveBody aInfo);
    }
    interface  View{
        void onGetOrderSuccess(List<OrderApprovalData> aList);
        void onSaveSuccess(String message);
        void onError(String message);
    }
}
