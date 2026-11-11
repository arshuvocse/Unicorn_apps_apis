package com.creatrix.unicorn.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import com.creatrix.unicorn.Activity.Approval.ApprovalPanelFragment;
import com.creatrix.unicorn.Activity.Fragment.HomeFragment;
import com.creatrix.unicorn.Activity.Fragment.MoreFragment;
import com.creatrix.unicorn.Activity.Fragment.ReportFragment;
import com.creatrix.unicorn.DBAdapter.DBCrudHelper;
import com.creatrix.unicorn.DBAdapter.SyncDb_Helper;
import com.creatrix.unicorn.Services.SyncCheckWorker;
import com.creatrix.unicorn.R;
import com.creatrix.unicorn.UtilityHelper.LocationAccess;
import com.creatrix.unicorn.UtilityHelper.SessionManagement;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;
import java.util.HashMap;
public class MainDashboardActivity extends AppCompatActivity {

    public static final String EXTRA_OPEN_HOME = "open_home";
    ChipNavigationBar bottomNav;
    FragmentManager fragmentManager;
    //ImageBadgeView badgeView;
    SessionManagement session;
    SyncDb_Helper syncDb_helper;
    DBCrudHelper dbCrudHelper;
    private static final int REQUEST_LOCATION_PERMISSION = 9001;
    private static final String PREF_KEY_REQUESTED_LOCATION_PERMISSION = "requested_location_permission";
    private AlertDialog gpsDisabledDialog;
    private AlertDialog locationPermissionDialog;
    private final BroadcastReceiver locationProviderReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && LocationManager.PROVIDERS_CHANGED_ACTION.equals(intent.getAction())) {
                checkLocationStatus();
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_dashboard);
        bottomNav = findViewById(R.id.bottomNav);
        session = new SessionManagement(getApplicationContext());
        HashMap<String, String> user = session.getUserDetails();
        String empId = user.get(SessionManagement.KEY_EmpId);
        syncDb_helper = new SyncDb_Helper(this);
        dbCrudHelper = new DBCrudHelper(this);

        // Lightweight background check: only re-syncs Customer/Doctor lists when the
        // server-side count actually differs from what's stored locally.
        try {
            SyncCheckWorker.schedule(getApplicationContext());
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        //TODO:
      /*  if(!syncDb_helper.CheckDataInTable("tblInitTable")){
            try {
                Intent i = new Intent(MainDashboardActivity.this, SyncFromServerAc.class);
                startActivity(i);
                finish();
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }*/

//        GetNotificationCount(Integer.parseInt(empId));

//        final Handler handler = new Handler();
//        handler.postDelayed(new Runnable() {
//            public void run() {
//              GetNotificationCount(Integer.parseInt(empId));
////                Toast.makeText(MainDashboardActivity.this,"After 30 Sec ",Toast.LENGTH_LONG).show();
//                handler.postDelayed(this, 20000); //now is every 1 minutes
//            }
//        }, 20000); //Every 120000 ms (1 minutes)


//        badgeView = findViewById(R.id.notifiationBadge);
//        badgeView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent i = new Intent(MainDashboardActivity.this, NotificationActivity.class);
//                startActivity(i);
//                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
//            }
//        });
        FirebaseMessaging.getInstance().subscribeToTopic("mychannel")
                .addOnCompleteListener(task -> {
                    //String msg = getString(R.string.msg_subscribed);
                    if (!task.isSuccessful()) {
                       // msg = getString(R.string.msg_subscribe_failed);
                    }

                });
        if (savedInstanceState==null){
            openHomeFragment();
        }
        bottomNav.setOnItemSelectedListener(id -> {
            Fragment fragment = null;
            switch (id){
                case R.id.navigation_home:
                    fragment = new HomeFragment();
                    break;
                case R.id.navigation_approval:
                    fragment = new ApprovalPanelFragment();
                    break;
                case R.id.navigation_reports:
                    fragment = new ReportFragment();
                    break;
                case R.id.navigation_more:
                    fragment = new MoreFragment();
                    break;
            }
            if (fragment !=null){
                fragmentManager = getSupportFragmentManager();
                fragmentManager.beginTransaction().setCustomAnimations(R.anim.slide_in_right,R.anim.slide_out_left).replace(R.id.fragmentContainer,fragment).commit();
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        if (intent != null && intent.getBooleanExtra(EXTRA_OPEN_HOME, false)) {
            openHomeFragment();
        }
    }

    private void openHomeFragment() {
        bottomNav.setItemSelected(R.id.navigation_home,true);
        fragmentManager = getSupportFragmentManager();
        HomeFragment homeFragment = new HomeFragment();
        fragmentManager.beginTransaction().setCustomAnimations(R.anim.slide_in_right,R.anim.slide_out_left).replace(R.id.fragmentContainer,homeFragment).commit();
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Exit App")
                .setMessage("Are you sure you want to exit?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            //TODO:Exit hole login Dorkar Porbe na
                           /* session.logoutUser();
                            dbCrudHelper._deleteAllRecordsFromaTable("tblInitTable");
                            dbCrudHelper._deleteAllRecordsFromaTable("tbl_ProductInfo");
                            dbCrudHelper._deleteAllRecordsFromaTable("tbl_ProductSampleInfo");
                            dbCrudHelper._deleteAllRecordsFromaTable("tblCustomerInfo");
                            dbCrudHelper._deleteAllRecordsFromaTable("tblDoctorInfo");
                            dbCrudHelper._deleteAllRecordsFromaTable("tblDoctorBrand");*/
                            finish();
                        } catch (Exception exception) {
                            exception.printStackTrace();
                        }
                    }

                })
                .setNegativeButton("No", null)
                .show();
    }
    /*public void GetNotificationCount(int empId){

        try{
            GetDataService service = RetrofitClientInstance.getRetrofitInstance().create(GetDataService.class);
            Call<Integer> call = service.GetNotificationCount(empId);
            HttpUrl ds = call.request().url();
            call.enqueue(new Callback<Integer>() {
                @Override
                public void onResponse(Call<Integer> call, Response<Integer> response) {
                    if(response.body() !=null){
                     Integer nCount = response.body();
                     if(nCount > 0){
                         badgeView.startAnimation(AnimationUtils.loadAnimation(MainDashboardActivity.this,R.anim.shake));
                         badgeView.setBadgeValue(nCount);
                    }
                    }


                }
                @Override
                public void onFailure(Call<Integer> call, Throwable t) {
                    Log.d("zxc", "f: "+t);

                    if(t instanceof SocketTimeoutException){
                    }else{
                    }
                }
            });

        }catch (Exception ex){
            Log.d("zxc", "e: "+ex);

        }

    }*/


    @Override
    protected void onRestart() {
        super.onRestart();
        HashMap<String, String> user = session.getUserDetails();
        String empId = user.get(SessionManagement.KEY_EmpId);

    }

    @Override
    protected void onResume() {
        super.onResume();
        checkLocationStatus();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            checkLocationStatus();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver(locationProviderReceiver, new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION));
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(locationProviderReceiver);
    }

    private void checkLocationStatus() {
        if (!isLocationEnabled()) {
            showGpsDisabledDialogIfNeeded();
            return;
        }
        if (gpsDisabledDialog != null && gpsDisabledDialog.isShowing()) {
            gpsDisabledDialog.dismiss();
        }
        showLocationPermissionDialogIfNeeded();
    }

    private void showGpsDisabledDialogIfNeeded() {
        if (isLocationEnabled()) {
            if (gpsDisabledDialog != null && gpsDisabledDialog.isShowing()) {
                gpsDisabledDialog.dismiss();
            }
            return;
        }

        if (gpsDisabledDialog != null && gpsDisabledDialog.isShowing()) {
            return;
        }

        gpsDisabledDialog = new AlertDialog.Builder(this)
                .setMessage("GPS is disabled in your device. Would you like to enable it?")
                .setCancelable(false)
                .setPositiveButton("Goto Settings Page To Enable GPS",
                        (dialog, id) -> {
                            Intent callGPSSettingIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(callGPSSettingIntent);
                        })
                .create();
        gpsDisabledDialog.show();
    }

    private void showLocationPermissionDialogIfNeeded() {
        if (LocationAccess.hasForegroundPermission(this)) {
            if (locationPermissionDialog != null && locationPermissionDialog.isShowing()) {
                locationPermissionDialog.dismiss();
            }
            return;
        }

        if (locationPermissionDialog != null && locationPermissionDialog.isShowing()) {
            return;
        }

        boolean rationaleShouldShow = ActivityCompat.shouldShowRequestPermissionRationale(
                this, Manifest.permission.ACCESS_FINE_LOCATION);
        boolean previouslyRequested = getPreferences(MODE_PRIVATE)
                .getBoolean(PREF_KEY_REQUESTED_LOCATION_PERMISSION, false);
        boolean permanentlyDenied = previouslyRequested && !rationaleShouldShow;

        locationPermissionDialog = new AlertDialog.Builder(this)
                .setTitle("Location Permission Required")
                .setMessage("You must grant location permission to use this app.")
                .setCancelable(false)
                .setPositiveButton(permanentlyDenied ? "Open App Settings" : "Grant Permission",
                        (dialog, id) -> {
                            if (permanentlyDenied) {
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                        Uri.fromParts("package", getPackageName(), null));
                                startActivity(intent);
                            } else {
                                getPreferences(MODE_PRIVATE).edit()
                                        .putBoolean(PREF_KEY_REQUESTED_LOCATION_PERMISSION, true)
                                        .apply();
                                ActivityCompat.requestPermissions(this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        REQUEST_LOCATION_PERMISSION);
                            }
                        })
                .create();
        locationPermissionDialog.show();
    }

    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (locationManager == null) {
            return false;
        }

        try {
            return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                    || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception exception) {
            return false;
        }
    }
}
