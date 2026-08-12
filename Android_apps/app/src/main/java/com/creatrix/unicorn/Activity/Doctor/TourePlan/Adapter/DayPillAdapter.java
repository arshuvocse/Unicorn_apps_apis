package com.creatrix.unicorn.Activity.Doctor.TourePlan.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.creatrix.unicorn.R;

import java.util.List;

public class DayPillAdapter extends RecyclerView.Adapter<DayPillAdapter.DayViewHolder> {

    public interface OnDayClickListener {
        void onDayClick(int dayNumber);
    }

    private Context context;
    private List<Integer> dayList;
    private int selectedDay;
    private OnDayClickListener listener;

    public DayPillAdapter(Context context, List<Integer> dayList, int initialSelectedDay, OnDayClickListener listener) {
        this.context = context;
        this.dayList = dayList;
        this.selectedDay = initialSelectedDay;
        this.listener = listener;
    }

    public void updateDays(List<Integer> newDayList, int defaultDay) {
        this.dayList = newDayList;
        this.selectedDay = defaultDay;
        notifyDataSetChanged();
    }

    public void setSelectedDay(int day) {
        this.selectedDay = day;
        notifyDataSetChanged();
    }

    public int getSelectedDay() {
        return selectedDay;
    }

    @NonNull
    @Override
    public DayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_day_pill, parent, false);
        return new DayViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DayViewHolder holder, int position) {
        int day = dayList.get(position);
        holder.tvDayNumber.setText(String.valueOf(day));

        if (day == selectedDay) {
            holder.llContainer.setBackgroundResource(R.drawable.bg_day_pill_selected);
            holder.tvDayNumber.setTextColor(Color.WHITE);
        } else {
            holder.llContainer.setBackgroundResource(R.drawable.bg_day_pill_unselected);
            holder.tvDayNumber.setTextColor(Color.BLACK);
        }

        holder.itemView.setOnClickListener(v -> {
            int previousSelected = selectedDay;
            selectedDay = day;
            notifyDataSetChanged();
            if (listener != null) {
                listener.onDayClick(day);
            }
        });
    }

    @Override
    public int getItemCount() {
        return dayList != null ? dayList.size() : 0;
    }

    static class DayViewHolder extends RecyclerView.ViewHolder {
        LinearLayout llContainer;
        TextView tvDayNumber;

        public DayViewHolder(@NonNull View itemView) {
            super(itemView);
            llContainer = itemView.findViewById(R.id.llDayPillContainer);
            tvDayNumber = itemView.findViewById(R.id.tvDayNumber);
        }
    }
}
