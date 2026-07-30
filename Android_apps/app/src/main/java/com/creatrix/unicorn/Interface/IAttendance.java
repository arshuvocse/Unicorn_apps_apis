package com.creatrix.unicorn.Interface;

import android.widget.RelativeLayout;

import com.creatrix.unicorn.Activity.Attendance.Model.ApproveRQ;
import com.creatrix.unicorn.Activity.Attendance.Model.AttenApproval;
import com.creatrix.unicorn.Activity.Attendance.Model.ButtonRP;
import com.creatrix.unicorn.Activity.Attendance.Model.ShiftCategory;
import com.creatrix.unicorn.Model.Attendance;
import com.creatrix.unicorn.Model.ResultInfo;

import java.util.List;
import java.util.Map;

public interface IAttendance {
    interface Presenter {
        void doSavePuncINInfo(Attendance attendance, RelativeLayout masterLayout);
        void teamAttendanceList(String list, String param, String role);
        void teamAttendanceListFilter(String list, String param, Map<String, String> filter);
        void teamAttendanceApprove(ApproveRQ approveRQ);
        void teamAttendanceApproveAll(ApproveRQ approveRQ);
        void button(int empid);
        void getShiftCategoryList(int empId);
        void saveShiftCategory(int empId, String shiftCategory);
    }
    interface View {
        void onSuccessapprove(boolean t);
        void onSuccessapproveAll(String t);
        void onSuccess(String message, boolean status, int type);
        void onSuccessTeamAtten(List<AttenApproval> teamlist);
        void onError(String message, int type);
        void onButtonView(ButtonRP buttonRP);
        void onShiftCategoryListSuccess(List<ShiftCategory> categories);
        void onSaveShiftCategorySuccess(ResultInfo resultInfo);
    }
}
