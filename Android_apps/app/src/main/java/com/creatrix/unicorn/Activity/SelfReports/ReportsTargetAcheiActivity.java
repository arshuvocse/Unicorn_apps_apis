package com.creatrix.unicorn.Activity.SelfReports;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.view.LayoutInflater;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.creatrix.unicorn.UtilityHelper.ReportFilterHelper;
import com.creatrix.unicorn.Model.Rp_TargetAcchivment;
import com.creatrix.unicorn.Network.APICall_Report_i;
import com.creatrix.unicorn.Network.RetrofitClientInstance;
import com.creatrix.unicorn.R;
import com.creatrix.unicorn.RecyclerAdapter._targetAcchivementProductList_Recycler;
import com.creatrix.unicorn.UtilityHelper.MathUtil;
import com.creatrix.unicorn.UtilityHelper.SessionManagement;
import com.creatrix.unicorn.UtilityHelper.UtilityHelper;
import com.github.dewinjm.monthyearpicker.MonthYearPickerDialog;
import com.github.dewinjm.monthyearpicker.MonthYearPickerDialogFragment;
import com.toptoche.searchablespinnerlibrary.SearchableSpinner;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import okhttp3.HttpUrl;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReportsTargetAcheiActivity extends AppCompatActivity {
    ProgressDialog progressDoalog;
    private RecyclerView recyclerView;
    private _targetAcchivementProductList_Recycler mAdapter;
    TextView selectedMonthTxt;
    int selectedEmpId = 0, selMonth, selYear; // 0 = All (whole hierarchy)
    String selectedEmpName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports_target_achei);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_custom);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        SessionManagement session = new SessionManagement(getApplicationContext());
        session.checkLogin();
        HashMap<String, String> user = session.getUserDetails();
        String userName = user.get(SessionManagement.KEY_LoginName);
        int empId = Integer.parseInt(user.get(SessionManagement.KEY_EmpId));

        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        selMonth = month; selYear = year;
        GetReportData(empId,(month + 1),year);

        selectedMonthTxt = findViewById(R.id.selectedMonthTxt);
        selectedMonthTxt.setText(UtilityHelper.monthNameArrayFull[month]+','+year);


        ImageView datePickerDeliveryDate = (ImageView)findViewById(R.id.atteFilter);
        datePickerDeliveryDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFilter(empId);
            }
        });
    }


    // Sales Person (same list/rules as the Attendance Report) + month, then one report call
    private void openFilter(int empId) {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        View v = LayoutInflater.from(this).inflate(R.layout.y_modal_target_filter, null);
        dialog.setContentView(v);
        dialog.show();
        dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);

        SearchableSpinner spnEmp = v.findViewById(R.id.spnEmp);
        ReportFilterHelper.loadSalesPersons(this, spnEmp);
        TextView txtMonth = v.findViewById(R.id.txtMonth);
        txtMonth.setText(UtilityHelper.monthNameArrayFull[selMonth] + ',' + selYear);
        View.OnClickListener pick = x -> {
            MonthYearPickerDialogFragment f = MonthYearPickerDialogFragment.getInstance(selMonth, selYear);
            f.show(getSupportFragmentManager(), null);
            f.setOnDateSetListener((year, monthOfYear) -> {
                selMonth = monthOfYear; selYear = year;
                txtMonth.setText(UtilityHelper.monthNameArrayFull[selMonth] + ',' + selYear);
            });
        };
        v.findViewById(R.id.monthPicker).setOnClickListener(pick);
        txtMonth.setOnClickListener(pick);

        ((Button) v.findViewById(R.id.psubmitBnt)).setOnClickListener(x -> {
            dialog.cancel();
            selectedEmpId = ReportFilterHelper.selectedEmpIdOrZero(spnEmp);
            if (spnEmp.getSelectedItem() != null) ((TextView) findViewById(R.id.toolbar_title)).setText(String.valueOf(spnEmp.getSelectedItem()));
            selectedMonthTxt.setText(UtilityHelper.monthNameArrayFull[selMonth] + ',' + selYear);
            GetReportData(empId, selMonth + 1, selYear);
        });
    }

    public void GetReportData(int empId,int month,int year) {
        progressDoalog = new ProgressDialog(ReportsTargetAcheiActivity.this);
        progressDoalog.setMessage("Target & Achievement is Loading....");
        progressDoalog.show();
        progressDoalog.setCanceledOnTouchOutside(false);
        try{
            APICall_Report_i service = RetrofitClientInstance.getRetrofitInstance().create(APICall_Report_i.class);
            Call<List<Rp_TargetAcchivment>> call = service.GetTarvsAcchi(empId,month,year,selectedEmpId);
            HttpUrl ds = call.request().url();
            call.enqueue(new Callback<List<Rp_TargetAcchivment>>() {
                @Override
                public void onResponse(Call<List<Rp_TargetAcchivment>> call, Response<List<Rp_TargetAcchivment>> response) {
                    progressDoalog.dismiss();
                    if (!response.isSuccessful() || response.body() == null) {
                        LoadinView(new ArrayList<>());
                        ErrorView("Target & Achievement could not be loaded. Please try again.");
                        return;
                    }
                    LoadinView(response.body());
                    if (response.body().isEmpty()) {
                        // no target uploaded and no sales yet for this month is a normal state, not an error
                        ErrorView("No target or sales found for " + selectedMonthTxt.getText());
                    }
                }
                @Override
                public void onFailure(Call<List<Rp_TargetAcchivment>> call, Throwable t) {
                    progressDoalog.dismiss();
                    if(t instanceof SocketTimeoutException){
                        ErrorView("Slow Connection Detected");
                    }else{
                        ErrorView("Some Error Occurred");
                    }


                }
            });

        }catch (Exception ex){
            progressDoalog.dismiss();
            ErrorView("Some Error Occurred");

        }

    }

    public void LoadinView(List<Rp_TargetAcchivment> aList){
        if(aList != null){
            MakeTotalTargetAndAcchivement(aList);

            recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mAdapter = new _targetAcchivementProductList_Recycler(aList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);
        if (recyclerView.getItemDecorationCount() == 0) recyclerView.addItemDecoration(new DividerItemDecoration(getApplicationContext(),
                DividerItemDecoration.VERTICAL));
        recyclerView.setItemAnimator(null);
        recyclerView.scrollToPosition(0);
        mAdapter.notifyDataSetChanged();
        }
    }

    public  void ErrorView(String msg){
        Toast.makeText(ReportsTargetAcheiActivity.this,msg,Toast.LENGTH_LONG).show();
    }


    public  void MakeTotalTargetAndAcchivement(List<Rp_TargetAcchivment> aList){

        try {
            double totalTar = 0;
            double totalAc = 0;

            for (Rp_TargetAcchivment item : aList){
                totalTar += item.getTargetAmt();
                totalAc += item.getSalesAmt();
            }

            TextView mainTarget = findViewById(R.id.mainTarget);
            TextView mainAcciv = findViewById(R.id.mainAcciv);
            TextView mainPerc = findViewById(R.id.mainPerc);
            TextView mainBalance = findViewById(R.id.mainBalance);

            mainTarget.setText(String.format(Locale.US, "%.2f", totalTar));
            mainAcciv.setText(String.format(Locale.US, "%.2f", totalAc));
            mainBalance.setText(String.format(Locale.US, "%.2f", totalTar - totalAc));
            mainPerc.setText(totalTar == 0 ? "0%" : String.format(Locale.US, "%.2f%%", totalAc * 100 / totalTar));

        }catch (Exception ex){
            ex.printStackTrace();
        }

    }

    public void MonthPicker(int empId) {
        int yearSelected;
        int monthSelected;
        Calendar calendar = Calendar.getInstance();
        yearSelected = calendar.get(Calendar.YEAR);
        monthSelected = calendar.get(Calendar.MONTH);
        MonthYearPickerDialogFragment dialogFragment = MonthYearPickerDialogFragment
                .getInstance(monthSelected, yearSelected);
        dialogFragment.show(getSupportFragmentManager(), null);
        dialogFragment.setOnDateSetListener(new MonthYearPickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(int year, int monthOfYear) {
                selectedMonthTxt.setText("");
                selectedMonthTxt.setText(UtilityHelper.monthNameArrayFull[monthOfYear]+','+year);

                GetReportData(empId,(monthOfYear+1),year);


            }
        });
    }

    }