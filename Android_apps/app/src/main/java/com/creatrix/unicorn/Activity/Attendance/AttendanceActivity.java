package com.creatrix.unicorn.Activity.Attendance;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.creatrix.unicorn.Activity.Attendance.Model.AttenApproval;
import com.creatrix.unicorn.Activity.Attendance.Model.ButtonRP;
import com.creatrix.unicorn.Activity.Attendance.Model.PunchWindow;
import com.creatrix.unicorn.Activity.Attendance.Model.ShiftCategory;
import com.creatrix.unicorn.Model.ResultInfo;
import com.creatrix.unicorn.Activity.MainDashboardActivity;
import com.creatrix.unicorn.DBAdapter.DBCrudHelper;
import com.creatrix.unicorn.Interface.IAttendance;
import com.creatrix.unicorn.Model.Attendance;
import com.creatrix.unicorn.Presenter.AttendancePresenter;
import com.creatrix.unicorn.R;
import com.creatrix.unicorn.UtilityHelper.CameraHelper;
import com.creatrix.unicorn.UtilityHelper.DateTimeUtil;
import com.creatrix.unicorn.UtilityHelper.LocationGuard;
import com.creatrix.unicorn.UtilityHelper.NetworkInformation;
import com.creatrix.unicorn.UtilityHelper.SessionManagement;
import com.creatrix.unicorn.UtilityHelper.SnackBarManagement;
import com.creatrix.unicorn.databinding.ActivityAttendanceBinding;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import id.zelory.compressor.Compressor;

public class AttendanceActivity extends AppCompatActivity implements OnMapReadyCallback, IAttendance.View {
    ActivityAttendanceBinding binding;
    ProgressDialog pd;
    GoogleMap mGoogleMap;
    SupportMapFragment mapFrag;
    LocationRequest mLocationRequest;
    Location mLastLocation;
    Marker mCurrLocationMarker;
    FusedLocationProviderClient mFusedLocationClient;
    Uri imageuri;
    File imgFile;
    String lat, lng, address,path;


    IAttendance.Presenter presenter;
    SessionManagement session;
    DBCrudHelper dbCrudHelper;


    private static final int CAMERA_REQUEST_In = 1888;
    private static int CAMERA_REQUEST_In2;
    private static final int CAMERA_REQUEST_Out = 2888;
    private static int CAMERA_REQUEST_Out2;

    //Data save server
    String formattedDate, time;
    int empId;
    int shiftId = 0;
    String shiftInTime;
    String shiftOutTime;
    PunchWindow punchWindow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAttendanceBinding.inflate(getLayoutInflater());
        //setContentView(R.layout.activity_attendance);
        setContentView(binding.getRoot());
        pd = new ProgressDialog(AttendanceActivity.this);
        presenter = new AttendancePresenter(this, this);
        dbCrudHelper = new DBCrudHelper(this);
        session = new SessionManagement(getApplicationContext());
        session.checkLogin();
        HashMap<String, String> user = session.getUserDetails();
        empId = Integer.parseInt(Objects.requireNonNull(user.get(SessionManagement.KEY_EmpId)));
        hitApi_Btn(empId);
        presenter.getShiftCategoryList(empId);
        presenter.getPunchWindow(empId);

        binding.toolbarCustom.setNavigationOnClickListener(v -> finish());
        initCmeraPermission();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        lat = null;
        lng = null;
        LocationManager locationManagerdd = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (locationManagerdd.isProviderEnabled(locationManagerdd.GPS_PROVIDER)) {
        } else {
            showGPSDisabledAlertToUser();
        }
        Date c = Calendar.getInstance().getTime();
        System.out.println("Current time => " + c);

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String date = DateTimeUtil.getDisplayDate();
        formattedDate = df.format(c);


        SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a");
        time = dateFormat.format(new Date());
        binding.punchInTime.setText(time);
        binding.punchOutTime.setText(time);
        binding.punchInDate.setText(formattedDate);
        binding.punchOutDate.setText(formattedDate);
        binding.dTXT.setText(date);
        mapFrag = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFrag.getMapAsync(this);


// AttType Means from save function 1 = Punch In Save , 2 = Punch Out Save , 3 = Total Save

        binding.btnPunchIn.setOnClickListener(v -> {
            if (!NetworkInformation.isConnected(AttendanceActivity.this)) {
                SnackBarManagement.NoInternetSnackbar(binding.masterLayout);
                Intent i = new Intent(AttendanceActivity.this, MainDashboardActivity.class);
                startActivity(i);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            } else {
                if (isLocationBlocked()) {
                    return;
                }
                if (isWithinShiftTime(true) && isWithinPunchWindow(true)) {
                    handleCaptureImage("In");
                }
            }

        });
        binding.btnPunchOut.setOnClickListener(v -> {
            if (!NetworkInformation.isConnected(AttendanceActivity.this)) {
                SnackBarManagement.NoInternetSnackbar(binding.masterLayout);
                Intent i = new Intent(AttendanceActivity.this, MainDashboardActivity.class);
                startActivity(i);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            } else {
                if (isLocationBlocked()) {
                    return;
                }
                handleCaptureImage("Out");
            }
        });
        binding.attRefresh.setOnClickListener(v -> {
            finish();
            overridePendingTransition(0, 0);
            startActivity(getIntent());
        });
    }

    // Strict location rule: precise permission + GPS on + fresh, accurate, non-mock fix.
    private boolean isLocationBlocked() {
        String error = LocationGuard.checkEnvironment(this);
        if (error == null) {
            error = LocationGuard.checkFix(mLastLocation);
        }
        if (error != null) {
            SnackBarManagement._warning_CustomMessage(binding.masterLayout, error);
            return true;
        }
        return false;
    }

    private void hitApi_Btn(int empId) {
        pd.setMessage("Preparing Attendance..");
        pd.show();
        pd.setCanceledOnTouchOutside(false);
        presenter.button(empId);
    }


    @Override
    protected void onResume() {
        super.onResume();
        startLocationUpdates();
    }

    @Override
    public void onPause() {
        super.onPause();

        //stop location updates when Activity is no longer active
        if (mFusedLocationClient != null) {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        // mGoogleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);

        if (hasLocationPermission()) {
            startLocationUpdates();
        } else {
            //Request Location Permission
            checkLocationPermission();
        }
    }

    // Fine or coarse is enough: on Android 12+ the user may grant "approximate" only.
    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    // Also called from onResume: onPause removes the updates (e.g. while the camera is open).
    private void startLocationUpdates() {
        if (mFusedLocationClient == null || !hasLocationPermission()) {
            return;
        }
        if (mLocationRequest == null) {
            mLocationRequest = new LocationRequest();
            mLocationRequest.setInterval(10000);
            mLocationRequest.setFastestInterval(5000);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        }
        try {
            // Seed lat/lng right away instead of waiting for the first update.
            mFusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null && lat == null
                        && Math.abs(System.currentTimeMillis() - location.getTime()) <= 2 * 60 * 1000) {
                    onNewLocation(location);
                }
            });
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.getMainLooper());
            if (mGoogleMap != null) {
                mGoogleMap.setMyLocationEnabled(true);
            }

            // Multi-tier fallback: if GPS/Network cannot resolve indoors, try IP geolocation.
            if (lat == null) {
                LocationGuard.requestFix(this, (location, error) -> {
                    if (isFinishing() || isDestroyed()) return;
                    if (location != null && lat == null) {
                        onNewLocation(location);
                    }
                });
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private void onNewLocation(Location location) {
        Log.i("MapsActivity", "Location: " + location.getLatitude() + " " + location.getLongitude());
        // Set the coordinates first so a geocoder/map failure can't leave lat/lng null.
        mLastLocation = location;
        lat = String.valueOf(location.getLatitude());
        lng = String.valueOf(location.getLongitude());

        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        String title = null;
        try {
            List<Address> addresses = new Geocoder(getApplicationContext(), Locale.getDefault())
                    .getFromLocation(location.getLatitude(), location.getLongitude(), 5);
            if (addresses != null && !addresses.isEmpty()) {
                address = addresses.get(0).getAddressLine(0);
                title = address;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (mGoogleMap == null) {
            return;
        }
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title(title);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
        mCurrLocationMarker = mGoogleMap.addMarker(markerOptions);
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));
    }

    LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            //The last location in the list is the newest
            Location location = locationResult.getLastLocation();
            if (location != null) {
                onNewLocation(location);
            }
        }
    };

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    private void checkLocationPermission() {
        if (!hasLocationPermission()) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle("Location Permission Needed")
                        .setMessage("This app needs the Location permission, please accept to use location functionality")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(AttendanceActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && hasLocationPermission()) {
                    startLocationUpdates();
                } else {

                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }


    @Override
    public void onSuccessapprove(boolean t) {

    }

    @Override
    public void onSuccessapproveAll(String t) {

    }

    @Override
    public void onSuccess(String message, boolean status, int type) {
        try {
            if (status) {
                SnackBarManagement._success_CustomMessage(binding.masterLayout, message);
                new AlertDialog.Builder(this)
                        .setIcon(R.drawable.ic_alertz)
                        .setTitle("Success")
                        .setMessage(message)
                        .setCancelable(false)
                        .setPositiveButton("OK", (dialog, which) -> {
                            presenter.button(empId);
                            finish();
                        })
                        .show();
            }
            if (!status) {
                SnackBarManagement._error_CustomMessage(binding.masterLayout, "Something Went Wrong!!");
            }

          /*  new AlertDialog.Builder(this)
                    .setIcon(R.drawable.tikiconwhite)
                    .setTitle("Success")
                    .setMessage(message)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent id = new Intent(AttendanceActivity.this, MainDashboardActivity.class);
                            id.addFlags(id.FLAG_ACTIVITY_CLEAR_TOP | id.FLAG_ACTIVITY_CLEAR_TASK | id.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(id);
                            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                        }
                    }).setCancelable(false)
                    .show();*/

        } catch (Exception exception) {
            exception.printStackTrace();
        }


    }

    @Override
    public void onSuccessTeamAtten(List<AttenApproval> teamlist) {

    }

    @Override
    public void onError(String message, int type) {
        if ((pd != null) && pd.isShowing())
        {
            pd.dismiss();
        }
        SnackBarManagement._error_CustomMessage(binding.masterLayout,message);

    }

    @Override
    public void onButtonView(ButtonRP buttonRP) {
        if ((pd != null) && pd.isShowing())
        {
            pd.dismiss();
        }

        if (buttonRP.getShiftId() != null) {
            shiftId = buttonRP.getShiftId();
        }
        shiftInTime = buttonRP.getShiftInTime();
        shiftOutTime = buttonRP.getShiftOutTime();
        showShiftInfo(buttonRP);

        String punchInBtn = buttonRP.getPunchInBtn();
        String punchOutBtn = buttonRP.getPunchOUTBtn();

        binding.punchOutDiv.setVisibility(View.VISIBLE);

        if ("OFF".equals(punchInBtn) && "OFF".equals(punchOutBtn)) {
            binding.punchInDIv.setVisibility(View.GONE);
            binding.divAttCompleted.setVisibility(View.VISIBLE);
            updatePunchButtonState();
        } else if ("ON".equals(punchInBtn) && "OFF".equals(punchOutBtn)) {
            binding.punchInDIv.setVisibility(View.VISIBLE);
            binding.divAttCompleted.setVisibility(View.GONE);
            updatePunchButtonState();
        } else if ("OFF".equals(punchInBtn) && "ON".equals(punchOutBtn)) {
            binding.punchInDIv.setVisibility(View.GONE);
            binding.divAttCompleted.setVisibility(View.GONE);
            updatePunchButtonState();
        }

    }


    private void showGPSDisabledAlertToUser() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("GPS is disabled in your device. Would you like to enable it?")
                .setCancelable(false)
                .setPositiveButton("Goto Settings Page To Enable GPS",
                        (dialog, id) -> {
                            Intent callGPSSettingIntent = new Intent(
                                    android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(callGPSSettingIntent);
                        });
        alertDialogBuilder.setNegativeButton("Cancel",
                (dialog, id) -> {
                    dialog.cancel();
                    dialog.dismiss();
                    startActivity(new Intent(getApplicationContext(), MainDashboardActivity.class));
                });
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    /*@Override
    protected void onRestart() {
        super.onRestart();
        finish();
        overridePendingTransition(0, 0);
        startActivity(getIntent());
        overridePendingTransition(0, 0);

    }*/

    @Override
    public void finish() {
        super.finish();
        if ((pd != null) && pd.isShowing())
        {
            pd.dismiss();
        }
        overridePendingTransition(R.anim.slide_in_left,
                R.anim.slide_out_right);
    }


    private void handleCaptureImage(String type) {
        switch (type) {
            case "In":
              /*  Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST_In);*/

                try {
                  /*  Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    intent.putExtra("android.intent.extras.CAMERA_FACING", 1);
                    startActivityForResult(intent, CAMERA_REQUEST_In);*/
                    CAMERA_REQUEST_In2=78911;
                    ImagePicker.Companion.with(AttendanceActivity.this)
                            .cameraOnly()
                            .crop()
                            //Crop image(Optional), Check Customization for more option
                            .compress(1024)            //Final image size will be less than 1 MB(Optional)
                            .maxResultSize(1080, 1080)    //Final image resolution will be less than 1080 x 1080(Optional)
                            .start();

                } catch (Exception exception) {
                    exception.printStackTrace();
                }
                break;
            case "Out":
               /* Intent cameraIntento = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntento, CAMERA_REQUEST_Out);*/
                try {
             /*       Intent cameraIntento = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    cameraIntento.putExtra("android.intent.extras.CAMERA_FACING", 1);
                    startActivityForResult(cameraIntento, CAMERA_REQUEST_Out);*/

                    CAMERA_REQUEST_Out2=83411;
                    ImagePicker.Companion.with(AttendanceActivity.this)
                            .cameraOnly()
                            .crop()
                            //Crop image(Optional), Check Customization for more option
                            .compress(1024)            //Final image size will be less than 1 MB(Optional)
                            .maxResultSize(1080, 1080)    //Final image resolution will be less than 1080 x 1080(Optional)
                            .start();
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (CAMERA_REQUEST_In2 == 78911 && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                imageuri = data.getData();
                if (imageuri != null) {
                    path = getUriRealPathAboveKitkat(AttendanceActivity.this, imageuri);
                    if (path == null)
                        return;
                    imgFile = new File(path);
                    String img_str = null;
                    try {
                        imgFile = new Compressor(AttendanceActivity.this).compressToFile(imgFile);
                        Bitmap bitmap = new Compressor(this).compressToBitmap(imgFile);
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream);
                        byte[] image = stream.toByteArray();
                        img_str = Base64.encodeToString(image, 0);
                        System.out.println("img : " + img_str);
                        hitPunchIn(img_str);
                    } catch (IOException e) {
                        Toast.makeText(AttendanceActivity.this, "Image Exception", Toast.LENGTH_SHORT).show();
                    }
                }
            }
         /*   try {
            *//*      Bitmap photo = (Bitmap) data.getExtras().get("data");
                  //imageView.setImageBitmap(photo);

                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                photo.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                byte[] image = stream.toByteArray();
                String img_str = Base64.encodeToString(image, 0);*//*



               // hitPunchIn(img_str);
              //  hitPunchIn(img_str);

            } catch (Exception exception) {
                exception.printStackTrace();
            }*/

        }
        if (CAMERA_REQUEST_Out2 == 83411 && resultCode == Activity.RESULT_OK) {
            try {
            /*    Bitmap photo = (Bitmap) data.getExtras().get("data");
                //imageView.setImageBitmap(photo);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                photo.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                byte[] image = stream.toByteArray();
                String img_stro = Base64.encodeToString(image, 0);*/
                if (data != null) {
                    imageuri = data.getData();
                    if (imageuri != null) {
                        path = getUriRealPathAboveKitkat(AttendanceActivity.this, imageuri);
                        if (path == null)
                            return;
                        imgFile = new File(path);
                        String img_stro = null;
                        try {
                            imgFile = new Compressor(AttendanceActivity.this).compressToFile(imgFile);
                            Bitmap bitmap = new Compressor(this).compressToBitmap(imgFile);
                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream);
                            byte[] image = stream.toByteArray();
                            img_stro = Base64.encodeToString(image, 0);
                            System.out.println("img : " + img_stro);
                            hitPunchOut(img_stro);
                        } catch (IOException e) {
                            Toast.makeText(AttendanceActivity.this, "Image Exception", Toast.LENGTH_SHORT).show();
                        }
                    }
                }

            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
    }
    //here get file path from uri
    private String getUriRealPathAboveKitkat(AttendanceActivity activity, Uri contentURI) {
        String ret = "";
        if (activity != null && contentURI != null) {
            if (CameraHelper.isContentUri(contentURI)) {
                //if(isGooglePhotoDoc(contentURI.getAuthority()))
                if (CameraHelper.isGooglePhotoDoc(contentURI.getAuthority())) {
                    ret = contentURI.getLastPathSegment();
                } else {
                    ret = getImageRealPath(getContentResolver(), contentURI, null);
                }
            } else if (CameraHelper.isFileUri(contentURI)) {
                ret = contentURI.getPath();
            } else if (CameraHelper.isDocumentUri(activity, contentURI)) {
                // Get uri related document id.
                String documentId = DocumentsContract.getDocumentId(contentURI);
                // Get uri authority.
                String uriAuthority = contentURI.getAuthority();
                if (CameraHelper.isMediaDoc(uriAuthority)) {
                    String[] idArr = documentId.split(":");
                    if (idArr.length == 2) {
                        // First item is document type.
                        String docType = idArr[0];
                        // Second item is document real id.
                        String realDocId = idArr[1];
                        // Get content uri by document type.
                        Uri mediaContentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                        if ("image".equals(docType)) {
                            mediaContentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                        }
                        // Get where clause with real document id.
                        String whereClause = MediaStore.Images.Media._ID + " = " + realDocId;
                        ret = getImageRealPath(getContentResolver(), mediaContentUri, whereClause);
                    }
                } else if (CameraHelper.isDownloadDoc(uriAuthority)) {
                    // Build download URI.
                    Uri downloadUri = Uri.parse("content://downloads/public_downloads");
                    // Append download document id at URI end.
                    Uri downloadUriAppendId = ContentUris.withAppendedId(downloadUri, Long.valueOf(documentId));
                    ret = getImageRealPath(getContentResolver(), downloadUriAppendId, null);
                } else if (CameraHelper.isExternalStoreDoc(uriAuthority)) {
                    String[] idArr = documentId.split(":");
                    if (idArr.length == 2) {
                        String type = idArr[0];
                        String realDocId = idArr[1];
                        if ("primary".equalsIgnoreCase(type)) {
                            ret = Environment.getExternalStorageDirectory() + "/" + realDocId;
                        }
                    }
                }
            }
        }
        return ret;
    }

    private String getImageRealPath(ContentResolver contentResolver, Uri uri, String whereClause) {
        String ret = "";
        // Query the URI with the condition.
        Cursor cursor = contentResolver.query(uri, null, whereClause, null, null);
        if (cursor != null) {
            boolean moveToFirst = cursor.moveToFirst();
            if (moveToFirst) {
                // Get columns name by URI type.
                String columnName = MediaStore.Images.Media.DATA;
                if (uri == MediaStore.Images.Media.EXTERNAL_CONTENT_URI) {
                    columnName = MediaStore.Images.Media.DATA;
                } else if (uri == MediaStore.Audio.Media.EXTERNAL_CONTENT_URI) {
                    columnName = MediaStore.Audio.Media.DATA;
                } else if (uri == MediaStore.Video.Media.EXTERNAL_CONTENT_URI) {
                    columnName = MediaStore.Video.Media.DATA;
                }
                // Get column index.
                int imageColumnIndex = cursor.getColumnIndex(columnName);
                // Get column value which is the uri related file local path.
                ret = cursor.getString(imageColumnIndex);
            }
        }
        return ret;
    }

    public void hitPunchIn(String img_str) {
        try {
            if (!isWithinShiftTime(true) || !isWithinPunchWindow(true)) {
                return;
            }
            if (!img_str.isEmpty()) {
                Date punchDate = new Date();
                String punchTime = getPunchTime(punchDate);
                String punchDateTime = getPunchDateTime(punchDate);
                Attendance aInfo = new Attendance();
                aInfo.setAttendanceId(0);
                aInfo.setAttendanceDate(punchDateTime);
                aInfo.setPunchInTime(punchTime);
                aInfo.setEmpInfoId(empId);
                aInfo.setPInLat(lat);
                aInfo.setPInLog(lng);
                aInfo.setEntryDate(punchDateTime);
                aInfo.setAttAddress(address);
                aInfo.setAttType(1);
                aInfo.setShiftId(shiftId);
                aInfo.setAttImg(img_str);
                aInfo.setPOutRemarks("");
                String sds = lat;
                String sds2 = lng;
                if ((lat == null) || (lng == null)) {
                    SnackBarManagement._warning_CustomMessage(binding.masterLayout, "Error in Getting Location.Please Refresh the Page first");
                } else if (!com.creatrix.unicorn.BuildConfig.DEBUG && LocationGuard.isMock(mLastLocation)) {
                    SnackBarManagement._warning_CustomMessage(binding.masterLayout, "Fake/mock location detected. Turn off any mock location app to continue.");
                } else {
                    Gson gson = new Gson();
                    String data = gson.toJson(aInfo);
                    System.out.println("indata :"+data);
                   presenter.doSavePuncINInfo(aInfo, binding.masterLayout);
                }
            } else {
                SnackBarManagement._warning_CustomMessage(binding.masterLayout, "Your Photo Not Captured." + "\nPunch In Again");
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public void hitPunchOut(String img_str) {
        try {
            if (!img_str.isEmpty()) {
                Date punchDate = new Date();
                String punchTime = getPunchTime(punchDate);
                String punchDateTime = getPunchDateTime(punchDate);
                Attendance aInfo = new Attendance();
                aInfo.setAttendanceId(0);
                aInfo.setAttendanceDate(punchDateTime);
                aInfo.setPunchInTime(punchTime);
                aInfo.setEmpInfoId(empId);
                aInfo.setPInLat(lat);
                aInfo.setPInLog(lng);
                aInfo.setEntryDate(punchDateTime);
                aInfo.setAttAddress(address);
                aInfo.setAttType(2);
                aInfo.setShiftId(shiftId);
                aInfo.setAttImg(img_str);
                String remarks = binding.editText.getText().toString();
                aInfo.setPOutRemarks(remarks);
                if ((lat == null) || (lng == null)) {
                    SnackBarManagement._warning_CustomMessage(binding.masterLayout, "Error in Getting Location.Please Refresh the Page first");
                } else if (!com.creatrix.unicorn.BuildConfig.DEBUG && LocationGuard.isMock(mLastLocation)) {
                    SnackBarManagement._warning_CustomMessage(binding.masterLayout, "Fake/mock location detected. Turn off any mock location app to continue.");
                } else {

                    Gson gson=new Gson();
                    String data=gson.toJson(aInfo);
                    presenter.doSavePuncINInfo(aInfo, binding.masterLayout);
                }
            } else {
                SnackBarManagement._warning_CustomMessage(binding.masterLayout, "Your Photo Not Captured." + "\nPunch In Again");
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private String getPunchTime(Date date) {
        return new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(date);
    }

    private String getPunchDateTime(Date date) {
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(date);
    }

    private void showShiftInfo(ButtonRP buttonRP) {
        String shiftText = getDisplayText(buttonRP.getShiftText(), "Shift");
        String shiftInTime = "In " + getDisplayText(buttonRP.getShiftInTime(), "--:--:--");
        String shiftOutTime = "Out " + getDisplayText(buttonRP.getShiftOutTime(), "--:--:--");

        binding.shiftTextPunchIn.setText(shiftText);
        binding.shiftInTimePunchIn.setText(shiftInTime);
        binding.shiftOutTimePunchIn.setText(shiftOutTime);

        binding.shiftTextPunchOut.setText(shiftText);
        binding.shiftInTimePunchOut.setText(shiftInTime);
        binding.shiftOutTimePunchOut.setText(shiftOutTime);
    }

    private void updatePunchButtonState() {
        boolean withinShift = isWithinShiftTime(false) && isWithinPunchWindow(false);
        binding.btnPunchIn.setEnabled(withinShift);
        binding.btnPunchOut.setEnabled(true);
        binding.btnPunchIn.setAlpha(withinShift ? 1f : 0.45f);
        binding.btnPunchOut.setAlpha(1f);
    }

    private boolean isWithinShiftTime(boolean showMessage) {
        if (shiftInTime == null || shiftInTime.trim().isEmpty()
                || shiftOutTime == null || shiftOutTime.trim().isEmpty()) {
            if (showMessage) {
                SnackBarManagement._warning_CustomMessage(binding.masterLayout, "Shift time not found. Please refresh the page");
            }
            return false;
        }

        try {
            int nowSeconds = getSecondsFromTime(getPunchTime(new Date()));
            int shiftInSeconds = getSecondsFromTime(shiftInTime);
            int shiftOutSeconds = getSecondsFromTime(shiftOutTime);
            
            // Add 2 hours (in seconds) to shift out time limit
            int extendedShiftOutSeconds = (shiftOutSeconds + (2 * 60 * 60)) % (24 * 60 * 60);
            
            boolean withinShift;

            if (shiftInSeconds <= extendedShiftOutSeconds) {
                withinShift = nowSeconds >= shiftInSeconds && nowSeconds <= extendedShiftOutSeconds;
            } else {
                withinShift = nowSeconds >= shiftInSeconds || nowSeconds <= extendedShiftOutSeconds;
            }

            if (!withinShift && showMessage) {
                String extendedTimeStr = formatSecondsToTime(extendedShiftOutSeconds);
                SnackBarManagement._warning_CustomMessage(
                        binding.masterLayout,
                        "Punch allowed only between " + shiftInTime + " and " + extendedTimeStr
                );
            }
            return withinShift;
        } catch (Exception exception) {
            if (showMessage) {
                SnackBarManagement._warning_CustomMessage(binding.masterLayout, "Invalid shift time. Please refresh the page");
            }
            return false;
        }
    }

    @Override
    public void onPunchWindow(PunchWindow window) {
        punchWindow = window;
        if (window.isRestricted()) {
            binding.punchWindowText.setText("Punch-in allowed " + window.getPunchInFrom() + " - " + window.getPunchInTo());
            binding.punchWindowText.setVisibility(View.VISIBLE);
        } else {
            binding.punchWindowText.setVisibility(View.GONE);
        }
        updatePunchButtonState();
    }

    // Employees without an active window are unrestricted. Server re-checks on save.
    private boolean isWithinPunchWindow(boolean showMessage) {
        if (punchWindow == null || !punchWindow.isRestricted()) {
            return true;
        }
        try {
            int now = getSecondsFromTime(getPunchTime(new Date()));
            int from = getSecondsFromTime(punchWindow.getPunchInFrom());
            int to = getSecondsFromTime(punchWindow.getPunchInTo()) + 59;
            boolean allowed = from <= to ? (now >= from && now <= to) : (now >= from || now <= to);
            if (!allowed && showMessage) {
                SnackBarManagement._warning_CustomMessage(binding.masterLayout,
                        "Punch in allowed only between " + punchWindow.getPunchInFrom() + " and " + punchWindow.getPunchInTo());
            }
            return allowed;
        } catch (Exception exception) {
            return true;
        }
    }

    private int getSecondsFromTime(String timeValue) {
        String[] parts = timeValue.trim().split(":");
        int hour = Integer.parseInt(parts[0]);
        int minute = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
        int second = parts.length > 2 ? Integer.parseInt(parts[2]) : 0;
        return (hour * 60 * 60) + (minute * 60) + second;
    }

    private String formatSecondsToTime(int totalSeconds) {
        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;
        return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
    }

    private String getDisplayText(String value, String fallback) {
        if (value == null || value.trim().isEmpty()) {
            return fallback;
        }
        return value;
    }

    public void initCmeraPermission() {
        Dexter.withContext(AttendanceActivity.this)
                .withPermission(Manifest.permission.CAMERA)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {

                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {

                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }

    @Override
    public void onShiftCategoryListSuccess(List<ShiftCategory> categories) {
        if (categories == null || categories.isEmpty()) {
            return;
        }

        String showModal = categories.get(0).getModalShow();
        if (showModal == null || !showModal.equalsIgnoreCase("YES")) {
            return;
        }

        String[] items = new String[categories.size()];
        for (int i = 0; i < categories.size(); i++) {
            items[i] = categories.get(i).getShiftCategory();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(AttendanceActivity.this);
        builder.setTitle("Select Shift Category");
        builder.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String selectedCategory = items[which];
                dialog.dismiss();
                presenter.saveShiftCategory(empId, selectedCategory);
            }
        });
        builder.setCancelable(false);

        if (!isFinishing()) {
            builder.show();
        }
    }

    @Override
    public void onSaveShiftCategorySuccess(ResultInfo resultInfo) {
        if (resultInfo != null) {
            if (resultInfo.getSuccess()) {
                String msg = resultInfo.getMsd() != null ? resultInfo.getMsd() : "Shift Category Saved successfully";
                SnackBarManagement._success_CustomMessage(binding.masterLayout, msg);
                presenter.button(empId);
            } else {
                String errMsg = resultInfo.getErrorMessage() != null ? resultInfo.getErrorMessage() : "Failed to save shift category";
                SnackBarManagement._error_CustomMessage(binding.masterLayout, errMsg);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if ((pd != null) && pd.isShowing()) {
            pd.dismiss();
        }
    }
}
