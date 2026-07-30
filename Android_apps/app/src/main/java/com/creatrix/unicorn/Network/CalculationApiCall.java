package com.creatrix.unicorn.Network;

import com.creatrix.unicorn.Activity.Approval.DA.ApproveDARQ;
import com.creatrix.unicorn.Activity.Approval.DA.DAListData;
import com.creatrix.unicorn.Activity.DWSP.Model.DWSPDailyModel;
import com.creatrix.unicorn.Activity.DWSP.Model.DWSPTotals;
import com.creatrix.unicorn.Activity.DWSP.Model.SaveDWSP;
import com.creatrix.unicorn.Activity.Expense.Model.ApproveExpRQ;
import com.creatrix.unicorn.Activity.Expense.Model.ExpListTeam;
import com.creatrix.unicorn.Activity.Expense.Report.ExpenseSummeryData;
import com.creatrix.unicorn.Activity.MileageClaim.Model.ApproveMilRQ;
import com.creatrix.unicorn.Activity.MileageClaim.Model.MileageListTeam;
import com.creatrix.unicorn.Model.BonusCampaignViewModel;
import com.creatrix.unicorn.Model.ExpenseClaimMaster;
import com.creatrix.unicorn.Model.ExpenseReportViewModel;
import com.creatrix.unicorn.Model.ExpenseTypeDetails;
import com.creatrix.unicorn.Model.ExpenseTypeMaster;
import com.creatrix.unicorn.Model.MilageClaimReport;
import com.creatrix.unicorn.Model.MileageClaimSM;
import com.creatrix.unicorn.Model.ResultInfo;
import com.creatrix.unicorn.Model.TadaAmount;
import com.creatrix.unicorn.Model.TadaClaimSM;
import com.creatrix.unicorn.Model.TadaList;
import com.creatrix.unicorn.Model.TourDetailForTADA;
import com.creatrix.unicorn.Model.Transport;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;

public interface CalculationApiCall {
    @Headers({"Accept: application/json"})

    @GET("/api/TADA/GetTourPlanForTada")
    Call<List<TourDetailForTADA>> GetTourPlanForTada(@Query("empId") int empId,@Query("tourDate") String  tourDate);

    @POST("/api/TADA/SaveTadaClaim")
    Call<ResultInfo> SaveTadaClaim(@Body TadaClaimSM aInfo);

//Expense claim
// @GET("/api/Calculation/Get_ExpenseType")
// @GET("/api/Calculation/Get_ExpenseTypebyRoleType")
@GET("/api/Calculation/Get_ExpenseTypebyRoleEmp")
Call<List<ExpenseTypeMaster>> Get_ExpenseType(@Query("RoleType") String roleType, @Query("empId") String empId);

    @GET("/api/Calculation/Get_ExpenseTypeDetails")
    Call<List<ExpenseTypeDetails>> Get_ExpenseTypeDetails(@Query("id") int id);

    @POST("/api/Calculation/SaveExpenseClaim")
    Call<ResultInfo> SaveExpenseClaim(@Body ExpenseClaimMaster aInfo);

    @GET("/api/Calculation/Get_ExpenseClaimList")
    Call<List<ExpenseReportViewModel>> Get_ExpenseClaimList(@QueryMap Map<String,String> mapparam);

    @GET("/api/Calculation/ExpenseClaim_del")
    Call<ResultInfo> DelExpenseClaimList(@Query("id") int id);

    @GET("/api/Calculation/GetExpenseClaim_Applog")
    Call<List<ExpListTeam>> GetExpApproval(@Query("pram") String pram, @QueryMap Map<String,String> mapparam);

    @POST("/api/Calculation/SaveExpenseClaim_Applog")
    Call<ResultInfo> SaveExpApproval(@Body ApproveExpRQ approveExpRQ);


    //for report
    @GET("/api/Calculation/GetTadaList")
    Call<List<TadaList>> GetTadaList(@QueryMap Map<String,String> mapparam);

    @GET("/api/Reports/GetExpenseSummery")
    Call<ExpenseSummeryData> GetExpenseSummery(@QueryMap Map<String,String> mapparam);


    @GET("/api/Calculation/GetTADA_Applog")
    Call<List<DAListData>> GetDAApproval(@Query(value = "pram", encoded = true) String pram, @QueryMap Map<String,String> mapparam);

    @POST("/api/Calculation/SaveTADA_Applog")
    Call<ResultInfo> SaveDAApproval(@Body ApproveDARQ approveDARQ);

    // Mileage Claim

    @GET("/api/Calculation/GetTransportList")
    Call<List<Transport>> GetTransportList();

    @POST("/api/Calculation/SaveMileageClaim")
    Call<ResultInfo> SaveMileageClaim(@Body MileageClaimSM aInfo);

    @GET("/api/Calculation/GetMileageCLaimList")
    Call<List<MilageClaimReport>> GetMileageCLaimList(@QueryMap Map<String,String> map);

    @GET("/api/Calculation/GetMileageClaim_Applog")
    Call<List<MileageListTeam>> GetMileageApproval(@Query("pram") String pram, @QueryMap Map<String,String> mapparam);

    @POST("/api/Calculation/SaveMileageClaim_Applog")
    Call<ResultInfo> SaveTeamMileageClaim(@Body ApproveMilRQ aInfo);

    @GET("/api/Calculation/GetBonusCampaign")
    Call<List<BonusCampaignViewModel>> GetBonusCampaign(@Query("customerId") int  customerId, @Query("empId") int empId);


    //DWSP
    @GET("/api/Doctor/GetDWSPDetail")
    Call<List<DWSPDailyModel>> GetDWSP(@Query("month") int month,@Query("year") int yearValue,@Query("empId") int empId);
    @POST("/api/Doctor/SaveDWSP")
    Call<ResultInfo> SaveDWSP(@Body SaveDWSP saveDWSP);
    @GET("/api/Doctor/GetDWSPTotals")
    Call <List<DWSPTotals>> GetDWSPTotals(@Query("month") int month, @Query("year") int yearValue, @Query("empId") int empId);

}
