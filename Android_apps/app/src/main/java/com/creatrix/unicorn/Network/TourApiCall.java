package com.creatrix.unicorn.Network;

import com.creatrix.unicorn.Activity.Approval.DWSP.Model.DWSPApprovalData;
import com.creatrix.unicorn.Activity.Approval.TourPlan.Model.MorningEveningTimeModel;
import com.creatrix.unicorn.Activity.Approval.TourPlan.Model.TPApprovalSaveBody;
import com.creatrix.unicorn.Activity.Approval.TourPlan.Model.TimeValidationResponse;
import com.creatrix.unicorn.Activity.Approval.TourPlan.Model.TourPlanApprovalData;
import com.creatrix.unicorn.Activity.DWSP.Model.DWSPApprovalSaveBody;
import com.creatrix.unicorn.Activity.DWSP.Model.ModelDWSPTargetArea;
import com.creatrix.unicorn.Activity.DWSP.ResponseDWSP;
import com.creatrix.unicorn.Activity.Doctor.TourePlan.Model.TourPlanReq;
import com.creatrix.unicorn.Model.CustomerViewModel;
import com.creatrix.unicorn.Model.MarketStructure.StructureTable.Market;
import com.creatrix.unicorn.Model.MarketViewModel;
import com.creatrix.unicorn.Model.ResultInfo;
import com.creatrix.unicorn.Model.ShiftViewModel;
import com.creatrix.unicorn.Model.SubMarket;
import com.creatrix.unicorn.Model.TerritoryViewModel;
import com.creatrix.unicorn.Model.TourPlanMasterViewModel;
import com.creatrix.unicorn.Model.TourPlanPostModel;
import com.creatrix.unicorn.Model.TourPlanViewModel;
import com.creatrix.unicorn.Model.TourPlanWorkedWith;
import com.creatrix.unicorn.Model.TourPurposeViewModel;
import com.creatrix.unicorn.Model.TourTypeViewModel;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;

public interface TourApiCall {

    @Headers({"Accept: application/json"})
    @GET("/api/TourPlan/GetTerritory")
    Call<List<TerritoryViewModel>> GetTerritoryByEmpId(@Query("empId") int empId);

    @GET("/api/TourPlan/GetMarket")
    Call<List<MarketViewModel>> GetMarketByTerritoryId(@Query("territoryId") int territoryId);

    @GET("/api/TourPlan/GetSubmarket")
    Call<List<SubMarket>> GetSubMarketByMarket(@Query("marketId") int marketId);

    @GET("/api/TourPlan/GetCustomer")
    Call<List<CustomerViewModel>> GetCustomerByMarket(@Query("marketId") int marketId);


    @GET("/api/TourPlan/GetShift")
    Call<List<ShiftViewModel>> GetShift(@Query("empId") int empId);

    @GET("/api/TourPlan/GetType")
    Call<List<TourTypeViewModel>> GetType(@Query("empId") int empId);


    @GET("/api/TourPlan/GetPurpose")
    Call<List<TourPurposeViewModel>> GetPurpose(@Query("empId") int empId);

    //Tour plan for list show
   // @GET("/api/TourPlan/GetTourPlanData")
    @GET("/api/TourPlan/GetTourPlanData_new")
    Call<List<TourPlanViewModel>> GetTourPlanData(@Query("month") int month, @Query("year") int year, @Query("empId") int empId);
//old
//    @GET("/api/TourPlan/GetTourPlanDataDetail")
//    Call<List<TourPlanViewModel>> GetTourPlanDataDetail(@Query("month") int month, @Query("year") int year, @Query("empId") int empId,@Query("Date") String date);
//new
    @GET("/api/TourPlan/GetTourPlanDataDetailNew")
    Call<List<TourPlanViewModel>> GetTourPlanDataDetail(@Query("month") int month, @Query("year") int year, @Query("empId") int empId,@Query("Date") String date);

    @POST("/api/TourPlan")
    Call<ResultInfo> SaveTourPlan(@Body TourPlanPostModel aInfo);

    //Old api
   /* @POST("/api/TourPlan/SaveTourPlanList")
    Call<ResultInfo> SaveTourPlanperday(@Body TourPlanReq aTourPlanInfo);*/

////New Api
//    @POST("/api/TourPlan/SaveTourPlanListNew")
//    Call<ResultInfo> SaveTourPlanperday(@Body TourPlanReq aTourPlanInfo);
//New Api
    @POST("/api/TourPlan/SaveTourPlanList_vThree")
    Call<ResultInfo> SaveTourPlanperday(@Body TourPlanReq aTourPlanInfo);

    // Day-wise tour plan (doctor-wise / chamber-wise) - the DCR gate checks this plan
    @POST("/api/TourPlan/SaveDayWiseTourPlan")
    Call<ResultInfo> SaveDayWiseTourPlan(@Body com.creatrix.unicorn.Activity.Doctor.TourePlan.Model.DayWiseTourPlanReq req);

    @GET("/api/TourPlan/GetDayWiseTourPlan")
    Call<List<com.creatrix.unicorn.Activity.Doctor.TourePlan.Model.DayWiseTourPlanRow>> GetDayWiseTourPlan(@Query("empId") int empId, @Query("month") int month, @Query("year") int year);

    //Call<ResultInfo> SaveTourPlanperday(@Body List<TourPlanPostModel> aInfo);
    //Delete tour plan
    @POST("/api/TourPlan/DelTourPlan")
    Call<ResultInfo> DeleteTourPlanData(@Query("id") int id);

    @GET("/api/TourPlan/GetTourPlanMasterData")
    Call<List<TourPlanMasterViewModel>> GetTourPlanMasterData(@Query("month") int month, @Query("year") int year, @Query("empId") int empId);

    @GET("/api/TourPlan/GetTourPlanForWorkedwith")
    Call<TourPlanWorkedWith> GetTourPlanForWorkedwith(@Query("empId") int empId, @Query("tDate") String tDate);


    @GET("/api/TourPlan/GetTourPlanEditbyId")
    Call<TourPlanViewModel> GetTourPlanEditbyId(@Query("TourPlanId") int TourPlanId, @Query("tDate") String tDate);
    @GET("/api/TourPlan/GetOtherMarketVisitListTourPlanEditbyId")
    Call  <List<Market>> GetOtherMarketVisitListTourPlanEditbyId(@Query("TourPlanId") int TourPlanId, @Query("tDate") String tDate);
    @GET("/api/TourPlan/GetTourPlanForWorkedwithCopy")
    Call<TourPlanViewModel> GetTourPlanForWorkedwithCopy(@Query("empId") int empId, @Query("tDate") String tDate, @Query("morEve") String morEve);


    @GET("/api/TourPlan/GetMorningEveningTime")
    Call<MorningEveningTimeModel> GetMorningEveningTime(@Query("ShiftInfo") String ShiftInfo);


    @POST("/api/TourPlan/ValidateTimeInRange")
    Call<TimeValidationResponse> ValidateTimeInRange(@Query("MainTime") String MainTime, @Query("ShiftInfo") String ShiftInfo);

    @GET("/api/TourPlan/UpdateTourPlan")
    Call<ResultInfo> UpdateTourFinalSubmit(@Query("empId") int empId, @Query("month") int month, @Query("year") int year, @Query("remarks") String remarks);


    @GET("/api/Doctor/GetTourPlan_Applog")
    Call<List<TourPlanApprovalData>> GetTPApproval(@Query("pram") String pram, @QueryMap Map<String,String> mapparam);

    @POST("/api/Doctor/SaveTourPlan_Applog")
    Call<ResultInfo> SaveTPApproval(@Body TPApprovalSaveBody approvalSaveBody);

    @GET("/api/TourPlan/GetDWSP_Applog")
    Call<DWSPApprovalData> GetDWSPApproval(@Query("pram") String pram, @QueryMap Map<String,String> mapparam);

    @POST("/api/TourPlan/SavetDWSP_Applog")
    Call<ResultInfo> SaveDWSPApproval(@Body DWSPApprovalSaveBody approvalSaveBody);

    @POST("/api/TourPlan/FinalSubmitDWSP")
    Call<ResultInfo> DWSPFinalSubmit(@Query("empId") int empId, @Query("month") int month, @Query("year") int year,@Query("remarks") String remarks);


    @GET("/api/TourPlan/GetDWSP_Target")
    Call<ResponseDWSP> GetDWSPTargetData(@QueryMap Map<String,String> mapparam);

}
