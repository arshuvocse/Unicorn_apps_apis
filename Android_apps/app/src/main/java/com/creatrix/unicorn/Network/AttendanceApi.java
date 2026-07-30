package com.creatrix.unicorn.Network;

import com.creatrix.unicorn.Activity.Attendance.Model.ApproveRQ;
import com.creatrix.unicorn.Activity.Attendance.Model.AttenApproval;
import com.creatrix.unicorn.Activity.Attendance.Model.ButtonRP;
import com.creatrix.unicorn.Activity.Attendance.Model.ShiftCategory;
import com.creatrix.unicorn.Activity.Attendance.Model.SaveShiftCategoryRequest;
import com.creatrix.unicorn.Model.Attendance;
import com.creatrix.unicorn.Model.ResultInfo;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;

public interface AttendanceApi {
    @GET("/api/Attendance/GetShiftCategoryList")
    Call<List<ShiftCategory>> GetShiftCategoryList(@Query("empId") int empId);

    @POST("/api/Attendance/SaveShiftCategoryList")
    Call<ResultInfo> SaveShiftCategoryList(@Body SaveShiftCategoryRequest request);

    @Headers({"Accept: application/json", "Content-Type: application/json"})
    @POST("/api/Attendance/SavePunch")
    Call<ResultInfo> SavePunInfo(@Body Attendance aInfo);

    @GET("/api/AttendanceApi/GetAttendanceInformation_New")
    Call<List<AttenApproval>> GetAttenInfoNew(@Query("pram") String pram, @QueryMap Map<String,String> mapparam);

    @GET("/api/AttendanceApi/GetAttendanceInformation")
    Call<List<AttenApproval>> GetAttenInfo(@Query("pram") String pram, @Query("emprole") String emprole);

    @POST("/api/AttendanceApi/SaveAttendanceLog")
    Call<ResultInfo> ApproveApi(@Body ApproveRQ approveRQ);

    @POST("/api/Attendance/GetPunchInOutInfo")
    Call<ButtonRP> GetButtonStatus(@Query("EmpInfoId") int EmpInfoId);
}
