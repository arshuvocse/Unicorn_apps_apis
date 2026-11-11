package com.creatrix.unicorn.Activity.Doctor.TourePlan.Adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.text.HtmlCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.creatrix.unicorn.Model.Customer;
import com.creatrix.unicorn.Model.Doctor.DoctorChamberName;
import com.creatrix.unicorn.Model.Doctor.DoctorListViewModel;
import com.creatrix.unicorn.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DayWiseTourPlanAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int MODE_CUSTOMER = 1;
    public static final int MODE_DOCTOR = 2;

    public interface OnItemSelectionChangeListener {
        void onSelectionChanged(int selectedCount);
    }

    private Context context;
    private int currentMode = MODE_CUSTOMER;
    private List<Customer> customerList = new ArrayList<>();
    private List<DoctorListViewModel> doctorList = new ArrayList<>();
    private Set<Integer> activeSelectedIds;
    private OnItemSelectionChangeListener selectionListener;
    private boolean readOnly = false;
    private Map<Integer, List<DoctorChamberName>> chambersByDoctor = new HashMap<>();
    private Map<Integer, Integer> activeChamberIds; // doctorId -> chamberId; absent = doctor-wise

    public DayWiseTourPlanAdapter(Context context, OnItemSelectionChangeListener selectionListener) {
        this.context = context;
        this.selectionListener = selectionListener;
    }

    public void setCustomerData(List<Customer> customers, Set<Integer> selectedCustomerIds) {
        this.currentMode = MODE_CUSTOMER;
        this.customerList = (customers != null) ? customers : new ArrayList<>();
        this.doctorList.clear();
        this.activeSelectedIds = selectedCustomerIds;
        notifyDataSetChanged();
    }

    public void setDoctorData(List<DoctorListViewModel> doctors, Set<Integer> selectedDoctorIds, Map<Integer, Integer> selectedChamberIds) {
        this.currentMode = MODE_DOCTOR;
        this.doctorList = (doctors != null) ? doctors : new ArrayList<>();
        this.customerList.clear();
        this.activeSelectedIds = selectedDoctorIds;
        this.activeChamberIds = selectedChamberIds;
        notifyDataSetChanged();
    }

    public void setChambersByDoctor(Map<Integer, List<DoctorChamberName>> chambersByDoctor) {
        this.chambersByDoctor = (chambersByDoctor != null) ? chambersByDoctor : new HashMap<>();
    }

    /** Names come HTML-encoded from the server (e.g. "&amp;"). */
    private static String decodeHtml(String text) {
        return text == null ? null : HtmlCompat.fromHtml(text, HtmlCompat.FROM_HTML_MODE_LEGACY).toString().trim();
    }

    /** Hides a detail line that has no value, so cards stay short. */
    private static void setLine(TextView tv, String label, String value) {
        boolean empty = TextUtils.isEmpty(value) || value.replace(";", "").replace("N/A", "").trim().isEmpty();
        tv.setVisibility(empty ? View.GONE : View.VISIBLE);
        if (!empty) tv.setText(label + value);
    }

    /** Read-only (past dates): items stay visible but the selection cannot be changed. Applied on the next set*Data. */
    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    public void updateActiveSelectedIds(Set<Integer> selectedIds) {
        this.activeSelectedIds = selectedIds;
        notifyDataSetChanged();
    }

    public int getCurrentMode() {
        return currentMode;
    }

    @Override
    public int getItemViewType(int position) {
        return currentMode;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MODE_CUSTOMER) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_tour_plan_customer, parent, false);
            return new CustomerViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_tour_plan_doctor, parent, false);
            return new DoctorViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == MODE_CUSTOMER) {
            CustomerViewHolder vh = (CustomerViewHolder) holder;
            Customer customer = customerList.get(position);
            int customerId = customer.getCustomerMasterId();

            String customerName = customer.getCustomerName() != null ? customer.getCustomerName() : "";
            vh.tvCustomerName.setText(customerName);
            setLine(vh.tvCustomerCode, "Code: ", customerName.contains(String.valueOf(customer.getCustomerCode())) ? null : customer.getCustomerCode());
            vh.tvCustomerType.setText(customer.getCustomerType() != null && !customer.getCustomerType().isEmpty() ? customer.getCustomerType() : "Customer");
            setLine(vh.tvCustomerMarket, "Market: ", customer.getMarketName());
            setLine(vh.tvCustomerAddress, "Address: ", customer.getAddress());

            // Avoid recycled listener triggers
            vh.cbSelectCustomer.setOnCheckedChangeListener(null);
            boolean isChecked = activeSelectedIds != null && activeSelectedIds.contains(customerId);
            vh.cbSelectCustomer.setChecked(isChecked);

            View.OnClickListener clickListener = v -> {
                if (readOnly) {
                    vh.cbSelectCustomer.setChecked(activeSelectedIds != null && activeSelectedIds.contains(customerId));
                    return;
                }
                if (activeSelectedIds != null) {
                    if (activeSelectedIds.contains(customerId)) {
                        activeSelectedIds.remove(customerId);
                        vh.cbSelectCustomer.setChecked(false);
                    } else {
                        activeSelectedIds.add(customerId);
                        vh.cbSelectCustomer.setChecked(true);
                    }
                    if (selectionListener != null) {
                        selectionListener.onSelectionChanged(activeSelectedIds.size());
                    }
                }
            };

            vh.itemView.setOnClickListener(clickListener);
            vh.cbSelectCustomer.setOnClickListener(clickListener);

        } else {
            DoctorViewHolder vh = (DoctorViewHolder) holder;
            DoctorListViewModel doctor = doctorList.get(position);
            int doctorId = doctor.getDoctorId();

            String doctorName = doctor.getDoctorName() != null ? doctor.getDoctorName() : "";
            vh.tvDoctorName.setText(doctorName);
            setLine(vh.tvDoctorCode, "Code: ", doctorName.contains(String.valueOf(doctor.getDoctorCode())) ? null : doctor.getDoctorCode());

            String typeName = doctor.getDoctorTypeName();
            if (TextUtils.isEmpty(typeName)) {
                typeName = "Doctor";
            }
            vh.tvDoctorType.setText(typeName);

            setLine(vh.tvDoctorSpeciality, "Speciality/Program: ", doctor.getProgramTypeName());
            setLine(vh.tvDoctorMarket, "Market: ", doctor.getMarketName());

            String chamberStr = doctor.getChemberName();
            if (TextUtils.isEmpty(chamberStr) || "N/A".equals(chamberStr)) {
                chamberStr = doctor.getStreetAddress();
            }
            setLine(vh.tvDoctorChamber, "Chamber/Address: ", chamberStr == null ? null : decodeHtml(chamberStr));
            bindChamberSpinner(vh, doctorId);

            // Avoid recycled listener triggers
            vh.cbSelectDoctor.setOnCheckedChangeListener(null);
            boolean isChecked = activeSelectedIds != null && activeSelectedIds.contains(doctorId);
            vh.cbSelectDoctor.setChecked(isChecked);

            View.OnClickListener clickListener = v -> {
                if (readOnly) {
                    vh.cbSelectDoctor.setChecked(activeSelectedIds != null && activeSelectedIds.contains(doctorId));
                    return;
                }
                if (activeSelectedIds != null) {
                    if (activeSelectedIds.contains(doctorId)) {
                        activeSelectedIds.remove(doctorId);
                        if (activeChamberIds != null) activeChamberIds.remove(doctorId);
                        if (vh.spChamber.getVisibility() == View.VISIBLE) vh.spChamber.setSelection(0, false);
                        vh.cbSelectDoctor.setChecked(false);
                    } else {
                        activeSelectedIds.add(doctorId);
                        vh.cbSelectDoctor.setChecked(true);
                    }
                    if (selectionListener != null) {
                        selectionListener.onSelectionChanged(activeSelectedIds.size());
                    }
                }
            };

            vh.itemView.setOnClickListener(clickListener);
            vh.cbSelectDoctor.setOnClickListener(clickListener);
        }
    }

    /** Chamber-wise planning: position 0 = any chamber (doctor-wise); picking a chamber also selects the doctor. */
    private void bindChamberSpinner(DoctorViewHolder vh, int doctorId) {
        vh.spChamber.setOnItemSelectedListener(null);
        List<DoctorChamberName> chambers = chambersByDoctor.get(doctorId);
        if (chambers == null || chambers.isEmpty()) {
            vh.spChamber.setVisibility(View.GONE);
            return;
        }
        List<String> names = new ArrayList<>();
        names.add("Any chamber (doctor-wise)");
        int selectedPos = 0;
        Integer plannedChamberId = activeChamberIds != null ? activeChamberIds.get(doctorId) : null;
        for (int i = 0; i < chambers.size(); i++) {
            DoctorChamberName c = chambers.get(i);
            names.add("Chamber: " + decodeHtml(c.getChemberName()));
            if (plannedChamberId != null && plannedChamberId == c.getChemberId()) {
                selectedPos = i + 1;
            }
        }
        ArrayAdapter<String> chamberAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, names);
        chamberAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        vh.spChamber.setAdapter(chamberAdapter);
        vh.spChamber.setSelection(selectedPos, false);
        vh.spChamber.setEnabled(!readOnly);
        vh.spChamber.setVisibility(View.VISIBLE);
        vh.spChamber.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (readOnly || activeChamberIds == null || activeSelectedIds == null) return;
                if (position == 0) {
                    activeChamberIds.remove(doctorId);
                    return;
                }
                activeChamberIds.put(doctorId, chambers.get(position - 1).getChemberId());
                if (!activeSelectedIds.contains(doctorId)) {
                    activeSelectedIds.add(doctorId);
                    vh.cbSelectDoctor.setChecked(true);
                    if (selectionListener != null) {
                        selectionListener.onSelectionChanged(activeSelectedIds.size());
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    @Override
    public int getItemCount() {
        if (currentMode == MODE_CUSTOMER) {
            return customerList != null ? customerList.size() : 0;
        } else {
            return doctorList != null ? doctorList.size() : 0;
        }
    }

    static class CustomerViewHolder extends RecyclerView.ViewHolder {
        CheckBox cbSelectCustomer;
        TextView tvCustomerName, tvCustomerCode, tvCustomerType, tvCustomerMarket, tvCustomerAddress;

        public CustomerViewHolder(@NonNull View itemView) {
            super(itemView);
            cbSelectCustomer = itemView.findViewById(R.id.cbSelectCustomer);
            tvCustomerName = itemView.findViewById(R.id.tvCustomerName);
            tvCustomerCode = itemView.findViewById(R.id.tvCustomerCode);
            tvCustomerType = itemView.findViewById(R.id.tvCustomerType);
            tvCustomerMarket = itemView.findViewById(R.id.tvCustomerMarket);
            tvCustomerAddress = itemView.findViewById(R.id.tvCustomerAddress);
        }
    }

    static class DoctorViewHolder extends RecyclerView.ViewHolder {
        CheckBox cbSelectDoctor;
        TextView tvDoctorName, tvDoctorCode, tvDoctorType, tvDoctorSpeciality, tvDoctorMarket, tvDoctorChamber;
        Spinner spChamber;

        public DoctorViewHolder(@NonNull View itemView) {
            super(itemView);
            cbSelectDoctor = itemView.findViewById(R.id.cbSelectDoctor);
            tvDoctorName = itemView.findViewById(R.id.tvDoctorName);
            tvDoctorCode = itemView.findViewById(R.id.tvDoctorCode);
            tvDoctorType = itemView.findViewById(R.id.tvDoctorType);
            tvDoctorSpeciality = itemView.findViewById(R.id.tvDoctorSpeciality);
            tvDoctorMarket = itemView.findViewById(R.id.tvDoctorMarket);
            tvDoctorChamber = itemView.findViewById(R.id.tvDoctorChamber);
            spChamber = itemView.findViewById(R.id.spChamber);
        }
    }
}
