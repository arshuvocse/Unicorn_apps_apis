package com.creatrix.unicorn.RecyclerAdapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.creatrix.unicorn.Model.Rp_CampainViewModel;
import com.creatrix.unicorn.Model.Rp_TargetAcchivment;
import com.creatrix.unicorn.R;
import com.creatrix.unicorn.UtilityHelper.MathUtil;

import java.util.List;
import java.util.Locale;

public class _targetAcchivementProductList_Recycler extends RecyclerView.Adapter<_targetAcchivementProductList_Recycler.BookViewHolder> {
    private Context context;
    private final static int FADE_DURATION = 500; //FADE_DURATION in milliseconds
    private List<Rp_TargetAcchivment> aList;
    int lastposition = -1;

    public _targetAcchivementProductList_Recycler(List<Rp_TargetAcchivment> nList) {
        this.aList = nList;
    }


    public class BookViewHolder extends RecyclerView.ViewHolder {
        public TextView productTxt;
        public TextView targetTxt;
        public TextView acchiveTxt;
        public TextView percenTxt;
        public TextView balanceTxt;
        public BookViewHolder(View view) {
            super(view);
            productTxt = (TextView) view.findViewById(R.id.productTxt);
            targetTxt = (TextView) view.findViewById(R.id.targetTxt);
            acchiveTxt = (TextView) view.findViewById(R.id.acchiveTxt);
            percenTxt = (TextView) view.findViewById(R.id.percenTxt);
            balanceTxt = (TextView) view.findViewById(R.id.balanceTxt);
        }
    }

    @Override
    public _targetAcchivementProductList_Recycler.BookViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.zrv_targetachievement_data, parent, false);
        context = parent.getContext();
        return new _targetAcchivementProductList_Recycler.BookViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(_targetAcchivementProductList_Recycler.BookViewHolder holder, int position) {
        Rp_TargetAcchivment item = aList.get(position);
        holder.productTxt.setText(item.getSubTerritoryCode() + (item.getSubTerritoryName() == null ? "" : " : " + item.getSubTerritoryName()));
        holder.targetTxt.setText(String.format(Locale.US, "%.2f", item.getTargetAmt()));
        holder.acchiveTxt.setText(String.format(Locale.US, "%.2f", item.getSalesAmt()));
        holder.percenTxt.setText(String.format(Locale.US, "%.2f%%", item.getAchvPercent()));
        holder.balanceTxt.setText(String.format(Locale.US, "%.2f", item.getBalanceAmt()));
    }

    @Override
    public int getItemCount() {
        return aList.size();
    }
}