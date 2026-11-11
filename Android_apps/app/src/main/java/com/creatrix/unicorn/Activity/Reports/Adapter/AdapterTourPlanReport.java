package com.creatrix.unicorn.Activity.Reports.Adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.creatrix.unicorn.Activity.Reports.Model.ModelTourPlanReport;
import com.creatrix.unicorn.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** One card per employee and day; inside it shift headings (Morning, Evening, Any time) with their visit types. */
public class AdapterTourPlanReport extends RecyclerView.Adapter<AdapterTourPlanReport.BookViewHolder> {
    private final List<List<ModelTourPlanReport>> days = new ArrayList<>();

    public AdapterTourPlanReport(List<ModelTourPlanReport> aList) {
        // rows come ordered day > employee > shift > visit type (sp_Webapi_Get_TourPlanReport)
        Map<String, List<ModelTourPlanReport>> byDay = new LinkedHashMap<>();
        if (aList != null) {
            for (ModelTourPlanReport r : aList) {
                String key = r.getTourPlanDate() + "|" + r.getEmpInfoId();
                if (!byDay.containsKey(key)) byDay.put(key, new ArrayList<>());
                byDay.get(key).add(r);
            }
        }
        days.addAll(byDay.values());
    }

    public static class BookViewHolder extends RecyclerView.ViewHolder {
        public TextView tvDate, tvEmp, tvSummary;
        public LinearLayout llShifts;

        public BookViewHolder(View view) {
            super(view);
            tvDate = view.findViewById(R.id.tvDate);
            tvEmp = view.findViewById(R.id.tvEmp);
            tvSummary = view.findViewById(R.id.tvSummary);
            llShifts = view.findViewById(R.id.llShifts);
        }
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.rv_tour_plan_report, parent, false);
        return new BookViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        List<ModelTourPlanReport> rows = days.get(holder.getAdapterPosition());
        ModelTourPlanReport first = rows.get(0);
        holder.tvDate.setText(formatDate(first.getTourPlanDate()));
        holder.tvEmp.setText(first.getEmpCode() + " : " + first.getEmpName());

        LayoutInflater inflater = LayoutInflater.from(holder.itemView.getContext());
        holder.llShifts.removeAllViews();
        String shown = null;
        int doctors = 0, customers = 0;
        for (ModelTourPlanReport r : rows) {
            String shift = isEmpty(r.getShift()) ? "" : r.getShift().trim();
            if (!shift.equals(shown)) {
                TextView tvShift = (TextView) inflater.inflate(R.layout.item_tour_plan_report_shift, holder.llShifts, false);
                tvShift.setText(shift.isEmpty() ? "Any time" : shift);
                tvShift.setBackgroundColor(shiftColor(shift));
                holder.llShifts.addView(tvShift);
                shown = shift;
            }

            View block = inflater.inflate(R.layout.item_tour_plan_report_block, holder.llShifts, false);
            String type = r.getVisitType() == null ? "" : r.getVisitType();
            List<String> items = splitLines(r.getDoctorCustomer());
            if ("Doctor".equals(type)) doctors += items.size();
            if ("Customer".equals(type)) customers += items.size();

            block.findViewById(R.id.vBar).setBackgroundColor(shiftColor(shift));
            ((TextView) block.findViewById(R.id.tvTitle)).setText(title(type, items.size()));
            TextView tvStatus = block.findViewById(R.id.tvStatus);
            tvStatus.setText(r.getApprovalStatus());
            tvStatus.setTextColor(statusColor(r.getApprovalStatus()));

            StringBuilder sb = new StringBuilder();
            for (String item : items) sb.append(sb.length() == 0 ? "" : "\n").append("•  ").append(item);
            setText(block.findViewById(R.id.tvItems), sb.toString());

            String area = r.getAreaName() == null ? "" : r.getAreaName();
            String market = r.getMarketName() == null ? "" : r.getMarketName();
            String purpose = r.getPurpose() == null ? "" : r.getPurpose();
            String objective = r.getObjective() == null ? "" : r.getObjective();
            StringBuilder info = new StringBuilder();
            String areaMarket = isEmpty(area) || isEmpty(market) ? area + market : area + " / " + market;
            if (!isEmpty(areaMarket)) info.append("Area / Market: ").append(areaMarket);
            String purposeText = isEmpty(objective) ? purpose : isEmpty(purpose) ? objective : purpose + " - " + objective;
            if (!isEmpty(purposeText)) info.append(info.length() == 0 ? "" : "\n").append("Purpose: ").append(purposeText);
            setText(block.findViewById(R.id.tvInfo), info.toString());

            holder.llShifts.addView(block);
        }

        List<String> summary = new ArrayList<>();
        if (doctors > 0) summary.add(doctors + (doctors == 1 ? " Doctor" : " Doctors"));
        if (customers > 0) summary.add(customers + (customers == 1 ? " Customer" : " Customers"));
        setText(holder.tvSummary, android.text.TextUtils.join("\n", summary));
    }

    /** Day-wise names come one per line; other visit types keep their own comma list as one item. */
    private static List<String> splitLines(String text) {
        List<String> items = new ArrayList<>();
        if (isEmpty(text)) return items;
        for (String s : text.split("\n")) {
            if (!isEmpty(s)) items.add(s.trim());
        }
        return items;
    }

    private static String title(String type, int count) {
        if ("Doctor".equals(type)) return "Doctors (" + count + ")";
        if ("Customer".equals(type)) return "Customers (" + count + ")";
        return type;
    }

    private static int shiftColor(String shift) {
        if ("Morning".equalsIgnoreCase(shift)) return Color.parseColor("#F29D0B");
        if ("Evening".equalsIgnoreCase(shift)) return Color.parseColor("#3F51B5");
        return Color.parseColor("#78909C");
    }

    private static int statusColor(String status) {
        if (status == null) return Color.GRAY;
        switch (status) {
            case "Submitted":
            case "Approved":
                return Color.parseColor("#00A045");
            case "Rejected":
                return Color.parseColor("#D32F2F");
            case "Not Submitted":
                return Color.parseColor("#E65100");
            default:
                return Color.parseColor("#1565C0");
        }
    }

    private static String formatDate(String yyyyMMdd) {
        try {
            return new SimpleDateFormat("EEE, dd MMM yyyy", Locale.US)
                    .format(new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(yyyyMMdd));
        } catch (ParseException | NullPointerException e) {
            return yyyyMMdd;
        }
    }

    private static boolean isEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static void setText(TextView tv, String value) {
        tv.setVisibility(isEmpty(value) ? View.GONE : View.VISIBLE);
        tv.setText(value);
    }

    @Override
    public int getItemCount() {
        return days.size();
    }
}
