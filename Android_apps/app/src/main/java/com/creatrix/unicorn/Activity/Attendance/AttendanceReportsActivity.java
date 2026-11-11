package com.creatrix.unicorn.Activity.Attendance;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.creatrix.unicorn.Network.GetDataService;
import com.creatrix.unicorn.Network.RetrofitClientInstance;
import com.creatrix.unicorn.Interface.IAttendanceReport;
import com.creatrix.unicorn.Model.Report_AttendanceViewModel;
import com.creatrix.unicorn.Presenter.Rpt_AttendancePresenter;
import com.creatrix.unicorn.R;
import com.creatrix.unicorn.RecyclerAdapter.Attendance_data_RecyclerAdapter;
import com.creatrix.unicorn.UtilityHelper.DateTimeUtil;
import com.creatrix.unicorn.UtilityHelper.ReportFilterHelper;
import com.creatrix.unicorn.UtilityHelper.SessionManagement;
import com.creatrix.unicorn.UtilityHelper.UtilityHelper;
import com.toptoche.searchablespinnerlibrary.SearchableSpinner;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AttendanceReportsActivity extends AppCompatActivity implements IAttendanceReport.View {

    public List<Report_AttendanceViewModel> aList = new ArrayList<>();
    private RecyclerView recyclerView;
    private Attendance_data_RecyclerAdapter mAdapter;
    ImageView atteFilter;
    TextView fromDateViewTxt;
    TextView toDateViewTxt;
    IAttendanceReport.Presenter presenter;
    SessionManagement session;
    int selectedEmpId; // 0 = All (self + hierarchy)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance_reports);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_custom);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        session = new SessionManagement(getApplicationContext());
        presenter = new Rpt_AttendancePresenter(this);
        atteFilter = (ImageView) findViewById(R.id.atteFilter);
        fromDateViewTxt = (TextView) findViewById(R.id.fromDateViewTxt);
        toDateViewTxt = (TextView) findViewById(R.id.toDateViewTxt);

        Date c = Calendar.getInstance().getTime();
        String formattedDate = DateTimeUtil.getDisplayDate(c);
        String currentDate = formattedDate;

        HashMap<String, String> user = session.getUserDetails();
        int empId = Integer.parseInt(user.get(SessionManagement.KEY_EmpId));
        selectedEmpId = empId;
        presenter.GetAttendanceData(empId, currentDate, currentDate);

        fromDateViewTxt.setText(currentDate.toString());
        toDateViewTxt.setText(currentDate.toString());
        atteFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final Dialog dialog = new Dialog(AttendanceReportsActivity.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setCancelable(true);
                View popupView = LayoutInflater.from(AttendanceReportsActivity.this).inflate(R.layout.y_modal_attenfilter, null);
                dialog.setContentView(popupView);
                dialog.show();
                dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);


                ImageView datePickerFromDate = (ImageView) popupView.findViewById(R.id.datePickerFromDate);
                ImageView datePickerToDate = (ImageView) popupView.findViewById(R.id.datePickerToDate);
                TextView txtFromDate = (TextView) popupView.findViewById(R.id.txtFromDate);
                TextView txtToDate = (TextView) popupView.findViewById(R.id.txtToDate);
                SearchableSpinner spnEmp = (SearchableSpinner) popupView.findViewById(R.id.spnEmp);
                // Self (+ All and the team for managers; MIO: Self only, read-only)
                ReportFilterHelper.loadSalesPersons(AttendanceReportsActivity.this, spnEmp);

                txtFromDate.setText(UtilityHelper._GetCurrentDate());
                txtToDate.setText(UtilityHelper._GetCurrentDate());

                datePickerFromDate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        UtilityHelper._datePickerDialogeForDates(txtFromDate, AttendanceReportsActivity.this);
                    }
                });
                datePickerToDate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        UtilityHelper._datePickerDialogeForDates(txtToDate, AttendanceReportsActivity.this);
                    }
                });


                Button btnDone = (Button) popupView.findViewById(R.id.psubmitBnt);
                btnDone.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.cancel();

                        String fromDate = txtFromDate.getText().toString();
                        String toDate = txtToDate.getText().toString();
                        if (spnEmp.getSelectedItem() != null) {
                            selectedEmpId = ReportFilterHelper.selectedEmpIdOrZero(spnEmp);
                            ((TextView) findViewById(R.id.toolbar_title)).setText(spnEmp.getSelectedItem().toString());
                        }
                        if (!fromDate.equals("") && !toDate.equals("")) {
                            if (selectedEmpId == 0) loadAllAttendance(fromDate, toDate);
                            else presenter.GetAttendanceData(selectedEmpId, fromDate, toDate);
                        }
                        fromDateViewTxt.setText(fromDate);
                        toDateViewTxt.setText(toDate);


                    }
                });

            }
        });

    }

    // "All": one call with allTeam=1 (self + hierarchy); the employee name is prefixed to
    // AttendanceDate so the adapter's per-date merge/header works per person
    private void loadAllAttendance(String fromDate, String toDate) {
        ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Attendance is Loading....Please wait");
        pd.setCanceledOnTouchOutside(false);
        pd.show();
        GetDataService service = RetrofitClientInstance.getRetrofitInstance().create(GetDataService.class);
        service.GetAttendanceData(Integer.parseInt(session.getUserDetails().get(SessionManagement.KEY_EmpId)), fromDate, toDate, true)
                .enqueue(new Callback<List<Report_AttendanceViewModel>>() {
            @Override
            public void onResponse(Call<List<Report_AttendanceViewModel>> call, Response<List<Report_AttendanceViewModel>> response) {
                pd.dismiss();
                List<Report_AttendanceViewModel> rows = response.body() == null ? new ArrayList<>() : response.body();
                for (Report_AttendanceViewModel r : rows) {
                    r.setAttendanceDate(r.getEmpName() + "  |  " + r.getAttendanceDate());
                }
                LoadAttendace(rows);
            }

            @Override
            public void onFailure(Call<List<Report_AttendanceViewModel>> call, Throwable t) {
                pd.dismiss();
                OnError("Some Error");
            }
        });
    }

    public void LoadAttendace(List<Report_AttendanceViewModel> aList) {
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mAdapter = new Attendance_data_RecyclerAdapter(aList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);
        recyclerView.setItemAnimator(null);
        recyclerView.scrollToPosition(0);
        mAdapter.notifyDataSetChanged();
    }
    @Override
    public void OnSuccess(List<Report_AttendanceViewModel> aList) {
        if (aList != null) {
            LoadAttendace(aList);
        }


    }
    @Override
    public void OnError(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();

    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left,
                R.anim.slide_out_right);
    }
}