package com.creatrix.unicorn.RecyclerAdapter;

import com.creatrix.unicorn.UtilityHelper.ReportFilterHelper;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.creatrix.unicorn.Activity.Doctor.Approval.Model.DoctorApproveModel;
import com.creatrix.unicorn.Activity.Doctor.DCR.AddDCRActivity;
import com.creatrix.unicorn.Activity.Doctor.Prescription.AddPrescriptionActivity;
import com.creatrix.unicorn.Activity.Doctor.DoctorTourPlanActivity;
import com.creatrix.unicorn.Activity.OrderProcess.SampleOrderActivity;
import com.creatrix.unicorn.Model.Doctor.DoctorListViewModel;
import com.creatrix.unicorn.R;
import com.creatrix.unicorn.Services.Constants;
import com.google.gson.Gson;

import java.util.List;

public class _doctor_listRecyclerAdapter extends RecyclerView.Adapter<_doctor_listRecyclerAdapter.BookViewHolder> {
    private Context context;
    private final static int FADE_DURATION = 700; //FADE_DURATION in milliseconds
    private List<DoctorListViewModel> aList;
    private List<DoctorApproveModel> dList;
    String fromStr,orderTYpe;
    private DoctorLocationClickListener locationClickListener;


    public _doctor_listRecyclerAdapter(List<DoctorListViewModel> aList,String fromType) {
        this.aList = aList;
        this.fromStr = fromType;
    }

    public void setLocationClickListener(DoctorLocationClickListener locationClickListener) {
        this.locationClickListener = locationClickListener;
    }

    @Override
    public _doctor_listRecyclerAdapter.BookViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.zrv_doctor_list, parent, false);
        context = parent.getContext();
        return new BookViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull _doctor_listRecyclerAdapter.BookViewHolder holder, int position) {

        holder.nameTxt.setText(ReportFilterHelper.withCode(aList.get(position).getDoctorCode(), aList.get(position).getDoctorName()));
        if(aList.get(position).getChemberName() !=null){
            holder.chamberTxt.setText(aList.get(position).getChemberName());

        }
        if(aList.get(position).getDocContact() !=null){
            holder.mobileTxt.setText(aList.get(position).getDocContact());

        }
        if (aList.get(position).getStreetAddress() != null && !aList.get(position).getStreetAddress().trim().isEmpty()) {
            holder.addressTxt.setText(aList.get(position).getStreetAddress());
            holder.locationBtn.setVisibility(View.GONE);
        } else {
            holder.addressTxt.setText("N/A");
            holder.locationBtn.setVisibility(View.VISIBLE);
        }
        holder.docClickdv.setOnClickListener(v -> {
            DoctorListViewModel doctorListViewModel = aList.get(holder.getAdapterPosition());
            Pair[] pair = new Pair[3];
            pair[0] = new Pair<View,String>(holder.nameTxt,"nametrans");
            pair[1] = new Pair<View,String>(holder.docimg,"imgtrans");
            pair[2] = new Pair<View,String>(holder.mobileTxt,"mobiletrans");

            if(fromStr.equals("Prescription")||fromStr.equals("AddPrescrip")){
                Constants.WHO="DoclitAdapter";
                Intent intent = new Intent(context, AddPrescriptionActivity.class);
                Gson gson = new Gson();
                String myJson = gson.toJson(doctorListViewModel);
                intent.putExtra("myjson", myJson);
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    ActivityOptions activityOptions =  ActivityOptions.makeSceneTransitionAnimation((Activity) context,pair);
                    context.startActivity(intent,activityOptions.toBundle());
                   // ((Activity)context).finish();
                }else{
                    context.startActivity(intent);
                    //((Activity)context).finish();
                }

            }
            if(fromStr.equals("TourPlan")){
                Intent intent = new Intent(context, DoctorTourPlanActivity.class);
                Gson gson = new Gson();
                String myJson = gson.toJson(doctorListViewModel);
                intent.putExtra("myjson", myJson);
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    ActivityOptions activityOptions =  ActivityOptions.makeSceneTransitionAnimation((Activity) context,pair);
                    context.startActivity(intent,activityOptions.toBundle());
                }else{
                    context.startActivity(intent);
                }

            }
            if(fromStr.equals("DCR")){
                Constants.WHO="DoclitAdapter";
                Intent intent = new Intent(context, AddDCRActivity.class);
                Gson gson = new Gson();
                String myJson = gson.toJson(doctorListViewModel);
                intent.putExtra("myjson", myJson);
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    ActivityOptions activityOptions =  ActivityOptions.makeSceneTransitionAnimation((Activity) context,pair);
                    context.startActivity(intent,activityOptions.toBundle());
                }else{
                    context.startActivity(intent);
                }

            }
            if(fromStr.equals("Samplerequi")){
                Intent intent = new Intent(context, SampleOrderActivity.class);
                Gson gson = new Gson();
                String myJson = gson.toJson(doctorListViewModel);
                intent.putExtra("myjson", myJson);
               // intent.putExtra("OrderType", orderTYpe);
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    ActivityOptions activityOptions =  ActivityOptions.makeSceneTransitionAnimation((Activity) context,pair);
                    context.startActivity(intent,activityOptions.toBundle());
                }else{
                    context.startActivity(intent);
                }
            }






        });
        holder.locationBtn.setOnClickListener(v -> {
            int adapterPosition = holder.getAdapterPosition();
            if (adapterPosition != RecyclerView.NO_POSITION && locationClickListener != null) {
                locationClickListener.onDoctorLocationClick(aList.get(adapterPosition));
            }
        });

        Animation animation = AnimationUtils.loadAnimation(context,R.anim.slide_intop);
        animation.setStartOffset(30 * position);//Provide delay here
        holder.itemView.startAnimation(animation);
    }

    public void filterList(List<DoctorListViewModel> aList){
        this.aList = aList;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return aList.size();
    }

    public static class BookViewHolder extends RecyclerView.ViewHolder {
        public TextView nameTxt,mobileTxt,chamberTxt,addressTxt;
        public ImageView docimg;
        public ImageButton locationBtn;
        public CardView docClickdv;

        public BookViewHolder(View view) {
            super(view);
            nameTxt = view.findViewById(R.id.nameTxt);
            mobileTxt = view.findViewById(R.id.mobileTxt);
            chamberTxt = view.findViewById(R.id.chamberTxt);
            addressTxt = view.findViewById(R.id.addressTxt);
            docClickdv = view.findViewById(R.id.docClickdv);
            docimg = view.findViewById(R.id.docimg);
            locationBtn = view.findViewById(R.id.locationBtn);
        }
    }

    public interface DoctorLocationClickListener {
        void onDoctorLocationClick(DoctorListViewModel doctor);
    }


    private void setFadeAnimation(View view) {
        AlphaAnimation anim = new AlphaAnimation(0.0f, 1.0f);
        anim.setDuration(FADE_DURATION);
        view.startAnimation(anim);
    }


}

