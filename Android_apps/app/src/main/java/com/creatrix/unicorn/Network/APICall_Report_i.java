package com.creatrix.unicorn.Network;

import com.creatrix.unicorn.Activity.DA.EmpTotalModel;
import com.creatrix.unicorn.Activity.Reports.Model.ModelTourPlanReport;
import com.creatrix.unicorn.Activity.Reports.Model.ResponseBonusGift;
import com.creatrix.unicorn.Activity.SelfReports.TVAReport.Model.ModelTvaData;
import com.creatrix.unicorn.Model.Rp_CampainViewModel;
import com.creatrix.unicorn.Model.Rp_StockViewModel;
import com.creatrix.unicorn.Model.Rp_TargetAcchivment;
import com.creatrix.unicorn.Model.mCompanyUnit;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;

public interface APICall_Report_i {
    @Headers({"Accept: application/json"})
    @GET("/api/Reports/GetProductStock")
    Call<List<Rp_StockViewModel>> GetStockData(@Query("empId") int empId,@Query("type") String type);

    @GET("/api/Reports/GetCampaingData")
    Call<List<Rp_CampainViewModel>> GetCampaingData(@Query("empId") int empId);

    @GET("/api/Reports/GetTargetAchievement")
    Call<List<Rp_TargetAcchivment>> GetTarvsAcchi(@Query("empId") int empId, @Query("month") int month, @Query("year") int year);

    @GET("/api/Reports/GetTargetAchievement")
    Call<List<Rp_TargetAcchivment>> GetTarvsAcchi(@Query("empId") int empId, @Query("month") int month, @Query("year") int year, @Query("selectedEmpId") int selectedEmpId);

    @GET("/api/Reports/GetTerritoryTargetvsAcchive")
    ///Call<List<Rp_TargetAcchivment>> GetTeriTarvsAcchi(@Query("empId") int empId, @Query("FromDate") String FromDate, @Query("ToDate") String ToDate,@Query("Role") String Role);
    Call<ModelTvaData> GetTeriTarvsAch(@QueryMap Map<String,String> map);


    @GET("/api/Reports/GetEmployeeWiseTotalCount")
    Call<EmpTotalModel> GetEmpTotal(@Query("empId") String empId, @Query("Date") String date);

    @GET("/api/CusDemo/GetBonusGiftList")
    Call<List<ResponseBonusGift>> GetBonusgift(@QueryMap Map<String,String> data);

    @GET("/api/CusDemo/GetCollectionList")
    Call<List<ResponseBonusGift>> GetCollection(@QueryMap Map<String,String> data);

    @GET("/api/Reports/GetTourPlanReport")
    Call<List<ModelTourPlanReport>> GetTourPlanReport(@QueryMap Map<String,String> data);
}
