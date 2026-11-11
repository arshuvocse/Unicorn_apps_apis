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
import com.creatrix.unicorn.Activity.Doctor.TourePlan.Model.DayWiseTourPlanReq;
import com.creatrix.unicorn.Activity.Doctor.TourePlan.Model.DayWiseTourPlanRow;
import com.creatrix.unicorn.DBAdapter.DBCrudHelper;
import com.creatrix.unicorn.DBAdapter.DBDoctor.DBDoctorHelper;
import com.creatrix.unicorn.Model.Customer;
import com.creatrix.unicorn.Model.Doctor.DoctorChamberName;
import com.creatrix.unicorn.Model.Doctor.DoctorListViewModel;
import com.creatrix.unicorn.Model.ResultInfo;
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

    // last, current and next year only, so a far-off year can't be picked by mistake
    private final int thisYear = Calendar.getInstance().get(Calendar.YEAR);
    private String[] yearList = {String.valueOf(thisYear - 1), String.valueOf(thisYear), String.valueOf(thisYear + 1)};
    private String[] monthNameArray = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};

    private Toolbar toolbarCustom;
    private Spinner monthSpinner, yearSpinner;
    private RecyclerView rvDaySelector, rvItems;
    private RadioGroup rgVisitType;
    private RadioButton rbtnCustomer, rbtnDoctor, rbtnMorning;
    private RadioGroup rgShift;
    private View llShift;
    private Map<Integer, List<DoctorChamberName>> allChambersByDoctor = new HashMap<>();
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
    // Chamber-wise plan: date -> (doctorId -> chamberId); a selected doctor without an entry is doctor-wise
    private Map<String, Map<Integer, Integer>> selectedChamberIdsByDate = new HashMap<>();
    // "yyyy-MM" months already loaded from the server, so going back to a month keeps unsaved edits
    private Set<String> loadedMonths = new HashSet<>();

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
        loadSavedPlan();
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
        rgShift = findViewById(R.id.rgShift);
        rbtnMorning = findViewById(R.id.rbtnMorning);
        llShift = findViewById(R.id.llShift);
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
                    loadSavedPlan();
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

    private Map<Integer, Integer> getOrCreateChamberMap(String dateStr) {
        if (!selectedChamberIdsByDate.containsKey(dateStr)) {
            selectedChamberIdsByDate.put(dateStr, new HashMap<>());
        }
        return selectedChamberIdsByDate.get(dateStr);
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
                etSearch.setHint("Search customer");
            } else {
                etSearch.setHint("Search doctor");
            }
            llShift.setVisibility(checkedId == R.id.rbtnDoctor ? View.VISIBLE : View.GONE);
            refreshCurrentListAndHeader();
        });
        rgShift.setOnCheckedChangeListener((group, checkedId) -> refreshCurrentListAndHeader());

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
                // shortcut: one small SQLite query per doctor; add a bulk chamber query if doctor lists get large
                final Map<Integer, List<DoctorChamberName>> chambersByDoctor = new HashMap<>();
                for (DoctorListViewModel d : doctors) {
                    chambersByDoctor.put(d.getDoctorId(), dbDoctorHelper.getChamberIdListFromSQLite(d.getDoctorId()));
                }

                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    masterCustomerList = (customers != null) ? customers : new ArrayList<>();
                    masterDoctorList = doctors;
                    allChambersByDoctor = chambersByDoctor;

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

    /** Shows the plan already saved on the server for the selected month as ticked items. */
    private void loadSavedPlan() {
        String month = String.format(Locale.US, "%04d-%02d", selectedYear, selectedMonthIndex + 1);
        if (empId == 0 || !loadedMonths.add(month)) return;
        TourApiCall service = RetrofitClientInstance.getRetrofitInstance().create(TourApiCall.class);
        service.GetDayWiseTourPlan(empId, selectedMonthIndex + 1, selectedYear).enqueue(new Callback<List<DayWiseTourPlanRow>>() {
            @Override
            public void onResponse(@NonNull Call<List<DayWiseTourPlanRow>> call, @NonNull Response<List<DayWiseTourPlanRow>> response) {
                if (response.body() == null) {
                    loadedMonths.remove(month);
                    return;
                }
                for (DayWiseTourPlanRow row : response.body()) {
                    if ("C".equals(row.Type)) {
                        getOrCreateCustomerSet(row.TourPlanDate).add(row.Id);
                    } else {
                        getOrCreateDoctorSet(row.TourPlanDate).add(row.Id);
                        if (row.ChamberId > 0) getOrCreateChamberMap(row.TourPlanDate).put(row.Id, row.ChamberId);
                    }
                }
                refreshCurrentListAndHeader();
            }

            @Override
            public void onFailure(@NonNull Call<List<DayWiseTourPlanRow>> call, @NonNull Throwable t) {
                loadedMonths.remove(month); // retry on the next month change
                Log.e(TAG, "Loading saved day-wise tour plan failed", t);
            }
        });
    }

    /** Days before today are view-only. */
    private boolean isPastDate() {
        Calendar today = Calendar.getInstance();
        String todayStr = String.format(Locale.US, "%04d-%02d-%02d",
                today.get(Calendar.YEAR), today.get(Calendar.MONTH) + 1, today.get(Calendar.DAY_OF_MONTH));
        return currentDateStr.compareTo(todayStr) < 0;
    }

    private void applyReadOnlyState() {
        boolean past = isPastDate();
        tourPlanAdapter.setReadOnly(past);
        btnSave.setEnabled(!past);
        btnFinalSubmit.setEnabled(!past);
        btnSave.setAlpha(past ? 0.4f : 1f);
        btnFinalSubmit.setAlpha(past ? 0.4f : 1f);
    }

    private void filterCurrentList(String query) {
        applyReadOnlyState();
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
            // Only chambers of the chosen shift; a doctor without such a chamber is hidden for that shift
            boolean morning = rbtnMorning.isChecked();
            Map<Integer, List<DoctorChamberName>> shiftChambers = new HashMap<>();
            for (Map.Entry<Integer, List<DoctorChamberName>> e : allChambersByDoctor.entrySet()) {
                List<DoctorChamberName> list = new ArrayList<>();
                if (e.getValue() != null) {
                    for (DoctorChamberName c : e.getValue()) {
                        if (morning ? c.isMorning() : c.isEvening()) list.add(c);
                    }
                }
                if (!list.isEmpty()) shiftChambers.put(e.getKey(), list);
            }
            tourPlanAdapter.setChambersByDoctor(shiftChambers);
            for (DoctorListViewModel item : masterDoctorList) {
                if (!shiftChambers.containsKey(item.getDoctorId())) continue;
                if (filterPattern.isEmpty()) {
                    filteredDoctorList.add(item);
                } else {
                    boolean matchName = item.getDoctorName() != null && item.getDoctorName().toLowerCase().contains(filterPattern);
                    boolean matchCode = item.getDoctorCode() != null && item.getDoctorCode().toLowerCase().contains(filterPattern);
                    boolean matchMarket = item.getMarketName() != null && item.getMarketName().toLowerCase().contains(filterPattern);
                    boolean matchType = item.getDoctorTypeName() != null && item.getDoctorTypeName().toLowerCase().contains(filterPattern);

                    if (matchName || matchCode || matchMarket || matchType) {
                        filteredDoctorList.add(item);
                    }
                }
            }
            tourPlanAdapter.setDoctorData(filteredDoctorList, getOrCreateDoctorSet(currentDateStr), getOrCreateChamberMap(currentDateStr));
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
        // Save keeps a draft on the server (so the selection is there next time); Final Submit marks it submitted
        btnSave.setOnClickListener(v -> submitTourPlanToServer(false));

        btnFinalSubmit.setOnClickListener(v -> submitTourPlanToServer(true));
    }

    private boolean validateSelectionForDate(String dateStr) {
        Set<Integer> custSet = getOrCreateCustomerSet(dateStr);
        Set<Integer> docSet = getOrCreateDoctorSet(dateStr);

        return (custSet != null && !custSet.isEmpty()) || (docSet != null && !docSet.isEmpty());
    }

    private void submitTourPlanToServer(boolean finalSubmit) {
        if (isPastDate()) {
            Toast.makeText(this, "Past dates are view-only.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!validateSelectionForDate(currentDateStr)) {
            Toast.makeText(this, "Please select at least one Customer or Doctor.", Toast.LENGTH_LONG).show();
            return;
        }

        Set<Integer> custIds = getOrCreateCustomerSet(currentDateStr);
        Set<Integer> docIds = getOrCreateDoctorSet(currentDateStr);
        Map<Integer, Integer> chamberIds = getOrCreateChamberMap(currentDateStr);

        DayWiseTourPlanReq dayReq = new DayWiseTourPlanReq();
        dayReq.EmpInfoId = empId;
        dayReq.TourPlanDate = currentDateStr;
        dayReq.IsFinalSubmit = finalSubmit;
        dayReq.CustomerIds.addAll(custIds);
        for (Integer doctorId : docIds) {
            Integer chamberId = chamberIds.get(doctorId);
            dayReq.DoctorChambers.add(new DayWiseTourPlanReq.DoctorChamber(doctorId, chamberId != null ? chamberId : 0));
        }

        String done = String.format(Locale.US, "Tour Plan %s for %s: %d Customers, %d Doctors",
                finalSubmit ? "submitted" : "saved", currentDateStr, custIds.size(), docIds.size());
        progressDialog.setMessage(finalSubmit ? "Submitting Tour Plan..." : "Saving Tour Plan...");
        progressDialog.show();
        try {
            TourApiCall service = RetrofitClientInstance.getRetrofitInstance().create(TourApiCall.class);
            service.SaveDayWiseTourPlan(dayReq).enqueue(new Callback<ResultInfo>() {
                @Override
                public void onResponse(@NonNull Call<ResultInfo> call, @NonNull Response<ResultInfo> response) {
                    progressDialog.dismiss();
                    ResultInfo result = response.body();
                    if (response.isSuccessful() && result != null && result.getSuccess()) {
                        Toast.makeText(DayWiseTourPlanActivity.this, done, Toast.LENGTH_LONG).show();
                    } else {
                        String msg = result != null && result.getErrorMessage() != null ? result.getErrorMessage() : "Tour Plan submission failed";
                        Toast.makeText(DayWiseTourPlanActivity.this, msg, Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ResultInfo> call, @NonNull Throwable t) {
                    progressDialog.dismiss();
                    Log.e(TAG, "Day-wise tour plan submission failed", t);
                    Toast.makeText(DayWiseTourPlanActivity.this, "Tour Plan not submitted (network error). Please try again.", Toast.LENGTH_LONG).show();
                }
            });
        } catch (Exception e) {
            progressDialog.dismiss();
            Log.e(TAG, "Error submitting day-wise Tour Plan", e);
            Toast.makeText(DayWiseTourPlanActivity.this, "Tour Plan not submitted. Please try again.", Toast.LENGTH_LONG).show();
        }
    }
}
