package com.creatrix.unicorn.Activity.Doctor.AddDoctor;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.widget.LinearLayout;
import android.text.method.DigitsKeyListener;
import android.text.method.TextKeyListener;
import android.util.Log;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.SearchView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.creatrix.unicorn.Activity.Doctor.Approval.DoctorApprovalListActivity;
import com.creatrix.unicorn.Activity.Doctor.Approval.Model.ChembarTypeWithName;
import com.creatrix.unicorn.Activity.Doctor.Approval.Model.DoctorApproveModel;
import com.creatrix.unicorn.Activity.MainDashboardActivity;
import com.creatrix.unicorn.DBAdapter.DBCrudHelper;
import com.creatrix.unicorn.DBAdapter.DBDDTU.DBDDTUHelper;
import com.creatrix.unicorn.DBAdapter.DBDoctor.DBDoctorHelper;
import com.creatrix.unicorn.Interface.IDoctor;
import com.creatrix.unicorn.Interface.IMarketStracture;
import com.creatrix.unicorn.Model.Doctor.Brand;
import com.creatrix.unicorn.Model.Doctor.ChembarList;
import com.creatrix.unicorn.Model.Doctor.ContactTypeVM;
import com.creatrix.unicorn.Model.Doctor.DoctorCategory;
import com.creatrix.unicorn.Model.Doctor.DoctorChamberTypeVM;
import com.creatrix.unicorn.Model.Doctor.DoctorContact;
import com.creatrix.unicorn.Model.Doctor.DoctorDegreeViewModel;
import com.creatrix.unicorn.Model.Doctor.DoctorDesignation;
import com.creatrix.unicorn.Model.Doctor.DoctorSM;
import com.creatrix.unicorn.Model.Doctor.DoctorSpecialityViewModel;
import com.creatrix.unicorn.Model.Doctor.DoctorTypeVM;
import com.creatrix.unicorn.Model.Doctor.ProgramType;
import com.creatrix.unicorn.Model.Doctor.SpecialDay;
import com.creatrix.unicorn.Model.InstitutionVM;
import com.creatrix.unicorn.Model.MarketStructure.StructureTable.Area;
import com.creatrix.unicorn.Model.MarketStructure.StructureTable.Group;
import com.creatrix.unicorn.Model.MarketStructure.StructureTable.Market;
import com.creatrix.unicorn.Model.MarketStructure.StructureTable.Region;
import com.creatrix.unicorn.Model.MarketStructure.StructureTable.SubTeritorry;
import com.creatrix.unicorn.Model.MarketStructure.StructureTable.Teritorry;
import com.creatrix.unicorn.Model.ModelProviderType;
import com.creatrix.unicorn.Model.ModelSMCType;
import com.creatrix.unicorn.Model.StationType;
import com.creatrix.unicorn.Presenter.DoctorPresenter;
import com.creatrix.unicorn.Presenter.MarketStructurePresenter;
import com.creatrix.unicorn.R;
import com.creatrix.unicorn.RecyclerAdapter._chamber_ListAdapter;
import com.creatrix.unicorn.RecyclerAdapter._contact_ListAdapter;
import com.creatrix.unicorn.RecyclerAdapter._specialday_ListAdapter;
import com.creatrix.unicorn.Services.Constants;
import com.creatrix.unicorn.UtilityHelper.NetworkInformation;
import com.creatrix.unicorn.UtilityHelper.LocationGet;
import com.creatrix.unicorn.UtilityHelper.LocationGuard;
import com.creatrix.unicorn.UtilityHelper.SessionManagement;
import com.creatrix.unicorn.UtilityHelper.SnackBarManagement;
import com.creatrix.unicorn.UtilityHelper.UtilityHelper;
import com.creatrix.unicorn.databinding.ActivityDoctorBinding;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class DoctorActivity extends AppCompatActivity implements IDoctor.View, IMarketStracture.View {
    ActivityDoctorBinding viewBinding;
    Dialog popAdd,popContact;
    private static String monthNameArray[] = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
    public static String monthNameArrayFull[] = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
    DatePickerDialog picker;

    IDoctor.Presenter presenter;
    IMarketStracture.Presenter mpresenter;

    String[] listItemsBrand;
    boolean[] checkedItems_Brand;
    ArrayList<Integer> mUserItems_Brand = new ArrayList<>();

    String[] listItemsDegree = new String[0];
    boolean[] checkedItems = new boolean[0];
    ArrayList<Integer> mUserItems = new ArrayList<>();

    String[] listItems_Specility;
    boolean[] checkedItems_Specility;
    ArrayList<Integer> mUserItems_Specility = new ArrayList<>();

    String[] listItems_Type;
    boolean[] checkedItems_Type;
    ArrayList<Integer> mUserItems_Type = new ArrayList<>();

    String[] listItems_ins;
    boolean[] checkedItems_ins;
    ArrayList<Integer> mUserItems_ins = new ArrayList<>();


    DBCrudHelper dbCrudHelper;
    DBDoctorHelper dbDoctorHelper;
    DBDDTUHelper dbddtuHelper;
    _chamber_ListAdapter itemsAdapter;
    _contact_ListAdapter contactAdapter;
    _specialday_ListAdapter spdayAdapter;

    List<DoctorDesignation> doc_desigList;
    List<DoctorDegreeViewModel> doc_degreeList;
    List<DoctorSpecialityViewModel> doc_specilityList;
    List<DoctorTypeVM> doc_typeList;
    List<InstitutionVM> doc_institutionList;
    List<DoctorChamberTypeVM> doc_chamberList;
    List<Brand> brandList;


    List<DoctorChamberTypeVM> itemList = new ArrayList<>();
    List<ChembarList> chembarLists;
    List<SpecialDay> specialdayLists = new ArrayList<>();
    List<DoctorContact> doccontactLists = new ArrayList<>();
    DoctorDesignation desigSelected;

    int selectedRegionId, selectedAreaId, selectedTeriId, selectedSTeri, selectedMarket;
    int selectedDoctypeId, selectedchembarTypeId, selectedsclialdayId,selectedcontactTypeId;
    String selectedchembarTypeName,selectedcontactTypeName,selectedsclialdayName;
    TextView cmbrtype,contacttype;
    EditText chamberName,contactName,sittingTime,chamberAddress;
    TextInputLayout til_chamberDD,til_sittingTime,til_chamberAddress,til_contactName;
    RadioGroup rgShift;
    RadioButton rbMorning, rbEvening;
    String roleType;
    int brand_count = 0;
    int selectedbrand_count = 0;
    int brand_countValue, empId, doctorid;
    DoctorApproveModel docInfos;

    String edit_dName,spcialday, latitude, longitude;
    int edit_did;
    boolean is_Contact,is_Chamber;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //    setContentView(R.layout.activity_doctor);
        viewBinding = com.creatrix.unicorn.databinding.ActivityDoctorBinding.inflate(getLayoutInflater());
        View mainView = viewBinding.getRoot();

        dbCrudHelper = new DBCrudHelper(DoctorActivity.this);
        dbDoctorHelper = new DBDoctorHelper(DoctorActivity.this);
        dbddtuHelper = new DBDDTUHelper(DoctorActivity.this);
        setContentView(mainView);
        setupFieldReorder();

        presenter = new DoctorPresenter(this, DoctorActivity.this);
        mpresenter = new MarketStructurePresenter(this, DoctorActivity.this);

        viewBinding.toolbarCustom.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        SessionManagement session = new SessionManagement(getApplicationContext());
        //session.checkLogin();

        HashMap<String, String> user = session.getUserDetails();
        String userName = user.get(SessionManagement.KEY_LoginName);
        empId = Integer.parseInt(user.get(SessionManagement.KEY_EmpId));
        roleType = user.get(SessionManagement.KEY_EmpRoleType);
        switch (Constants.WHO) {
            case "DocApprovalAC":
                viewBinding.toolbarTitle.setText("Edit Doctor");
                viewBinding.psubmitBnt.setVisibility(View.GONE);
                Gson gson = new Gson();
                docInfos = gson.fromJson(getIntent().getStringExtra("DocEditdata"), DoctorApproveModel.class);
                presenter.GetDoctorDesignation(0);
                presenter.GetDoctorType(0);
                presenter.GetSpeciality();
                presenter.GetInstitute();
                presenter.GetBrand();
                presenter.GetDoctorCategory(0);
                presenter.GetChamber(0);
               // presenter.GetProgramType(0);
                presenter.GetProviderType(0);
                presenter.GetSMCType(0);

                presenter.GetContactType(0);
                presenter.GetSpecialType(0);

                mpresenter.GetRegionLocal(0);
                mpresenter.GetAreaLocal(0);
                mpresenter.GetTeritoryLocal(0);
                mpresenter.GetSTeritoryLocal(0);
                mpresenter.GetMarketLocal(0);

                viewBinding.regionSpinner.setBackground(null);
                viewBinding.areaSpinner.setBackground(null);
                viewBinding.territorySpinner.setBackground(null);
                viewBinding.sterritorySpinner.setBackground(null);
                viewBinding.marketSpinner.setBackground(null);

                viewBinding.regionSpinner.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        return true;
                    }
                });
                viewBinding.areaSpinner.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        return true;
                    }
                });
                viewBinding.territorySpinner.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        return true;
                    }
                });
                viewBinding.sterritorySpinner.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        return true;
                    }
                });
                viewBinding.marketSpinner.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        return true;
                    }
                });
                setupEdit(roleType, empId, docInfos);
                break;
            case "AddDoctor":
                if (!NetworkInformation.isConnected(this)) {
                    SnackBarManagement._warning_CustomMessage(viewBinding.master, "Your Internet Connection is Off.\nYou Can't Submit");
                    viewBinding.psubmitBnt.setVisibility(View.GONE);
                } else {
                    viewBinding.psubmitBnt.setVisibility(View.VISIBLE);
                }
                viewBinding.pupdateBnt.setVisibility(View.GONE);
                //Role wise spinner populate
                try {
                    switch (roleType) {
                        case "MIO":
                            viewBinding.areadiv.setVisibility(View.GONE);
                            viewBinding.regiondiv.setVisibility(View.GONE);
                            mpresenter.GetTeritoryLocal(0);
                            break;
                        case "AM":
                            viewBinding.regiondiv.setVisibility(View.GONE);
                            viewBinding.areadiv.setVisibility(View.VISIBLE);
                            mpresenter.GetAreaLocal(0);
                            break;
                        case "DZSM":
                            viewBinding.regiondiv.setVisibility(View.GONE);
                            viewBinding.areadiv.setVisibility(View.VISIBLE);
                            mpresenter.GetRegionLocal(0);
                            break;
                    }
                    viewBinding.regiondiv.setVisibility(View.GONE);
                    viewBinding.doctorTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            DoctorTypeVM doctype = (DoctorTypeVM) parent.getSelectedItem();
                            selectedDoctypeId = doctype.getDoctorTypeId();
                            mUserItems.clear();
                            viewBinding.degreeListStr.setText("");
                            presenter.GetDegree(selectedDoctypeId);
                            //viewBinding.degreeListStr.setText(item);
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });
                    iniPopup();
                    iniPopupContact();


                } catch (Exception exception) {
                    exception.printStackTrace();
                }
                presenter.GetDoctorDesignation(0);
                presenter.GetDoctorType(0);
                presenter.GetSpeciality();
                presenter.GetInstitute();
                presenter.GetBrand();
                presenter.GetDoctorCategory(0);
                presenter.GetChamber(0);
                presenter.GetProviderType(0);
                presenter.GetSMCType(0);
                presenter.GetContactType(0);
                presenter.GetSpecialType(0);
                viewBinding.regionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        Region region = (Region) parent.getSelectedItem();
                        selectedRegionId = region.getRegionId();
                        mpresenter.GetAreaLocal(selectedRegionId);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
                viewBinding.areaSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        Area area = (Area) parent.getSelectedItem();
                        selectedAreaId = area.getAreaId();
                        mpresenter.GetTeritoryLocal(selectedAreaId);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
                viewBinding.territorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        Teritorry teri = (Teritorry) parent.getSelectedItem();
                        selectedTeriId = teri.getTerritoryId();
                        mpresenter.GetSTeritoryLocal(selectedTeriId);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
                viewBinding.sterritorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        SubTeritorry steri = (SubTeritorry) parent.getSelectedItem();
                        selectedSTeri = steri.getSubTerritoryId();
                        mpresenter.GetMarketLocal(selectedSTeri);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
                viewBinding.marketSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        Market market = (Market) parent.getSelectedItem();
                        selectedMarket = market.getMarketId();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
                break;
        }

        hideDisabledDoctorFields();

        viewBinding.designationSpinner.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    showSearchableSingleSelectDialog(
                            "Doctor Designation",
                            doc_desigList,
                            viewBinding.designationSpinner
                    );
                }
                return true;
            }
        });

        viewBinding.datePickerDocDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UtilityHelper._datePickerDialogeForDates_DisableNextDates(viewBinding.scecialdateTxt, DoctorActivity.this);
            }
        });
        LocationManager locationManagerdd = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (locationManagerdd != null && (locationManagerdd.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManagerdd.isProviderEnabled(LocationManager.NETWORK_PROVIDER))) {
        } else {
            showGPSDisabledAlertToUser();
        }
        viewBinding.location.setOnClickListener(v -> showCurrentLocation());
        viewBinding.latlon.setOnClickListener(v -> showManualLocationDialog());
        showCurrentLocation(false);
        // viewBinding.docName.setFilters(new InputFilter[]{new InputFilter.AllCaps()});
        viewBinding.degreeAdd.setOnClickListener(view -> LoadDegree());
        viewBinding.degreeSection.setOnClickListener(view -> LoadDegree());
        viewBinding.specialityAdd.setOnClickListener(view -> LoadSpecilityInView());
        //viewBinding.typeAdd.setOnClickListener(view -> LoadDocTypeInView());
        viewBinding.institueAdd.setOnClickListener(view -> LoadInstituteInView());
        viewBinding.brandAdd.setOnClickListener(v -> LoadBrandInView());
        viewBinding.psubmitBnt.setOnClickListener(v -> {
            if (TextUtils.isEmpty(viewBinding.docName.getText().toString())) {
                SnackBarManagement._warning_CustomMessage(viewBinding.masterLayoutId, "Doctor Name is required");
                return;
            }
            if (TextUtils.isEmpty(viewBinding.degreeListStr.getText().toString())) {
                SnackBarManagement._warning_CustomMessage(viewBinding.masterLayoutId, "Degree is required");
                return;
            }
            if (TextUtils.isEmpty(viewBinding.specilityListStr.getText().toString())) {
                SnackBarManagement._warning_CustomMessage(viewBinding.masterLayoutId, "Speciality is required");
                return;
            }
            if (TextUtils.isEmpty(viewBinding.brandListStr.getText().toString())) {
                SnackBarManagement._warning_CustomMessage(viewBinding.masterLayoutId, "Brand is required");
                return;
            }
            if (itemList.size() == 0) {
                SnackBarManagement._warning_CustomMessage(viewBinding.masterLayoutId, "Minimum 1 Institute is required");
                return;
            }
            if (doccontactLists.size()==0) {
                SnackBarManagement._warning_CustomMessage(viewBinding.masterLayoutId, "Contact is required");
                return;
            }
            // viewBinding.rvChamberlist.
            SubmitClick(empId);
        });
    }

    private void showCurrentLocation() {
        showCurrentLocation(true);
    }

    /**
     * @param userInitiated false for the silent attempt on page load: no toasts, and no manual-entry
     *                       dialog popped on failure (that would surprise the user before they asked for it).
     */
    private void showCurrentLocation(boolean userInitiated) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (userInitiated) {
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                }, 1001);
            }
            return;
        }
        viewBinding.latlon.setText("Fetching location...");
        if (userInitiated) {
            Toast.makeText(this, "Fetching location...", Toast.LENGTH_SHORT).show();
        }
        android.util.Log.d("DoctorActivity", "Requesting location fix via LocationGuard...");
        LocationGuard.requestFix(this, (location, error) -> {
            if (location == null) {
                Location fallback = LocationGuard.getBestLocationFromManager(this);
                if (fallback != null && !LocationGuard.isMock(fallback)) {
                    latitude = String.valueOf(fallback.getLatitude());
                    longitude = String.valueOf(fallback.getLongitude());
                    viewBinding.latlon.setText(latitude + "," + longitude);
                    if (userInitiated) {
                        Toast.makeText(this, "Location detected", Toast.LENGTH_SHORT).show();
                    }
                    return;
                }
                if (latitude == null || latitude.isEmpty()) {
                    viewBinding.latlon.setText("Tap to enter location");
                } else {
                    viewBinding.latlon.setText(latitude + "," + longitude);
                }
                if (userInitiated) {
                    Toast.makeText(this, (error != null ? error : "Location not found.") + " Tap the location field to enter it manually.", Toast.LENGTH_LONG).show();
                }
                return;
            }
            latitude = String.valueOf(location.getLatitude());
            longitude = String.valueOf(location.getLongitude());
            viewBinding.latlon.setText(latitude + "," + longitude);
            if (userInitiated) {
                Toast.makeText(this, "Location detected: " + latitude + "," + longitude, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showManualLocationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Doctor Location (Coordinates)");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(60, 40, 60, 20);

        TextView hintText = new TextView(this);
        hintText.setText("Enter Latitude and Longitude coordinates manually or retry GPS detection:");
        hintText.setTextSize(13);
        hintText.setTextColor(Color.DKGRAY);
        hintText.setPadding(0, 0, 0, 20);
        layout.addView(hintText);

        final EditText inputLat = new EditText(this);
        inputLat.setHint("Latitude (e.g. 23.8103)");
        inputLat.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
        if (latitude != null && !latitude.isEmpty()) {
            inputLat.setText(latitude);
        }
        layout.addView(inputLat);

        final EditText inputLon = new EditText(this);
        inputLon.setHint("Longitude (e.g. 90.4125)");
        inputLon.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
        if (longitude != null && !longitude.isEmpty()) {
            inputLon.setText(longitude);
        }
        layout.addView(inputLon);

        builder.setView(layout);

        builder.setPositiveButton("Set Location", (dialog, which) -> {
            String latStr = inputLat.getText().toString().trim();
            String lonStr = inputLon.getText().toString().trim();
            if (!latStr.isEmpty() && !lonStr.isEmpty()) {
                latitude = latStr;
                longitude = lonStr;
                viewBinding.latlon.setText(latitude + "," + longitude);
                Toast.makeText(this, "Location set: " + latitude + "," + longitude, Toast.LENGTH_SHORT).show();
            } else {
                latitude = null;
                longitude = null;
                viewBinding.latlon.setText("Tap to enter location");
            }
        });

        builder.setNeutralButton("Retry GPS", (dialog, which) -> {
            showCurrentLocation();
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1001) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showCurrentLocation();
            } else {
                Toast.makeText(this, "Location permission is required to detect location", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void SubmitClick(int empId) {
        try {
            String docName = viewBinding.docName.getText().toString();
            String docDegree = viewBinding.degreeListStr.getText().toString();
            String docSpeciality = viewBinding.specilityListStr.getText().toString();
            String institute = viewBinding.instituteListStr.getText().toString();
            String instituteIdStr = "";
            if (doc_institutionList != null) {
                for (InstitutionVM item : doc_institutionList) {
                    if (item.getInstitution().equalsIgnoreCase(institute)) {
                        instituteIdStr = String.valueOf(item.getInstitutionId());
                        break;
                    }
                }
            }
            if (instituteIdStr.isEmpty()) {
                if (institute.equalsIgnoreCase("Male")) {
                    instituteIdStr = "1";
                } else if (institute.equalsIgnoreCase("Female")) {
                    instituteIdStr = "2";
                } else if (institute.equalsIgnoreCase("Others")) {
                    instituteIdStr = "3";
                } else if (institute.equals("----")) {
                    instituteIdStr = "";
                } else {
                    instituteIdStr = institute;
                }
            }
            String rawInstitute = viewBinding.instituteListStr.getText().toString().trim();
            if (rawInstitute.isEmpty() || rawInstitute.equals("----") || instituteIdStr.isEmpty()) {
                SnackBarManagement._warning_CustomMessage(viewBinding.masterLayoutId, "Gender is mandatory! Please select Gender.");
                return;
            }
            institute = instituteIdStr;
            String docBrand = viewBinding.brandListStr.getText().toString();
            String docAddress = viewBinding.docAddress.getText().toString().trim();

            if (docName.trim().isEmpty()) {
                viewBinding.docName.setError("Doctor Name is required");
                viewBinding.docName.requestFocus();
                SnackBarManagement._warning_CustomMessage(viewBinding.masterLayoutId, "Doctor Name is required!");
                return;
            }

            if (docAddress.isEmpty()) {
                viewBinding.docAddress.setError("Doctor Address is required");
                viewBinding.docAddress.requestFocus();
                SnackBarManagement._warning_CustomMessage(viewBinding.masterLayoutId, "Doctor Address is mandatory!");
                return;
            }

            DoctorTypeVM doctorType = (DoctorTypeVM) viewBinding.doctorTypeSpinner.getSelectedItem();
            int dtypeid = doctorType.getDoctorTypeId();

            DoctorCategory catType = (DoctorCategory) viewBinding.doctorCategorySpinner.getSelectedItem();
            int catTypeId = catType.getCategoryId();

            DoctorDesignation docDesi = (DoctorDesignation) viewBinding.designationSpinner.getSelectedItem();
            int designationId = docDesi.getDesignationId();

            chembarLists = new ArrayList<>();
            for (int i = 0; i < itemList.size(); i++) {
                String chamberName = itemList.get(i).getChamberName();
                int chamberTypeId = itemList.get(i).getChamberTypeId();
                String shift = itemList.get(i).getShift();
                String sitTime = itemList.get(i).getSittingTime();
                int instId = itemList.get(i).getInstitutionId();
                if (instId == 0) {
                    instId = chamberTypeId;
                }
                String instName = itemList.get(i).getInstitutionName();
                if (instName == null || instName.isEmpty()) {
                    instName = itemList.get(i).getChamberTypeName();
                }
                ChembarList cl = new ChembarList();
                cl.setChamberTypeId(chamberTypeId);
                cl.setName(chamberName);
                cl.setShift(shift);
                cl.setSittingTime(sitTime);
                cl.setInstitutionId(instId);
                cl.setInstitutionName(instName);
                cl.setAddress(itemList.get(i).getAddress());
                chembarLists.add(cl);
            }
          /*  doccontactLists = new ArrayList<>();
            for (int i = 0; i < doccontactLists.size(); i++) {
                String contactName = doccontactLists.get(i).getContact();
                int contactTypeId = doccontactLists.get(i).getContactTypeId();
                ChembarList cl = new ChembarList();
                cl.setChamberTypeId(chamberTypeId);
                cl.setName(chamberName);
                chembarLists.add(cl);
            }*/

            DoctorSM doctorSM = new DoctorSM();
            doctorSM.setDoctorName(docName);
            doctorSM.setDoctorAddress(docAddress);
            doctorSM.setDesignationId(designationId);
            doctorSM.setDegreeStr(docDegree);

            doctorSM.setSpecialityStr(docSpeciality);
            doctorSM.setInstitutionSTr(institute);
            doctorSM.setBrandStr(docBrand);

            doctorSM.setDoctorTypeId(dtypeid);
            doctorSM.setDoctorTypeStr(String.valueOf(dtypeid));
            doctorSM.setDoctorCategoryId(catTypeId);
            doctorSM.setaChemberListDAO(chembarLists);
            doctorSM.setaDoctorContactDAO(doccontactLists);
            doctorSM.setaDoctorSpecialDAO(specialdayLists);
            doctorSM.setProgramTypeId(0);
            doctorSM.setSMCTypeId(0);
            doctorSM.setAreaId(0);
            doctorSM.setTerritoryId(0);
            doctorSM.setSubTerritoryId(0);
            doctorSM.setMarketId(0);

            doctorSM.setSessionUser(empId);
            doctorSM.setEmpId(empId);
            doctorSM.setLatitude(latitude);
            doctorSM.setLongitude(longitude);
            // doctorSM.setRemarks(viewBinding.remarksTxt.getText().toString());
            Gson gs = new Gson();
            String ss = gs.toJson(doctorSM);
            System.out.println("submit : " + ss);
            presenter.SaveDoctor(doctorSM, "Submit");


        } catch (Exception ex) {
            Log.e("doctorSubmit-Ex", "SubmitClick: ", ex);
        }


    }

    private void hideDisabledDoctorFields() {
        viewBinding.providerTypeSection.setVisibility(View.GONE);
        viewBinding.pharmaPlatformSection.setVisibility(View.GONE);
        viewBinding.areadiv.setVisibility(View.GONE);
        viewBinding.teridiv.setVisibility(View.GONE);
        viewBinding.subTerritorySection.setVisibility(View.GONE);
        viewBinding.marketSection.setVisibility(View.GONE);
    }

    public void LoadDegree() {
        presenter.GetDegree(selectedDoctypeId);
        showSearchableMultiSelectDialog(
                "Doctor Degree",
                listItemsDegree,
                checkedItems,
                mUserItems,
                viewBinding.degreeListStr
        );
    }
    public void LoadSpecilityInView() {
        showSearchableMultiSelectDialog(
                "Doctor Specialties",
                listItems_Specility,
                checkedItems_Specility,
                mUserItems_Specility,
                viewBinding.specilityListStr
        );
    }
    public void LoadInstituteInView() {
        showSearchableSingleSelectDialogForTextView(
                "Select Gender",
                listItems_ins,
                viewBinding.instituteListStr
        );
    }

    private void showSearchableSingleSelectDialogForTextView(
            final String title,
            final String[] allItems,
            final TextView resultTextView
    ) {
        if (allItems == null || allItems.length == 0) {
            SnackBarManagement._warning_CustomMessage(
                    viewBinding.masterLayoutId,
                    "No " + title.toLowerCase() + " found"
            );
            return;
        }

        // Prepare dialog items
        final List<DialogItem> dialogItems = new ArrayList<>();
        for (int i = 0; i < allItems.length; i++) {
            dialogItems.add(new DialogItem(allItems[i], i));
        }
        final List<DialogItem> filteredItems = new ArrayList<>(dialogItems);

        // Inflate custom view
        View dialogView = getLayoutInflater().inflate(R.layout.searchable_list_dialog, null);
        SearchView searchView = dialogView.findViewById(R.id.search);
        ListView listView = dialogView.findViewById(R.id.listItems);

        final SearchableSingleAdapter adapter = new SearchableSingleAdapter(this, filteredItems);
        listView.setAdapter(adapter);

        AlertDialog.Builder mBuilder = new AlertDialog.Builder(DoctorActivity.this);
        mBuilder.setTitle(title);
        mBuilder.setView(dialogView);
        mBuilder.setCancelable(true);

        final AlertDialog mDialog = mBuilder.create();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                DialogItem item = adapter.getItem(position);
                resultTextView.setText(item.text);
                mDialog.dismiss();
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filteredItems.clear();
                for (DialogItem item : dialogItems) {
                    if (item.text.toLowerCase().contains(newText.toLowerCase())) {
                        filteredItems.add(item);
                    }
                }
                adapter.updateList(filteredItems);
                return true;
            }
        });

        mDialog.show();
    }
    public void LoadBrandInView() {
        showSearchableMultiSelectDialog(
                "Brand",
                listItemsBrand,
                checkedItems_Brand,
                mUserItems_Brand,
                viewBinding.brandListStr
        );
    }

    private void showSearchableMultiSelectDialog(
            final String title,
            final String[] allItems,
            final boolean[] checkedItems,
            final ArrayList<Integer> userItems,
            final TextView resultTextView
    ) {
        if (allItems == null || allItems.length == 0) {
            SnackBarManagement._warning_CustomMessage(
                    viewBinding.masterLayoutId,
                    "No " + title.toLowerCase() + " found"
            );
            return;
        }

        // Backup current checked states
        final boolean[] checkedItemsBackup = new boolean[checkedItems.length];
        System.arraycopy(checkedItems, 0, checkedItemsBackup, 0, checkedItems.length);

        // Prepare dialog items
        final List<DialogItem> dialogItems = new ArrayList<>();
        for (int i = 0; i < allItems.length; i++) {
            dialogItems.add(new DialogItem(allItems[i], i));
        }
        final List<DialogItem> filteredItems = new ArrayList<>(dialogItems);

        // Inflate custom view
        View dialogView = getLayoutInflater().inflate(R.layout.searchable_list_dialog, null);
        SearchView searchView = dialogView.findViewById(R.id.search);
        ListView listView = dialogView.findViewById(R.id.listItems);

        final SearchableAdapter adapter = new SearchableAdapter(this, filteredItems, checkedItems);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                DialogItem item = adapter.getItem(position);
                checkedItems[item.originalIndex] = !checkedItems[item.originalIndex];
                adapter.notifyDataSetChanged();
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filteredItems.clear();
                for (DialogItem item : dialogItems) {
                    if (item.text.toLowerCase().contains(newText.toLowerCase())) {
                        filteredItems.add(item);
                    }
                }
                adapter.updateList(filteredItems);
                return true;
            }
        });

        AlertDialog.Builder mBuilder = new AlertDialog.Builder(DoctorActivity.this);
        mBuilder.setTitle(title);
        mBuilder.setView(dialogView);
        mBuilder.setCancelable(false);

        mBuilder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                userItems.clear();
                for (int i = 0; i < checkedItems.length; i++) {
                    if (checkedItems[i]) {
                        userItems.add(i);
                    }
                }
                if (title.equalsIgnoreCase("Brand")) {
                    brand_count = userItems.size();
                }

                String item = "";
                for (int i = 0; i < userItems.size(); i++) {
                    item = item + allItems[userItems.get(i)];
                    if (i != userItems.size() - 1) {
                        item = item + ",";
                    }
                }
                resultTextView.setText(item);
            }
        });

        mBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                System.arraycopy(checkedItemsBackup, 0, checkedItems, 0, checkedItems.length);
                dialogInterface.dismiss();
            }
        });

        mBuilder.setNeutralButton("Clear All", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                for (int i = 0; i < checkedItems.length; i++) {
                    checkedItems[i] = false;
                }
                userItems.clear();
                if (title.equalsIgnoreCase("Brand")) {
                    brand_count = 0;
                }
                resultTextView.setText("");
            }
        });

        AlertDialog mDialog = mBuilder.create();
        mDialog.show();
    }

    private static class DialogItem {
        String text;
        int originalIndex;

        DialogItem(String text, int originalIndex) {
            this.text = text;
            this.originalIndex = originalIndex;
        }
    }

    private static class SearchableAdapter extends BaseAdapter {
        private List<DialogItem> items;
        private boolean[] checkedStates;
        private LayoutInflater inflater;

        SearchableAdapter(android.content.Context context, List<DialogItem> items, boolean[] checkedStates) {
            this.items = items;
            this.checkedStates = checkedStates;
            this.inflater = LayoutInflater.from(context);
        }

        void updateList(List<DialogItem> newItems) {
            this.items = newItems;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public DialogItem getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(android.R.layout.simple_list_item_multiple_choice, parent, false);
            }
            CheckedTextView checkedTextView = (CheckedTextView) convertView;
            DialogItem item = getItem(position);
            checkedTextView.setText(item.text);
            checkedTextView.setChecked(checkedStates[item.originalIndex]);
            return convertView;
        }
    }
    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left,
                R.anim.slide_out_right);
    }
    @Override
    public void onDegreeReceived(List<DoctorDegreeViewModel> aList) {
        try {
            doc_degreeList = aList;
            if (aList != null && !aList.isEmpty()) {
                listItemsDegree = new String[aList.size()];
                for (int i = 0; i < aList.size(); i++) {
                    listItemsDegree[i] = aList.get(i).getDegreeName();
                }
                checkedItems = new boolean[listItemsDegree.length];
            } else {
                listItemsDegree = new String[0];
                checkedItems = new boolean[0];
                mUserItems.clear();
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }

    }
    @Override
    public void onSpecialityReceived(List<DoctorSpecialityViewModel> aList) {
        try {
            if (aList != null) {
                listItems_Specility = new String[aList.size()];
                for (int i = 0; i < aList.size(); i++) {
                    listItems_Specility[i] = aList.get(i).getSpecialityName();
                }
                checkedItems_Specility = new boolean[listItems_Specility.length];

            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }

    }
    @Override
    public void onDoctorTypeReceived(List<DoctorTypeVM> aList) {
        try {
            if (aList.size()>0) {
                ArrayAdapter<DoctorTypeVM> dataAdapter = new ArrayAdapter<>(DoctorActivity.this, android.R.layout.simple_spinner_item, aList);
                dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                viewBinding.doctorTypeSpinner.setAdapter(dataAdapter);
            }


        } catch (Exception exception) {
            exception.printStackTrace();
        }

    }
    @Override
    public void onInstituteReceived(List<InstitutionVM> aList) {
        try {
            doc_institutionList = aList;
            if (aList.size()>0) {
                listItems_ins = new String[aList.size()];
                for (int i = 0; i < aList.size(); i++) {
                    listItems_ins[i] = aList.get(i).getInstitution();
                }
                checkedItems_ins = new boolean[listItems_ins.length];

            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
    @Override
    public void onBrandReceived(List<Brand> aList) {
        try {
            if (aList != null) {
                listItemsBrand = new String[aList.size()];
                for (int i = 0; i < aList.size(); i++) {
                    listItemsBrand[i] = aList.get(i).getProductSQName();
                }
                checkedItems_Brand = new boolean[listItemsBrand.length];

            }
            brandList = new ArrayList<>();
            brandList = dbDoctorHelper.getBrandListFromSQLite();
            if (brandList != null) {
                listItemsBrand = new String[brandList.size()];
                for (int i = 0; i < brandList.size(); i++) {
                    listItemsBrand[i] = brandList.get(i).getProductSQName();
                }
                checkedItems_Brand = new boolean[listItemsBrand.length];
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
    @Override
    public void onDocCategoryReceived(List<DoctorCategory> aList) {
        try {
            if (aList != null) {
                ArrayAdapter<DoctorCategory> dataAdapter = new ArrayAdapter<>(DoctorActivity.this, android.R.layout.simple_spinner_item, aList);
                dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                viewBinding.doctorCategorySpinner.setAdapter(dataAdapter);
            }

        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
    @Override
    public void onChamberReceived(List<DoctorChamberTypeVM> aList) {
        try {
            if (aList != null) {
                ArrayAdapter<DoctorChamberTypeVM> dataAdapter = new ArrayAdapter<>(DoctorActivity.this, R.layout._custom_spinner_tv, aList);
                viewBinding.chamberSpinner.setAdapter(dataAdapter);

                viewBinding.chamberSpinner.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {
                        DoctorChamberTypeVM cmm = (DoctorChamberTypeVM) dataAdapter.getItem(position);
                        selectedchembarTypeId = cmm.getChamberTypeId();
                        selectedchembarTypeName = cmm.getChamberTypeName();
                        cmbrtype.setText(selectedchembarTypeName);
                        chamberName.setText("");
                        if (sittingTime != null) sittingTime.setText("");
                        if (chamberAddress != null) chamberAddress.setText("");
                        if (rbMorning != null) rbMorning.setChecked(true);
                        til_chamberDD.setHint("Chamber Name *");
                        is_Chamber=true;
                        popAdd.show();
                    }
                });
            }

        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
    @Override
    public void onContactTypeReceived(List<ContactTypeVM> aList) {
        try {
            if (aList.size()>0) {
                ArrayAdapter<ContactTypeVM> dataAdapter = new ArrayAdapter<>(DoctorActivity.this, R.layout._custom_spinner_tv, aList);
                viewBinding.contactTypeSpinner.setAdapter(dataAdapter);
                viewBinding.contactTypeSpinner.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {
                        ContactTypeVM cmm = (ContactTypeVM) dataAdapter.getItem(position);
                        selectedcontactTypeId = cmm.getContactTypeId();
                        selectedcontactTypeName = cmm.getContactType();
                        contacttype.setText(selectedcontactTypeName);
                        contactName.setText("");
                        til_contactName.setHint("Contact "+selectedcontactTypeName);
                        if(cmm.getContactType().equals("Mobile"))
                        {
                            contactName.setKeyListener(DigitsKeyListener.getInstance("0123456789"));
                            is_Contact=true;
                            contactName.setFilters(new InputFilter[] { new InputFilter.LengthFilter(11) });
                        }if(cmm.getContactType().equals("Email")) {
                            contactName.setKeyListener(TextKeyListener.getInstance());
                            contactName.setFilters(new InputFilter[] { new InputFilter.LengthFilter(50) });
                            is_Contact=true;
                        }
                        popContact.show();
                    }
                });
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }

    }

  /*  @Override
    public void onProgramTypeReceived(List<ProgramType> aList) {
       *//* try {

            if (aList != null) {
                ArrayAdapter<ProgramType> dataAdapter = new ArrayAdapter<>(DoctorActivity.this, android.R.layout.simple_spinner_item, aList);
                dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                viewBinding.programTypeSpinner.setAdapter(dataAdapter);
            }

        } catch (Exception exception) {
            exception.printStackTrace();
        }*//*

    }*/

    @Override
    public void onProviderTypeReceived(List<ModelProviderType> aList) {
        try {
            if (aList != null) {
                ArrayAdapter<ModelProviderType> dataAdapter = new ArrayAdapter<>(DoctorActivity.this, android.R.layout.simple_spinner_item, aList);
                dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                viewBinding.programTypeSpinner.setAdapter(dataAdapter);
            }

        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
    @Override
    public void onSMCTypeReceived(List<ModelSMCType> aList) {
        try {
            if (aList != null) {
                ArrayAdapter<ModelSMCType> dataAdapter = new ArrayAdapter<>(DoctorActivity.this, android.R.layout.simple_spinner_item, aList);
                dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                viewBinding.smcTypeSpinner.setAdapter(dataAdapter);
            }

        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
    @Override
    public void onSpecialTypeReceived(List<SpecialDay> aList) {
        try {
            if (aList.size()>0) {
                ArrayAdapter<SpecialDay> dataAdapter = new ArrayAdapter<>(DoctorActivity.this, R.layout._custom_spinner_tv, aList);
                viewBinding.spcialdayTypeSpinner.setAdapter(dataAdapter);
                viewBinding.spcialdayTypeSpinner.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {
                        SpecialDay cmm = (SpecialDay) dataAdapter.getItem(position);
                        selectedsclialdayId = cmm.getSpecialDayId();
                        selectedsclialdayName = cmm.getSpecialDay();

                        final Calendar cldr = Calendar.getInstance();
                        int day = cldr.get(Calendar.DAY_OF_MONTH);
                        int month = cldr.get(Calendar.MONTH);
                        int year = cldr.get(Calendar.YEAR);
                        picker = new DatePickerDialog(DoctorActivity.this,
                                new DatePickerDialog.OnDateSetListener() {
                                    @Override
                                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                        int monthNumber = monthOfYear + 1;
                                        String docspcialday = Integer.toString(dayOfMonth) + "-" + monthNameArray[monthOfYear] + "-" + year;
                                        SpecialDay isitm = new SpecialDay(selectedsclialdayId, docspcialday,selectedsclialdayName);
                                        specialdayLists.add(isitm);
                                        spdayAdapter = new _specialday_ListAdapter(DoctorActivity.this, specialdayLists);
                                        viewBinding.rvSpecialdaylist.setLayoutManager(new LinearLayoutManager(DoctorActivity.this));
                                        viewBinding.rvSpecialdaylist.setHasFixedSize(true);
                                        viewBinding.rvSpecialdaylist.setAdapter(spdayAdapter);
                                        spdayAdapter.notifyDataSetChanged();
                                    }
                                }, year, month, day);
                        picker.show();


                    }
                });

            }

        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
    @Override
    public void onDoctorDesignationGet(List<DoctorDesignation> aList) {
        try {
            doc_desigList = aList;
            if (aList != null) {
                ArrayAdapter<DoctorDesignation> dataAdapter = new ArrayAdapter<>(DoctorActivity.this, android.R.layout.simple_spinner_item, aList);
                dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                viewBinding.designationSpinner.setAdapter(dataAdapter);
                //viewBinding.designationSpinner.setSelection(2);
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void showSearchableSingleSelectDialog(
            final String title,
            final List<DoctorDesignation> items,
            final Spinner targetSpinner
    ) {
        if (items == null || items.size() == 0) {
            SnackBarManagement._warning_CustomMessage(
                    viewBinding.masterLayoutId,
                    "No designations found"
            );
            return;
        }

        // Prepare dialog items
        final List<DialogItem> dialogItems = new ArrayList<>();
        for (int i = 0; i < items.size(); i++) {
            dialogItems.add(new DialogItem(items.get(i).getDesignationName(), i));
        }
        final List<DialogItem> filteredItems = new ArrayList<>(dialogItems);

        // Inflate custom view
        View dialogView = getLayoutInflater().inflate(R.layout.searchable_list_dialog, null);
        SearchView searchView = dialogView.findViewById(R.id.search);
        ListView listView = dialogView.findViewById(R.id.listItems);

        final SearchableSingleAdapter adapter = new SearchableSingleAdapter(this, filteredItems);
        listView.setAdapter(adapter);

        AlertDialog.Builder mBuilder = new AlertDialog.Builder(DoctorActivity.this);
        mBuilder.setTitle(title);
        mBuilder.setView(dialogView);
        mBuilder.setCancelable(true);

        final AlertDialog mDialog = mBuilder.create();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                DialogItem item = adapter.getItem(position);
                targetSpinner.setSelection(item.originalIndex);
                mDialog.dismiss();
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filteredItems.clear();
                for (DialogItem item : dialogItems) {
                    if (item.text.toLowerCase().contains(newText.toLowerCase())) {
                        filteredItems.add(item);
                    }
                }
                adapter.updateList(filteredItems);
                return true;
            }
        });

        mDialog.show();
    }

    private static class SearchableSingleAdapter extends BaseAdapter {
        private List<DialogItem> items;
        private LayoutInflater inflater;

        SearchableSingleAdapter(android.content.Context context, List<DialogItem> items) {
            this.items = items;
            this.inflater = LayoutInflater.from(context);
        }

        void updateList(List<DialogItem> newItems) {
            this.items = newItems;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public DialogItem getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
            }
            TextView textView = (TextView) convertView;
            DialogItem item = getItem(position);
            textView.setText(item.text);
            return convertView;
        }
    }
    private int getIndex(Spinner designationSpinner, String edit_dname) {
        for (int i = 0; i < designationSpinner.getCount(); i++) {
            if (designationSpinner.getItemAtPosition(i).toString().equalsIgnoreCase(edit_dname)) {
                return i;
            }
        }
        return 0;
    }
    @Override
    public void onSubmitSuccess(String mesg) {
        if (mesg.equals("Submit")) {
            new androidx.appcompat.app.AlertDialog.Builder(DoctorActivity.this)
                    .setTitle("Success")
                    .setMessage("Doctor Saved Successfully")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            Intent mIntent = getIntent();
                            finish();
                            startActivity(mIntent);

                        }

                    }).setCancelable(false).show();
        }
        if (mesg.equals("Update")) {
            new androidx.appcompat.app.AlertDialog.Builder(DoctorActivity.this)
                    .setTitle("Success")
                    .setMessage("Doctor Update Successfully")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            Intent mIntent = new Intent(DoctorActivity.this, DoctorApprovalListActivity.class);
                            startActivity(mIntent);
                            finish();
                        }
                    }).setCancelable(false).show();
        }
    }
    @Override
    public void onSubmitError(String mesg) {
        new androidx.appcompat.app.AlertDialog.Builder(DoctorActivity.this)
                .setTitle("Error")
                .setMessage(mesg)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }

                }).setCancelable(false).show();

    }
    @Override
    public void vGroup(List<Group> groupList) {
    }
    @Override
    public void vRegion(List<Region> regionList) {
        try {
            if (regionList != null) {
                ArrayAdapter<Region> dataAdapter = new ArrayAdapter<>(DoctorActivity.this, android.R.layout.simple_spinner_item, regionList);
                dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                viewBinding.regionSpinner.setAdapter(dataAdapter);
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
    @Override
    public void vArea(List<Area> areaList) {
        try {
            if (areaList != null) {
                ArrayAdapter<Area> dataAdapter = new ArrayAdapter<>(DoctorActivity.this, android.R.layout.simple_spinner_item, areaList);
                dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                viewBinding.areaSpinner.setAdapter(dataAdapter);
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
    @Override
    public void vTeritory(List<Teritorry> teritoryList) {
        try {
            if (teritoryList != null) {
                ArrayAdapter<Teritorry> dataAdapter = new ArrayAdapter<>(DoctorActivity.this, android.R.layout.simple_spinner_item, teritoryList);
                dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                viewBinding.territorySpinner.setAdapter(dataAdapter);
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
    @Override
    public void vSTeritory(List<SubTeritorry> steritoryList) {
        try {
            if (steritoryList != null) {
                ArrayAdapter<SubTeritorry> dataAdapter = new ArrayAdapter<>(DoctorActivity.this, android.R.layout.simple_spinner_item, steritoryList);
                dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                viewBinding.sterritorySpinner.setAdapter(dataAdapter);
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
    @Override
    public void vMarket(List<Market> marketList) {
        try {
            if (marketList != null) {
                ArrayAdapter<Market> dataAdapter = new ArrayAdapter<>(DoctorActivity.this, android.R.layout.simple_spinner_item, marketList);
                dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                viewBinding.marketSpinner.setAdapter(dataAdapter);
            }
           /* viewBinding.marketSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    Market market = (Market) parent.getSelectedItem();
                    selectedMarket = market.getMarketId();

                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });*/
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
    // Degree / Speciality / Brand / Special Day sit in one column; the user drags them
    // into the order they want and it is remembered on the device.
    private static final String PREF_DOCTOR_FORM = "doctorForm";
    private static final String KEY_FIELD_ORDER = "fieldOrder";

    private void setupFieldReorder() {
        try {
            final LinearLayout box = viewBinding.reorderSection;
            applySavedFieldOrder(box);
            View.OnDragListener dragListener = new View.OnDragListener() {
                @Override
                public boolean onDrag(View v, DragEvent event) {
                    View dragged = (View) event.getLocalState();
                    switch (event.getAction()) {
                        case DragEvent.ACTION_DRAG_ENTERED:
                            if (dragged != null && dragged != v) {
                                int from = box.indexOfChild(dragged);
                                int to = box.indexOfChild(v);
                                if (from >= 0 && to >= 0) {
                                    box.removeViewAt(from);
                                    box.addView(dragged, to);
                                }
                            }
                            break;
                        case DragEvent.ACTION_DRAG_ENDED:
                            if (dragged != null) dragged.setVisibility(View.VISIBLE);
                            // the dragged card is invisible while dragging, so the container saves
                            if (v == box) saveFieldOrder(box);
                            break;
                    }
                    return true;
                }
            };
            box.setOnDragListener(dragListener);
            for (int i = 0; i < box.getChildCount(); i++) {
                final View card = box.getChildAt(i);
                card.setOnDragListener(dragListener);
                View handle = card.findViewWithTag("dragHandle");
                if (handle == null) continue;
                handle.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        ViewCompat.startDragAndDrop(card, null, new View.DragShadowBuilder(card), card, 0);
                        card.setVisibility(View.INVISIBLE);
                        return true;
                    }
                });
            }
        } catch (Exception exception) {
            Log.e("DoctorActivity", "setupFieldReorder: ", exception);
        }
    }

    private void applySavedFieldOrder(LinearLayout box) {
        applyFieldOrder(box, getSharedPreferences(PREF_DOCTOR_FORM, MODE_PRIVATE)
                .getString(KEY_FIELD_ORDER, ""));
    }

    private void saveFieldOrder(LinearLayout box) {
        getSharedPreferences(PREF_DOCTOR_FORM, MODE_PRIVATE)
                .edit().putString(KEY_FIELD_ORDER, fieldOrderOf(box)).apply();
    }

    // cards named in the saved order move to the front in that order; anything not
    // named (a card added or removed in a newer build) keeps its place behind them
    static void applyFieldOrder(LinearLayout box, String saved) {
        if (saved.isEmpty()) return;
        int target = 0;
        for (String name : saved.split(",")) {
            View card = findCard(box, name);
            if (card == null) continue;
            box.removeView(card);
            box.addView(card, target++);
        }
    }

    static String fieldOrderOf(LinearLayout box) {
        StringBuilder order = new StringBuilder();
        for (int i = 0; i < box.getChildCount(); i++) {
            int id = box.getChildAt(i).getId();
            if (id == View.NO_ID) continue;
            if (order.length() > 0) order.append(',');
            order.append(box.getResources().getResourceEntryName(id));
        }
        return order.toString();
    }

    private static View findCard(LinearLayout box, String name) {
        for (int i = 0; i < box.getChildCount(); i++) {
            View card = box.getChildAt(i);
            int id = card.getId();
            if (id != View.NO_ID && name.equals(box.getResources().getResourceEntryName(id))) {
                return card;
            }
        }
        return null;
    }

    private void iniPopup() {
        popAdd = new Dialog(DoctorActivity.this);
        popAdd.setContentView(R.layout.pop_chambername);
        //popAddQty.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popAdd.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        popAdd.getWindow().setLayout(Toolbar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.WRAP_CONTENT);
        popAdd.getWindow().getAttributes().gravity = Gravity.CENTER;

        // ini popup widgets
        til_chamberDD = popAdd.findViewById(R.id.chamberDD);
        chamberName = popAdd.findViewById(R.id.chamberName);
        cmbrtype = popAdd.findViewById(R.id.cmbrtype);
        rgShift = popAdd.findViewById(R.id.rg_shift);
        rbMorning = popAdd.findViewById(R.id.rb_morning);
        rbEvening = popAdd.findViewById(R.id.rb_evening);
        til_sittingTime = popAdd.findViewById(R.id.til_sitting_time);
        sittingTime = popAdd.findViewById(R.id.sittingTime);
        til_chamberAddress = popAdd.findViewById(R.id.til_chamber_address);
        chamberAddress = popAdd.findViewById(R.id.chamberAddress);
        Button cmbrBnt = popAdd.findViewById(R.id.cmbrBnt);

        //itemList = new ArrayList<>();
        cmbrBnt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String chembarName = chamberName.getText().toString().trim();
                String sitTime = sittingTime != null ? sittingTime.getText().toString().trim() : "";
                String chAddress = chamberAddress != null ? chamberAddress.getText().toString().trim() : "";

                if (chembarName.isEmpty()) {
                    chamberName.setError("Enter Chamber Name");
                    chamberName.requestFocus();
                    return;
                }
                if (sitTime.isEmpty()) {
                    if (sittingTime != null) {
                        sittingTime.setError("Enter Sitting Time");
                        sittingTime.requestFocus();
                    }
                    return;
                }

                String shift = "Morning";
                if (rbEvening != null && rbEvening.isChecked()) {
                    shift = "Evening";
                }

                DoctorChamberTypeVM isitm = new DoctorChamberTypeVM(
                        selectedchembarTypeId,
                        selectedchembarTypeName,
                        chembarName,
                        shift,
                        sitTime,
                        selectedchembarTypeId,
                        selectedchembarTypeName,
                        chAddress
                );
                itemList.add(isitm);

                itemsAdapter = new _chamber_ListAdapter(DoctorActivity.this, itemList);
                viewBinding.rvChamberlist.setLayoutManager(new LinearLayoutManager(DoctorActivity.this));
                viewBinding.rvChamberlist.setHasFixedSize(true);
                viewBinding.rvChamberlist.setAdapter(itemsAdapter);
                itemsAdapter.notifyDataSetChanged();
                chamberName.setText("");
                if (sittingTime != null) sittingTime.setText("");
                if (chamberAddress != null) chamberAddress.setText("");
                if (rbMorning != null) rbMorning.setChecked(true);
                popAdd.dismiss();
            }
        });
    }
    private void iniPopupContact() {
        popContact = new Dialog(DoctorActivity.this);
        popContact.setContentView(R.layout.pop_chambername);
        //popAddQty.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popContact.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        popContact.getWindow().setLayout(Toolbar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.WRAP_CONTENT);
        popContact.getWindow().getAttributes().gravity = Gravity.CENTER;

        // Hide chamber specific fields in contact popup
        RadioGroup cRg = popContact.findViewById(R.id.rg_shift);
        if (cRg != null) cRg.setVisibility(View.GONE);
        TextInputLayout cTilTime = popContact.findViewById(R.id.til_sitting_time);
        if (cTilTime != null) cTilTime.setVisibility(View.GONE);
        TextInputLayout cTilAddress = popContact.findViewById(R.id.til_chamber_address);
        if (cTilAddress != null) cTilAddress.setVisibility(View.GONE);

        // ini popup widgets
        til_contactName = popContact.findViewById(R.id.chamberDD);
        til_contactName.setHint("Contact Name / Number");
        contactName = popContact.findViewById(R.id.chamberName);
        contacttype = popContact.findViewById(R.id.cmbrtype);
        Button contactBnt = popContact.findViewById(R.id.cmbrBnt);
        contactBnt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!contactName.getText().toString().isEmpty()) {
                    String contacName = contactName.getText().toString();
                    if ("Mobile".equals(selectedcontactTypeName)
                            && (contacName.length() != 11 || !contacName.startsWith("01"))) {
                        contactName.setError("Enter a valid 11 digit mobile number starting with 01");
                        return;
                    }
                    DoctorContact isitm = new DoctorContact(selectedcontactTypeId, contacName,selectedcontactTypeName);
                    doccontactLists.add(isitm);

                    contactAdapter = new _contact_ListAdapter(DoctorActivity.this, doccontactLists);
                    viewBinding.rvContactlist.setLayoutManager(new LinearLayoutManager(DoctorActivity.this));
                    viewBinding.rvContactlist.setHasFixedSize(true);
                    viewBinding.rvContactlist.setAdapter(contactAdapter);
                    contactAdapter.notifyDataSetChanged();
                    contactName.setText("");
                    popContact.dismiss();
                } else {
                    contactName.setError("Enter Contact Name");
                }
            }
        });
    }
    private void showGPSDisabledAlertToUser() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("GPS is disabled in your device. Would you like to enable it?")
                .setCancelable(false)
                .setPositiveButton("Goto Settings Page To Enable GPS",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent callGPSSettingIntent = new Intent(
                                        android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivity(callGPSSettingIntent);
                            }
                        });
        alertDialogBuilder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        startActivity(new Intent(getApplicationContext(), MainDashboardActivity.class));
                    }
                });
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }
    private void setupEdit(String roleType, int empId, DoctorApproveModel cal) {
        iniPopup();
        iniPopupContact();
        viewBinding.pupdateBnt.setVisibility(View.VISIBLE);
        switch (roleType) {
            case "MIO":
                viewBinding.areadiv.setVisibility(View.GONE);
                viewBinding.regiondiv.setVisibility(View.GONE);
                break;
            case "AM":
                viewBinding.areadiv.setVisibility(View.VISIBLE);
                viewBinding.regiondiv.setVisibility(View.GONE);
                break;
            case "DZSM":

                viewBinding.regiondiv.setVisibility(View.VISIBLE);
                viewBinding.areadiv.setVisibility(View.VISIBLE);
                break;

        }

        if (cal.getRegionName() != null) {
            edit_dName = cal.getRegionName();
            viewBinding.regionSpinner.setSelection(getIndex(viewBinding.regionSpinner, edit_dName));
        } else {
        }
        if (cal.getAreaName() != null) {
            edit_dName = cal.getAreaName();
            viewBinding.areaSpinner.setSelection(getIndex(viewBinding.areaSpinner, edit_dName));
        } else {
        }
        if (cal.getTerritoryName() != null) {
            edit_dName = cal.getTerritoryName();
            viewBinding.territorySpinner.setSelection(getIndex(viewBinding.territorySpinner, edit_dName));
        } else {
        }
        if (cal.getSubTerritoryName() != null) {
            edit_dName = cal.getSubTerritoryName();
            viewBinding.sterritorySpinner.setSelection(getIndex(viewBinding.sterritorySpinner, edit_dName));
        } else {
        }
        if (cal.getMarketName() != null) {
            edit_dName = cal.getMarketName();
            viewBinding.marketSpinner.setSelection(getIndex(viewBinding.marketSpinner, edit_dName));
        } else {
        }

        //name
        if (cal.getDoctorName() != null) {
            viewBinding.docName.setText(cal.getDoctorName());
        } else {
            viewBinding.docName.setText("---- ----");
        }
        if (cal.getDoctorAddress() != null) {
            viewBinding.docAddress.setText(cal.getDoctorAddress());
        } else {
            viewBinding.docAddress.setText("---- ----");
        }
        //DoctorType Spinner
        if (cal.getDesignationName() != null) {
            edit_dName = cal.getDesignationName();
            viewBinding.designationSpinner.setSelection(getIndex(viewBinding.designationSpinner, edit_dName));
        } else {
            //presenter.GetDoctorType(0);
        }
        //DoctorType Spinner
        if (cal.getDoctorTypeStr() != null) {
            edit_dName = cal.getDoctorTypeStr();
            viewBinding.doctorTypeSpinner.setSelection(getIndex(viewBinding.doctorTypeSpinner, edit_dName));
        } else {
            //presenter.GetDoctorType(0);
        }
        viewBinding.doctorTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                DoctorTypeVM doctype = (DoctorTypeVM) parent.getSelectedItem();
                selectedDoctypeId = doctype.getDoctorTypeId();
                mUserItems.clear();
                //viewBinding.degreeListStr.setText("");
                presenter.GetDegree(selectedDoctypeId);
                //viewBinding.degreeListStr.setText(item);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        //degree
        if (cal.getDegreeStr() != null) {
            viewBinding.degreeListStr.setText(cal.getDegreeStr());
        } else {
            viewBinding.degreeListStr.setText("---- ----");
        }
        //speciality
        if (cal.getSpecialityStr() != null) {
            viewBinding.specilityListStr.setText(cal.getSpecialityStr());
        } else {
            viewBinding.specilityListStr.setText("---- ----");
        }
        //Inistitute
        if (cal.getInstitutionSTr() != null) {
            String dbGender = cal.getInstitutionSTr();
            String matchedInsName = "";
            if (doc_institutionList != null) {
                for (InstitutionVM item : doc_institutionList) {
                    if (String.valueOf(item.getInstitutionId()).equals(dbGender)) {
                        matchedInsName = item.getInstitution();
                        break;
                    }
                }
            }
            if (!matchedInsName.isEmpty()) {
                viewBinding.instituteListStr.setText(matchedInsName);
            } else {
                if (dbGender.equals("1")) {
                    viewBinding.instituteListStr.setText("Male");
                } else if (dbGender.equals("2")) {
                    viewBinding.instituteListStr.setText("Female");
                } else if (dbGender.equals("3")) {
                    viewBinding.instituteListStr.setText("Others");
                } else {
                    viewBinding.instituteListStr.setText(dbGender);
                }
            }
        } else {
            viewBinding.instituteListStr.setText("----");
        }
        //Brand
        if (cal.getBrandStr() != null) {
            viewBinding.brandListStr.setText(cal.getBrandStr());
        } else {
            viewBinding.brandListStr.setText("----");
        }
        //Doc Category Spinner
        if (cal.getCategoryName() != null) {
            edit_dName = cal.getCategoryName();
            viewBinding.doctorCategorySpinner.setSelection(getIndex(viewBinding.doctorCategorySpinner, edit_dName));
        } else {
            // presenter.GetDoctorCategory(0);
        }

        //Chamber
        if (cal.getaChemberListDAO() != null) {
            List<ChembarTypeWithName> clist = cal.getaChemberListDAO();
            int c;
            for (c = 0; c < cal.getaChemberListDAO().size(); c++) {
                String chembarName = clist.get(c).getChamberName();
                String chembarTypeName = clist.get(c).getChamberTypeName();
                int cmbrtypeid = clist.get(c).getChamberTypeId();
                String shift = clist.get(c).getShift();
                String sitTime = clist.get(c).getSittingTime();
                String chAddress = clist.get(c).getAddress();
                DoctorChamberTypeVM isitm = new DoctorChamberTypeVM(cmbrtypeid, chembarTypeName, chembarName, shift, sitTime, chAddress);
                itemList.add(isitm);
            }
            itemsAdapter = new _chamber_ListAdapter(DoctorActivity.this, itemList);
            viewBinding.rvChamberlist.setLayoutManager(new LinearLayoutManager(DoctorActivity.this));
            viewBinding.rvChamberlist.setHasFixedSize(true);
            viewBinding.rvChamberlist.setAdapter(itemsAdapter);
            itemsAdapter.notifyDataSetChanged();


        } else {
        }
        //Doc Contact Type Spinner
        //TODO:Contact Type
        //Contact
        if (cal.getaDoctorContactDAO() != null) {
            List<DoctorContact> clist = cal.getaDoctorContactDAO();
           /* int c;
            for (c = 0; c < cal.getaDoctorContactDAO().size(); c++) {
                String contactName = clist.get(c).getContact();
                String cntctTypeName = clist.get(c).getContactType();
                int cntcttypeid = clist.get(c).getContactTypeId();
                DoctorChamberTypeVM isitm = new DoctorChamberTypeVM(cmbrtypeid, chembarTypeName, chembarName);
                itemList.add(isitm);
            }*/
            contactAdapter = new _contact_ListAdapter(DoctorActivity.this, clist);
            viewBinding.rvContactlist.setLayoutManager(new LinearLayoutManager(DoctorActivity.this));
            viewBinding.rvContactlist.setHasFixedSize(true);
            viewBinding.rvContactlist.setAdapter(contactAdapter);
            contactAdapter.notifyDataSetChanged();

        } else {
        }

      /*  if (cal.getContactType() != null) {
            edit_dName = cal.getContactType();
            viewBinding.contactTypeSpinner.setSelection(getIndex(viewBinding.contactTypeSpinner, edit_dName));
        } else {
            presenter.GetContactType(0);
        }*/
        //TODO:Contact text
        /*if (cal.getContact() != null) {
            viewBinding.contactTxt.setText(cal.getContact());
        } else {
            viewBinding.contactTxt.setText("----");
        }*/
        //ProgramType Spinner
        if (cal.getProgramTypeName() != null) {
            edit_dName = cal.getProgramTypeName();
            viewBinding.programTypeSpinner.setSelection(getIndex(viewBinding.programTypeSpinner, edit_dName));

        } else {
            // presenter.GetProgramType(0);
        }

        //ProgramType Spinner
        if (cal.getSMCTypeName() != null) {
            edit_dName = cal.getSMCTypeName();
            viewBinding.smcTypeSpinner.setSelection(getIndex(viewBinding.smcTypeSpinner, edit_dName));

        } else {
            // presenter.GetProgramType(0);
        }
        //Special Day Spinner
      /*  if (cal.getSpecialDay() != null) {
            edit_dName = cal.getSpecialDay();
            viewBinding.spcialdayTypeSpinner.setSelection(getIndex(viewBinding.spcialdayTypeSpinner, edit_dName));
        } else {
            // presenter.GetSpecialType(0);
        }*/
        //TODO:Special Day
        //Special date
        if (cal.getaDoctorSpecialDAO() != null) {
            List<SpecialDay> clist = cal.getaDoctorSpecialDAO();
            spdayAdapter = new _specialday_ListAdapter(DoctorActivity.this, clist);
            viewBinding.rvSpecialdaylist.setLayoutManager(new LinearLayoutManager(DoctorActivity.this));
            viewBinding.rvSpecialdaylist.setHasFixedSize(true);
            viewBinding.rvSpecialdaylist.setAdapter(spdayAdapter);
            spdayAdapter.notifyDataSetChanged();

        } else {
        }

        if (cal.getSpeciaDateStr() != null) {
            viewBinding.scecialdateTxt.setText(cal.getSpeciaDateStr());
        } else {
            viewBinding.scecialdateTxt.setText("----");
        }

        viewBinding.pupdateBnt.setOnClickListener(v -> {
            try {
                String docName = viewBinding.docName.getText().toString().trim();
                String docDegree = viewBinding.degreeListStr.getText().toString();
                String docSpeciality = viewBinding.specilityListStr.getText().toString();
                String institute = viewBinding.instituteListStr.getText().toString();
                String instituteIdStr = "";
                if (doc_institutionList != null) {
                    for (InstitutionVM item : doc_institutionList) {
                        if (item.getInstitution().equalsIgnoreCase(institute)) {
                            instituteIdStr = String.valueOf(item.getInstitutionId());
                            break;
                        }
                    }
                }
                if (instituteIdStr.isEmpty()) {
                    if (institute.equalsIgnoreCase("Male")) {
                        instituteIdStr = "1";
                    } else if (institute.equalsIgnoreCase("Female")) {
                        instituteIdStr = "2";
                    } else if (institute.equalsIgnoreCase("Others")) {
                        instituteIdStr = "3";
                    } else if (institute.equals("----")) {
                        instituteIdStr = "";
                    } else {
                        instituteIdStr = institute;
                    }
                }
                String rawInstitute = viewBinding.instituteListStr.getText().toString().trim();
                if (rawInstitute.isEmpty() || rawInstitute.equals("----") || instituteIdStr.isEmpty()) {
                    SnackBarManagement._warning_CustomMessage(viewBinding.masterLayoutId, "Gender is mandatory! Please select Gender.");
                    return;
                }
                institute = instituteIdStr;
                String docBrand = viewBinding.brandListStr.getText().toString();
                String docAddress = viewBinding.docAddress.getText().toString().trim();

                if (docName.isEmpty()) {
                    viewBinding.docName.setError("Doctor Name is required");
                    viewBinding.docName.requestFocus();
                    SnackBarManagement._warning_CustomMessage(viewBinding.masterLayoutId, "Doctor Name is required!");
                    return;
                }

                if (docAddress.isEmpty()) {
                    viewBinding.docAddress.setError("Doctor Address is required");
                    viewBinding.docAddress.requestFocus();
                    SnackBarManagement._warning_CustomMessage(viewBinding.masterLayoutId, "Doctor Address is mandatory!");
                    return;
                }

                DoctorTypeVM doctorType = (DoctorTypeVM) viewBinding.doctorTypeSpinner.getSelectedItem();
                int dtypeid = doctorType.getDoctorTypeId();

                //TODO:Contact
                /*ContactTypeVM contactType = (ContactTypeVM) viewBinding.contactTypeSpinner.getSelectedItem();
                int contactId = contactType.getContactTypeId();
                String contactDD = viewBinding.contactTxt.getText().toString();*/

                DoctorCategory catType = (DoctorCategory) viewBinding.doctorCategorySpinner.getSelectedItem();
                int catTypeId = catType.getCategoryId();

               // ProgramType progType = (ProgramType) viewBinding.programTypeSpinner.getSelectedItem();
                ModelProviderType progType = (ModelProviderType) viewBinding.programTypeSpinner.getSelectedItem();
                int programTypeId = progType.getProviderTypeId();

                ModelSMCType smcType = (ModelSMCType) viewBinding.smcTypeSpinner.getSelectedItem();
                int smcTypeId = smcType.getSMCTypeId();
             /*   SpecialDay spday = (SpecialDay) viewBinding.spcialdayTypeSpinner.getSelectedItem();
                int spdayId = spday.getSpecialDayId();*/


                DoctorDesignation docDesi = (DoctorDesignation) viewBinding.designationSpinner.getSelectedItem();
                int designationId = docDesi.getDesignationId();

                chembarLists = new ArrayList<>();
                for (int i = 0; i < itemList.size(); i++) {
                    String chamberName = itemList.get(i).getChamberName();
                    int chamberTypeId = itemList.get(i).getChamberTypeId();
                    String shift = itemList.get(i).getShift();
                    String sitTime = itemList.get(i).getSittingTime();
                    int instId = itemList.get(i).getInstitutionId();
                    if (instId == 0) {
                        instId = chamberTypeId;
                    }
                    String instName = itemList.get(i).getInstitutionName();
                    if (instName == null || instName.isEmpty()) {
                        instName = itemList.get(i).getChamberTypeName();
                    }
                    ChembarList cl = new ChembarList();
                    cl.setChamberTypeId(chamberTypeId);
                    cl.setName(chamberName);
                    cl.setShift(shift);
                    cl.setSittingTime(sitTime);
                    cl.setInstitutionId(instId);
                    cl.setInstitutionName(instName);
                    cl.setAddress(itemList.get(i).getAddress());
                    chembarLists.add(cl);
                }

                DoctorSM doctorSM = new DoctorSM();
                doctorSM.setDoctorId(cal.getDoctorId());
                doctorSM.setDoctorName(docName);
                doctorSM.setDoctorAddress(docAddress);
                doctorSM.setDesignationId(designationId);
                doctorSM.setDegreeStr(docDegree);

                doctorSM.setSpecialityStr(docSpeciality);
                doctorSM.setInstitutionSTr(institute);
                doctorSM.setBrandStr(docBrand);

                doctorSM.setDoctorTypeId(dtypeid);
                doctorSM.setDoctorTypeStr(String.valueOf(dtypeid));
                doctorSM.setDoctorCategoryId(catTypeId);
                doctorSM.setaChemberListDAO(chembarLists);
                doctorSM.setaDoctorContactDAO(doccontactLists);
                doctorSM.setaDoctorSpecialDAO(specialdayLists);
               // doctorSM.setContactTypeId(contactId);
                //doctorSM.setContact(contactDD);
                doctorSM.setProgramTypeId(programTypeId);
                doctorSM.setSMCTypeId(smcTypeId);
                //doctorSM.setSpecialDayId(spdayId);
                doctorSM.setSpeciaDateStr(viewBinding.scecialdateTxt.getText().toString());

                Area area = (Area) viewBinding.areaSpinner.getSelectedItem();
                int aeraId = area.getAreaId();
                Teritorry teritorry = (Teritorry) viewBinding.territorySpinner.getSelectedItem();
                int teritoryId = teritorry.getTerritoryId();
                SubTeritorry steri = (SubTeritorry) viewBinding.sterritorySpinner.getSelectedItem();
                int steriId = steri.getSubTerritoryId();
                Market market = (Market) viewBinding.marketSpinner.getSelectedItem();
                int marketId = market.getMarketId();

                doctorSM.setAreaId(aeraId);
                doctorSM.setTerritoryId(teritoryId);
                doctorSM.setSubTerritoryId(steriId);
                doctorSM.setMarketId(marketId);
                doctorSM.setSessionUser(empId);
                doctorSM.setEmpId(empId);
                doctorSM.setLatitude(latitude);
                doctorSM.setLongitude(longitude);
                // doctorSM.setRemarks(viewBinding.remarksTxt.getText().toString());

                Gson gs = new Gson();
                String ss = gs.toJson(doctorSM);
                System.out.println("post : " + ss);
                // Toast.makeText(this, "data "+ss, Toast.LENGTH_SHORT).show();
                presenter.SaveDoctor(doctorSM, "Update");
            } catch (Exception ex) {
                Log.e("doctorSubmit-Ex", "SubmitClick: ", ex);
            }
        });
    }

}
