package com.creatrix.unicorn.Activity.SelfReports;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.creatrix.unicorn.Activity.Approval.DCR.DcrApprovalListActivity;
import com.creatrix.unicorn.Activity.Doctor.DoctorListActivity;
import com.creatrix.unicorn.Interface.IDoctor;
import com.creatrix.unicorn.Model.DcrVM;
import com.creatrix.unicorn.Model.Doctor.Brand;
import com.creatrix.unicorn.Model.Doctor.ContactTypeVM;
import com.creatrix.unicorn.Model.Doctor.DoctorCategory;
import com.creatrix.unicorn.Model.Doctor.DoctorChamberTypeVM;
import com.creatrix.unicorn.Model.Doctor.DoctorDegreeViewModel;
import com.creatrix.unicorn.Model.Doctor.DoctorDesignation;
import com.creatrix.unicorn.Model.Doctor.DoctorSpecialityViewModel;
import com.creatrix.unicorn.Model.Doctor.DoctorTypeVM;
import com.creatrix.unicorn.Model.Doctor.SpecialDay;
import com.creatrix.unicorn.Model.InstitutionVM;
import com.creatrix.unicorn.Model.MarketStructure.AssignEmpTable.ASM;
import com.creatrix.unicorn.Model.MarketStructure.AssignEmpTable.MIO;
import com.creatrix.unicorn.Model.MarketStructure.AssignEmpTable.RSM;
import com.creatrix.unicorn.Model.ModelProviderType;
import com.creatrix.unicorn.Model.ModelSMCType;
import com.creatrix.unicorn.Model.Report_DcrViewModel;
import com.creatrix.unicorn.Network.ApiDoctorCall;
import com.creatrix.unicorn.Network.RetrofitClientInstance;
import com.creatrix.unicorn.Presenter.DoctorPresenter;
import com.creatrix.unicorn.R;
import com.creatrix.unicorn.RecyclerAdapter._dcr_ListRecyclerAdapter;
import com.creatrix.unicorn.UtilityHelper.SessionManagement;
import com.creatrix.unicorn.UtilityHelper.UtilityHelper;
import com.creatrix.unicorn.UtilityHelper.ReportFilterHelper;
import com.creatrix.unicorn.databinding.ActivityReportsDcrBinding;
import com.creatrix.unicorn.databinding.FilterMasterBinding;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.HttpUrl;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReportsDcrActivity extends AppCompatActivity implements IDoctor.View{
    ActivityReportsDcrBinding binding;
    List<Report_DcrViewModel> aList = new ArrayList<>();
    _dcr_ListRecyclerAdapter mAdapter;
    Map<String, String> filter;
    BottomSheetDialog bsheetdlg;
    String fromdat, todate, RoleType, selectedTyp = "";
    int RoleTypeId,empID,loginEmpId;

    DoctorPresenter dpresenter;
    List<DoctorTypeVM> dtypeList;
    List<ModelProviderType> providertypeList;
    List<ModelSMCType> pharmatypeList;
    String selecteddoctortypeId="0", selectedprogramId="0",selectedpharmatypeId="0";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityReportsDcrBinding.inflate(getLayoutInflater());
        //setContentView(R.layout.activity_reports_dcr);
        setContentView(binding.getRoot());
        dpresenter = new DoctorPresenter(this, this);
        binding.toolbarCustom.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        SessionManagement session = new SessionManagement(getApplicationContext());
        session.checkLogin();
        HashMap<String, String> user = session.getUserDetails();
        String empId = user.get(SessionManagement.KEY_EmpId);
        empID=Integer.parseInt(user.get(SessionManagement.KEY_EmpId));
        loginEmpId = empID;
        RoleTypeId = Integer.parseInt(user.get(SessionManagement.KEY_EmpRoleTypeId));
        RoleType = user.get(SessionManagement.KEY_EmpRoleType);
        dpresenter.GetProviderType(0);
        dpresenter.GetDoctorType(0);
        dpresenter.GetSMCType(0);
        filter = new HashMap<>();
        // today, same as the filter shows (an empty date used to return nothing)
        filter.put("FromDt", UtilityHelper._GetCurrentDate());
        filter.put("ToDt", UtilityHelper._GetCurrentDate());
        filter.put("EmpId", empId);
        filter.put("providertype","0");
        filter.put("pharmatype","0");
        filter.put("doctortype","0");
        GetDCRList(filter);

        binding.atteFilter.setOnClickListener(v -> {
            openFilter();
        });
    }

    private void openFilter() {
        FilterMasterBinding ftm;
        ftm = FilterMasterBinding.inflate(getLayoutInflater());
        bsheetdlg = new BottomSheetDialog(ReportsDcrActivity.this);
        bsheetdlg.setContentView(ftm.getRoot());
        // bsheetdlg.setContentView(R.layout.filter_master);
        bsheetdlg.setCanceledOnTouchOutside(true);
        ftm.linearLayoutemp.setVisibility(View.GONE);
        ftm.filterStatusLayout.linearLayout.setVisibility(View.GONE);
        ftm.filterTypeLayout.tilCusttype.setVisibility(View.GONE);
        // Provider / Pharma type hidden; Sales Person + Doctor added (searchable, like the other reports)
        ftm.filterTypeLayout.tilProgtypetype.setVisibility(View.GONE);
        ftm.filterTypeLayout.tilPharmatype.setVisibility(View.GONE);
        ftm.salesPersonLay.setVisibility(View.VISIBLE);
        ftm.doctorLay.setVisibility(View.VISIBLE);
        ReportFilterHelper.loadSalesPersons(ReportsDcrActivity.this, ftm.spinnerSalesPerson);
        ReportFilterHelper.loadDoctors(ReportsDcrActivity.this, ftm.spinnerDoctor);
        ftm.filterMonthYearLayout.txtFromDate.setText(UtilityHelper._GetCurrentDate());
        ftm.filterMonthYearLayout.txtToDate.setText(UtilityHelper._GetCurrentDate());
        ftm.filterMonthYearLayout.ivDatePickerFromDate.setOnClickListener(v -> {
            UtilityHelper._datePickerDialogeForDates(ftm.filterMonthYearLayout.txtFromDate, ReportsDcrActivity.this);
        });
        ftm.filterMonthYearLayout.ivDatePickerToDate.setOnClickListener(v -> {
            UtilityHelper._datePickerDialogeForDates(ftm.filterMonthYearLayout.txtToDate, ReportsDcrActivity.this);
        });

        DoctorTypeVM r = new DoctorTypeVM();
        r.setDoctorTypeName("Select");
        dtypeList.add(0, r);
        ArrayAdapter<DoctorTypeVM> doctorTypeAdapter = new ArrayAdapter<>(ReportsDcrActivity.this, R.layout._custom_spinner_tv, dtypeList);
        ftm.filterTypeLayout.spinnerDoctortype.setAdapter(doctorTypeAdapter);
        ftm.filterTypeLayout.spinnerDoctortype.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                if(position==0)
                {
                    selecteddoctortypeId="0";
                }else {
                    DoctorTypeVM cmm = (DoctorTypeVM) doctorTypeAdapter.getItem(position);
                    selecteddoctortypeId = String.valueOf(cmm.getDoctorTypeId());
                }

            }
        });

        ModelProviderType ptype = new ModelProviderType();
        ptype.setProviderType("Select");
        providertypeList.add(0, ptype);
        ArrayAdapter<ModelProviderType> providerAdapter = new ArrayAdapter<>(ReportsDcrActivity.this, R.layout._custom_spinner_tv, providertypeList);
        ftm.filterTypeLayout.spinnerProgramtype.setAdapter(providerAdapter);
        ftm.filterTypeLayout.spinnerProgramtype.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                if(position==0)
                {
                    selectedprogramId="0";
                }else {
                    ModelProviderType cmm = (ModelProviderType) providerAdapter.getItem(position);
                    selectedprogramId = String.valueOf(cmm.getProviderTypeId());
                }
            }
        });

        ArrayAdapter<ModelSMCType> pharmaAdapter = new ArrayAdapter<>(ReportsDcrActivity.this, R.layout._custom_spinner_tv, pharmatypeList);
        ftm.filterTypeLayout.spinnerPharmatype.setAdapter(pharmaAdapter);
        ftm.filterTypeLayout.spinnerPharmatype.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                ModelSMCType cmm = (ModelSMCType) pharmaAdapter.getItem(position);
                selectedpharmatypeId = String.valueOf(cmm.getSMCTypeId());
            }
        });

        ftm.filterTopLayout.btnDonefilter.setOnClickListener(v -> {
            int pickedEmp = ReportFilterHelper.selectedEmpIdOrZero(ftm.spinnerSalesPerson); // 0 = All
            boolean allTeam = pickedEmp == 0;
            empID = allTeam ? loginEmpId : pickedEmp;
            fromdat = ftm.filterMonthYearLayout.txtFromDate.getText().toString();
            todate = ftm.filterMonthYearLayout.txtToDate.getText().toString();
            //Map<String,String> filter=new HashMap<>();
            filter = new HashMap<>();
            filter.put("FromDt", fromdat);
            filter.put("ToDt", todate);
            if (String.valueOf(empID) == null) {
                filter.put("EmpId", "");
            } else {
                filter.put("EmpId", String.valueOf(empID));
            }
            filter.put("providertype", "0"); // provider / pharma type filters are hidden
            filter.put("pharmatype", "0");
            if (selecteddoctortypeId == null) {
                filter.put("doctortype", "0");
            } else {
                filter.put("doctortype", selecteddoctortypeId);
            }
            filter.put("doctorId", String.valueOf(ReportFilterHelper.selectedDoctorId(ftm.spinnerDoctor)));
            filter.put("allTeam", String.valueOf(allTeam));

            GetDCRList(filter);
            bsheetdlg.cancel();
        });
        ftm.filterTopLayout.ivCancel.setOnClickListener(v -> {
            bsheetdlg.cancel();
        });
        bsheetdlg.show();
    }

    public void GetDCRList(Map<String, String> filter) {
        try {
            ApiDoctorCall service = RetrofitClientInstance.getRetrofitInstance().create(ApiDoctorCall.class);
            Call<List<DcrVM>> call = service.GetDcrList(filter);
            //  Call<List<DcrVM>> call = service.GetDcrList(empId);
            HttpUrl ds = call.request().url();
            call.enqueue(new Callback<List<DcrVM>>() {
                @Override
                public void onResponse(Call<List<DcrVM>> call, Response<List<DcrVM>> response) {
                    LoadRecycler(response.body());
                }
                @Override
                public void onFailure(Call<List<DcrVM>> call, Throwable t) {
                    if (t instanceof SocketTimeoutException) {
                    } else {
                    }
                }
            });
        } catch (Exception ex) {
        }
    }

    public void LoadRecycler(List<DcrVM> aList) {
        if (aList != null) {
            binding.count.setText(String.valueOf(aList.size()));
            mAdapter = new _dcr_ListRecyclerAdapter(ReportsDcrActivity.this, aList);
            LinearLayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
            binding.recyclerView.setLayoutManager(mLayoutManager);
            binding.recyclerView.setItemAnimator(new DefaultItemAnimator());
            binding.recyclerView.setAdapter(mAdapter);
           /* recyclerView.addItemDecoration(new DividerItemDecoration(getApplicationContext(),
                    DividerItemDecoration.VERTICAL));*/
            binding.recyclerView.setItemAnimator(null);
            binding.recyclerView.scrollToPosition(0);
            mAdapter.notifyDataSetChanged();
        }

    }

    @Override
    public void onDoctorDesignationGet(List<DoctorDesignation> aList) {

    }

    @Override
    public void onDoctorTypeReceived(List<DoctorTypeVM> aList) {
        if (aList.size() > 0) {
            dtypeList = aList;
        }
    }

    @Override
    public void onDegreeReceived(List<DoctorDegreeViewModel> aList) {

    }

    @Override
    public void onSpecialityReceived(List<DoctorSpecialityViewModel> aList) {

    }

    @Override
    public void onInstituteReceived(List<InstitutionVM> aList) {

    }

    @Override
    public void onBrandReceived(List<Brand> aList) {

    }

    @Override
    public void onDocCategoryReceived(List<DoctorCategory> aList) {

    }

    @Override
    public void onChamberReceived(List<DoctorChamberTypeVM> aList) {

    }

    @Override
    public void onContactTypeReceived(List<ContactTypeVM> aList) {

    }

    @Override
    public void onProviderTypeReceived(List<ModelProviderType> aList) {
        if (aList.size() > 0) {
            providertypeList = aList;
        }
    }

    @Override
    public void onSMCTypeReceived(List<ModelSMCType> aList) {
        if (aList.size() > 0) {
            pharmatypeList = aList;
        }
    }

    @Override
    public void onSpecialTypeReceived(List<SpecialDay> aList) {

    }

    @Override
    public void onSubmitSuccess(String mesg) {

    }

    @Override
    public void onSubmitError(String mesg) {

    }
}