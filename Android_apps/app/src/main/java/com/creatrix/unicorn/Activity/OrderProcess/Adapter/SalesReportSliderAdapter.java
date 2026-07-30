package com.creatrix.unicorn.Activity.OrderProcess.Adapter;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import com.creatrix.unicorn.Activity.SelfReports.SalesReport.CustomerWiseSalesReport;
import com.creatrix.unicorn.Activity.SelfReports.SalesReport.ProductWiseSalesReport;
import com.creatrix.unicorn.Services.Constants;

public class SalesReportSliderAdapter extends FragmentStateAdapter {
    Context context;
    String from;

    public SalesReportSliderAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle, Context context,String from) {
        super(fragmentManager, lifecycle);
        this.context = context;
        this.from = from;
    }


    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 1) {
            Constants.From=from;
            return new ProductWiseSalesReport();
        }
        Constants.From=from;
        return new CustomerWiseSalesReport();
    }

    @Override
    public int getItemCount() {
        return 2;
    }

}
