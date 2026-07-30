package com.creatrix.unicorn.Activity.Customer.Approval;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.creatrix.unicorn.Activity.Customer.Approval.Model.CustomerApprovalList;
import com.creatrix.unicorn.Activity.Customer.Approval.Model.CustomerApprovalPresenter;
import com.creatrix.unicorn.Activity.Customer.Approval.Model.CustomerApprovalRQ;
import com.creatrix.unicorn.Activity.Customer.Approval.Model.ICustomerApproval;
import com.creatrix.unicorn.Activity.Customer.CustomerListActivity;
import com.creatrix.unicorn.Activity.Customer.CustomerPresenter;
import com.creatrix.unicorn.Activity.Customer.ICustomerAdd;
import com.creatrix.unicorn.DBAdapter.DBCrudHelper;
import com.creatrix.unicorn.DBAdapter.DBDoctor.DBDoctorHelper;
import com.creatrix.unicorn.Interface.ICustomer;
import com.creatrix.unicorn.Model.CustomerType;
import com.creatrix.unicorn.Model.Doctor.ProgramType;
import com.creatrix.unicorn.Model.MarketStructure.StructureTable.Teritorry;
import com.creatrix.unicorn.Model.ModelProviderType;
import com.creatrix.unicorn.Model.ModelSMCType;
import com.creatrix.unicorn.Model.MarketStructure.AssignEmpTable.ASM;
import com.creatrix.unicorn.Model.MarketStructure.AssignEmpTable.MIO;
import com.creatrix.unicorn.Model.MarketStructure.AssignEmpTable.RSM;
import com.creatrix.unicorn.Model.StationType;
import com.creatrix.unicorn.R;
import com.creatrix.unicorn.Services.Constants;
import com.creatrix.unicorn.UtilityHelper.SessionManagement;
import com.creatrix.unicorn.UtilityHelper.SnackBarManagement;
import com.creatrix.unicorn.UtilityHelper.UtilityHelper;
import com.creatrix.unicorn.databinding.ActivityCustomerApprovalListBinding;
import com.creatrix.unicorn.databinding.FilterMasterBinding;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.gson.Gson;

import java.security.Provider;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CustomerApprovalListActivity extends AppCompatActivity implements ICustomerApproval.View, ICustomerAdd.View,customerTeamApprovalAdapter.approvalKlick {
    ActivityCustomerApprovalListBinding binding;
    customerTeamApprovalAdapter adapter;
    CustomerApprovalPresenter presenter;
    CustomerPresenter presenterCutstomer;
    SessionManagement session;
    DBCrudHelper dbCrudHelper;
    HashMap<String, String> userInfo = new HashMap<>();

    String fromdat, todate, RoleType, params, tagA, tagR, tagN, Areaid, Regionid, Groupid, selectedTyp = "";
    int RoleTypeId, empid, pending;
    BottomSheetDialog bsheetdlg;
    DBDoctorHelper dbDoctorHelper;
    FilterMasterBinding ftm;
    ProgressDialog pd;

    List<CustomerType> ctypeList;
    List<ModelProviderType> providertypeList;
    List<ModelSMCType> pharmatypeList;
    String selectedcustomertypeId = "0", selectedprogramId = "0", selectedpharmatypeId = "0", selectedteriId = "0";

    private int next = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCustomerApprovalListBinding.inflate(getLayoutInflater());
        //setContentView(R.layout.activity_customer_approval_list);
        setContentView(binding.getRoot());
        ftm = FilterMasterBinding.inflate(getLayoutInflater());
        binding.toolbarCustom.setOnClickListener(v -> finish());
        pd = new ProgressDialog(CustomerApprovalListActivity.this);

        session = new SessionManagement(getApplicationContext());
        presenter = new CustomerApprovalPresenter(this, this);
        presenterCutstomer = new CustomerPresenter(this, this);

        userInfo = session.getUserDetails();
        empid = Integer.parseInt(userInfo.get(SessionManagement.KEY_EmpId));
        RoleTypeId = Integer.parseInt(userInfo.get(SessionManagement.KEY_EmpRoleTypeId));
        RoleType = userInfo.get(SessionManagement.KEY_EmpRoleType);
        dbDoctorHelper = new DBDoctorHelper(CustomerApprovalListActivity.this);

        presenterCutstomer.GetCustomerType();
        presenterCutstomer.GetSMCType();
        presenterCutstomer.GetProviderType();

        try {
            tagA = ".EmpAreaId=";
            tagR = ".EmpRegionId=";
            tagN = ".EmpGroupId=";
            dbCrudHelper = new DBCrudHelper(this);
            switch (RoleType) {
                case "AM":
                    Areaid = String.valueOf(dbCrudHelper.getCurrentUserAreaId_SQLite());
                    params = "AND View_Webapi_EmployeeFieldForceInfo" + tagA + Areaid;
                    break;
                case "DZSM":
                    Regionid = String.valueOf(dbCrudHelper.getCurrentUserRegionId_SQLite());
                    params = "AND View_Webapi_EmployeeFieldForceInfo" + tagR + Regionid;
                    break;
                case "NSM":
                    Groupid = String.valueOf(dbCrudHelper.getCurrentUserGroupId_SQLite());
                    params = "AND View_Webapi_EmployeeFieldForceInfo" + tagN + Groupid;
                    break;

                case "Admin":
                    params = "";
                    break;
            }

            Map<String, String> filter = new HashMap<>();
            filter.put("Role", RoleType);
            filter.put("AppStatus", "");
            filter.put("FromDt", "");
            filter.put("ToDt", "");
            filter.put("TerritoryId", "");
            filter.put("EmpId", "");

            filter.put("CustomerTypeId", selectedcustomertypeId);
            filter.put("ProgramTypeId", selectedprogramId);
            filter.put("SMCTypeId", selectedpharmatypeId);
            Constants.filtermap=filter;
            Constants.filterparams=params;
            hitApi(params, filter);

            openFilter();
            binding.atteFilter.setOnClickListener(v -> bsheetdlg.show());
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        binding.swipteamcustomer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Map<String, String> filters = new HashMap<>();
                filters.put("Role", RoleType);
                filters.put("AppStatus", "");
                filters.put("FromDt", "");
                filters.put("ToDt", "");
                filters.put("TerritoryId", "");
                filters.put("EmpId", "");
//
                filters.put("CustomerTypeId", selectedcustomertypeId);
                filters.put("ProgramTypeId", selectedprogramId);
                filters.put("SMCTypeId", selectedpharmatypeId);
                hitApi(params, filters);
                // presenter.GetCustomerApprovalList(params, filters, binding.master);
                binding.swipteamcustomer.setRefreshing(false);
            }
        });
    }

    private void hitApi(String params, Map<String, String> filter) {
        pd.setMessage("Customer List Loading...");
        pd.show();
        pd.setCanceledOnTouchOutside(false);
        presenter.GetCustomerApprovalList(params, filter, binding.master);
    }
    private void openFilter() {
        bsheetdlg = new BottomSheetDialog(CustomerApprovalListActivity.this);
        bsheetdlg.setContentView(ftm.getRoot());
        //bsheetdlg.setContentView(R.layout.filter_master);
        bsheetdlg.setCanceledOnTouchOutside(true);
        switch (RoleType) {
            case "MIO":
                ftm.linearLayoutemp.setVisibility(View.GONE);
                break;
            case "AM":
            case "DZSM":
            case "NSM":
            case "Admin":
                ftm.linearLayoutemp.setVisibility(View.VISIBLE);
                break;
        }

        List<String> emptype = new ArrayList<>();
        switch (RoleType) {
            case "AM":
                emptype.add("Select");
                emptype.add("MIO");
                try {
                    ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(CustomerApprovalListActivity.this, android.R.layout.simple_spinner_item, emptype);
                    dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    ftm.spinnerEmployeeType.setAdapter(dataAdapter);
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
                break;
            case "DZSM":
                emptype.add("Select");
                emptype.add("MIO");
                emptype.add("AM");
                ArrayAdapter<String> dataAdapterrsm = new ArrayAdapter<>(CustomerApprovalListActivity.this, android.R.layout.simple_spinner_item, emptype);
                dataAdapterrsm.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                ftm.spinnerEmployeeType.setAdapter(dataAdapterrsm);
                break;
            case "NSM":
            case "Admin":
                emptype.add("Select");
                emptype.add("MIO");
                emptype.add("AM");
                emptype.add("DZSM");
                ArrayAdapter<String> dataAdapternsm = new ArrayAdapter<>(CustomerApprovalListActivity.this, android.R.layout.simple_spinner_item, emptype);
                dataAdapternsm.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                ftm.spinnerEmployeeType.setAdapter(dataAdapternsm);
                break;
        }

        ftm.spinnerEmployeeType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedTyp = String.valueOf(ftm.spinnerEmployeeType.getSelectedItem());
                switch (selectedTyp) {
                    case "Select":
                        ftm.miolay.setVisibility(View.GONE);
                        ftm.asmlay.setVisibility(View.GONE);
                        ftm.rsmlay.setVisibility(View.GONE);
                        break;

                    case "MIO":
                        try {
                            ftm.miolay.setVisibility(View.VISIBLE);
                            ftm.asmlay.setVisibility(View.GONE);
                            ftm.rsmlay.setVisibility(View.GONE);
                            List<MIO> mioList = null;
                            try {
                                mioList = dbCrudHelper.getMIOList_SQLite();
                            } catch (Exception exception) {
                                exception.printStackTrace();
                            }
                            if (mioList != null) {
                                ArrayAdapter<MIO> dataAdapter = new ArrayAdapter<>(CustomerApprovalListActivity.this, android.R.layout.simple_spinner_item, mioList);
                                dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                ftm.spinnerApprovalMio.setAdapter(dataAdapter);
                            } else {
                                SnackBarManagement._warning_CustomMessage(binding.getRoot(), "No MIO Found!!");
                            }
                        } catch (Exception exception) {
                            exception.printStackTrace();
                        }
                        break;
                    case "AM":
                        try {
                            ftm.asmlay.setVisibility(View.VISIBLE);
                            ftm.miolay.setVisibility(View.GONE);
                            ftm.rsmlay.setVisibility(View.GONE);
                            List<ASM> asmList = null;
                            try {
                                asmList = dbCrudHelper.getASMList_SQLite();
                            } catch (Exception exception) {
                                exception.printStackTrace();
                            }
                            if (asmList != null) {
                                ArrayAdapter<ASM> dataAdapter = new ArrayAdapter<>(CustomerApprovalListActivity.this, android.R.layout.simple_spinner_item, asmList);
                                dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                ftm.spinnerApprovalAsm.setAdapter(dataAdapter);
                            } else {
                                SnackBarManagement._warning_CustomMessage(binding.getRoot(), "No AM Found!!");
                            }
                        } catch (Exception exception) {
                            exception.printStackTrace();
                        }
                        break;
                    case "DZSM":
                        try {
                            ftm.rsmlay.setVisibility(View.VISIBLE);
                            ftm.miolay.setVisibility(View.GONE);
                            ftm.asmlay.setVisibility(View.GONE);
                            List<RSM> rsmList = null;
                            try {
                                rsmList = dbCrudHelper.getRSMList_SQLite();
                            } catch (Exception exception) {
                                exception.printStackTrace();
                            }
                            if (rsmList != null) {
                                ArrayAdapter<RSM> dataAdapter = new ArrayAdapter<>(CustomerApprovalListActivity.this, android.R.layout.simple_spinner_item, rsmList);
                                dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                ftm.spinnerApprovalRsm.setAdapter(dataAdapter);
                            } else {
                                SnackBarManagement._warning_CustomMessage(binding.getRoot(), "No RSM Found!!");
                            }
                        } catch (Exception exception) {
                            exception.printStackTrace();
                        }
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        ftm.filterStatusLayout.linearLayout.setVisibility(View.GONE);
        ftm.filterTypeLayout.tilDoctype.setVisibility(View.GONE);
        ftm.filterTypeLayout.tilProgtypetype.setVisibility(View.GONE);
        ftm.filterTypeLayout.spinnerDoctortype.setVisibility(View.GONE);
        ftm.filterTypeLayout.tilTeritorryid.setVisibility(View.GONE);

        CustomerType r = new CustomerType();
        r.setCustomerType("Select");
        ctypeList.add(0, r);
        ArrayAdapter<CustomerType> customerTypeAdapter = new ArrayAdapter<>(CustomerApprovalListActivity.this, R.layout._custom_spinner_tv, ctypeList);
        ftm.filterTypeLayout.spinnerCusttype.setAdapter(customerTypeAdapter);
        ftm.filterTypeLayout.spinnerCusttype.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                if (position == 0) {
                    selectedcustomertypeId = "0";
                } else {
                    CustomerType cmm = (CustomerType) customerTypeAdapter.getItem(position);
                    selectedcustomertypeId = String.valueOf(cmm.getCustomerTypeId());
                }

            }
        });

        ModelProviderType ptype = new ModelProviderType();
        ptype.setProviderType("Select");
        providertypeList.add(0, ptype);
        ArrayAdapter<ModelProviderType> providerAdapter = new ArrayAdapter<>(CustomerApprovalListActivity.this, R.layout._custom_spinner_tv, providertypeList);
        ftm.filterTypeLayout.spinnerProgramtype.setAdapter(providerAdapter);
        ftm.filterTypeLayout.spinnerProgramtype.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                if (position == 0) {
                    selectedprogramId = "0";
                } else {
                    ModelProviderType cmm = (ModelProviderType) providerAdapter.getItem(position);
                    selectedprogramId = String.valueOf(cmm.getProviderTypeId());
                }
            }
        });

        ArrayAdapter<ModelSMCType> pharmaAdapter = new ArrayAdapter<>(CustomerApprovalListActivity.this, R.layout._custom_spinner_tv, pharmatypeList);
        ftm.filterTypeLayout.spinnerPharmatype.setAdapter(pharmaAdapter);
        ftm.filterTypeLayout.spinnerPharmatype.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                ModelSMCType cmm = (ModelSMCType) pharmaAdapter.getItem(position);
                selectedpharmatypeId = String.valueOf(cmm.getSMCTypeId());
            }
        });

        ArrayAdapter<Teritorry> teritoryAdapter = new ArrayAdapter<>(CustomerApprovalListActivity.this, R.layout._custom_spinner_tv, dbCrudHelper.getTerritoryByIdList_SQLite(0));
        ftm.filterTypeLayout.spinnerTeritorryid.setAdapter(teritoryAdapter);
        ftm.filterTypeLayout.spinnerTeritorryid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Teritorry cmm = (Teritorry) teritoryAdapter.getItem(position);
                selectedteriId = String.valueOf(cmm.getTerritoryId());
            }
        });
        ftm.filterMonthYearLayout.txtFromDate.setText(UtilityHelper._GetCurrentDate());
        ftm.filterMonthYearLayout.txtToDate.setText(UtilityHelper._GetCurrentDate());
        ftm.filterMonthYearLayout.ivDatePickerFromDate.setOnClickListener(v -> UtilityHelper._datePickerDialogeForDates(ftm.filterMonthYearLayout.txtFromDate, CustomerApprovalListActivity.this));
        ftm.filterMonthYearLayout.ivDatePickerToDate.setOnClickListener(v -> UtilityHelper._datePickerDialogeForDates(ftm.filterMonthYearLayout.txtToDate, CustomerApprovalListActivity.this)
        );
        ftm.filterTopLayout.btnDonefilter.setOnClickListener(v -> {
            try {
                switch (selectedTyp) {
                    case "MIO":
                        MIO mioempid = (MIO) ftm.spinnerApprovalMio.getSelectedItem();
                        if (mioempid != null) {
                            empid = mioempid.getMIOEmpId();
                        }
                        break;
                    case "AM":
                        ASM amempid = (ASM) ftm.spinnerApprovalAsm.getSelectedItem();
                        if (amempid != null) {
                            empid = amempid.getASMEmpId();
                        }
                        break;
                    case "DZSM":
                        RSM dzsmempid = (RSM) ftm.spinnerApprovalRsm.getSelectedItem();
                        if (dzsmempid != null) {
                            empid = dzsmempid.getRSMEmpId();
                        }
                        break;
                    default:
                        HashMap<String, String> user = session.getUserDetails();
                        empid = Integer.parseInt(user.get(SessionManagement.KEY_EmpId));
                        break;
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
            fromdat = ftm.filterMonthYearLayout.txtFromDate.getText().toString();
            todate = ftm.filterMonthYearLayout.txtToDate.getText().toString();
            Map<String, String> filters = new HashMap<>();
            filters.put("Role", RoleType);
            filters.put("AppStatus", "");
            filters.put("FromDt", fromdat);
            filters.put("ToDt", todate);
            filters.put("TerritoryId", selectedteriId);
            filters.put("EmpId", String.valueOf(empid));
            filters.put("CustomerTypeId", selectedcustomertypeId);
            filters.put("ProgramTypeId", selectedprogramId);
            filters.put("SMCTypeId", selectedpharmatypeId);

            Constants.filterparams=params;
            Constants.filtermap=filters;
            hitApi(params, filters);
            bsheetdlg.cancel();
        });
        ftm.filterTopLayout.ivCancel.setOnClickListener(v -> bsheetdlg.cancel());
    }

    @Override
    public void OnRevieveCustomerApproval(List<CustomerApprovalList> aList) {
        if (pd != null || pd.isShowing()) {
            pd.dismiss();
        }
        try {
            if (aList != null) {
                pending = aList.size();
                binding.pending.setText(String.valueOf(pending));
                adapter = new customerTeamApprovalAdapter(this, aList,this);
               /* int pos= 0;
                try {
                    pos = binding.rvCustomerrapproval.getAdapter().getItemCount()-1;
                } catch (Exception exception) {
                    exception.printStackTrace();
                }*/
                LinearLayoutManager mLayoutManager = new LinearLayoutManager(CustomerApprovalListActivity.this);
              /*  mLayoutManager.setReverseLayout(true);
                mLayoutManager.setStackFromEnd(true);*/
                binding.rvCustomerrapproval.setLayoutManager(mLayoutManager);
                binding.rvCustomerrapproval.setItemAnimator(new DefaultItemAnimator());
                binding.rvCustomerrapproval.setAdapter(adapter);
                binding.rvCustomerrapproval.setItemAnimator(null);
                //binding.rvCustomerrapproval.scrollToPosition(0);
                //  binding.rvCustomerrapproval.smoothScrollToPosition(pos);


                adapter.notifyDataSetChanged();
                //binding.userCount.setText(String.valueOf(teamlist.size()));
                if (aList.size() == 0) {
                    binding.nodta.setVisibility(View.VISIBLE);
                    binding.rvCustomerrapproval.setVisibility(View.GONE);
                } else {
                    binding.nodta.setVisibility(View.GONE);
                    binding.rvCustomerrapproval.setVisibility(View.VISIBLE);
                }
            } else {
                binding.nodta.setVisibility(View.VISIBLE);
                binding.rvCustomerrapproval.setVisibility(View.GONE);
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public void OnSuccess(String msg) {
        if(pd!=null || pd.isShowing())
        {
            pd.dismiss();
        }
        SnackBarManagement._success_CustomMessage(binding.getRoot(),msg);
        Map<String, String> filter = new HashMap<>();
        filter.put("Role", RoleType);
        filter.put("AppStatus", "");
        filter.put("FromDt", "");
        filter.put("ToDt", "");
        filter.put("TerritoryId", "");
        filter.put("EmpId", "");

        filter.put("CustomerTypeId", selectedcustomertypeId);
        filter.put("ProgramTypeId", selectedprogramId);
        filter.put("SMCTypeId", selectedpharmatypeId);
        hitApi(Constants.filterparams, Constants.filtermap);
    }

    @Override
    public void OnError(int type) {
        if (pd != null || pd.isShowing()) {
            pd.dismiss();
        }
        if (type == 1) {
            SnackBarManagement._error_CustomMessage(binding.getRoot(), "NO Data");
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        try {
            Map<String, String> filterz = new HashMap<>();
            filterz.put("Role", RoleType);
            filterz.put("AppStatus", "");
            filterz.put("FromDt", fromdat);
            filterz.put("ToDt", todate);
            filterz.put("TerritoryId", selectedteriId);
            filterz.put("EmpId", "");
            filterz.put("CustomerTypeId", selectedcustomertypeId);
            filterz.put("ProgramTypeId", selectedprogramId);
            filterz.put("SMCTypeId", selectedpharmatypeId);
            hitApi(params, filterz);
            // presenter.GetCustomerApprovalList(params, filterz, binding.master);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }


    @Override
    public void onProgramType(List<ProgramType> ptype) {

    }

    @Override
    public void onProviderType(List<ModelProviderType> ptypeList) {
        if (ptypeList.size() > 0) {
            providertypeList = ptypeList;
        }
    }

    @Override
    public void onSMCType(List<ModelSMCType> smctypeList) {
        if (smctypeList.size() > 0) {
            pharmatypeList = smctypeList;
        }
    }

    @Override
    public void onCustomerTypeReceived(List<CustomerType> aList) {
        if (aList.size() > 0) {
            ctypeList = aList;
        }
    }

    @Override
    public void onStationReceived(List<StationType> aList) {

    }

    @Override
    public void onSubmitSuccess(String mesg, String who) {

    }

    @Override
    public void onSubmitError(String mesg) {

    }

    @Override
    public void approvalTcust(CustomerApprovalList cl) {
        if (RoleTypeId == 2) {
            next = cl.getRSMEMPId();
        }
        if (RoleTypeId == 3) {
            next = cl.getNSMEMPId();
        }
        if (RoleTypeId == 4) {
            next = 0;
        }

        CustomerApprovalRQ req = new CustomerApprovalRQ();
        int step = cl.getStep();
        int fstep = step + 1;

        req.setCustomerApprovalId(0);
        req.setFromEmpId(empid);
        req.setToEmpId(next);
        req.setTableId(cl.getCustomerSMListDao().getCustomerMasterId());
        req.setStatus("Verified");//Accepted==approve for Admin
        req.setType(cl.getType());
        req.setStep(fstep);
        req.setEntryByApp(empid);
        String entrydate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
        String entrytime = new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date());
        req.setEntryDateApp(entrydate);
        req.setEntryTimeApp(entrytime);
        req.setMenuId(302);

        Gson gson=new Gson();
        String approv=gson.toJson(req);
        System.out.println("systen"+approv);
        presenter.SaveCustomerApprovalList(req);
    }
}