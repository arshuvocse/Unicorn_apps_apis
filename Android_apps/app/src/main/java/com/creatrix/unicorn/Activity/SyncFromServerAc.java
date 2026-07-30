package com.creatrix.unicorn.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.creatrix.unicorn.Activity.Doctor.DCR.NonEffectiveReason;
import com.creatrix.unicorn.DBAdapter.DBCrudHelper;
import com.creatrix.unicorn.DBAdapter.SyncDb_Helper;
import com.creatrix.unicorn.DBInfo.DBHelperMain;
import com.creatrix.unicorn.Interface.ISyncMaster;
import com.creatrix.unicorn.Model.Doctor.ContactTypeVM;
import com.creatrix.unicorn.Model.Customer;
import com.creatrix.unicorn.Model.CustomerType;
import com.creatrix.unicorn.Model.Doctor.Brand;
import com.creatrix.unicorn.Model.Doctor.DoctorCategory;
import com.creatrix.unicorn.Model.Doctor.DoctorChamberName;
import com.creatrix.unicorn.Model.Doctor.DoctorChamberTypeVM;
import com.creatrix.unicorn.Model.Doctor.DoctorDegreeViewModel;
import com.creatrix.unicorn.Model.Doctor.DoctorDesignation;
import com.creatrix.unicorn.Model.Doctor.DoctorListViewModel;
import com.creatrix.unicorn.Model.Doctor.DoctorSpecialityViewModel;
import com.creatrix.unicorn.Model.Doctor.DoctorTypeVM;
import com.creatrix.unicorn.Model.Doctor.ProgramType;
import com.creatrix.unicorn.Model.Doctor.SpecialDay;
import com.creatrix.unicorn.Model.ExpenseTypeMaster;
import com.creatrix.unicorn.Model.Gift;
import com.creatrix.unicorn.Model.InstitutionVM;
import com.creatrix.unicorn.Model.LeaveTypeInfo;
import com.creatrix.unicorn.Model.MarketStructure.AssignEmpTable.ASM;
import com.creatrix.unicorn.Model.MarketStructure.AssignEmpTable.MIO;
import com.creatrix.unicorn.Model.MarketStructure.AssignEmpTable.NSM;
import com.creatrix.unicorn.Model.MarketStructure.AssignEmpTable.RSM;
import com.creatrix.unicorn.Model.MarketStructure.StructureTable.Area;
import com.creatrix.unicorn.Model.MarketStructure.StructureTable.Group;
import com.creatrix.unicorn.Model.MarketStructure.StructureTable.Market;
import com.creatrix.unicorn.Model.MarketStructure.StructureTable.Region;
import com.creatrix.unicorn.Model.MarketStructure.StructureTable.SubTeritorry;
import com.creatrix.unicorn.Model.MarketStructure.StructureTable.Teritorry;
import com.creatrix.unicorn.Model.PrescriptionTYpe;
import com.creatrix.unicorn.Model.Product;
import com.creatrix.unicorn.Model.ProductSample;
import com.creatrix.unicorn.Model.QuotedPrice;
import com.creatrix.unicorn.Model.ResultInfo;
import com.creatrix.unicorn.Model.StationType;
import com.creatrix.unicorn.Model.TerritoryViewModel;
import com.creatrix.unicorn.Model.TourPurposeViewModel;
import com.creatrix.unicorn.Model.TourTypeViewModel;
import com.creatrix.unicorn.Model.Transport;
import com.creatrix.unicorn.Model.UserByRole;
import com.creatrix.unicorn.Model.UserRole;
import com.creatrix.unicorn.Network.ApiCustomerCall;
import com.creatrix.unicorn.Network.ApiDoctorCall;
import com.creatrix.unicorn.Network.ApiMasterSync;
import com.creatrix.unicorn.Network.CalculationApiCall;
import com.creatrix.unicorn.Network.GetDataService;
import com.creatrix.unicorn.Network.OrderProcessAPICALL;
import com.creatrix.unicorn.Network.ProductApi;
import com.creatrix.unicorn.Network.RetrofitClientInstance;
import com.creatrix.unicorn.Network.RetrofitClientOrderProcessInstance;
import com.creatrix.unicorn.Network.TourApiCall;
import com.creatrix.unicorn.Network.UserProcessAPI;
import com.creatrix.unicorn.Network.apiSeedDataCall;
import com.creatrix.unicorn.Presenter.SyncMasterPresenter;
import com.creatrix.unicorn.R;
import com.creatrix.unicorn.ResposeModel.ResponseInfo;
import com.creatrix.unicorn.Services.Constants;
import com.creatrix.unicorn.UtilityHelper.NetworkInformation;
import com.creatrix.unicorn.UtilityHelper.SessionManagement;
import com.creatrix.unicorn.UtilityHelper.ToastManagment;
import com.creatrix.unicorn.databinding.ActivitySyncFromServerBinding;
import com.google.gson.Gson;

import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import okhttp3.HttpUrl;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SyncFromServerAc extends AppCompatActivity implements ISyncMaster.View {
    private static final int MAX_ATTEMPTS = 2; // 1 try + 1 retry on error, then move on

    private java.util.List<Runnable> syncTasks = new java.util.ArrayList<>();
    private java.util.List<String> syncLabels = new java.util.ArrayList<>();
    private int totalSyncTasks = 0;
    private int currentTaskIndex = 0;
    private int currentAttempt = 0;

    private void addSyncTask(String label, Runnable r) {
        syncLabels.add(label);
        syncTasks.add(r);
    }

    private void executeNextSync() {
        if (currentTaskIndex >= syncTasks.size()) {
            AllSyncDone();
            return;
        }
        currentAttempt = 0;
        runCurrentTask();
    }

    private void runCurrentTask() {
        int percent = totalSyncTasks == 0 ? 0 : Math.min(100, currentTaskIndex * 100 / totalSyncTasks);
        viewBindings.splashProgress.setProgress(percent);

        String label = syncLabels.get(currentTaskIndex);
        String retrySuffix = currentAttempt > 0 ? " (retrying)" : "";
        viewBindings.top.setText("Synchronizing " + label + retrySuffix + "... (" + (currentTaskIndex + 1) + "/" + totalSyncTasks + ") " + percent + "%");
        syncTasks.get(currentTaskIndex).run();
    }

    // Called once a task's result is known. On failure it retries the same task
    // up to MAX_ATTEMPTS times before giving up and moving to the next one, so a
    // flaky API gets a second chance instead of being skipped immediately.
    private void onSyncTaskResult(boolean success) {
        if (!success && currentAttempt < MAX_ATTEMPTS - 1) {
            currentAttempt++;
            runCurrentTask();
            return;
        }
        currentTaskIndex++;
        executeNextSync();
    }

    // Runs a callback's UI-update body and guarantees onSyncTaskResult() still
    // fires even if that body throws, so one bad callback can never stall the queue.
    private void runSafely(boolean success, Runnable body) {
        try {
            body.run();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            onSyncTaskResult(success);
        }
    }

    SyncMasterPresenter spresenter;
    ProgressBar splashProgress;
    int SPLASH_TIME = 1000; //This is 3 seconds
    ActivitySyncFromServerBinding viewBindings;
    DBHelperMain dbHelperMain;
    SyncDb_Helper syncDb_helper;
    DBCrudHelper crudHelper;
    SessionManagement session;
    HashMap<String, String> user = new HashMap<>();

    //    boolean isSymptomDone = false;
    boolean isGroupDone = false;
    boolean isRegionDone = false;
    boolean isAreaDone = false;
    boolean isTeritorryDone = false;
    boolean isSubTeritorryDone = false;
    boolean isMarketDone = false;
    boolean isNSMDone = false;
    boolean isRSMDone = false;
    boolean isASMDone = false;
    boolean isMIODone = false;

    boolean isDoctorDone = false;
    boolean isDesignationDone = false;
    boolean isDoctorDegreeDone = false;
    boolean isDoctorSpecilityDone = false;
    boolean isDoctorSpecilDAyDone = false;
    boolean isDoctorTypeDone = false;
    boolean isContTypeDone = false;
    boolean isCustTypeDone = false;
    boolean isLeaveTypeDone = false;
    boolean isVisitTypeDone = false;
    boolean isDocConType = false;
    boolean isDoctorInstitutionDone = false;
    boolean isChamberType = false;
    boolean isChamberName = false;
    boolean isBrand = false;
    boolean isDocCategory = false;
    boolean isProgramType = false;


    boolean isDivisionDone = false;
    boolean isDistrictDone = false;
    boolean isThanaDone = false;

    boolean isDcrDone = false;

    boolean isTeritoryDone = false;
    //boolean isMarketDone = false;


    boolean isProductDone = false;
    boolean isSampleProductDone = false;
    boolean isGiftProductDone = false;
    boolean isquotedprice = false;

    boolean isExpenseTypeDone = false;
    boolean isCustStationDone = false;
    boolean isTransport = false;
    boolean isTourPurpose = false;
    boolean isPrescType = false;
    boolean isNoneffect = false;
    boolean isCustomerList = false;
    boolean isRoleList = false;
    boolean isRoleUser = false;
    boolean isProviderType=false;
    boolean isSMCType=false;


    String LastSyncTime;
    String CurrentYear;
    String empcode;
    String emprole;
    int empId, year;
    boolean masterDone, areaDone, customerDone, doctorDone, productDone, typeDone;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setContentView(R.layout.activity_sync_from_server);
        viewBindings = ActivitySyncFromServerBinding.inflate(getLayoutInflater());
        setContentView(viewBindings.getRoot());
        spresenter = new SyncMasterPresenter(this, SyncFromServerAc.this);
        syncDb_helper = new SyncDb_Helper(SyncFromServerAc.this);
        crudHelper = new DBCrudHelper(SyncFromServerAc.this);
        // This is additional feature, used to run a progress bar
        viewBindings.splashProgress.setProgressTintList(ColorStateList.valueOf(Color.WHITE));
        viewBindings.splashProgress.setMax(100);
        viewBindings.splashProgress.setProgress(0);
      /*  viewBindings.syncview.setMinFrame(20);
        viewBindings.syncview.setMaxFrame(50);
    */
        // playProgress();
        // viewBindings.syncview.playAnimation();
        if (!NetworkInformation.isConnected(this)) {
            NoInternet();
        } else {
            SessionManagement session = new SessionManagement(getApplicationContext());
            HashMap<String, String> user = session.getUserDetails();
            empId = Integer.parseInt(user.get(SessionManagement.KEY_EmpId));
            empcode = user.get(SessionManagement.KEY_EmpMasterCode);
            emprole = user.get(SessionManagement.KEY_EmpRole);
            year = Integer.parseInt(CurrentYear = new SimpleDateFormat("yyyy", Locale.getDefault()).format(new Date()));
            // RunSync(empId, year, empcode, emprole);
            callRunSync(empId, year, empcode, emprole);
        }
    }

    private void callRunSync(int empId, int year, String empcode, String emprole) {
        viewBindings.btnGoHome.setVisibility(android.view.View.GONE);
        viewBindings.splashProgress.setProgress(0);
        viewBindings.top.setText("Synchronizing... 0%");

        addSyncTask("Doctor List", () -> spresenter.cllDoctor(empId));
        addSyncTask("Customers", () -> spresenter.cllCustomer(empId));
        addSyncTask("Customer Station", () -> spresenter.cllCustomerStation(empId));
        addSyncTask("Provider Type", () -> spresenter.callProviderType());
        addSyncTask("NSM", () -> spresenter.cllNSM(empcode, emprole));
        addSyncTask("RSM", () -> spresenter.cllDZSM(empcode, emprole));
        addSyncTask("ASM", () -> spresenter.cllAM(empcode, emprole));
        addSyncTask("MIO", () -> spresenter.cllMIO(empcode, emprole));
        addSyncTask("Group", () -> spresenter.cllGroup(empcode, emprole));
        addSyncTask("Region", () -> spresenter.cllZone(empcode, emprole));
        addSyncTask("Area", () -> spresenter.cllArea(empcode, emprole));
        addSyncTask("Territory", () -> spresenter.cllTeritory(empcode, emprole));
        addSyncTask("Sub Territory", () -> spresenter.cllSTeritory(empcode, emprole));
        addSyncTask("Market", () -> spresenter.cllMarket(empcode, emprole));
        addSyncTask("Designation", () -> spresenter.cllDesignation(empId));
        addSyncTask("Degree", () -> spresenter.cllDegree(empId));
        addSyncTask("Speciality", () -> spresenter.cllSpeciality(empId));
        addSyncTask("Special Day", () -> spresenter.cllSpecialday(empId));
        addSyncTask("Institution", () -> spresenter.cllInstitution(empId));
        addSyncTask("Institute Type", () -> spresenter.cllChamberType(empId));
        addSyncTask("Institute Name", () -> spresenter.cllChamberName(empId));
        addSyncTask("Brand", () -> spresenter.cllBrand(empId));
        addSyncTask("Doctor Category", () -> spresenter.cllDoccategory(empId));
        addSyncTask("User Role", () -> spresenter.cllUserRole(empId));
        addSyncTask("Role wise User", () -> spresenter.cllUserByRole(empId));
        addSyncTask("Products", () -> spresenter.cllProduct(empId));
        addSyncTask("Sample Products", () -> spresenter.cllProductSample(empId));
        addSyncTask("Gift Products", () -> spresenter.cllProductGift(empId));
        addSyncTask("Quoted Price", () -> spresenter.cllQuotedPrice(empId));
        addSyncTask("Doctor Type", () -> spresenter.cllDoctorType(empId, year));
        addSyncTask("Customer Type", () -> spresenter.cllCustomerType(empId, year));
        addSyncTask("Contact Type", () -> spresenter.cllDoctorContactType(empId, year));
        addSyncTask("Expense Type", () -> spresenter.cllExpenseType(empId, year, emprole));
        addSyncTask("Leave Type", () -> spresenter.cllLeaveType(empId, year));
        addSyncTask("Prescription Type", () -> spresenter.cllPrescriptionType(empId, year));
        addSyncTask("Non-Effective Reason", () -> spresenter.cllNonEffectivereason(empId, year));
        addSyncTask("Transport", () -> spresenter.cllTransportList(empId, year));
        addSyncTask("Tour Purpose", () -> spresenter.cllTourPurpose(empId, year));
        addSyncTask("Visit Type", () -> spresenter.cllVisitType(empId, year));
        addSyncTask("SMC Type", () -> spresenter.callSMCType());
        addSyncTask("Other Employee List", () -> spresenter.cllOtherEmpList("49"));

        totalSyncTasks = syncTasks.size();
        currentTaskIndex = 0;
        executeNextSync();
    }
    private void hitMain() {
        if (masterDone && areaDone && customerDone
                && doctorDone && productDone && typeDone) {
           // Toast.makeText(this, "Master"+String.valueOf(masterDone), Toast.LENGTH_SHORT).show();
           // Toast.makeText(this, "Master"+String.valueOf(areaDone), Toast.LENGTH_SHORT).show();
           // Toast.makeText(this, "Master"+String.valueOf(customerDone), Toast.LENGTH_SHORT).show();
           // Toast.makeText(this, "Master"+String.valueOf(doctorDone), Toast.LENGTH_SHORT).show();
           // Toast.makeText(this, "Master"+String.valueOf(productDone), Toast.LENGTH_SHORT).show();
           // Toast.makeText(this, "Master"+String.valueOf(typeDone), Toast.LENGTH_SHORT).show();
            AllSyncDone();
        }
    }

    //Method to run progress bar for 5 seconds
    private void playProgress() {
        ObjectAnimator.ofInt(splashProgress, "progress", 100)
                .setDuration(25000)
                .start();
    }

    public void AllSyncDone() {
        try {
            String today = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
            SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a");
            String todaytime = dateFormat.format(new Date());
            try {
                syncDb_helper.Insert_InitTableB(today, todaytime);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            System.out.println("AllSync Done!!!!");
            viewBindings.splashProgress.setProgress(100);
            viewBindings.top.setText("Sync Complete");
            viewBindings.btnGoHome.setVisibility(android.view.View.VISIBLE);
            viewBindings.btnGoHome.setOnClickListener(v -> {
                Intent i = new Intent(SyncFromServerAc.this, MainDashboardActivity.class);
                i.addFlags(i.FLAG_ACTIVITY_CLEAR_TOP | i.FLAG_ACTIVITY_CLEAR_TASK | i.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            });
        } catch (Exception ex) {
            ex.printStackTrace();
            ToastManagment.GetLongToast(SyncFromServerAc.this, "Something went wrong");
        }
    }

    public void NoInternet() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(SyncFromServerAc.this);
        alertDialogBuilder.setIcon(R.drawable.ic_warning);
        alertDialogBuilder.setTitle("No Internet Connection!!!");
        alertDialogBuilder.setMessage("Please Connect internet and then press Done");
        alertDialogBuilder.setPositiveButton("Done",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        finish();
                        startActivity(getIntent());
                    }
                });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.setCancelable(false);
        alertDialog.show();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onGetNSM(String t, boolean a) {
        runSafely(a, () -> {
            isNSMDone = a;
            if (isNSMDone && isRSMDone && isASMDone
                    && isMIODone && isRoleList && isRoleUser)  {
                masterDone=true;
                viewBindings.masterAlldoneTxt.setText("----Complete");
            }
        });
    }

    @Override
    public void onGetDZSM(String t, boolean a) {
        runSafely(a, () -> {
            isRSMDone = a;
            if (isNSMDone && isRSMDone && isASMDone
                    && isMIODone && isRoleList && isRoleUser) {
                masterDone=true;
                viewBindings.masterAlldoneTxt.setText("----Complete");
            }
        });
    }

    @Override
    public void onGetAM(String t, boolean a) {
        runSafely(a, () -> {
            isASMDone = a;
            if (isNSMDone && isRSMDone && isASMDone
                    && isMIODone && isRoleList && isRoleUser) {
                masterDone=true;
                viewBindings.masterAlldoneTxt.setText("----Complete");
            }
        });
    }

    @Override
    public void onGetMIO(String t, boolean a) {
        runSafely(a, () -> {
            isMIODone = a;
            if (isNSMDone && isRSMDone && isASMDone
                    && isMIODone && isRoleList && isRoleUser) {
                masterDone=true;
                viewBindings.masterAlldoneTxt.setText("----Complete");
            }
        });
    }

    @Override
    public void onGetGroup(String t, boolean a) {
        runSafely(a, () -> {
            viewBindings.groupTxt.setText(t);
            isGroupDone = a;
            if (isGroupDone && isRegionDone && isAreaDone
                    && isTeritorryDone && isSubTeritorryDone && isMarketDone) {
                areaDone=true;
                viewBindings.areaAlldoneTxt.setText("----Complete");
            }
        });
    }

    @Override
    public void onGetZone(String t, boolean a) {
        runSafely(a, () -> {
            viewBindings.regionTxt.setText(t);
            isRegionDone = a;
            if (isGroupDone && isRegionDone && isAreaDone
                    && isTeritorryDone && isSubTeritorryDone && isMarketDone) {
                areaDone=true;
                viewBindings.areaAlldoneTxt.setText("----Complete");
            }
        });
    }

    @Override
    public void onGetArea(String t, boolean a) {
        runSafely(a, () -> {
            viewBindings.areaTxt.setText(t);
            isAreaDone = a;
            if (isGroupDone && isRegionDone && isAreaDone
                    && isTeritorryDone && isSubTeritorryDone && isMarketDone) {
                areaDone=true;
                viewBindings.areaAlldoneTxt.setText("----Complete");
            }
        });
    }

    @Override
    public void onGetTeritory(String t, boolean a) {
        runSafely(a, () -> {
            viewBindings.teritoryTxt.setText(t);
            isTeritorryDone = a;
            if (isGroupDone && isRegionDone && isAreaDone
                    && isTeritorryDone && isSubTeritorryDone && isMarketDone) {
                areaDone=true;
                viewBindings.areaAlldoneTxt.setText("----Complete");
            }
        });
    }

    @Override
    public void onGetSTeritory(String t, boolean a) {
        runSafely(a, () -> {
            isSubTeritorryDone = a;
            if (isGroupDone && isRegionDone && isAreaDone
                    && isTeritorryDone && isSubTeritorryDone && isMarketDone) {
                areaDone=true;
                viewBindings.areaAlldoneTxt.setText("----Complete");
            }
        });
    }

    @Override
    public void onGetMarket(String t, boolean a) {
        runSafely(a, () -> {
            isMarketDone = a;
            if (isGroupDone && isRegionDone && isAreaDone
                    && isTeritorryDone && isSubTeritorryDone && isMarketDone) {
                areaDone=true;
                viewBindings.areaAlldoneTxt.setText("----Complete");
            }
        });
    }

    @Override
    public void onGetCustomer(String t, boolean a) {
        runSafely(a, () -> {
            viewBindings.custsTxt.setText(t);
            isCustomerList = a;
            if (isCustomerList && isCustStationDone) {
                customerDone=true;
                viewBindings.custAlldoneTxt.setText("----Complete");
            }
        });
    }

    @Override
    public void onGetCustomerStation(String t, boolean a) {
        runSafely(a, () -> {
            viewBindings.custstationTxt.setText(t);
            isCustStationDone = a;
            if (isCustomerList && isCustStationDone) {
                customerDone=true;
                viewBindings.custAlldoneTxt.setText("----Complete");
            }
        });
    }

    @Override
    public void onGetDoctor(String t, boolean a) {
        runSafely(a, () -> {
            viewBindings.docTxt.setText(t);
            isDoctorDone = a;
            System.out.println("1.DoctorDone"+isDoctorDone);

            if (isDoctorDone && isDesignationDone && isDoctorDegreeDone
                    && isDoctorSpecilityDone && isDoctorSpecilDAyDone && isDoctorInstitutionDone && isChamberType &&
                    isChamberName && isBrand && isDocCategory /*&& isProgramType*/) {
                doctorDone=true;
                viewBindings.docAlldoneTxt.setText("----Complete");
            }
        });
    }

    @Override
    public void onGetDesignation(String t, boolean a) {
        runSafely(a, () -> {
            viewBindings.desigTxt.setText(t);
            isDesignationDone = a;
            System.out.println("2.DoctorDone"+isDoctorDone);
            if (isDoctorDone && isDesignationDone && isDoctorDegreeDone
                    && isDoctorSpecilityDone && isDoctorSpecilDAyDone && isDoctorInstitutionDone && isChamberType &&
                    isChamberName && isBrand && isDocCategory /*&& isProgramType*/) {
                doctorDone=true;
                viewBindings.docAlldoneTxt.setText("----Complete");
            }
        });
    }

    @Override
    public void onGetDegree(String t, boolean a) {
        runSafely(a, () -> {
            viewBindings.degTxt.setText(t);
            isDoctorDegreeDone = a;
            if (isDoctorDone && isDesignationDone && isDoctorDegreeDone
                    && isDoctorSpecilityDone && isDoctorSpecilDAyDone && isDoctorInstitutionDone && isChamberType &&
                    isChamberName && isBrand && isDocCategory /*&& isProgramType*/) {
                doctorDone=true;
                viewBindings.docAlldoneTxt.setText("----Complete");
            }
        });
    }

    @Override
    public void onGetSpeciality(String t, boolean a) {
        runSafely(a, () -> {
            viewBindings.spcTxt.setText(t);
            isDoctorSpecilityDone = a;
            if (isDoctorDone && isDesignationDone && isDoctorDegreeDone
                    && isDoctorSpecilityDone && isDoctorSpecilDAyDone && isDoctorInstitutionDone && isChamberType &&
                    isChamberName && isBrand && isDocCategory /*&& isProgramType*/) {
                doctorDone=true;
                viewBindings.docAlldoneTxt.setText("----Complete");
            }
        });
    }

    @Override
    public void onGetSpecialday(String t, boolean a) {
        runSafely(a, () -> {
            viewBindings.spcdayTxt.setText(t);
            isDoctorSpecilDAyDone = a;
            if (isDoctorDone && isDesignationDone && isDoctorDegreeDone
                    && isDoctorSpecilityDone && isDoctorSpecilDAyDone && isDoctorInstitutionDone && isChamberType &&
                    isChamberName && isBrand && isDocCategory /*&& isProgramType*/) {
                doctorDone=true;
                viewBindings.docAlldoneTxt.setText("----Complete");
            }
        });
    }

    @Override
    public void onGetInstitution(String t, boolean a) {
        runSafely(a, () -> {
            viewBindings.institutionTxt.setText(t);
            isDoctorInstitutionDone = a;
            if (isDoctorDone && isDesignationDone && isDoctorDegreeDone
                    && isDoctorSpecilityDone && isDoctorSpecilDAyDone && isDoctorInstitutionDone && isChamberType &&
                    isChamberName && isBrand && isDocCategory /*&& isProgramType*/) {
                doctorDone=true;
                viewBindings.docAlldoneTxt.setText("----Complete");
            }
        });
    }

    @Override
    public void onGetChamberType(String t, boolean a) {
        runSafely(a, () -> {
            viewBindings.ChamberTxt.setText(t);
            isChamberType = a;
            if (isDoctorDone && isDesignationDone && isDoctorDegreeDone
                    && isDoctorSpecilityDone && isDoctorSpecilDAyDone && isDoctorInstitutionDone && isChamberType &&
                    isChamberName && isBrand && isDocCategory /*&& isProgramType*/) {
                doctorDone=true;
                viewBindings.docAlldoneTxt.setText("----Complete");
            }
        });
    }

    @Override
    public void onGetChamberName(String t, boolean a) {
        runSafely(a, () -> {
            viewBindings.ChamberNameTxt.setText(t);
            isChamberName= a;
            if (isDoctorDone && isDesignationDone && isDoctorDegreeDone
                    && isDoctorSpecilityDone && isDoctorSpecilDAyDone && isDoctorInstitutionDone && isChamberType &&
                    isChamberName && isBrand && isDocCategory /*&& isProgramType*/) {
                doctorDone=true;
                viewBindings.docAlldoneTxt.setText("----Complete");
            }
        });
    }

    @Override
    public void onGetBrand(String t, boolean a) {
        runSafely(a, () -> {
            viewBindings.brandTxt.setText(t);
            isBrand= a;
            if (isDoctorDone && isDesignationDone && isDoctorDegreeDone
                    && isDoctorSpecilityDone && isDoctorSpecilDAyDone && isDoctorInstitutionDone && isChamberType &&
                    isChamberName && isBrand && isDocCategory /*&& isProgramType*/) {
                doctorDone=true;
                viewBindings.docAlldoneTxt.setText("----Complete");
            }
        });
    }

    @Override
    public void onGetDoccategory(String t, boolean a) {
        runSafely(a, () -> {
            viewBindings.doccatTxt.setText(t);
            isDocCategory= a;
            if (isDoctorDone && isDesignationDone && isDoctorDegreeDone
                    && isDoctorSpecilityDone && isDoctorSpecilDAyDone && isDoctorInstitutionDone && isChamberType &&
                    isChamberName && isBrand && isDocCategory /*&& isProgramType*/) {
                doctorDone=true;
                viewBindings.docAlldoneTxt.setText("----Complete");
            }
        });
    }

    @Override
    public void onGetProgramtypey(String t, boolean a) {
        runSafely(a, () -> {
            viewBindings.progtypeTxt.setText(t);
            isProgramType= a;
            if (isDoctorDone && isDesignationDone && isDoctorDegreeDone
                    && isDoctorSpecilityDone && isDoctorSpecilDAyDone && isDoctorInstitutionDone && isChamberType &&
                    isChamberName && isBrand && isDocCategory /*&& isProgramType*/) {
                doctorDone=true;
                viewBindings.docAlldoneTxt.setText("----Complete");
            }
        });
    }

    @Override
    public void onGetUserRole(String t, boolean a) {
        runSafely(a, () -> {
            viewBindings.roleTxt.setText(t);
            isRoleList= a;
            if (isNSMDone && isRSMDone && isASMDone
                    && isMIODone && isRoleList && isRoleUser) {
                masterDone=true;
                viewBindings.masterAlldoneTxt.setText("----Complete");
            }
        });
    }

    @Override
    public void onGetUserByRole(String t, boolean a) {
        runSafely(a, () -> {
            viewBindings.roleuserTxt.setText(t);
            isRoleUser= a;
            if (isNSMDone && isRSMDone && isASMDone
                    && isMIODone && isRoleList && isRoleUser) {
                masterDone=true;
                viewBindings.masterAlldoneTxt.setText("----Complete");
            }
        });
    }

    @Override
    public void onGetProduct(String t, boolean a) {
        runSafely(a, () -> {
            viewBindings.productsTxt.setText(t);
            isProductDone= a;
            if (isProductDone && isSampleProductDone && isGiftProductDone && isquotedprice) {
                productDone=true;
                viewBindings.productAlldoneTxt.setText("----Complete");
            }
        });
    }

    @Override
    public void onGetProductSample(String t, boolean a) {
        runSafely(a, () -> {
            viewBindings.sampleproductsTxt.setText(t);
            isSampleProductDone= a;
            if (isProductDone && isSampleProductDone && isGiftProductDone && isquotedprice) {
                productDone=true;
                viewBindings.productAlldoneTxt.setText("----Complete");
            }
        });
    }

    @Override
    public void onGetProductGift(String t, boolean a) {
        runSafely(a, () -> {
            isGiftProductDone= a;
            if (isProductDone && isSampleProductDone && isGiftProductDone && isquotedprice) {
                productDone=true;
                viewBindings.productAlldoneTxt.setText("----Complete");
            }
        });
    }

    @Override
    public void onGetQuotedPrice(String t, boolean a) {
        runSafely(a, () -> {
            isquotedprice= a;
            if (isProductDone && isSampleProductDone && isGiftProductDone && isquotedprice) {
                productDone=true;
                viewBindings.productAlldoneTxt.setText("----Complete");
            }
        });
    }

    @Override
    public void onGetDoctorType(String t, boolean a) {
        runSafely(a, () -> {
            viewBindings.doctypeTxt.setText(t);
            isDoctorTypeDone= a;
            if (isDoctorTypeDone && isCustTypeDone && isDocConType && isExpenseTypeDone
                    && isLeaveTypeDone && isPrescType && isNoneffect && isTransport && isTourPurpose
                    && isVisitTypeDone && isProviderType && isSMCType) {
                typeDone = true;
                viewBindings.typeAlldoneTxt.setText("----Complete");
            }
        });
    }

    @Override
    public void onGetCustomerType(String t, boolean a) {
        runSafely(a, () -> {
            viewBindings.custtypeTxt.setText(t);
            isCustTypeDone= a;
            if (isDoctorTypeDone && isCustTypeDone && isDocConType && isExpenseTypeDone
                    && isLeaveTypeDone && isPrescType && isNoneffect && isTransport && isTourPurpose
                    && isVisitTypeDone && isProviderType && isSMCType) {
                typeDone=true;
                viewBindings.typeAlldoneTxt.setText("----Complete");
            }
        });
    }

    @Override
    public void onGetDoctorContactType(String t, boolean a) {
        runSafely(a, () -> {
            viewBindings.dcontypeTxt.setText(t);
            isDocConType= a;
            if (isDoctorTypeDone && isCustTypeDone && isDocConType && isExpenseTypeDone
                    && isLeaveTypeDone && isPrescType && isNoneffect &&
                    isTransport && isTourPurpose && isVisitTypeDone && isProviderType && isSMCType) {
                typeDone=true;
                viewBindings.typeAlldoneTxt.setText("----Complete");
            }
        });
    }

    @Override
    public void onGetExpenseType(String t, boolean a) {
        runSafely(a, () -> {
            viewBindings.expensetypeTxt.setText(t);
            isExpenseTypeDone= a;
            if (isDoctorTypeDone && isCustTypeDone && isDocConType && isExpenseTypeDone
                    && isLeaveTypeDone && isPrescType && isNoneffect &&
                    isTransport && isTourPurpose && isVisitTypeDone && isProviderType && isSMCType) {
                typeDone=true;
                viewBindings.typeAlldoneTxt.setText("----Complete");
            }
        });
    }

    @Override
    public void onGetLeaveType(String t, boolean a) {
        runSafely(a, () -> {
            viewBindings.leavetypeTxt.setText(t);
            isLeaveTypeDone= a;
            if (isDoctorTypeDone && isCustTypeDone && isDocConType && isExpenseTypeDone
                    && isLeaveTypeDone && isPrescType && isNoneffect &&
                    isTransport && isTourPurpose && isVisitTypeDone && isProviderType && isSMCType) {
                typeDone=true;
                viewBindings.typeAlldoneTxt.setText("----Complete");
            }
        });
    }

    @Override
    public void onGetPrescriptionType(String t, boolean a) {
        runSafely(a, () -> {
            viewBindings.prescriptypeTxt.setText(t);
            isPrescType= a;
            if (isDoctorTypeDone && isCustTypeDone && isDocConType && isExpenseTypeDone
                    && isLeaveTypeDone && isPrescType && isNoneffect &&
                    isTransport && isTourPurpose && isVisitTypeDone && isProviderType && isSMCType) {
                typeDone=true;
                viewBindings.typeAlldoneTxt.setText("----Complete");
            }
        });
    }

    @Override
    public void onGetNonEffectivereason(String t, boolean a) {
        runSafely(a, () -> {
            viewBindings.reasonTxt.setText(t);
            isNoneffect= a;
            if (isDoctorTypeDone && isCustTypeDone && isDocConType && isExpenseTypeDone
                    && isLeaveTypeDone && isPrescType && isNoneffect &&
                    isTransport && isTourPurpose && isVisitTypeDone && isProviderType && isSMCType) {
                typeDone=true;
                viewBindings.typeAlldoneTxt.setText("----Complete");
            }
        });
    }

    @Override
    public void onGetTransportList(String t, boolean a) {
        runSafely(a, () -> {
            viewBindings.transportTxt.setText(t);
            isTransport= a;
            if (isDoctorTypeDone && isCustTypeDone && isDocConType && isExpenseTypeDone
                    && isLeaveTypeDone && isPrescType && isNoneffect &&
                    isTransport && isTourPurpose && isVisitTypeDone && isProviderType && isSMCType) {
                typeDone=true;
                viewBindings.typeAlldoneTxt.setText("----Complete");
            }
        });
    }

    @Override
    public void onGetTourPurpose(String t, boolean a) {
        runSafely(a, () -> {
            viewBindings.tourPurposeTxt.setText(t);
            isTourPurpose= a;
            if (isDoctorTypeDone && isCustTypeDone && isDocConType && isExpenseTypeDone
                    && isLeaveTypeDone && isPrescType && isNoneffect &&
                    isTransport && isTourPurpose && isVisitTypeDone && isProviderType && isSMCType) {
                typeDone=true;
                viewBindings.typeAlldoneTxt.setText("----Complete");
            }
        });
    }

    @Override
    public void onGetVisitType(String t, boolean a) {
        runSafely(a, () -> {
            viewBindings.visittypeTxt.setText(t);
            isVisitTypeDone= a;
            if (isDoctorTypeDone && isCustTypeDone && isDocConType && isExpenseTypeDone
                    && isLeaveTypeDone && isPrescType && isNoneffect &&
                    isTransport && isTourPurpose && isVisitTypeDone && isProviderType && isSMCType) {
                typeDone=true;
                viewBindings.typeAlldoneTxt.setText("----Complete");
            }
        });
    }

    @Override
    public void onGetProviderType(String t, boolean a) {
        runSafely(a, () -> {
            viewBindings.progtypeTxt.setText(t);
            isProviderType= a;
            if (isDoctorTypeDone && isCustTypeDone && isDocConType && isExpenseTypeDone
                    && isLeaveTypeDone && isPrescType && isNoneffect &&
                    isTransport && isTourPurpose && isVisitTypeDone && isProviderType && isSMCType) {
                typeDone=true;
                viewBindings.typeAlldoneTxt.setText("----Complete");
            }
        });
    }

    @Override
    public void onGetSMCType(String a, boolean t) {
        runSafely(t, () -> {
            isSMCType= t;
            if (isDoctorTypeDone && isCustTypeDone && isDocConType && isExpenseTypeDone
                    && isLeaveTypeDone && isPrescType && isNoneffect &&
                    isTransport && isTourPurpose && isVisitTypeDone && isProviderType && isSMCType) {
                typeDone=true;
                viewBindings.typeAlldoneTxt.setText("----Complete");
            }
        });
    }

    @Override
    public void onGetDivision(String t, boolean a) {
        runSafely(a, () -> { });
    }

    @Override
    public void onGetDistrict(String t, boolean a) {
        runSafely(a, () -> { });
    }

    @Override
    public void onGetThana(String t, boolean a) {
        runSafely(a, () -> { });
    }

    @Override
    public void onGetOtherEmpList(String t, boolean a) {
        runSafely(a, () -> { });
    }
}
