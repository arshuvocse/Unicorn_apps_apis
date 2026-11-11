package com.creatrix.unicorn.RecyclerAdapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.creatrix.unicorn.Model.Doctor.DoctorChamberTypeVM;
import com.creatrix.unicorn.R;


import java.util.List;

public class _chamber_ListAdapter extends RecyclerView.Adapter<_chamber_ListAdapter.BookViewHolder> {
    private Context context;
    private final static int FADE_DURATION = 500; //FADE_DURATION in milliseconds
    //private List<DoctorChamberName> aChamberList;
    private List<DoctorChamberTypeVM> aChamberList;


    public _chamber_ListAdapter(Context context, List<DoctorChamberTypeVM> aChamberList) {
        this.context = context;
        this.aChamberList = aChamberList;
    }

    @Override
    public _chamber_ListAdapter.BookViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout._rv_chamber_item, parent, false);
        context = parent.getContext();
        return new _chamber_ListAdapter.BookViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(_chamber_ListAdapter.BookViewHolder holder, int position) {
        DoctorChamberTypeVM ad = aChamberList.get(position);

        String shift = ad.getShift();
        if (shift != null && !shift.isEmpty()) {
            holder.tv_shift.setVisibility(View.VISIBLE);
            holder.tv_shift.setText(shift);
        } else {
            holder.tv_shift.setVisibility(View.GONE);
        }

        if (ad.getChamberTypeName() != null && !ad.getChamberTypeName().isEmpty()) {
            holder.tv_chamberTypeName.setVisibility(View.VISIBLE);
            holder.tv_chamberTypeName.setText(ad.getChamberTypeName());
        } else {
            holder.tv_chamberTypeName.setVisibility(View.GONE);
        }

        holder.chamberName.setText(ad.getChamberName());

        String sittingTime = ad.getSittingTime();
        if (sittingTime != null && !sittingTime.isEmpty()) {
            holder.tv_sittingTime.setVisibility(View.VISIBLE);
            holder.tv_sittingTime.setText("Time: " + sittingTime);
        } else {
            holder.tv_sittingTime.setVisibility(View.GONE);
        }

        String address = ad.getAddress();
        if (address != null && !address.isEmpty()) {
            holder.tv_chamberAddress.setVisibility(View.VISIBLE);
            holder.tv_chamberAddress.setText("Address: " + address);
        } else {
            holder.tv_chamberAddress.setVisibility(View.GONE);
        }

        holder.del.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int adapterPosition = holder.getAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION && adapterPosition < aChamberList.size()) {
                    aChamberList.remove(adapterPosition);
                    notifyDataSetChanged();
                }
            }
        });

        setFadeAnimation(holder.itemView);
    }

    @Override
    public int getItemCount() {
        return aChamberList.size();
    }


    public class BookViewHolder extends RecyclerView.ViewHolder {
        public TextView tv_shift, tv_chamberTypeName, chamberName, tv_sittingTime, tv_chamberAddress;
        public ImageView del;

        public BookViewHolder(View view) {
            super(view);
            tv_shift = view.findViewById(R.id.tv_shift);
            tv_chamberTypeName = view.findViewById(R.id.tv_chamberTypeName);
            chamberName = view.findViewById(R.id.tv_chamberName);
            tv_sittingTime = view.findViewById(R.id.tv_sittingTime);
            tv_chamberAddress = view.findViewById(R.id.tv_chamberAddress);
            del = view.findViewById(R.id.iv_del);
        }
    }


    private void setFadeAnimation(View view) {
        AlphaAnimation anim = new AlphaAnimation(0.0f, 1.0f);
        anim.setDuration(FADE_DURATION);
        view.startAnimation(anim);
    }


}