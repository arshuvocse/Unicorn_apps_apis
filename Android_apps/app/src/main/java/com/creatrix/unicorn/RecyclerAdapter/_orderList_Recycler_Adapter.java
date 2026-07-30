package com.creatrix.unicorn.RecyclerAdapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.creatrix.unicorn.Activity.OrderStartActivity;
import com.creatrix.unicorn.Model.Customer;
import com.creatrix.unicorn.Model.OrderMaster;
import com.creatrix.unicorn.Model.Product;
import com.creatrix.unicorn.R;
import com.google.gson.Gson;

import java.util.List;

public class _orderList_Recycler_Adapter extends RecyclerView.Adapter<_orderList_Recycler_Adapter.BookViewHolder> {
    private Context context;
    private final static int FADE_DURATION = 500; //FADE_DURATION in milliseconds
    private List<Product> aOrderlist;

    public _orderList_Recycler_Adapter(List<Product> aOrderlist) {
        this.aOrderlist = aOrderlist;
    }

    public class BookViewHolder extends RecyclerView.ViewHolder {
        public TextView name;
        public TextView totalQuantity;
        public TextView totalAmt;
        public LinearLayout clickID;

        public BookViewHolder(View view) {
            super(view);
            name = (TextView) view.findViewById(R.id.option1);
            totalQuantity = (TextView) view.findViewById(R.id.totalQuantity);
            totalAmt = (TextView) view.findViewById(R.id.totalAmt);
        }
    }

    @Override
    public _orderList_Recycler_Adapter.BookViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout._recyclerview_order_details, parent, false);
        context = parent.getContext();
        return new _orderList_Recycler_Adapter.BookViewHolder(itemView);
    }
    @Override
    public void onBindViewHolder(_orderList_Recycler_Adapter.BookViewHolder holder, int position) {
        holder.name.setText(aOrderlist.get(position).getProductName());
        holder.totalQuantity.setText(Integer.toString(aOrderlist.get(position).getQuantity()));

        int Qty = aOrderlist.get(position).getQuantity();
        Double price = aOrderlist.get(position).getUnitPrice();
        double tp = Qty * price;

        holder.totalAmt.setText(Double.toString(tp));
    }

    @Override
    public int getItemCount() {
        return aOrderlist.size();
    }







}