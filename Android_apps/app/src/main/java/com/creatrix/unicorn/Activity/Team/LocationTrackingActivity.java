package com.creatrix.unicorn.Activity.Team;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.creatrix.unicorn.Activity.Team.Model.LiveTrackingLocation;
import com.creatrix.unicorn.Network.RetrofitClientInstance;
import com.creatrix.unicorn.Network.UserProcessAPI;
import com.creatrix.unicorn.R;
import com.creatrix.unicorn.databinding.ActivityLocationTrackingBinding;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LocationTrackingActivity extends AppCompatActivity implements OnMapReadyCallback {
    public static final String EXTRA_EMP_ID = "emp_id";
    public static final String EXTRA_EMP_NAME = "emp_name";

    private ActivityLocationTrackingBinding binding;
    private final Calendar selectedDate = Calendar.getInstance();
    private final SimpleDateFormat apiDateFormat =
            new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    private GoogleMap googleMap;
    private Call<List<LiveTrackingLocation>> trackingCall;
    private int empId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLocationTrackingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        empId = getIntent().getIntExtra(EXTRA_EMP_ID, 0);
        String empName = getIntent().getStringExtra(EXTRA_EMP_NAME);
        binding.employeeName.setText(empName == null ? "" : empName);
        binding.employeeCode.setText("Code: ");
        binding.employeeDesignation.setText("Designation: ");
        binding.employeeDisplayName.setText("Role: ");
        binding.selectedDate.setText(apiDateFormat.format(selectedDate.getTime()));
        binding.toolbarTracking.setNavigationOnClickListener(v -> finish());
        binding.selectedDate.setOnClickListener(v -> showDatePicker());

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.tracking_map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    private void showDatePicker() {
        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    selectedDate.set(year, month, dayOfMonth);
                    binding.selectedDate.setText(apiDateFormat.format(selectedDate.getTime()));
                    loadTracking();
                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)
        );
        dialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        dialog.show();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        loadTracking();
    }

    private void loadTracking() {
        if (googleMap == null) {
            return;
        }
        if (empId <= 0) {
            showMessage("Employee ID not found");
            return;
        }

        if (trackingCall != null) {
            trackingCall.cancel();
        }
        binding.trackingProgress.setVisibility(View.VISIBLE);
        binding.trackingMessage.setVisibility(View.GONE);
        googleMap.clear();

        UserProcessAPI service = RetrofitClientInstance.getRetrofitInstance()
                .create(UserProcessAPI.class);
        trackingCall = service.GetLiveTracking(
                empId,
                apiDateFormat.format(selectedDate.getTime())
        );
        trackingCall.enqueue(new Callback<List<LiveTrackingLocation>>() {
            @Override
            public void onResponse(
                    @NonNull Call<List<LiveTrackingLocation>> call,
                    @NonNull Response<List<LiveTrackingLocation>> response
            ) {
                binding.trackingProgress.setVisibility(View.GONE);
                if (!response.isSuccessful() || response.body() == null) {
                    showMessage("Unable to load tracking data");
                    return;
                }
                showLocations(response.body());
            }

            @Override
            public void onFailure(
                    @NonNull Call<List<LiveTrackingLocation>> call,
                    @NonNull Throwable throwable
            ) {
                if (call.isCanceled()) {
                    return;
                }
                binding.trackingProgress.setVisibility(View.GONE);
                showMessage("Unable to load tracking data");
            }
        });
    }

    private void showLocations(List<LiveTrackingLocation> locations) {
        List<LatLng> points = new ArrayList<>();
        List<LiveTrackingLocation> validLocations = new ArrayList<>();
        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        bindHeaderInfo(locations);

        for (LiveTrackingLocation location : locations) {
            LatLng point = parsePoint(location);
            if (point == null) {
                continue;
            }

            points.add(point);
            validLocations.add(location);
            boundsBuilder.include(point);
        }

        for (int index = 0; index < points.size(); index++) {
            LatLng point = points.get(index);
            LiveTrackingLocation location = validLocations.get(index);
            String title = buildMarkerTitle(location);
            String snippet = buildMarkerSnippet(location);
            float markerColor = BitmapDescriptorFactory.HUE_AZURE;
            if (points.size() == 1) {
                markerColor = BitmapDescriptorFactory.HUE_RED;
            } else if (index == 0) {
                markerColor = BitmapDescriptorFactory.HUE_GREEN;
            } else if (index == points.size() - 1) {
                markerColor = BitmapDescriptorFactory.HUE_RED;
            }
            googleMap.addMarker(new MarkerOptions()
                    .position(point)
                    .title(title)
                    .snippet(snippet)
                    .icon(BitmapDescriptorFactory.defaultMarker(markerColor)));
        }

        if (points.isEmpty()) {
            showMessage("No tracking data found for the selected date");
            return;
        }

        if (points.size() > 1) {
            googleMap.addPolyline(new PolylineOptions()
                    .addAll(points)
                    .color(getColor(R.color.main))
                    .width(8f));
            binding.trackingRoot.post(() ->
                    googleMap.animateCamera(
                            CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 80)
                    )
            );
        } else {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(points.get(0), 16f));
        }
    }

    private LatLng parsePoint(LiveTrackingLocation location) {
        try {
            double latitude = Double.parseDouble(location.getLatValue());
            double longitude = Double.parseDouble(location.getLongValue());
            if (latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180) {
                return null;
            }
            return new LatLng(latitude, longitude);
        } catch (NumberFormatException | NullPointerException exception) {
            return null;
        }
    }

    private String buildMarkerTitle(LiveTrackingLocation location) {
        String time = location.getTrackTime();
        if (time == null || time.trim().isEmpty()) {
            return "Tracked location";
        }
        return time.trim();
    }

    private String buildMarkerSnippet(LiveTrackingLocation location) {
        return trimAddress(location.getAddressName(), 50);
    }

    private void bindHeaderInfo(List<LiveTrackingLocation> locations) {
        if (locations == null || locations.isEmpty()) {
            return;
        }

        LiveTrackingLocation firstLocation = locations.get(0);
        binding.employeeName.setText(nonEmpty(firstLocation.getEmpName(), binding.employeeName.getText().toString()));
        binding.employeeCode.setText("Code: " + nonEmpty(firstLocation.getEmpMasterCode(), ""));
        binding.employeeDesignation.setText("Designation: " + nonEmpty(firstLocation.getDesigName(), ""));
        binding.employeeDisplayName.setText("Role: " + nonEmpty(firstLocation.getDisplayName(), ""));
    }

    private String trimAddress(String address, int limit) {
        String cleanAddress = nonEmpty(address, "").replace("\n", " ").trim();
        if (cleanAddress.length() <= limit) {
            return cleanAddress;
        }
        return cleanAddress.substring(0, limit).trim() + "...";
    }

    private String nonEmpty(String value, String fallback) {
        if (value == null) {
            return fallback;
        }
        String trimmedValue = value.trim();
        return trimmedValue.isEmpty() ? fallback : trimmedValue;
    }

    private void showMessage(String message) {
        binding.trackingMessage.setText(message);
        binding.trackingMessage.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        if (trackingCall != null) {
            trackingCall.cancel();
        }
        super.onDestroy();
    }
}
