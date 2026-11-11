package com.creatrix.unicorn.Activity.Doctor.DCR;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.creatrix.unicorn.Activity.Customer.CustomerActivity;
import com.creatrix.unicorn.Activity.Doctor.DoctorListActivity;
import com.creatrix.unicorn.Activity.MainDashboardActivity;
import com.creatrix.unicorn.Activity.TodaysTaskActivity;
import com.creatrix.unicorn.DBAdapter.DBCrudHelper;
import com.creatrix.unicorn.Interface.IDCR;
import com.creatrix.unicorn.Model.DcrSM;
import com.creatrix.unicorn.Model.Doctor.DoctorBrand;
import com.creatrix.unicorn.Model.Doctor.DoctorChamberName;
import com.creatrix.unicorn.Model.Doctor.DoctorListViewModel;
import com.creatrix.unicorn.Model.Gift;
import com.creatrix.unicorn.Model.TourTypeViewModel;
import com.creatrix.unicorn.Model.UserByRole;
import com.creatrix.unicorn.Model.UserRole;
import com.creatrix.unicorn.Presenter.DCRPresenter;
import com.creatrix.unicorn.R;
import com.creatrix.unicorn.RecyclerAdapter._VisitedWithRecylcer;
import com.creatrix.unicorn.RecyclerAdapter._product_brandAdapter;
import com.creatrix.unicorn.RecyclerAdapter._product_giftAdapter;
import com.creatrix.unicorn.Services.Constants;
import com.creatrix.unicorn.UtilityHelper.DateTimeUtil;
import com.creatrix.unicorn.UtilityHelper.LocationGuard;
import android.widget.Toast;
import com.creatrix.unicorn.UtilityHelper.NetworkInformation;
import com.creatrix.unicorn.UtilityHelper.SessionManagement;
import com.creatrix.unicorn.UtilityHelper.SnackBarManagement;
import com.creatrix.unicorn.databinding.ActivityAddDCRBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.Priority;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import static com.creatrix.unicorn.Activity.Attendance.AttendanceActivity.MY_PERMISSIONS_REQUEST_LOCATION;

public class AddDCRActivity extends AppCompatActivity implements LocationListener, IDCR.View, _product_giftAdapter.QtyListener, _VisitedWithRecylcer.DltListener {
    ActivityAddDCRBinding binding;
    private static final String TAG = "AddDCRActivity";
    SessionManagement session;
    IDCR.Presenter presenter;
    DBCrudHelper dbCrudHelper;

    _product_giftAdapter mAdapterPoduct_Gift;
    _product_brandAdapter mAdapterPoduct_Brand;
    _VisitedWithRecylcer aVistiedAdapter;

    List<Gift> aProList_Gift = new ArrayList<>();
    String[] listItemProduct_Gift;
    boolean[] checkedItems_Gift;

    ArrayList<Integer> mUserItems_Gift = new ArrayList<>();
    List<Gift> aGiftList = new ArrayList<>();
    List<Gift> aFinalGiftList = new ArrayList<>();


    List<DoctorBrand> aProList_Brand = new ArrayList<>();
    String[] listItemBrand;
    boolean[] checkedItems_Brand;
    ArrayList<Integer> mUserItems_Brand = new ArrayList<>();
    List<DoctorBrand> aBrandList = new ArrayList<>();

    List<TourTypeViewModel> aTourTypeLIst;
    List<DoctorChamberName> aChamberList;
    boolean isCvrFlow = false;
    // DCR or CVR ("ccrAdapter") sent to the server; a pending (offline) entry keeps the type it was saved with.
    String visitType;

    List<UserByRole> aVisitedList = new ArrayList<>();
    String empId, roletypeid, entryTime, empName, empCode;

    int dcrLocalId;
    String edit_dName;
    String lat, lon, setStreetAddress;
    android.location.Location lastFix;
    LocationManager locationManager;
    int giftQty, giftID;

    private final LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(@NonNull LocationResult locationResult) {
            super.onLocationResult(locationResult);
            android.location.Location loc = locationResult.getLastLocation();
            if (loc == null) return; // can be null on first hit or if provider cold-starts
            applyLocation(loc);
        }
    };

    private void applyLocation(android.location.Location loc) {
        double latitude = loc.getLatitude();
        double longitude = loc.getLongitude();
        lastFix = loc;

        // Set the coordinates first so a slow/failed reverse-geocode can't leave them empty.
        lat = String.valueOf(latitude);
        lon = String.valueOf(longitude);
        if (binding != null && binding.latlon != null) {
            binding.latlon.setText(lat + "," + lon);
        }

        try {
            setStreetAddress = getCompleteAddressString(latitude, longitude);
        } catch (Exception ignored) {
            // keep going even if reverse-geocode fails
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Start fetching as soon as the user grants location (previously nothing happened until a manual retry).
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getCLocation();
        }
    }

    @Override
    protected void onDestroy() {
        try {
            LocationServices.getFusedLocationProviderClient(this).removeLocationUpdates(locationCallback);
        } catch (Exception ignored) { }
        super.onDestroy();
    }

    private void navigateHome() {
        Intent i = new Intent(AddDCRActivity.this, DoctorListActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.putExtra("From", "DCR");
        startActivity(i);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        finish();
    }

    @Override
    public void onBackPressed() {
        navigateHome();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddDCRBinding.inflate(getLayoutInflater());
        //setContentView(R.layout.activity_add_d_c_r);
        setContentView(binding.getRoot());
        dbCrudHelper = new DBCrudHelper(AddDCRActivity.this);
        binding.toolbarCustom.setNavigationOnClickListener(v -> navigateHome());

        binding.rgShift.setOnCheckedChangeListener((group, checkedId) -> applyChamberShiftFilter());
        binding.chamberSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateChamberInfoSection();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                binding.chamberInfoSection.setVisibility(View.GONE);
            }
        });

        binding.chamberLocationBtn.setOnClickListener(v -> saveChamberLocation());

        //imgaeView = findViewById(R.id.imgaeView);
        presenter = new DCRPresenter(this, this);
        session = new SessionManagement(getApplicationContext());
        session.checkLogin();
        HashMap<String, String> user = session.getUserDetails();
        empId = user.get(SessionManagement.KEY_EmpId);
        roletypeid = user.get(SessionManagement.KEY_EmpRoleTypeId);
        empName = user.get(SessionManagement.KEY_UserName);
        empCode = user.get(SessionManagement.KEY_EmpMasterCode);

        if (ContextCompat.checkSelfPermission(AddDCRActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(AddDCRActivity.this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(AddDCRActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION}, 101);
        }
        locationManager = (LocationManager) AddDCRActivity.this.getSystemService(Context.LOCATION_SERVICE);

        if (!NetworkInformation.isConnected(this)) {
            checkLocationPermission();
            locationEnabled();
            getCLocation();
        } else {
        }

        Gson gson = new Gson();
        DoctorListViewModel aInfoData = gson.fromJson(getIntent().getStringExtra("myjson"), DoctorListViewModel.class);
        visitType = Constants.WHO;
        switch (Constants.WHO) {
            case "PendingDcrAdapter":
                DcrSM pData = gson.fromJson(getIntent().getStringExtra("dcrDtails"), DcrSM.class);
                dcrLocalId = pData.getDcrId();
                binding.dcrdate.setText(pData.getDcrDate());
                //  binding.visitedWithAdd.setVisibility(View.GONE);
                //   binding.giftAdd.setVisibility(View.GONE);
                presenter.GetVisitType();
                if ("ccrAdapter".equals(pData.getType())) {
                    visitType = "ccrAdapter";
                    isCvrFlow = true;
                    binding.rgShift.setVisibility(View.GONE);
                    binding.chamberInfoSection.setVisibility(View.GONE);
                    binding.toolbarTitle.setText("Add CVR");
                    presenter.GetCustomerChamber(aInfoData.getDoctorId());
                } else {
                    presenter.GetChamber(aInfoData.getDoctorId());
                }
                presenter.GetBrand();

                if (pData.getVisitTypeName() != null) {
                    edit_dName = pData.getVisitTypeName();
                    binding.spinerVisitType.setSelection(getIndex(binding.spinerVisitType, edit_dName));
                } else {
                }
                if (pData.getDoclist().getChemberName() != null) {
                    edit_dName = pData.getDoclist().getChemberName();
                    preselectShiftForEdit(edit_dName);
                    binding.chamberSpinner.setSelection(getIndex(binding.chamberSpinner, edit_dName));
                } else {
                }
                aVisitedList = pData.getAempList();
                if (aVisitedList != null) {
                    aVistiedAdapter = new _VisitedWithRecylcer(AddDCRActivity.this, aVisitedList, this::onDltClick);
                    RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
                    binding.recyclerViewVisitedWith.setLayoutManager(mLayoutManager);
                    binding.recyclerViewVisitedWith.setItemAnimator(new DefaultItemAnimator());
                    binding.recyclerViewVisitedWith.setAdapter(aVistiedAdapter);
                    aVistiedAdapter.notifyDataSetChanged();
                }

                aBrandList = pData.getDoctorBrand();
                if (aBrandList != null) {
                    mAdapterPoduct_Brand = new _product_brandAdapter(AddDCRActivity.this, aBrandList);
                    RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
                    binding.recyclerViewBrand.setLayoutManager(mLayoutManager);
                    binding.recyclerViewBrand.setItemAnimator(new DefaultItemAnimator());
                    binding.recyclerViewBrand.setAdapter(mAdapterPoduct_Brand);
                    mAdapterPoduct_Brand.notifyDataSetChanged();
                }

                aGiftList = pData.getGiftList();
                if (aGiftList != null) {
                    mAdapterPoduct_Gift = new _product_giftAdapter(aGiftList, this);
                    RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
                    binding.recyclerViewGift.setLayoutManager(mLayoutManager);
                    binding.recyclerViewGift.setItemAnimator(new DefaultItemAnimator());
                    binding.recyclerViewGift.setAdapter(mAdapterPoduct_Gift);
                    mAdapterPoduct_Gift.notifyDataSetChanged();

                    // aProList_Gift = aGiftList;
                  /*  listItemProduct_Gift = new String[aGiftList.size()];
                    try {

                        for(int i=0;i<aGiftList.size();i++)
                        {
                            int pos=aGiftList.get(i).getPosition();
                            Toast.makeText(this, "lp "+pos, Toast.LENGTH_SHORT).show();
                            mUserItems_Gift.add(pos);
                        }
                        checkedItems_Gift = new boolean[listItemProduct_Gift.length];
                        for (int i = 0; i < checkedItems_Gift.length; i++) {
                            checkedItems_Gift[i] = true;
                            //mUserItems_Gift.clear();
                        }
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }*/

                  /*   try {
                            aProList_Gift = aGiftList;
                            listItemProduct_Gift = new String[aProList_Gift.size()];
                            for (int i = 0; i < aProList_Gift.size(); i++) {
                                listItemProduct_Gift[i] = aProList_Gift.get(i).getProductName();
                            }
                            checkedItems_Gift = new boolean[listItemProduct_Gift.length];


                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }*/
                }

                binding.comment.setEnabled(false);
                binding.comment.setText(pData.getRemarks());

                break;
            case "DoclitAdapter":
                binding.dcrdate.setText(DateTimeUtil.getDisplayDateTime());
                try {
                    presenter.GetVisitType();
                    presenter.GetChamber(aInfoData.getDoctorId());
                   try {
                       presenter.GetBrand();
                   }catch(Exception e) {

                    }
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
                break;
            case "ToDayDoclitAdapter":
                binding.dcrdate.setText(DateTimeUtil.getDisplayDateTime());
                try {
                    presenter.GetVisitType();
                    presenter.GetChamber(aInfoData.getDoctorId());
                   try {
                       presenter.GetBrand();
                   }catch(Exception e) {

                    }
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
                break;
            case "ccrAdapter":

                isCvrFlow = true;
                binding.rgShift.setVisibility(View.GONE);
                binding.chamberInfoSection.setVisibility(View.GONE);
                binding.toolbarTitle.setText("Add CVR");
            binding.dcrdate.setText(DateTimeUtil.getDisplayDateTime());
                try {
                    presenter.GetVisitType();
                    presenter.GetCustomerChamber(aInfoData.getDoctorId());
                   try {
                       presenter.GetBrand();
                   }catch(Exception e) {

                    }
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
                break;
        }
        try {
            binding.dochead.nameTxt.setText(aInfoData.getDoctorName());
            binding.dochead.mobileTxt.setText(aInfoData.getDocContact());
            binding.dochead.doctypeTxt.setText(aInfoData.getDoctorTypeName());
            binding.dochead.programTxt.setText(aInfoData.getProgramTypeName());
//            addressTxt.setText(aInfoData.getChamberAddress());
        } catch (Exception ex) {
            Log.e(TAG, "onCreate: ", ex);
        }
        binding.brandAdd.setOnClickListener(v -> showDialog_Brand());
        binding.visitedWithAdd.setOnClickListener(v -> presenter.GetUserRole());

        entryTime = new SimpleDateFormat("hh:mm a", Locale.US).format(new Date());

        binding.latlon.setText(lat + "," + lon);
        binding.submitbtnClick.setOnClickListener(v -> {
            if (!isCommentValid()) {
                return;
            }
            if (aVisitedList.size() == 0) {
                SnackBarManagement._error_CustomMessage(findViewById(android.R.id.content), "Visited With is Required");
                return;
            }
            SubmitDCR(aInfoData, empId);
        });
        binding.location.setOnClickListener(view -> {
            binding.latlon.setText("Fetching location...");
            getCLocation();
        });
        binding.latlon.setOnClickListener(view -> {
            binding.latlon.setText("Fetching location...");
            getCLocation();
        });

        try {
            binding.latlon.setText("Fetching location...");
            getCLocation();
        } catch (Exception e){

        }
        askLocationAccuracy();
    }

    // Both re-fetch the location once the user comes back from the system dialog / WiFi panel.
    private final ActivityResultLauncher<IntentSenderRequest> accuracyLauncher = registerForActivityResult(
            new ActivityResultContracts.StartIntentSenderForResult(), result -> {
                getCLocation();
                askWifiOn();
            });
    private final ActivityResultLauncher<Intent> wifiLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> getCLocation());

    /** Android's own dialog asks the user to turn on Google Location Accuracy (GPS + WiFi + mobile network). */
    private void askLocationAccuracy() {
        LocationSettingsRequest request = new LocationSettingsRequest.Builder()
                .addLocationRequest(new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000L).build())
                .setAlwaysShow(true)
                .build();
        LocationServices.getSettingsClient(this).checkLocationSettings(request)
                .addOnSuccessListener(response -> askWifiOn())
                .addOnFailureListener(error -> {
                    if (error instanceof ResolvableApiException) {
                        try {
                            accuracyLauncher.launch(new IntentSenderRequest.Builder(
                                    ((ResolvableApiException) error).getResolution()).build());
                            return;
                        } catch (RuntimeException ignored) {
                            // no resolution screen on this device; WiFi still helps
                        }
                    }
                    askWifiOn();
                });
    }

    /** WiFi (even unconnected) gives a far better fix indoors; asks first, then opens the WiFi panel. */
    private void askWifiOn() {
        if (isFinishing() || isDestroyed()) return;
        WifiManager wifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifi == null || wifi.isWifiEnabled() || wifi.isScanAlwaysAvailable()) return;
        new AlertDialog.Builder(this)
                .setTitle("Turn on WiFi")
                .setMessage("WiFi helps find your location inside a hospital or chamber. You do not need to connect to any network.")
                .setPositiveButton("Turn on", (d, w) -> {
                    try {
                        wifiLauncher.launch(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
                                ? new Intent(Settings.Panel.ACTION_WIFI)
                                : new Intent(Settings.ACTION_WIFI_SETTINGS));
                    } catch (ActivityNotFoundException ignored) {
                        // no WiFi settings screen; the location still works without it
                    }
                })
                .setNegativeButton("Not now", null)
                .show();
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(this)
                        .setTitle("Location Permission Needed")
                        .setMessage("This app needs the Location permission, please accept to use location functionality")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(AddDCRActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
        }
    }

    private void locationEnabled() {
        LocationManager lm = (LocationManager) AddDCRActivity.this.getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;
        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!gps_enabled && !network_enabled) {
            new AlertDialog.Builder(AddDCRActivity.this)
                    .setTitle("Enable GPS Service")
                    .setMessage("We need your GPS location to show Near Places around you.")
                    .setCancelable(false)
                    .setPositiveButton("Enable", new
                            DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                                    startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                                    //turnGPSOn();
                                }
                            })

                    // .setNegativeButton("Cancel", null)
                    .show();
        }
    }

    // Request code (only used if we need to ask for permission here)
    private static final int REQ_LOCATION = 1001;
    public void getCLocation() {
        try {
            // ---- Permission check ----
            boolean fine = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED;
            boolean coarse = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED;

            if (!fine && !coarse) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                        REQ_LOCATION
                );
                return;
            }

            // Multi-tier location: Fused cache → live Fused/Network → IP geolocation fallback (indoors).
            LocationGuard.requestFix(this, (location, error) -> {
                if (isFinishing() || isDestroyed()) return;
                if (location != null) {
                    applyLocation(location);
                } else if (error != null && lastFix == null) {
                    if (binding != null && binding.latlon != null) {
                        binding.latlon.setText("Location not found. Tap the location button to retry.");
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    public void showDialog_Gift() {
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(AddDCRActivity.this);
        mBuilder.setTitle("Select Promo Gift");
        mBuilder.setMultiChoiceItems(listItemProduct_Gift, checkedItems_Gift, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int position, boolean isChecked) {
                if (isChecked) {
                    mUserItems_Gift.add(position);
                } else {
                    mUserItems_Gift.remove((Integer.valueOf(position)));
                }
            }
        });
        mBuilder.setCancelable(false);
        mBuilder.setPositiveButton("Done", (dialogInterface, which) -> {
            String item = "";
            aGiftList.clear();
            try {
                if (mUserItems_Gift != null) {
                    for (int i = 0; i < mUserItems_Gift.size(); i++) {
                        int pos = mUserItems_Gift.get(i);
                        Gift g = new Gift();
                        g.setPosition(pos);
                        g.setProductId(aProList_Gift.get(pos).getProductId());
                        g.setProductName(aProList_Gift.get(pos).getProductName());
                        aGiftList.add(g);

                        item = item + listItemProduct_Gift[mUserItems_Gift.get(i)];
                        if (i != mUserItems_Gift.size() - 1) {
                            item = item + ",";
                        }
                    }

                    if (aGiftList != null) {
                        mAdapterPoduct_Gift = new _product_giftAdapter(aGiftList, AddDCRActivity.this);
                        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
                        binding.recyclerViewGift.setLayoutManager(mLayoutManager);
                        binding.recyclerViewGift.setItemAnimator(new DefaultItemAnimator());
                        binding.recyclerViewGift.setAdapter(mAdapterPoduct_Gift);
                        mAdapterPoduct_Gift.notifyDataSetChanged();
                    }


                }


            } catch (Exception ex) {
                throw ex;
            }


        });

        mBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        mBuilder.setNeutralButton("Clear All", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                for (int i = 0; i < checkedItems_Gift.length; i++) {
                    aGiftList.clear();
                    // mAdapterPoduct_Gift.notifyDataSetChanged();
                    checkedItems_Gift[i] = false;
                    mUserItems_Gift.clear();
                }
            }
        });

        AlertDialog mDialog = mBuilder.create();
        mDialog.show();


    }

    public void showDialog_Brand() {

        AlertDialog.Builder mBuilder = new AlertDialog.Builder(AddDCRActivity.this);
        mBuilder.setTitle("Select Brand ");
        mBuilder.setMultiChoiceItems(listItemBrand, checkedItems_Brand, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int position, boolean isChecked) {
                if (isChecked) {
                    mUserItems_Brand.add(position);
                } else {
                    mUserItems_Brand.remove((Integer.valueOf(position)));
                }
            }
        });
        mBuilder.setCancelable(false);
        mBuilder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                String item = "";
                aBrandList.clear();
                try {
                    if (mUserItems_Brand != null) {
                        for (int i = 0; i < mUserItems_Brand.size(); i++) {
                            int pos = mUserItems_Brand.get(i);
                            aBrandList.add(aProList_Brand.get(pos));
                            item = item + listItemBrand[mUserItems_Brand.get(i)];
                            if (i != mUserItems_Brand.size() - 1) {
                                item = item + ",";
                            }
                        }

                        if (aBrandList != null) {

                            mAdapterPoduct_Brand = new _product_brandAdapter(AddDCRActivity.this, aBrandList);
                            RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
                            binding.recyclerViewBrand.setLayoutManager(mLayoutManager);
                            binding.recyclerViewBrand.setItemAnimator(new DefaultItemAnimator());
                            binding.recyclerViewBrand.setAdapter(mAdapterPoduct_Brand);
                            mAdapterPoduct_Brand.notifyDataSetChanged();
                        }


                    }


                } catch (Exception ex) {
                    throw ex;
                }


            }
        });

        mBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        mBuilder.setNeutralButton("Clear All", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                for (int i = 0; i < checkedItems_Brand.length; i++) {
                    aBrandList.clear();
                    /*int pos=0;
                    {
                        pos=aBrandList.size();
                    }*/
                    mAdapterPoduct_Brand.notifyDataSetChanged();
                    checkedItems_Brand[i] = false;
                    mUserItems_Brand.clear();
                }
            }
        });

        AlertDialog mDialog = mBuilder.create();
        mDialog.show();


    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left,
                R.anim.slide_out_right);
    }

    @Override
    public void OnVisitTypeGet(List<TourTypeViewModel> aInfo) {
        try {
            if (aInfo != null) {
                aTourTypeLIst = aInfo;
                ArrayAdapter<TourTypeViewModel> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, aInfo);
                dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                binding.spinerVisitType.setAdapter(dataAdapter);
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }


    }

    @Override
    public void OnChamberGet(List<DoctorChamberName> aInfo) {
        try {
            if (aInfo != null) {
                aChamberList = aInfo;
                applyChamberShiftFilter();
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    /**
     * Filters the loaded chamber list by the selected Morning/Evening shift and repopulates
     * the spinner. Chambers without a saved Shift (older entries) are shown under either
     * choice so they don't silently disappear. Not used for the CVR (customer) flow, which
     * has no shift concept and keeps showing the full list.
     */
    private void applyChamberShiftFilter() {
        try {
            if (aChamberList == null) return;

            if (isCvrFlow) {
                setChamberSpinnerItems(aChamberList);
                return;
            }

            int checkedId = binding.rgShift.getCheckedRadioButtonId();
            if (checkedId == -1) {
                setChamberSpinnerItems(new ArrayList<>());
                return;
            }

            boolean wantEvening = checkedId == R.id.rb_evening;
            List<DoctorChamberName> filtered = new ArrayList<>();
            for (DoctorChamberName c : aChamberList) {
                boolean hasShift = c.getShift() != null && !c.getShift().trim().isEmpty();
                if (!hasShift || (wantEvening ? c.isEvening() : c.isMorning())) {
                    filtered.add(c);
                }
            }
            setChamberSpinnerItems(filtered);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void setChamberSpinnerItems(List<DoctorChamberName> aInfo) {
        ArrayAdapter<DoctorChamberName> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, aInfo);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.chamberSpinner.setAdapter(dataAdapter);
        updateChamberInfoSection();
    }

    // Map button shows only for a doctor chamber that still has no Lat/Lon (CVR customers have no chamber location)
    private void showChamberLocationButton(DoctorChamberName chamber) {
        boolean needsLocation = !isCvrFlow && chamber.getChemberId() > 0 && !chamber.hasLocation();
        binding.chamberLocationBtn.setVisibility(needsLocation ? View.VISIBLE : View.GONE);
    }

    private void saveChamberLocation() {
        Object selected = binding.chamberSpinner.getSelectedItem();
        if (!(selected instanceof DoctorChamberName)) return;
        DoctorChamberName chamber = (DoctorChamberName) selected;
        binding.chamberLocationBtn.setEnabled(false);
        LocationGuard.requestFix(this, (location, error) -> {
            if (isFinishing() || isDestroyed()) return;
            String fixError = location == null ? error : LocationGuard.checkFix(location);
            if (fixError != null) {
                binding.chamberLocationBtn.setEnabled(true);
                Toast.makeText(this, fixError, Toast.LENGTH_SHORT).show();
                return;
            }
            presenter.SaveChamberLocation(chamber, String.valueOf(location.getLatitude()), String.valueOf(location.getLongitude()));
        });
    }

    @Override
    public void OnChamberLocationSaved(DoctorChamberName chamber, String lat, String lon) {
        chamber.setLat(lat);
        chamber.setLon(lon);
        binding.chamberLocationBtn.setEnabled(true);
        updateChamberInfoSection();
        Toast.makeText(this, "Chamber location saved", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void OnChamberLocationError(String message) {
        binding.chamberLocationBtn.setEnabled(true);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void updateChamberInfoSection() {
        try {
            Object selected = binding.chamberSpinner.getSelectedItem();
            if (!(selected instanceof DoctorChamberName)) {
                binding.chamberInfoSection.setVisibility(View.GONE);
                return;
            }
            DoctorChamberName chamber = (DoctorChamberName) selected;
            String sittingTime = chamber.getSittingTime();
            String address = chamber.getAddress();
            boolean hasTime = sittingTime != null && !sittingTime.trim().isEmpty();
            boolean hasAddress = address != null && !address.trim().isEmpty();

            binding.chamberSittingTime.setVisibility(View.VISIBLE);
            binding.chamberSittingTime.setText("Time: " + (hasTime ? sittingTime.trim() : "N/A"));

            binding.chamberAddressTxt.setVisibility(View.VISIBLE);
            binding.chamberAddressTxt.setText("Address: " + (hasAddress ? address.trim() : "N/A"));

            binding.chamberInfoSection.setVisibility(View.VISIBLE);
            showChamberLocationButton(chamber);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    /**
     * Edit flow: a saved DCR only carries the chamber name, not its shift, so before
     * restoring the spinner selection by name we check the matching radio button first -
     * otherwise the shift filter would leave the spinner empty and the lookup would fail.
     */
    private void preselectShiftForEdit(String chamberName) {
        try {
            if (isCvrFlow || aChamberList == null || chamberName == null) return;
            for (DoctorChamberName c : aChamberList) {
                if (chamberName.equalsIgnoreCase(c.getChemberName())) {
                    binding.rgShift.check(c.isEvening() ? R.id.rb_evening : R.id.rb_morning);
                    return;
                }
            }
            binding.rgShift.check(R.id.rb_morning);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }


    @Override
    public void OnGiftProductGet(List<Gift> aList) {
        try {
            if (aList != null) {
                aProList_Gift = aList;
                listItemProduct_Gift = new String[aList.size()];
                for (int i = 0; i < aList.size(); i++) {
                    listItemProduct_Gift[i] = aList.get(i).getProductName();
                }
                checkedItems_Gift = new boolean[listItemProduct_Gift.length];

            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }

    }

    @Override
    public void OnDoctorBrandGet(List<DoctorBrand> aInfo) {
        try {
            if (aInfo != null) {
                aProList_Brand = aInfo;
                listItemBrand = new String[aInfo.size()];
                for (int i = 0; i < aInfo.size(); i++) {
                    listItemBrand[i] = aInfo.get(i).getBrandName();
                }
                checkedItems_Brand = new boolean[listItemBrand.length];

            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }


    @Override
    public void onUserRoleGet(List<UserRole> aList) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(AddDCRActivity.this);
        // Set the alert dialog title
        builder.setTitle("Choose Role");

        // Hardcoded array of roles
        final String[] aRoleList = {"Self", "Other Employee"};
        
        builder.setSingleChoiceItems(
                aRoleList, // Items list
                -1, // Index of checked item (-1 = no selection)
                new DialogInterface.OnClickListener() // Item click listener
                {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String selectedItem = aRoleList[i];
                        if (selectedItem.equals("Self")) {
                            //  List<UserByRole> aSelfList = new ArrayList<>();

                            aVisitedList = new ArrayList<>();
                            UserByRole user = new UserByRole();
                            user.setEmpName(empName);
                            user.setEmpMasterCode(empCode);
                            user.setEmpInfoId(Integer.parseInt(empId));
                            user.setUserRoleID(Integer.parseInt(roletypeid));
                            aVisitedList.add(user);
                            setSelf(aVisitedList);
                        } else {
                            openOtherEmployeeModal();
                        }
                        dialogInterface.dismiss();
                    }
                });

        // Set the alert dialog positive button
        builder.setPositiveButton("Close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        // Create the alert dialog
        AlertDialog dialog = builder.create();
        // Finally, display the alert dialog
        dialog.show();
    }

    private void LoadUserByRoleId(int userRoleID/*, int parseInt*/) {
        presenter.GetUserByRoleId(userRoleID/*, parseInt*/);
    }

    private void setSelf(List<UserByRole> aSelfList) {
        try {
            if (aSelfList != null) {
                if (aSelfList != null) {
                    aVistiedAdapter = new _VisitedWithRecylcer(AddDCRActivity.this, aSelfList, AddDCRActivity.this::onDltClick);
                    RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
                    binding.recyclerViewVisitedWith.setLayoutManager(mLayoutManager);
                    binding.recyclerViewVisitedWith.setItemAnimator(new DefaultItemAnimator());
                    binding.recyclerViewVisitedWith.setAdapter(aVistiedAdapter);
                    aVistiedAdapter.notifyDataSetChanged();
                }
            }


        } catch (Exception ex) {
            throw ex;
        }
    }

    @Override
    public void onUserGet(List<UserByRole> aList) {
        if (aList != null) {
            try {
                for (int i = 0; i < aList.size(); i++) {
                    UserByRole newUser = aList.get(i);
                    boolean exists = false;
                    for (UserByRole existing : aVisitedList) {
                        if (existing.getEmpInfoId() == newUser.getEmpInfoId()) {
                            exists = true;
                            break;
                        }
                    }
                    if (!exists) {
                        aVisitedList.add(newUser);
                    }
                }

                if (aVisitedList != null) {
                    aVistiedAdapter = new _VisitedWithRecylcer(AddDCRActivity.this, aVisitedList, AddDCRActivity.this::onDltClick);
                    RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
                    binding.recyclerViewVisitedWith.setLayoutManager(mLayoutManager);
                    binding.recyclerViewVisitedWith.setItemAnimator(new DefaultItemAnimator());
                    binding.recyclerViewVisitedWith.setAdapter(aVistiedAdapter);
                    aVistiedAdapter.notifyDataSetChanged();
                }
            } catch (Exception ex) {
                throw ex;
            }
        }
    }

    @Override
    public void onNoneffectiveGet(List<NonEffectiveReason> aList) {
    }

    public void SaveDCR(DoctorListViewModel dvm, String empId) {
        try {

            if (!IsValid()) {
                SnackBarManagement._error_CustomMessage(findViewById(android.R.id.content), "Please fill Date, Visit Type, Institute and Comment");
            } else {
                DcrSM aInfo = new DcrSM();
                TourTypeViewModel atpVm;
                atpVm = (TourTypeViewModel) binding.spinerVisitType.getSelectedItem();
                int visityTYpeId = atpVm.getTourTypeId();
                String visityTYpeName = atpVm.getTourTypeName();

                DoctorListViewModel dlvm = new DoctorListViewModel();
                DoctorChamberName chamberVM;
                chamberVM = (DoctorChamberName) binding.chamberSpinner.getSelectedItem();
                int chamberId = chamberVM.getChemberId();
                String chamberName = chamberVM.getChemberName();
                aInfo.setDoctorId(dvm.getDoctorId());
                aInfo.setDoctorName(dvm.getDoctorName());

                dlvm.setDocContact(dvm.getDocContact());
                dlvm.setDoctorTypeName(dvm.getDoctorTypeName());
                dlvm.setChemberName(chamberName);
                dlvm.setType(visitType);
                aInfo.setType(visitType);
                dlvm.setProgramTypeName(dvm.getProgramTypeName());

                aInfo.setDoclist(dlvm);
                aInfo.setSessionUser(empId);
                aInfo.setDcrDate(binding.dcrdate.getText().toString().trim().replace("Sept", "Sep"));
                aInfo.setEntryTime(entryTime);
                aInfo.setVisitTypeId(visityTYpeId);
                aInfo.setVisitTypeName(visityTYpeName);
                aInfo.setTourPlanTypeId(visityTYpeId);
                aInfo.setChamberId(chamberId);

                int checkedShiftId = binding.rgShift.getCheckedRadioButtonId();
                boolean isEvening = (checkedShiftId == R.id.rb_evening);
                aInfo.setShift(isEvening ? "Evening" : "Morning");
                aInfo.setIsMorning(isEvening ? "0" : "1");

                aInfo.getDoclist().setDoctorName(chamberName);
                //  aInfo.set(chamberId);
                aInfo.setRemarks(binding.comment.getText().toString());
                aInfo.setGiftList(new ArrayList<>());
                aInfo.setDoctorBrand(aBrandList);
                aInfo.setSampleList(new ArrayList<>());
                aInfo.setDocTPDetailsId(dvm.getDocTPDetailsId());
                aInfo.setAempList(aVisitedList);
                Gson gson = new Gson();
                String data = gson.toJson(aInfo);
                System.out.println("offline save" + data);

                if (dbCrudHelper.checkDataExistInDCRTable_(dcrLocalId) == true) {
                    boolean isOk;

                    isOk = dbCrudHelper.DeleteLocal_DcrData_SQLite(dcrLocalId);
                    if (isOk == true) {
                        boolean isResult = dbCrudHelper.UpdateDcrInfo_SQLite(aInfo);
                        if (isResult) {
                            RedirectSuccess("The DCR Updated Offline");
                        } else {
                            SnackBarManagement._warning_CustomMessage(binding.masterLayoutId, "Something went wrong.. Please try again");
                        }
                    }
                } else {
                    boolean isResult = dbCrudHelper.SaveDcrInfo_SQLite(aInfo);
                    if (isResult) {
                        RedirectSuccess("The DCR Saved Offline");
                    } else {
                        SnackBarManagement._warning_CustomMessage(binding.masterLayoutId, "Something went wrong.. Please try again");
                    }
                }

            }


        } catch (Exception exception) {
            Log.e(TAG, "SaveDCR: ", exception);
            exception.printStackTrace();
        }
    }

    public void SubmitDCR(DoctorListViewModel dcrSM, String empId) {
        SubmitDCR(dcrSM, empId, false);
    }

    private void SubmitDCR(DoctorListViewModel dcrSM, String empId, boolean fixRefreshed) {
        if (!IsValid()) {
            SnackBarManagement._error_CustomMessage(findViewById(android.R.id.content), "Please fill Date, Visit Type, Institute and Comment");
        } else {
            DcrSM aInfo = new DcrSM();
            TourTypeViewModel atpVm;
            atpVm = (TourTypeViewModel) binding.spinerVisitType.getSelectedItem();
            int visityTYpeId = atpVm.getTourTypeId();

            DoctorChamberName chamberVM;
            chamberVM = (DoctorChamberName) binding.chamberSpinner.getSelectedItem();
            int chamberId = chamberVM.getChemberId();


            aInfo.setDoctorId(dcrSM.getDoctorId());
            aInfo.setDcrDate(binding.dcrdate.getText().toString().trim().replace("Sept", "Sep"));
            aInfo.setEntryTime(entryTime);
            aInfo.setTourPlanTypeId(visityTYpeId);
            aInfo.setDocTPDetailsId(dcrSM.getDocTPDetailsId());
            aInfo.setChamberId(chamberId);

            int checkedShiftId = binding.rgShift.getCheckedRadioButtonId();
            boolean isEvening = (checkedShiftId == R.id.rb_evening);
            aInfo.setShift(isEvening ? "Evening" : "Morning");
            aInfo.setIsMorning(isEvening ? "0" : "1");

            aInfo.setRemarks(binding.comment.getText().toString());
            aInfo.setSessionUser(empId);
            aInfo.setGiftList(new ArrayList<>());
            aInfo.setSampleList(new ArrayList<>());
            aInfo.setDoctorBrand(aBrandList);
            aInfo.setAempList(aVisitedList);
            aInfo.setType(visitType);
            aInfo.setEntryDate_Apps(binding.dcrdate.getText().toString().trim().replace("Sept", "Sep"));

            // Strict location rule: precise permission + GPS on + fresh, accurate, non-mock fix
            // and (when the doctor has a saved location) the employee must be at the doctor.
            String locationError = LocationGuard.checkEnvironment(this);
            // Missing, stale (form took a while) or too coarse (indoors) fix: wait up to 20 s for a better
            // one instead of blocking the submit, then check once more.
            if (locationError == null && !fixRefreshed
                    && LocationGuard.checkFix(lastFix, LocationGuard.DCR_MAX_ACCURACY_M) != null) {
                SnackBarManagement._warning_CustomMessage(binding.masterLayoutId, "Getting an accurate location (up to 20 seconds)...");
                binding.submitbtnClick.setEnabled(false);
                LocationGuard.requestAccurateFix(this, LocationGuard.DCR_MAX_ACCURACY_M, 20_000L, (location, error) -> {
                    if (isFinishing() || isDestroyed()) return;
                    binding.submitbtnClick.setEnabled(true);
                    if (location != null) {
                        applyLocation(location);
                    }
                    SubmitDCR(dcrSM, empId, true);
                });
                return;
            }
            if (locationError == null) {
                locationError = LocationGuard.checkFix(lastFix, LocationGuard.DCR_MAX_ACCURACY_M);
            }
            if (locationError != null) {
                SnackBarManagement._warning_CustomMessage(binding.masterLayoutId, locationError);
                return;
            } else {
                aInfo.setLatitude(lat);
                aInfo.setLongitude(lon);
                aInfo.setStreetAddress(setStreetAddress);
            }

         /*   Gson gson=new Gson();
            String data=gson.toJson(aVisitedList);
            Toast.makeText(this, ""+data, Toast.LENGTH_SHORT).show();*/

            presenter.SaveDCR(aInfo);
        }
        try {

        } catch (Exception exception) {
            Log.e(TAG, "SaveDCR: ", exception);
            exception.printStackTrace();
        }
    }

    public boolean IsValid() {
        boolean isVal = true;

        if (binding.comment.getText().toString().trim().isEmpty()) {
            isVal = false;
        }

        String presDate = binding.dcrdate.getText().toString();
        if (presDate.equals("") || presDate == null) {
            isVal = false;
        }

        TourTypeViewModel atpVm;
        atpVm = (TourTypeViewModel) binding.spinerVisitType.getSelectedItem();
        int visityTYpeId = atpVm.getTourTypeId();
        if (visityTYpeId == 0) {
            isVal = false;
        }

        DoctorChamberName chamberVM;
        chamberVM = (DoctorChamberName) binding.chamberSpinner.getSelectedItem();

        if (chamberVM == null || chamberVM.getChemberId() == 0) {
            isVal = false;
        }

        return isVal;
    }

    private boolean isCommentValid() {
        if (binding.comment.getText().toString().trim().isEmpty()) {
            binding.comment.setError("Comment is required");
            binding.comment.requestFocus();
            SnackBarManagement._error_CustomMessage(
                    findViewById(android.R.id.content),
                    "Comment is required"
            );
            return false;
        }
        binding.comment.setError(null);
        return true;
    }

    @Override
    public void OnDcrSaveSuccess(String message) {
        /*new androidx.appcompat.app.AlertDialog.Builder(AddDCRActivity.this)
                .setTitle("Success")
                .setMessage(message)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        Intent i = new Intent(AddDCRActivity.this, MainDashboardActivity.class);
                        startActivity(i);
                        i.addFlags(i.FLAG_ACTIVITY_CLEAR_TOP | i.FLAG_ACTIVITY_CLEAR_TASK | i.FLAG_ACTIVITY_NEW_TASK);
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    }

                }).setCancelable(false).show();*/

        if (message.equals("Successfully Submitted")) {
            new AlertDialog.Builder(AddDCRActivity.this)
                    .setTitle("Success")
                    .setMessage(message)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            switch (Constants.WHO) {
                                case "DoclitAdapter":
                                 /*   Intent i = new Intent(AddDCRActivity.this, DoctorDashboardActivity.class);
                                   // i.addFlags(i.FLAG_ACTIVITY_CLEAR_TOP | i.FLAG_ACTIVITY_CLEAR_TASK | i.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(i);
                                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);*/
                                    finish();
                                    break;

                                    case "ccrAdapter":
                                    Intent i = new Intent(AddDCRActivity.this, TodaysTaskActivity.class);
                                   // i.addFlags(i.FLAG_ACTIVITY_CLEAR_TOP | i.FLAG_ACTIVITY_CLEAR_TASK | i.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(i);
                                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                                    //finish();
                                    break;
                                    case "ToDayDoclitAdapter":
                                    Intent i2 = new Intent(AddDCRActivity.this, TodaysTaskActivity.class);
                                   // i.addFlags(i.FLAG_ACTIVITY_CLEAR_TOP | i.FLAG_ACTIVITY_CLEAR_TASK | i.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(i2);
                                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                                    //finish();
                                    break;
                                case "PendingDcrAdapter":
                                    try {
                                        boolean isOk;
                                        isOk = dbCrudHelper.DeleteLocal_DcrTable_SQLite(dcrLocalId);
                                        // Toast.makeText(AddDCRActivity.this, "dcrLocalId inside "+dcrLocalId, Toast.LENGTH_SHORT).show();
                                        if (isOk == true) {
                                            onBackPressed();
                                            /*Intent a = new Intent(AddDCRActivity.this, DoctorDashboardActivity.class);
                                            //a.addFlags(a.FLAG_ACTIVITY_CLEAR_TOP | a.FLAG_ACTIVITY_CLEAR_TASK | a.FLAG_ACTIVITY_NEW_TASK);
                                            startActivity(a);*/
                                            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                                            finish();
                                        }

                                    } catch (Exception exception) {
                                        exception.printStackTrace();
                                    }

                                    break;

                            }


                        }

                    }).setCancelable(false).show();
        } else if (message.equals("Insufficient")) {
            SnackBarManagement._warning_CustomMessage(binding.masterLayoutId, "Insufficient Quantity!!");
        } else {
            SnackBarManagement._warning_CustomMessage(binding.masterLayoutId, "Something went wrong!!.Try Again");
        }


    }

    @Override
    public void OnDcrSaveError(String message) {
        new androidx.appcompat.app.AlertDialog.Builder(AddDCRActivity.this)
                .setTitle("Error")
                .setMessage(message)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }

                }).setCancelable(false).show();

    }

    public void RedirectSuccess(String txt) {
        new AlertDialog.Builder(AddDCRActivity.this)
                .setIcon(R.drawable.tikiconwhite)
                .setTitle("Success")
                .setMessage(txt + " has been " + " successfully.")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onBackPressed();
                    }
                }).setCancelable(false).show();
    }

    private int getIndex(Spinner designationSpinner, String edit_dname) {
        for (int i = 0; i < designationSpinner.getCount(); i++) {
            if (designationSpinner.getItemAtPosition(i).toString().equalsIgnoreCase(edit_dname)) {
                return i;
            }
        }
        return 0;
    }



   /* @Override
    public void Remove(int a,int b) {
        giftQty=a;
        giftID=b;

        //aFinalGiftList=new ArrayList<>();
       *//* Gift g=new Gift();
        g.setProductId(giftID);
        g.setQuantity(giftQty);
        aFinalGiftList.add(g);*//*
    }*/


    @Override
    public boolean onLongClick(int pos) {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
        builder1.setMessage("Are you sure wants to delete the Item ?");
        builder1.setCancelable(true);
        builder1.setPositiveButton(
                "Delete",
                (dialog, id) -> {
                    try {
                        aGiftList.remove(pos);
                        mAdapterPoduct_Gift.notifyItemRemoved(pos);
                        dialog.cancel();
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                });
        builder1.setNegativeButton(
                "Cancel",
                (dialog, id) -> dialog.cancel());

        AlertDialog alert11 = builder1.create();
        alert11.show();

        return true;
    }

    @Override
    public void onEditTextFocusChange(int postion, int value) {
        aGiftList.get(postion).setQuantity(value);
     /*   updateProductTotals(productListOrder.get(position));
        setTotals();*/
        if (!binding.recyclerViewGift.isComputingLayout()) {
            try {
                mAdapterPoduct_Gift.notifyItemChanged(postion);
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
    }

    @Override
    public boolean onDltClick(int position) {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
        builder1.setMessage("Are you sure wants to delete the Item ?");
        builder1.setCancelable(true);
        builder1.setPositiveButton(
                "Delete",
                (dialog, id) -> {
                    try {
                        aVisitedList.remove(position);
                        aVistiedAdapter.notifyItemRemoved(position);
                        dialog.cancel();
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                });
        builder1.setNegativeButton(
                "Cancel",
                (dialog, id) -> dialog.cancel());

        AlertDialog alert11 = builder1.create();
        alert11.show();
        return true;
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            double Lat = addresses.get(0).getLatitude();
            double Lon = addresses.get(0).getLongitude();
            lat = String.valueOf(Lat);
            lon = String.valueOf(Lon);
            setStreetAddress = addresses.get(0).getAddressLine(0);
        } catch (Exception e) {
        }
    }

    private String getCompleteAddressString(double LATITUDE, double LONGITUDE) {
        String strAdd = "";
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);
            if (addresses != null) {
                Address returnedAddress = addresses.get(0);
                StringBuilder strReturnedAddress = new StringBuilder("");

                for (int i = 0; i <= returnedAddress.getMaxAddressLineIndex(); i++) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n");
                }
                strAdd = strReturnedAddress.toString();
                Log.w("address", strReturnedAddress.toString());
            } else {
                Log.w("address", "No Address returned!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.w("address", "Canont get Address!");
        }
        return strAdd;
    }

    private void openOtherEmployeeModal() {
        List<OtherEmpWrapper> empList = new ArrayList<>();
        com.creatrix.unicorn.DBInfo.DBHelperMain dbHelperMain = new com.creatrix.unicorn.DBInfo.DBHelperMain(AddDCRActivity.this);
        android.database.sqlite.SQLiteDatabase database = dbHelperMain.getReadableDatabase();
        android.database.Cursor cursor = null;
        try {
            cursor = database.rawQuery("SELECT * FROM tblOtherEmpList", null);
            if (cursor.moveToFirst()) {
                do {
                    UserByRole user = new UserByRole();
                    int idIndex = cursor.getColumnIndex("EmpInfoId");
                    int codeIndex = cursor.getColumnIndex("EmpMasterCode");
                    if(idIndex != -1) user.setEmpInfoId(cursor.getInt(idIndex));
                    if(codeIndex != -1) {
                        String code = cursor.getString(codeIndex);
                        user.setEmpMasterCode(code);
                        user.setEmpName(code); 
                    }
                    user.setUserRoleID(0);
                    empList.add(new OtherEmpWrapper(user));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(cursor != null) cursor.close();
            database.close();
        }

        View dialogView = android.view.LayoutInflater.from(this).inflate(R.layout.searchable_list_dialog, null);
        android.widget.SearchView searchView = dialogView.findViewById(R.id.search);
        android.widget.ListView listView = dialogView.findViewById(R.id.listItems);

        OtherEmpAdapter adapter = new OtherEmpAdapter(this, empList);
        listView.setAdapter(adapter);

        searchView.setOnQueryTextListener(new android.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) { return false; }
            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return false;
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Employees");
        builder.setView(dialogView);
        builder.setPositiveButton("Confirm", (dialog, which) -> {
            List<UserByRole> selected = adapter.getSelectedUsers();
            for (UserByRole newUser : selected) {
                boolean exists = false;
                for (UserByRole existing : aVisitedList) {
                    if (existing.getEmpInfoId() == newUser.getEmpInfoId()) {
                        exists = true;
                        break;
                    }
                }
                if (!exists) {
                    aVisitedList.add(newUser);
                }
            }
            if (aVisitedList != null) {
                aVistiedAdapter = new _VisitedWithRecylcer(AddDCRActivity.this, aVisitedList, AddDCRActivity.this::onDltClick);
                androidx.recyclerview.widget.RecyclerView.LayoutManager mLayoutManager = new androidx.recyclerview.widget.LinearLayoutManager(getApplicationContext());
                binding.recyclerViewVisitedWith.setLayoutManager(mLayoutManager);
                binding.recyclerViewVisitedWith.setItemAnimator(new androidx.recyclerview.widget.DefaultItemAnimator());
                binding.recyclerViewVisitedWith.setAdapter(aVistiedAdapter);
                aVistiedAdapter.notifyDataSetChanged();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    class OtherEmpWrapper {
        UserByRole user;
        boolean isSelected;
        OtherEmpWrapper(UserByRole user) {
            this.user = user;
            this.isSelected = false;
        }
    }

    class OtherEmpAdapter extends android.widget.BaseAdapter implements android.widget.Filterable {
        private List<OtherEmpWrapper> originalList;
        private List<OtherEmpWrapper> filteredList;
        private Context context;

        public OtherEmpAdapter(Context context, List<OtherEmpWrapper> originalList) {
            this.context = context;
            this.originalList = originalList;
            this.filteredList = originalList;
        }

        @Override
        public int getCount() { return filteredList.size(); }
        @Override
        public Object getItem(int position) { return filteredList.get(position); }
        @Override
        public long getItemId(int position) { return position; }

        @Override
        public View getView(int position, View convertView, android.view.ViewGroup parent) {
            if (convertView == null) {
                convertView = android.view.LayoutInflater.from(context).inflate(R.layout.rv_doclist_checkbox, parent, false);
            }
            android.widget.CheckBox checkBox = convertView.findViewById(R.id.chkitm);
            android.widget.TextView textView = convertView.findViewById(R.id.docname);

            OtherEmpWrapper wrapper = filteredList.get(position);
            textView.setText(wrapper.user.getEmpName());
            
            checkBox.setOnCheckedChangeListener(null);
            checkBox.setChecked(wrapper.isSelected);

            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                wrapper.isSelected = isChecked;
            });
            
            convertView.setOnClickListener(v -> checkBox.setChecked(!checkBox.isChecked()));

            return convertView;
        }

        public List<UserByRole> getSelectedUsers() {
            List<UserByRole> selected = new ArrayList<>();
            for (OtherEmpWrapper wrapper : originalList) {
                if (wrapper.isSelected) {
                    selected.add(wrapper.user);
                }
            }
            return selected;
        }

        @Override
        public android.widget.Filter getFilter() {
            return new android.widget.Filter() {
                @Override
                protected android.widget.Filter.FilterResults performFiltering(CharSequence constraint) {
                    android.widget.Filter.FilterResults results = new android.widget.Filter.FilterResults();
                    if (constraint == null || constraint.length() == 0) {
                        results.values = originalList;
                        results.count = originalList.size();
                    } else {
                        String filterString = constraint.toString().toLowerCase();
                        List<OtherEmpWrapper> filtered = new ArrayList<>();
                        for (OtherEmpWrapper wrapper : originalList) {
                            if (wrapper.user.getEmpName() != null && wrapper.user.getEmpName().toLowerCase().contains(filterString)) {
                                filtered.add(wrapper);
                            }
                        }
                        results.values = filtered;
                        results.count = filtered.size();
                    }
                    return results;
                }

                @Override
                protected void publishResults(CharSequence constraint, android.widget.Filter.FilterResults results) {
                    filteredList = (List<OtherEmpWrapper>) results.values;
                    notifyDataSetChanged();
                }
            };
        }
    }
}
