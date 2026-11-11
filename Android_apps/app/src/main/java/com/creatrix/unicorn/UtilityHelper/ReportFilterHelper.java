package com.creatrix.unicorn.UtilityHelper;

import android.text.TextUtils;
import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.creatrix.unicorn.Activity.Team.Model.Team;
import com.creatrix.unicorn.DBAdapter.DBCrudHelper;
import com.creatrix.unicorn.DBAdapter.DBDoctor.DBDoctorHelper;
import com.creatrix.unicorn.Model.Doctor.DoctorListViewModel;
import com.creatrix.unicorn.Model.Customer;
import com.creatrix.unicorn.Model.MarketStructure.EmpInfoListModels;
import com.creatrix.unicorn.Network.RetrofitClientInstance;
import com.creatrix.unicorn.Network.UserProcessAPI;
import com.toptoche.searchablespinnerlibrary.SearchableSpinner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Sales Person + Customer dropdowns shared by the Attendance, Target & Achievement and Sales/Order Summary filters. */
public class ReportFilterHelper {

    /** Self first; managers also get "All" (id 0) and their team from the GetEmpTeamList API. MIO: self only, read-only. */
    public static void loadSalesPersons(Context ctx, SearchableSpinner spinner) {
        HashMap<String, String> user = new SessionManagement(ctx).getUserDetails();
        int empId = Integer.parseInt(user.get(SessionManagement.KEY_EmpId));
        String roleType = String.valueOf(user.get(SessionManagement.KEY_EmpRoleType));
        List<EmpInfoListModels> empList = new ArrayList<>();
        EmpInfoListModels self = new EmpInfoListModels();
        self.setMIOEmpId(empId);
        self.setEmpName(user.get(SessionManagement.KEY_UserName) + " (Self)");
        empList.add(self);
        spinner.setTitle("Select Sales Person");
        spinner.setPositiveButton("CLOSE");
        setList(ctx, spinner, empList);
        spinner.setEnabled(false);
        if (roleType.equals("MIO")) return;
        Map<String, String> filter = new HashMap<>();
        filter.put("role", roleType);
        filter.put("aparam", " ");
        RetrofitClientInstance.getRetrofitInstance().create(UserProcessAPI.class).GetTeamList(empId, filter).enqueue(new Callback<List<Team>>() {
            @Override
            public void onResponse(Call<List<Team>> call, Response<List<Team>> response) {
                if (response.body() == null || response.body().isEmpty()) return;
                EmpInfoListModels all = new EmpInfoListModels();
                all.setMIOEmpId(0); // 0 = whole hierarchy (the report SPs already match any hierarchy column)
                all.setEmpName("All");
                empList.add(0, all);
                for (Team t : response.body()) {
                    EmpInfoListModels e = new EmpInfoListModels();
                    e.setMIOEmpId(t.getEmpInfoId());
                    e.setEmpName(t.getEmpName() + (TextUtils.isEmpty(t.getDesignation()) ? "" : " (" + t.getDesignation() + ")")); // EmpRole from the team API repeats the name
                    empList.add(e);
                }
                setList(ctx, spinner, empList);
                spinner.setSelection(1); // Self
                spinner.setEnabled(true);
            }

            @Override
            public void onFailure(Call<List<Team>> call, Throwable t) {
                Toast.makeText(ctx, "Team list could not be loaded", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /** "All" (id 0) + every customer from the synced SQLite table. */
    public static void loadCustomers(Context ctx, SearchableSpinner spinner) {
        List<Customer> list = new ArrayList<>();
        Customer all = new Customer();
        all.setCustomerMasterId(0);
        all.setCustomerName("All");
        list.add(all);
        for (Customer c : new DBCrudHelper(ctx).getCustomerList_SQLite()) {
            c.setCustomerName(withCode(c.getCustomerCode(), c.getCustomerName()));
            list.add(c);
        }
        spinner.setTitle("Select Customer");
        spinner.setPositiveButton("CLOSE");
        setList(ctx, spinner, list);
    }

    /** "All" (id 0) + every doctor from the synced SQLite table. */
    public static void loadDoctors(Context ctx, SearchableSpinner spinner) {
        List<DoctorListViewModel> list = new ArrayList<>();
        DoctorListViewModel all = new DoctorListViewModel();
        all.setDoctorId(0);
        all.setDoctorName("All");
        list.add(all);
        for (DoctorListViewModel d : new DBDoctorHelper(ctx).getDoctorListFromSQLite()) {
            d.setDoctorName(withCode(d.getDoctorCode(), d.getDoctorName()));
            list.add(d);
        }
        spinner.setTitle("Select Doctor");
        spinner.setPositiveButton("CLOSE");
        setList(ctx, spinner, list);
    }

    /** Selected DoctorId, or 0 for All / nothing selected. */
    public static int selectedDoctorId(SearchableSpinner spinner) {
        Object sel = spinner.getSelectedItem();
        return sel == null ? 0 : ((DoctorListViewModel) sel).getDoctorId();
    }

    /** Selected person's EmpId; "All" and nothing selected both fall back to the logged-in EmpId. */
    public static String selectedEmpId(SearchableSpinner spinner, String loginEmpId) {
        int id = selectedEmpIdOrZero(spinner);
        return id == 0 ? loginEmpId : String.valueOf(id);
    }

    /** Selected person's EmpId, or 0 for All / nothing selected (for APIs whose SP treats 0 as the whole hierarchy). */
    public static int selectedEmpIdOrZero(SearchableSpinner spinner) {
        Object sel = spinner.getSelectedItem();
        return sel == null ? 0 : ((EmpInfoListModels) sel).getMIOEmpId();
    }

    /** Selected CustomerMasterId, or "" for All / nothing selected. */
    public static String selectedCustomerId(SearchableSpinner spinner) {
        Object sel = spinner.getSelectedItem();
        if (sel == null || ((Customer) sel).getCustomerMasterId() == 0) return "";
        return String.valueOf(((Customer) sel).getCustomerMasterId());
    }

    /** "CODE : Name" so every customer/doctor picker shows and finds by code too; names that already carry the code stay as they are. */
    public static String withCode(String code, String name) {
        if (code == null || code.isEmpty() || (name != null && name.contains(code))) return name == null ? "" : name;
        if (name == null || name.isEmpty()) return code;
        return code + " : " + name;
    }

    /** Case-insensitive "contains" search on the "CODE : Name" text, the same rule the searchable dropdowns use. */
    public static boolean matches(String code, String name, String query) {
        return withCode(code, name).toLowerCase().contains(query == null ? "" : query.trim().toLowerCase());
    }

    private static <T> void setList(Context ctx, SearchableSpinner spinner, List<T> list) {
        ArrayAdapter<T> adapter = new ArrayAdapter<>(ctx, android.R.layout.simple_spinner_item, list);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }
}
