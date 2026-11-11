package com.creatrix.unicorn.Activity.Reports;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.creatrix.unicorn.Activity.Reports.Adapter.AdapterTourPlanReport;
import com.creatrix.unicorn.Activity.Reports.Model.ModelTourPlanReport;
import com.creatrix.unicorn.Network.APICall_Report_i;
import com.creatrix.unicorn.Network.RetrofitClientInstance;
import com.creatrix.unicorn.R;
import com.creatrix.unicorn.UtilityHelper.ReportFilterHelper;
import com.creatrix.unicorn.UtilityHelper.SessionManagement;
import com.creatrix.unicorn.UtilityHelper.SnackBarManagement;
import com.creatrix.unicorn.UtilityHelper.UtilityHelper;
import com.creatrix.unicorn.databinding.ActivityReportsDcrBinding;
import com.creatrix.unicorn.databinding.FilterMasterBinding;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TourPlanReportActivity extends AppCompatActivity {
    ActivityReportsDcrBinding binding;
    BottomSheetDialog bsheetdlg;
    ProgressDialog pd;
    String empId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // same screen layout as the DCR report; only the title differs
        binding = ActivityReportsDcrBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.toolbarTitle.setText(R.string.tour_plan_report);
        binding.toolbarCustom.setNavigationOnClickListener(v -> finish());
        pd = new ProgressDialog(TourPlanReportActivity.this);
        SessionManagement session = new SessionManagement(getApplicationContext());
        session.checkLogin();
        HashMap<String, String> user = session.getUserDetails();
        empId = user.get(SessionManagement.KEY_EmpId);

        // default: current month, self + team
        Map<String, String> filter = new HashMap<>();
        filter.put("empId", empId);
        filter.put("selectedEmpId", "0");
        filter.put("FromDt", "");
        filter.put("ToDt", "");
        getTourPlanReport(filter);

        openFilter();
        binding.atteFilter.setOnClickListener(v -> bsheetdlg.show());
    }

    private void openFilter() {
        FilterMasterBinding ftm = FilterMasterBinding.inflate(getLayoutInflater());
        bsheetdlg = new BottomSheetDialog(TourPlanReportActivity.this);
        bsheetdlg.setContentView(ftm.getRoot());
        bsheetdlg.setCanceledOnTouchOutside(true);
        ftm.filterStatusLayout.getRoot().setVisibility(View.GONE);
        ftm.filterTypeLayout.getRoot().setVisibility(View.GONE);
        ftm.linearLayoutemp.setVisibility(View.GONE);

        // Sales Person (Self / All / team, like the Attendance report), Doctor and Customer with code + name
        ftm.salesPersonLay.setVisibility(View.VISIBLE);
        ftm.doctorLay.setVisibility(View.VISIBLE);
        ftm.customerLay.setVisibility(View.VISIBLE);
        ReportFilterHelper.loadSalesPersons(this, ftm.spinnerSalesPerson);
        ReportFilterHelper.loadDoctors(this, ftm.spinnerDoctor);
        ReportFilterHelper.loadCustomers(this, ftm.spinnerCustomer);

        ftm.filterMonthYearLayout.txtFromDate.setText(UtilityHelper._GetCurrentDate());
        ftm.filterMonthYearLayout.txtToDate.setText(UtilityHelper._GetCurrentDate());
        ftm.filterMonthYearLayout.ivDatePickerFromDate.setOnClickListener(v ->
                UtilityHelper._datePickerDialogeForDates(ftm.filterMonthYearLayout.txtFromDate, TourPlanReportActivity.this));
        ftm.filterMonthYearLayout.ivDatePickerToDate.setOnClickListener(v ->
                UtilityHelper._datePickerDialogeForDates(ftm.filterMonthYearLayout.txtToDate, TourPlanReportActivity.this));

        ftm.filterTopLayout.btnDonefilter.setOnClickListener(v -> {
            Map<String, String> filter = new HashMap<>();
            filter.put("empId", empId);
            filter.put("selectedEmpId", String.valueOf(ReportFilterHelper.selectedEmpIdOrZero(ftm.spinnerSalesPerson))); // 0 = All
            filter.put("FromDt", ftm.filterMonthYearLayout.txtFromDate.getText().toString());
            filter.put("ToDt", ftm.filterMonthYearLayout.txtToDate.getText().toString());
            filter.put("doctorId", String.valueOf(ReportFilterHelper.selectedDoctorId(ftm.spinnerDoctor)));
            String customerId = ReportFilterHelper.selectedCustomerId(ftm.spinnerCustomer);
            filter.put("customerId", customerId.isEmpty() ? "0" : customerId);
            getTourPlanReport(filter);
            bsheetdlg.cancel();
        });
        ftm.filterTopLayout.ivCancel.setOnClickListener(v -> bsheetdlg.cancel());
    }

    private void getTourPlanReport(Map<String, String> filter) {
        pd.setMessage("Loading...");
        pd.setCancelable(false);
        pd.show();
        APICall_Report_i service = RetrofitClientInstance.getRetrofitInstance().create(APICall_Report_i.class);
        service.GetTourPlanReport(filter).enqueue(new Callback<List<ModelTourPlanReport>>() {
            @Override
            public void onResponse(@NonNull Call<List<ModelTourPlanReport>> call, @NonNull Response<List<ModelTourPlanReport>> response) {
                pd.dismiss();
                List<ModelTourPlanReport> aList = response.body() == null ? new ArrayList<>() : response.body();
                AdapterTourPlanReport adapter = new AdapterTourPlanReport(aList);
                binding.count.setText(String.valueOf(adapter.getItemCount())); // days, not rows
                binding.recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                binding.recyclerView.setAdapter(adapter);
                if (aList.isEmpty()) {
                    SnackBarManagement._warning_CustomMessage(binding.getRoot(), "No Tour Plan Found!!");
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<ModelTourPlanReport>> call, @NonNull Throwable t) {
                pd.dismiss();
                SnackBarManagement._warning_CustomMessage(binding.getRoot(), "Failed to load Tour Plan report");
            }
        });
    }
}
