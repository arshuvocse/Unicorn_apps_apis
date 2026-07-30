package com.creatrix.unicorn.Interface;

import com.creatrix.unicorn.Model.ExpenseClaimMaster;
import com.creatrix.unicorn.Model.ExpenseTypeDetails;
import com.creatrix.unicorn.Model.ExpenseTypeMaster;

import java.util.List;

public interface IExpenseClaim {
    interface  Presenter{
        void GetExpenseType(String roleType, String empId);
        void GetExpenseDetails(int id);
        void SaveExpenseClaim(ExpenseClaimMaster aInfo);


    }
    interface View{
        void onExpenseTypeGet(List<ExpenseTypeMaster> aList);
        void onExpenseTypeDetailsGet(List<ExpenseTypeDetails> aList);
        void onSaveSuccess(String message);
        void onSaveError(String message);
    }
}
