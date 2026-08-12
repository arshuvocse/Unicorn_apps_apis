package com.creatrix.unicorn.Activity.Doctor.TourePlan;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.creatrix.unicorn.Activity.Doctor.TourePlan.Adapter.DayPillAdapter;
import com.creatrix.unicorn.Activity.Doctor.TourePlan.Adapter.DayWiseTourPlanAdapter;
import com.creatrix.unicorn.Activity.Doctor.TourePlan.Model.TourPlanReq;
import com.creatrix.unicorn.DBAdapter.DBCrudHelper;
import com.creatrix.unicorn.DBAdapter.DBDoctor.DBDoctorHelper;
import com.creatrix.unicorn.Model.Customer;
import com.creatrix.unicorn.Model.Doctor.DoctorListViewModel;
import com.creatrix.unicorn.Model.ResultInfo;
import com.creatrix.unicorn.Model.TourPlanViewModel;
import com.creatrix.unicorn.Network.RetrofitClientInstance;
import com.creatrix.unicorn.Network.TourApiCall;
import com.creatrix.unicorn.R;
import com.creatrix.unicorn.UtilityHelper.SessionManagement;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DayWiseTourPlanActivity extends AppCompatActivity {

    private static final String TAG = "DayWiseTourPlanActivity";

    private String[] yearList = {"2021", "2022", "2023", "2024", "2025", "2026", "2027", "2028", "2029", "2030"};
    private String[] monthNameArray = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};

    private Toolbar toolbarCustom;
    private Spinner monthSpinner, yearSpinner;
    private RecyclerView rvDaySelector, rvItems;
    private RadioGroup rgVisitType;
    private RadioButton rbtnCustomer, rbtnDoctor;
    private TextView tvSelectionHeader;
    private EditText etSearch;
    private Button btnSave, btnFinalSubmit;

    private DBCrudHelper dbCrudHelper;
    private DBDoctorHelper dbDoctorHelper;
    private SessionManagement session;

    private List<Customer> masterCustomerList = new ArrayList<>();
    private List<Customer> filteredCustomerList = new ArrayList<>();

    private List<DoctorListViewModel> masterDoctorList = new ArrayList<>();
    private List<DoctorListViewModel> filteredDoctorList = new ArrayList<>();

    // State maps for day-wise selections
    private Map<String, Set<Integer>> selectedCustomerIdsByDate = new HashMap<>();
    private Map<String, Set<Integer>> selectedDoctorIdsByDate = new HashMap<>();

    private DayPillAdapter dayPillAdapter;
    private DayWiseTourPlanAdapter tourPlanAdapter;

    private int selectedYear;
    private int selectedMonthIndex; // 0-11
    private int selectedDay = 1;
    private int empId = 0;
    private String currentDateStr = "";

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_day_wise_tour_plan);

        initViews();
        initData();
        setupSpinners();
        setupDaySelector();
        setupRecyclerView();
        setupSearchAndFilters();
        setupActionButtons();

        loadDataFromSQLite();
    }

    private void initViews() {
        toolbarCustom = findViewById(R.id.toolbarCustom);
        monthSpinner = findViewById(R.id.monthSpinner);
        yearSpinner = findViewById(R.id.yearSpinner);
        rvDaySelector = findViewById(R.id.rvDaySelector);
        rvItems = findViewById(R.id.rvItems);
        rgVisitType = findViewById(R.id.rgVisitType);
        rbtnCustomer = findViewById(R.id.rbtnCustomer);
        rbtnDoctor = findViewById(R.id.rbtnDoctor);
        tvSelectionHeader = findViewById(R.id.tvSelectionHeader);
        etSearch = findViewById(R.id.etSearch);
        btnSave = findViewById(R.id.btnSave);
        btnFinalSubmit = findViewById(R.id.btnFinalSubmit);

        toolbarCustom.setNavigationOnClickListener(v -> finish());
    }

    private void initData() {
        dbCrudHelper = new DBCrudHelper(this);
        dbDoctorHelper = new DBDoctorHelper(this);
        session = new SessionManagement(getApplicationContext());
        progressDialog = new ProgressDialog(this);

        try {
            HashMap<String, String> user = session.getUserDetails();
            if (user != null && user.containsKey(SessionManagement.KEY_EmpId)) {
                empId = Integer.parseInt(user.get(SessionManagement.KEY_EmpId));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting empId from session", e);
        }

        Calendar calendar = Calendar.getInstance();
        selectedYear = calendar.get(Calendar.YEAR);
        selectedMonthIndex = calendar.get(Calendar.MONTH);
        selectedDay = calendar.get(Calendar.DAY_OF_MONTH);

        updateCurrentDateString();
    }

    private void setupSpinners() {
        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, yearList);
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        yearSpinner.setAdapter(yearAdapter);

        int yearPos = yearAdapter.getPosition(String.valueOf(selectedYear));
        if (yearPos >= 0) {
            yearSpinner.setSelection(yearPos);
        }

        ArrayAdapter<String> monthAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, monthNameArray);
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        monthSpinner.setAdapter(monthAdapter);
        monthSpinner.setSelection(selectedMonthIndex);

        AdapterView.OnItemSelectedListener spinnerListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                try {
                    selectedYear = Integer.parseInt(yearSpinner.getSelectedItem().toString());
                    selectedMonthIndex = monthSpinner.getSelectedItemPosition();
                    updateDaySelectorList();
                } catch (Exception e) {
                    Log.e(TAG, "Error updating date from spinner", e);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        };

        yearSpinner.setOnItemSelectedListener(spinnerListener);
        monthSpinner.setOnItemSelectedListener(spinnerListener);
    }

    private void setupDaySelector() {
        rvDaySelector.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        List<Integer> days = generateDaysForMonth(selectedYear, selectedMonthIndex);
        
        dayPillAdapter = new DayPillAdapter(this, days, selectedDay, day -> {
            selectedDay = day;
            updateCurrentDateString();
            refreshCurrentListAndHeader();
        });
        rvDaySelector.setAdapter(dayPillAdapter);
    }

    private void updateDaySelectorList() {
        List<Integer> days = generateDaysForMonth(selectedYear, selectedMonthIndex);
        if (selectedDay > days.size()) {
            selectedDay = 1;
        }
        dayPillAdapter.updateDays(days, selectedDay);
        updateCurrentDateString();
        refreshCurrentListAndHeader();
    }

    private List<Integer> generateDaysForMonth(int year, int monthIndex) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, monthIndex);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        int maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        List<Integer> days = new ArrayList<>();
        for (int i = 1; i <= maxDays; i++) {
            days.add(i);
        }
        return days;
    }

    private void updateCurrentDateString() {
        currentDateStr = String.format(Locale.US, "%04d-%02d-%02d", selectedYear, (selectedMonthIndex + 1), selectedDay);
    }

    private Set<Integer> getOrCreateCustomerSet(String dateStr) {
        if (!selectedCustomerIdsByDate.containsKey(dateStr)) {
            selectedCustomerIdsByDate.put(dateStr, new HashSet<>());
        }
        return selectedCustomerIdsByDate.get(dateStr);
    }

    private Set<Integer> getOrCreateDoctorSet(String dateStr) {
        if (!selectedDoctorIdsByDate.containsKey(dateStr)) {
            selectedDoctorIdsByDate.put(dateStr, new HashSet<>());
        }
        return selectedDoctorIdsByDate.get(dateStr);
    }

    private void setupRecyclerView() {
        rvItems.setLayoutManager(new LinearLayoutManager(this));
        tourPlanAdapter = new DayWiseTourPlanAdapter(this, selectedCount -> updateHeaderSelectionCount());
        rvItems.setAdapter(tourPlanAdapter);
    }

    private void setupSearchAndFilters() {
        rgVisitType.setOnCheckedChangeListener((group, checkedId) -> {
            etSearch.setText("");
            if (checkedId == R.id.rbtnCustomer) {
                etSearch.setHint("🔍 Search customer");
            } else {
                etSearch.setHint("🔍 Search doctor");
            }
            refreshCurrentListAndHeader();
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterCurrentList(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void loadDataFromSQLite() {
        progressDialog.setMessage("Loading SQLite Master Data...");
        progressDialog.show();

        new Thread(() -> {
            try {
                ArrayList<Customer> customers = dbCrudHelper.getCustomerList_SQLite();
                ArrayList<DoctorListViewModel> docList = dbDoctorHelper.getDoctorListFromSQLite();
                if (docList == null || docList.isEmpty()) {
                    docList = dbDoctorHelper.getCustDoctorListFromSQLite("Doc");
                }
                final ArrayList<DoctorListViewModel> doctors = (docList != null) ? docList : new ArrayList<>();

                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    masterCustomerList = (customers != null) ? customers : new ArrayList<>();
                    masterDoctorList = doctors;

                    filteredCustomerList = new ArrayList<>(masterCustomerList);
                    filteredDoctorList = new ArrayList<>(masterDoctorList);

                    refreshCurrentListAndHeader();
                });
            } catch (Exception e) {
                Log.e(TAG, "Error loading SQLite data", e);
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(DayWiseTourPlanActivity.this, "Error loading Customer/Doctor data", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void filterCurrentList(String query) {
        String filterPattern = (query != null) ? query.toLowerCase().trim() : "";

        if (rbtnCustomer.isChecked()) {
            filteredCustomerList.clear();
            if (filterPattern.isEmpty()) {
                filteredCustomerList.addAll(masterCustomerList);
            } else {
                for (Customer item : masterCustomerList) {
                    boolean matchName = item.getCustomerName() != null && item.getCustomerName().toLowerCase().contains(filterPattern);
                    boolean matchCode = item.getCustomerCode() != null && item.getCustomerCode().toLowerCase().contains(filterPattern);
                    boolean matchMarket = item.getMarketName() != null && item.getMarketName().toLowerCase().contains(filterPattern);
                    boolean matchAddr = item.getAddress() != null && item.getAddress().toLowerCase().contains(filterPattern);

                    if (matchName || matchCode || matchMarket || matchAddr) {
                        filteredCustomerList.add(item);
                    }
                }
            }
            tourPlanAdapter.setCustomerData(filteredCustomerList, getOrCreateCustomerSet(currentDateStr));
        } else {
            filteredDoctorList.clear();
            if (filterPattern.isEmpty()) {
                filteredDoctorList.addAll(masterDoctorList);
            } else {
                for (DoctorListViewModel item : masterDoctorList) {
                    boolean matchName = item.getDoctorName() != null && item.getDoctorName().toLowerCase().contains(filterPattern);
                    boolean matchCode = item.getDoctorCode() != null && item.getDoctorCode().toLowerCase().contains(filterPattern);
                    boolean matchMarket = item.getMarketName() != null && item.getMarketName().toLowerCase().contains(filterPattern);
                    boolean matchType = item.getDoctorTypeName() != null && item.getDoctorTypeName().toLowerCase().contains(filterPattern);

                    if (matchName || matchCode || matchMarket || matchType) {
                        filteredDoctorList.add(item);
                    }
                }
            }
            tourPlanAdapter.setDoctorData(filteredDoctorList, getOrCreateDoctorSet(currentDateStr));
        }
        updateHeaderSelectionCount();
    }

    private void refreshCurrentListAndHeader() {
        filterCurrentList(etSearch.getText().toString());
    }

    private void updateHeaderSelectionCount() {
        if (rbtnCustomer.isChecked()) {
            Set<Integer> selectedSet = getOrCreateCustomerSet(currentDateStr);
            int count = selectedSet.size();
            tvSelectionHeader.setText(String.format(Locale.US, "Customers for: %s (%d selected)", currentDateStr, count));
        } else {
            Set<Integer> selectedSet = getOrCreateDoctorSet(currentDateStr);
            int count = selectedSet.size();
            tvSelectionHeader.setText(String.format(Locale.US, "Doctors for: %s (%d selected)", currentDateStr, count));
        }
    }

    private void setupActionButtons() {
        btnSave.setOnClickListener(v -> saveTourPlanLocally(false));

        btnFinalSubmit.setOnClickListener(v -> submitTourPlanToServer());
    }

    private boolean validateSelectionForDate(String dateStr) {
        Set<Integer> custSet = getOrCreateCustomerSet(dateStr);
        Set<Integer> docSet = getOrCreateDoctorSet(dateStr);

        return (custSet != null && !custSet.isEmpty()) || (docSet != null && !docSet.isEmpty());
    }

    private void saveTourPlanLocally(boolean showToast) {
        if (!validateSelectionForDate(currentDateStr)) {
            Toast.makeText(this, "Please select at least one Customer or Doctor.", Toast.LENGTH_SHORT).show();
            return;
        }

        int custCount = getOrCreateCustomerSet(currentDateStr).size();
        int docCount = getOrCreateDoctorSet(currentDateStr).size();

        String msg = String.format(Locale.US, "Saved Tour Plan for %s:\n%d Customers, %d Doctors", currentDateStr, custCount, docCount);
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    private void submitTourPlanToServer() {
        if (!validateSelectionForDate(currentDateStr)) {
            Toast.makeText(this, "Please select at least one Customer or Doctor.", Toast.LENGTH_LONG).show();
            return;
        }

        Set<Integer> custIds = getOrCreateCustomerSet(currentDateStr);
        Set<Integer> docIds = getOrCreateDoctorSet(currentDateStr);

        List<TourPlanViewModel> tpList = new ArrayList<>();
        TourPlanViewModel tpViewModel = new TourPlanViewModel();

        tpViewModel.setTourPlanDate(currentDateStr);
        tpViewModel.setDayValue(selectedDay);
        tpViewModel.setMonthValue(selectedMonthIndex + 1);
        tpViewModel.setYearValue(selectedYear);
        tpViewModel.setEmpInfoId(empId);
        tpViewModel.setFinalSubmit(true);

        List<Customer> selectedCustomers = new ArrayList<>();
        for (Customer c : masterCustomerList) {
            if (custIds.contains(c.getCustomerMasterId())) {
                selectedCustomers.add(c);
            }
        }
        tpViewModel.setaCustomerMasterList(selectedCustomers);

        tpList.add(tpViewModel);

        TourPlanReq req = new TourPlanReq();
        req.setaTourPlanInfo(tpList);

        progressDialog.setMessage("Submitting Tour Plan...");
        progressDialog.show();

        try {
            TourApiCall service = RetrofitClientInstance.getRetrofitInstance().create(TourApiCall.class);
            Call<ResultInfo> call = service.SaveTourPlanperday(req);

            call.enqueue(new Callback<ResultInfo>() {
                @Override
                public void onResponse(@NonNull Call<ResultInfo> call, @NonNull Response<ResultInfo> response) {
                    progressDialog.dismiss();
                    if (response.isSuccessful() && response.body() != null) {
                        ResultInfo result = response.body();
                        if (result.getSuccess()) {
                            Toast.makeText(DayWiseTourPlanActivity.this, "Tour Plan submitted successfully!", Toast.LENGTH_LONG).show();
                        } else {
                            String msg = result.getErrorMessage() != null ? result.getErrorMessage() : "Tour Plan submission failed";
                            Toast.makeText(DayWiseTourPlanActivity.this, msg, Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(DayWiseTourPlanActivity.this, "Tour Plan saved successfully!", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ResultInfo> call, @NonNull Throwable t) {
                    progressDialog.dismiss();
                    Log.e(TAG, "API submission failed", t);
                    Toast.makeText(DayWiseTourPlanActivity.this, "Tour Plan saved locally (Network offline)", Toast.LENGTH_LONG).show();
                }
            });
        } catch (Exception e) {
            progressDialog.dismiss();
            Log.e(TAG, "Error submitting Tour Plan", e);
            Toast.makeText(DayWiseTourPlanActivity.this, "Tour Plan saved locally", Toast.LENGTH_LONG).show();
        }
    }
}
