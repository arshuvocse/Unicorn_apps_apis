package com.creatrix.unicorn.Activity.Doctor.TourePlan.Adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.creatrix.unicorn.Model.Customer;
import com.creatrix.unicorn.Model.Doctor.DoctorListViewModel;
import com.creatrix.unicorn.R;

import java.util.ArrayList;
import java.util.List;
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

    public void setDoctorData(List<DoctorListViewModel> doctors, Set<Integer> selectedDoctorIds) {
        this.currentMode = MODE_DOCTOR;
        this.doctorList = (doctors != null) ? doctors : new ArrayList<>();
        this.customerList.clear();
        this.activeSelectedIds = selectedDoctorIds;
        notifyDataSetChanged();
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

            vh.tvCustomerName.setText(customer.getCustomerName() != null ? customer.getCustomerName() : "");
            vh.tvCustomerCode.setText("Code: " + (customer.getCustomerCode() != null ? customer.getCustomerCode() : "N/A"));
            vh.tvCustomerType.setText(customer.getCustomerType() != null && !customer.getCustomerType().isEmpty() ? customer.getCustomerType() : "Customer");
            
            String marketStr = customer.getMarketName();
            if (TextUtils.isEmpty(marketStr)) {
                marketStr = "N/A";
            }
            vh.tvCustomerMarket.setText("Market: " + marketStr);

            String addressStr = customer.getAddress();
            if (TextUtils.isEmpty(addressStr)) {
                addressStr = "N/A";
            }
            vh.tvCustomerAddress.setText("Address: " + addressStr);

            // Avoid recycled listener triggers
            vh.cbSelectCustomer.setOnCheckedChangeListener(null);
            boolean isChecked = activeSelectedIds != null && activeSelectedIds.contains(customerId);
            vh.cbSelectCustomer.setChecked(isChecked);

            View.OnClickListener clickListener = v -> {
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

            vh.tvDoctorName.setText(doctor.getDoctorName() != null ? doctor.getDoctorName() : "");
            vh.tvDoctorCode.setText("Code: " + (doctor.getDoctorCode() != null ? doctor.getDoctorCode() : "N/A"));
            
            String typeName = doctor.getDoctorTypeName();
            if (TextUtils.isEmpty(typeName)) {
                typeName = "Doctor";
            }
            vh.tvDoctorType.setText(typeName);

            String progType = doctor.getProgramTypeName();
            if (TextUtils.isEmpty(progType)) {
                progType = "N/A";
            }
            vh.tvDoctorSpeciality.setText("Speciality/Program: " + progType);

            String marketStr = doctor.getMarketName();
            if (TextUtils.isEmpty(marketStr)) {
                marketStr = "N/A";
            }
            vh.tvDoctorMarket.setText("Market: " + marketStr);

            String chamberStr = doctor.getChemberName();
            if (TextUtils.isEmpty(chamberStr)) {
                chamberStr = doctor.getStreetAddress();
            }
            if (TextUtils.isEmpty(chamberStr)) {
                chamberStr = "N/A";
            }
            vh.tvDoctorChamber.setText("Chamber/Address: " + chamberStr);

            // Avoid recycled listener triggers
            vh.cbSelectDoctor.setOnCheckedChangeListener(null);
            boolean isChecked = activeSelectedIds != null && activeSelectedIds.contains(doctorId);
            vh.cbSelectDoctor.setChecked(isChecked);

            View.OnClickListener clickListener = v -> {
                if (activeSelectedIds != null) {
                    if (activeSelectedIds.contains(doctorId)) {
                        activeSelectedIds.remove(doctorId);
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

        public DoctorViewHolder(@NonNull View itemView) {
            super(itemView);
            cbSelectDoctor = itemView.findViewById(R.id.cbSelectDoctor);
            tvDoctorName = itemView.findViewById(R.id.tvDoctorName);
            tvDoctorCode = itemView.findViewById(R.id.tvDoctorCode);
            tvDoctorType = itemView.findViewById(R.id.tvDoctorType);
            tvDoctorSpeciality = itemView.findViewById(R.id.tvDoctorSpeciality);
            tvDoctorMarket = itemView.findViewById(R.id.tvDoctorMarket);
            tvDoctorChamber = itemView.findViewById(R.id.tvDoctorChamber);
        }
    }
}
