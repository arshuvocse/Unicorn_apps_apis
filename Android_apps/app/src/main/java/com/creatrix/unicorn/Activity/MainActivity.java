package com.creatrix.unicorn.Activity;

import android.Manifest;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.creatrix.unicorn.Activity.Customer.CustomerDashboarActivity;
import com.creatrix.unicorn.DBAdapter.DBCrudHelper;
import com.creatrix.unicorn.DBAdapter.SyncDb_Helper;
import com.creatrix.unicorn.DBInfo.DBHelperMain;
import com.creatrix.unicorn.Interface.IVersionUpdate;
import com.creatrix.unicorn.Interface.LoginInterface;
import com.creatrix.unicorn.Model.Customer;
import com.creatrix.unicorn.Presenter.MainActivityPresenter;
import com.creatrix.unicorn.Presenter.VersionPresenter;
import com.creatrix.unicorn.R;
import com.creatrix.unicorn.Services.Constants;
import com.creatrix.unicorn.Services.LocationPermissionCheck;
import com.creatrix.unicorn.Services.LocationService;
import com.creatrix.unicorn.Setting.AppSetting;
import com.creatrix.unicorn.Setting.DeviceInfo;
import com.creatrix.unicorn.UtilityHelper.NetworkInformation;
import com.creatrix.unicorn.UtilityHelper.LocationAccess;
import com.creatrix.unicorn.UtilityHelper.SessionManagement;
import com.creatrix.unicorn.UtilityHelper.SnackBarManagement;
import com.creatrix.unicorn.databinding.ActivityMainBinding;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements LoginInterface.View, IVersionUpdate.View {
    ActivityMainBinding viewBinding;
    LoginInterface.Presenter presenter;
    IVersionUpdate.Presenter vpresenter;
    View aMasterlayout;
    SessionManagement session;
    DBHelperMain dbHelperMain;
    DBCrudHelper crudHelper;
    SyncDb_Helper syncDb_helper;
    List<Customer> aCusListDemo = new ArrayList<>();
    private static final int REQUEST_CODE_LOCATION_PERMISSION = 1;
    private static final int REQUEST_CODE_BACKGROUND_LOCATION_PERMISSION = 2;
    String imei="",device="", token;
    private static final int MY_PERMISSIONS_REQUEST_READ_PHONE_STATE = 0;
    private boolean permissionRequestInProgress;
    private AlertDialog locationRequiredDialog;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //setContentView(R.layout.activity_main);
        /*
                int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);
                if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, REQUEST_READ_PHONE_STATE);
                } else {
                    //TODO
                }*/
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        session = new SessionManagement(getApplicationContext());
        viewBinding = ActivityMainBinding.inflate(getLayoutInflater());
        View mainView = viewBinding.getRoot();
        setContentView(mainView);
        String year=new SimpleDateFormat("yyyy", Locale.getDefault()).format(new Date());
        viewBinding.year.setText(year);
        /* Dexter.withContext(MainActivity.this)
                .withPermission(permission)
                .withListener(listener)
                .check();*/
        Dexter.withContext(this)
                .withPermission(Manifest.permission.READ_PHONE_STATE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {/* ... */}

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {/* ... */}

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {/* ... */}
                }).check();

                FirebaseApp.initializeApp(MainActivity.this);
                FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            return;
                        }
                        // Get new FCM registration token
                        token = task.getResult();
                        System.out.println("device token: " + token);
                    }
                });

//        initPermission();

        dbHelperMain = new DBHelperMain(this);
        crudHelper = new DBCrudHelper(this);
        syncDb_helper = new SyncDb_Helper(this);
        aMasterlayout = findViewById(R.id.login_masterLayoutId);
        presenter = new MainActivityPresenter(this);
        vpresenter = new VersionPresenter(this);
        vpresenter.GetActiveVersion();
        session = new SessionManagement(getApplicationContext());
        HashMap<String, String> user = session.getUserDetails();
        String userName = user.get(SessionManagement.KEY_LoginName);
        String userPass = user.get(SessionManagement.KEY_Password);
        if (session.isLoggedIn()) {
            Intent i = new Intent(MainActivity.this, MainDashboardActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
            finish();
        }
        if (userName != null) {
            viewBinding.inputLoginName.setText(userName);
        }
        viewBinding.textView5.setText(AppSetting.AppName);
        aCusListDemo = GetDemoCustomerData();
        viewBinding.inputPassword.setText(userPass);
        viewBinding.vname.setText(GetVersionName());

        //Creates a button that mimics a crash when pressed
        /*  Button crashButton = new Button(this);
        crashButton.setText("Test Crash");
        crashButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                throw new RuntimeException("Test Crash"); // Force a crash
            }
        });

        addContentView(crashButton, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));*/

        // Login Button Click Event
        viewBinding.btnLogin.setOnClickListener(v -> {
            String userId = viewBinding.inputLoginName.getText().toString().trim();
            String password = viewBinding.inputPassword.getText().toString().trim();
            if (!userId.equals("") && !password.equals("")) {

                if (NetworkInformation.isConnected(getApplicationContext())) {
                    System.out.println("Imei:" + DeviceInfo.getIMEI(this));
                    if(DeviceInfo.getIMEI(this)==null)
                    {
                        imei="Unknown_imei_"+userId;
                    }else {
                        imei=DeviceInfo.getIMEI(this);
                    }
                    if(DeviceInfo.getDeviceInfo()==null)
                    {
                        device="Unknown"+userId;
                    }else {
                        device=DeviceInfo.getDeviceInfo();
                    }
                    String deviceToken = token == null || token.trim().isEmpty() ? userId : token;
                    presenter.doLogin(userId, password, MainActivity.this, imei, deviceToken, device, GetVersionCode(), "Android", DeviceInfo.getAndroidVersion());
                } else {
                    if (crudHelper.checkLoginProfileUserExist(userId, password) == true) {
                        crudHelper.getLoginProfile_SQLite();
                        Intent i = new Intent(MainActivity.this, MainDashboardActivity.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(i);
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    } else {
                        SnackBarManagement._error_CustomMessage(aMasterlayout, "Incorrect User Id or Password.");
                    }
                }
            } else {
                SnackBarManagement._warning_CustomMessage(aMasterlayout, "Please provide User Id and Password");
            }

        });
        // Forgot password event
        viewBinding.forgotPassword.setOnClickListener(v -> SnackBarManagement._warning_CustomMessage(viewBinding.loginMasterLayoutId, "Please Contact With Admin for Password Retrieval"));
        viewBinding.site.setOnClickListener(v -> {
            String url = "https://creatrixbd.com/";
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
        });
    }

    /* private void GotoHomaAsAM(String role) {
         session.createLoginSession(99,"AM Monir","AM Monir",99,role,"","AM");
         Intent i = new Intent(MainActivity.this,MainDashboardActivity.class);
         i.addFlags(i.FLAG_ACTIVITY_CLEAR_TOP | i.FLAG_ACTIVITY_CLEAR_TASK |i.FLAG_ACTIVITY_NEW_TASK);
         startActivity(i);
         overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
     }*/
    @Override
    public void OnSuccess(int userId, String userName, String empMasterCo, String loginName, String password, int empId, String userCo, int IsImeiMatch, String versionName, String role, int roleTypeid, String roleType, int isAccepted, int isForward, String DesigName) {
        if (userId > 0) {
            try {
                if (GetVersionName().trim().equals(nullToEmpty(versionName).trim())) {
                    if (userId > 0) {
                        try {
                            crudHelper.InsertIntoLoginProfile_SQLite(userId, userName, empMasterCo, loginName, password, empId, userCo, role, roleTypeid, roleType, isForward, isAccepted, DesigName);
                        } catch (Exception exception) {
                            exception.printStackTrace();
                        }
                        if (!syncDb_helper.CheckDataInTable("tblInitTable")) {
                            try {
                                session.createLoginSession(userId, userName, empMasterCo, loginName, password, empId, "", "", role, String.valueOf(roleTypeid), roleType, DesigName);
                                Intent i = new Intent(MainActivity.this, SyncFromServerAc.class);
                                startActivity(i);
                                finish();
                                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                            } catch (Exception exception) {
                                exception.printStackTrace();
                            }
                        } else {
                            session.createLoginSession(userId, userName, empMasterCo, loginName, password, empId, "", "", role, String.valueOf(roleTypeid), roleType, DesigName);
                            Intent i = new Intent(MainActivity.this, MainDashboardActivity.class);
                            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(i);
                            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                        }
                    }

                } else {
                    UpdateDialog();
                }

            } catch (Exception exception) {
                exception.printStackTrace();
                OnError("Login failed after server response. Please try again.");
            }


        }

    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    @Override
    public void OnError(String message) {
        SnackBarManagement._error_CustomMessage(aMasterlayout, message);

    }

    @Override
    public void onVersionGet(String VersionName) {
        try {
            if (!GetVersionName().equals(VersionName)) {
                UpdateDialog();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }

    }
    private String GetVersionName() {
        String version = "";
        try {
            PackageInfo pInfo = MainActivity.this.getPackageManager().getPackageInfo(getPackageName(), 0);
            version = pInfo.versionName;

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }


        return version;
    }
    private String GetVersionCode() {
        String versioncode = "";
        try {
            PackageInfo pInfo = MainActivity.this.getPackageManager().getPackageInfo(getPackageName(), 0);
            versioncode = String.valueOf(pInfo.versionCode);

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versioncode;
    }
    public void UpdateDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
        alertDialogBuilder.setIcon(R.drawable.ic_warning);
        alertDialogBuilder.setTitle("Update Available!!!");
        alertDialogBuilder.setMessage("An update is required to use the application.\n\nPlease download and update the app first. \nIf You don't get any apk file please contact Admin People");
        alertDialogBuilder.setPositiveButton("Go to Download Page",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        String url = "http://103.244.247.93:215/Login.aspx";
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse(url));
                        startActivity(i);
                    }
                });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.setCancelable(false);

        alertDialog.show();
    }


    public List<Customer> GetDemoCustomerData() {
        List<Customer> aList = new ArrayList<>();

        Customer a = new Customer();
        a.setCustomerMasterId(52754);
        a.setCustomerName("M/S Loni Medico");
        a.setCustomerCode("2195231");
        a.setAddress("Taltali Bz,Haji Para Temata,Sandwip,Chattogram");
        a.setCellNo("01878751454");
        aList.add(a);

        Customer b = new Customer();
        b.setCustomerMasterId(52750);
        b.setCustomerName("Mayer Dowa Pharmacy");
        b.setCustomerCode("2195349");
        b.setAddress("south keranigonj,kazi bari");
        b.setCellNo("01911644711");
        aList.add(b);

        Customer v = new Customer();
        v.setCustomerMasterId(52752);
        v.setCustomerName("Mim M Hall");
        v.setCustomerCode("2195315");
        v.setAddress("Baro Kumira,PHP Gate");
        v.setCellNo("01812579670");
        aList.add(v);

        Customer d = new Customer();
        v.setCustomerMasterId(52749);
        v.setCustomerName("Jonota Medicine");
        v.setCustomerCode("2195359");
        v.setAddress("Uttar pangaw,Codda,South Keranigonj");
        v.setCellNo("01911733728");
        aList.add(v);

        return aList;

    }

    public void GotoCustomerDashboard(String userId) {


        for (int p = 0; p < aCusListDemo.size(); p++) {
            if (userId.equals(aCusListDemo.get(p).getCustomerCode())) {

                int userid = aCusListDemo.get(p).getCustomerMasterId();
                String name = aCusListDemo.get(p).getCustomerName();
                String code = aCusListDemo.get(p).getCustomerCode();
                String extra = aCusListDemo.get(p).getAddress();
                String cell = aCusListDemo.get(p).getCellNo();

                session.createLoginSession(userid, name, code, "", "", 0, extra, cell, "Customer", "1", "", "");
                Intent i = new Intent(MainActivity.this, CustomerDashboarActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                break;
            }
        }


    }

/*    public void initPermission()
    {
        Dexter.withContext(MainActivity.this).withPermissions(Manifest.permission.CAMERA,Manifest.permission.CALL_PHONE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                        if(Environment.isExternalStorageManager())
                        {

                            *//*try {
                                folder_path = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + "/" + folderName);
                                mypath = new File(folder_path, "Patient.bin");
                                mypath.listFiles();
                            } catch (Exception exception) {
                                exception.printStackTrace();
                            }*//*
                        }
                        else
                        {
                            Intent permissionIntent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                            startActivity(permissionIntent);
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                }).check();
    }*/



}
