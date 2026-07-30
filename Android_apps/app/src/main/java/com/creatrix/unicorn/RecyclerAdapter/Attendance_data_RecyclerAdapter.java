package com.creatrix.unicorn.RecyclerAdapter;

import android.content.Context;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.creatrix.unicorn.Model.Notice;
import com.creatrix.unicorn.Model.Report_AttendanceViewModel;
import com.creatrix.unicorn.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Attendance_data_RecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private final static int FADE_DURATION = 500; //FADE_DURATION in milliseconds
    private List<Report_AttendanceViewModel> aList;
    private List<AdapterItem> adapterItems = new ArrayList<>();
    int lastposition = -1;

    public static class AdapterItem {
        public static final int TYPE_HEADER = 0;
        public static final int TYPE_CARD = 1;

        private final int type;
        private final String date;
        private final Report_AttendanceViewModel model;

        public AdapterItem(String date) {
            this.type = TYPE_HEADER;
            this.date = date;
            this.model = null;
        }

        public AdapterItem(Report_AttendanceViewModel model) {
            this.type = TYPE_CARD;
            this.date = null;
            this.model = model;
        }

        public int getType() {
            return type;
        }

        public String getDate() {
            return date;
        }

        public Report_AttendanceViewModel getModel() {
            return model;
        }
    }

    public Attendance_data_RecyclerAdapter(List<Report_AttendanceViewModel> nList) {
        this.aList = nList;
        processItems();
    }

    private void processItems() {
        adapterItems.clear();
        if (aList == null || aList.isEmpty()) {
            return;
        }

        // 1. Merge same-day same-shift items
        List<Report_AttendanceViewModel> mergedList = new ArrayList<>();
        for (Report_AttendanceViewModel item : aList) {
            String itemDate = item.getAttendanceDate();
            if (itemDate == null) itemDate = "";
            String itemShift = (item.getPunchInShiftName() != null && !item.getPunchInShiftName().isEmpty()) 
                                ? item.getPunchInShiftName() 
                                : item.getPunchOutShiftName();
            if (itemShift == null) itemShift = "";

            boolean merged = false;
            for (Report_AttendanceViewModel mergedItem : mergedList) {
                String mergedDate = mergedItem.getAttendanceDate();
                if (mergedDate == null) mergedDate = "";
                String mergedShift = (mergedItem.getPunchInShiftName() != null && !mergedItem.getPunchInShiftName().isEmpty()) 
                                    ? mergedItem.getPunchInShiftName() 
                                    : mergedItem.getPunchOutShiftName();
                if (mergedShift == null) mergedShift = "";

                if (itemDate.equals(mergedDate) && !itemShift.isEmpty() && itemShift.equals(mergedShift)) {
                    // Merge
                    if (item.getPunchInTime() != null && !item.getPunchInTime().trim().isEmpty()) {
                        mergedItem.setPunchInTime(item.getPunchInTime());
                        mergedItem.setPunchInShiftName(item.getPunchInShiftName());
                        mergedItem.setPInLat(item.getPInLat());
                        mergedItem.setPInLog(item.getPInLog());
                        mergedItem.setPInAttAddress(item.getPInAttAddress());
                    }
                    if (item.getPunchOutTime() != null && !item.getPunchOutTime().trim().isEmpty()) {
                        mergedItem.setPunchOutTime(item.getPunchOutTime());
                        mergedItem.setPunchOutShiftName(item.getPunchOutShiftName());
                        mergedItem.setPOutLat(item.getPOutLat());
                        mergedItem.setPOutLong(item.getPOutLong());
                        mergedItem.setPOutAttAddress(item.getPOutAttAddress());
                    }
                    merged = true;
                    break;
                }
            }
            if (!merged) {
                Report_AttendanceViewModel newItem = new Report_AttendanceViewModel();
                newItem.setAttendanceId(item.getAttendanceId());
                newItem.setAttendanceDate(item.getAttendanceDate());
                newItem.setAttendanceType(item.getAttendanceType());
                newItem.setAttendanceStatus(item.getAttendanceStatus());
                newItem.setAttendanceText(item.getAttendanceText());
                newItem.setAttendanceTime(item.getAttendanceTime());
                
                newItem.setPunchInTime(item.getPunchInTime());
                newItem.setPunchInShiftName(item.getPunchInShiftName());
                newItem.setPInLat(item.getPInLat());
                newItem.setPInLog(item.getPInLog());
                newItem.setPInAttAddress(item.getPInAttAddress());
                
                newItem.setPunchOutTime(item.getPunchOutTime());
                newItem.setPunchOutShiftName(item.getPunchOutShiftName());
                newItem.setPOutLat(item.getPOutLat());
                newItem.setPOutLong(item.getPOutLong());
                newItem.setPOutAttAddress(item.getPOutAttAddress());
                
                mergedList.add(newItem);
            }
        }

        // 2. Group by Date to AdapterItems
        String lastDate = "";
        for (Report_AttendanceViewModel item : mergedList) {
            String currentDate = item.getAttendanceDate();
            if (currentDate == null) currentDate = "N/A";
            
            if (!currentDate.equals(lastDate)) {
                adapterItems.add(new AdapterItem(currentDate));
                lastDate = currentDate;
            }
            adapterItems.add(new AdapterItem(item));
        }
    }

    public static class HeaderViewHolder extends RecyclerView.ViewHolder {
        public TextView headerDate;

        public HeaderViewHolder(View view) {
            super(view);
            headerDate = (TextView) view.findViewById(R.id.headerDate);
        }
    }

    public static class BookViewHolder extends RecyclerView.ViewHolder {
        public LinearLayout cardBackground;
        
        public LinearLayout punchInLayout;
        public TextView punchInShift;
        public TextView attTime;
        public TextView punchInAddress;
        public View timelineDivider;

        public LinearLayout punchOutLayout;
        public TextView punchOutShift;
        public TextView attTimePunchOut;
        public TextView punchOutAddress;

        public BookViewHolder(View view) {
            super(view);
            cardBackground = (LinearLayout) view.findViewById(R.id.cardBackground);
            
            punchInLayout = (LinearLayout) view.findViewById(R.id.punchInLayout);
            punchInShift = (TextView) view.findViewById(R.id.punchInShift);
            attTime = (TextView) view.findViewById(R.id.attTime);
            punchInAddress = (TextView) view.findViewById(R.id.punchInAddress);
            timelineDivider = (View) view.findViewById(R.id.timelineDivider);

            punchOutLayout = (LinearLayout) view.findViewById(R.id.punchOutLayout);
            punchOutShift = (TextView) view.findViewById(R.id.punchOutShift);
            attTimePunchOut = (TextView) view.findViewById(R.id.attTimePunchOut);
            punchOutAddress = (TextView) view.findViewById(R.id.punchOutAddress);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return adapterItems.get(position).getType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        if (viewType == AdapterItem.TYPE_HEADER) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_attendance_header, parent, false);
            return new HeaderViewHolder(itemView);
        } else {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.zrv_attendance_report, parent, false);
            return new BookViewHolder(itemView);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        AdapterItem item = adapterItems.get(position);
        if (item.getType() == AdapterItem.TYPE_HEADER) {
            HeaderViewHolder headerHolder = (HeaderViewHolder) holder;
            headerHolder.headerDate.setText(item.getDate());
        } else {
            BookViewHolder cardHolder = (BookViewHolder) holder;
            Report_AttendanceViewModel ratt = item.getModel();
            if (ratt != null) {
                try {
                    // 2. Set Up Punch In Info
                    boolean hasPunchIn = ratt.getPunchInTime() != null && !ratt.getPunchInTime().trim().isEmpty();
                    if (hasPunchIn) {
                        cardHolder.punchInLayout.setVisibility(View.VISIBLE);
                        cardHolder.attTime.setText(ratt.getPunchInTime());
                        
                        // Use PInAttAddress if present, otherwise fallback to Geocoder lookup
                        String pinAddress = ratt.getPInAttAddress();
                        if (pinAddress == null || pinAddress.trim().isEmpty()) {
                            if (ratt.getPInLat() != null && !ratt.getPInLat().isEmpty() && ratt.getPInLog() != null && !ratt.getPInLog().isEmpty()) {
                                try {
                                    pinAddress = getCompleteAddressString(Double.parseDouble(ratt.getPInLat()), Double.parseDouble(ratt.getPInLog()));
                                } catch (NumberFormatException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        if (pinAddress != null && !pinAddress.trim().isEmpty()) {
                            cardHolder.punchInAddress.setText(pinAddress);
                            cardHolder.punchInAddress.setVisibility(View.VISIBLE);
                        } else {
                            cardHolder.punchInAddress.setVisibility(View.GONE);
                        }

                        if (ratt.getPunchInShiftName() != null && !ratt.getPunchInShiftName().trim().isEmpty()) {
                            cardHolder.punchInShift.setText(ratt.getPunchInShiftName());
                            cardHolder.punchInShift.setVisibility(View.VISIBLE);
                        } else {
                            cardHolder.punchInShift.setVisibility(View.GONE);
                        }
                    } else {
                        cardHolder.punchInLayout.setVisibility(View.GONE);
                    }

                    // 3. Set Up Punch Out Info
                    boolean hasPunchOut = ratt.getPunchOutTime() != null && !ratt.getPunchOutTime().trim().isEmpty();
                    if (hasPunchOut) {
                        cardHolder.punchOutLayout.setVisibility(View.VISIBLE);
                        cardHolder.attTimePunchOut.setText(ratt.getPunchOutTime());

                        // Use POutAttAddress if present, otherwise fallback to Geocoder lookup
                        String poutAddress = ratt.getPOutAttAddress();
                        if (poutAddress == null || poutAddress.trim().isEmpty()) {
                            if (ratt.getPOutLat() != null && !ratt.getPOutLat().isEmpty() && ratt.getPOutLong() != null && !ratt.getPOutLong().isEmpty()) {
                                try {
                                    poutAddress = getCompleteAddressString(Double.parseDouble(ratt.getPOutLat()), Double.parseDouble(ratt.getPOutLong()));
                                } catch (NumberFormatException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        if (poutAddress != null && !poutAddress.trim().isEmpty()) {
                            cardHolder.punchOutAddress.setText(poutAddress);
                            cardHolder.punchOutAddress.setVisibility(View.VISIBLE);
                        } else {
                            cardHolder.punchOutAddress.setVisibility(View.GONE);
                        }

                        if (ratt.getPunchOutShiftName() != null && !ratt.getPunchOutShiftName().trim().isEmpty()) {
                            cardHolder.punchOutShift.setText(ratt.getPunchOutShiftName());
                            cardHolder.punchOutShift.setVisibility(View.VISIBLE);
                        } else {
                            cardHolder.punchOutShift.setVisibility(View.GONE);
                        }
                    } else {
                        cardHolder.punchOutLayout.setVisibility(View.GONE);
                    }

                    // 4. Adjust timeline connector divider visibility
                    if (hasPunchIn && hasPunchOut) {
                        cardHolder.timelineDivider.setVisibility(View.VISIBLE);
                    } else {
                        if (cardHolder.timelineDivider != null) {
                            cardHolder.timelineDivider.setVisibility(View.GONE);
                        }
                    }

                    // 5. Select Card Background Dynamic Gradient
                    if (hasPunchIn && hasPunchOut) {
                        cardHolder.cardBackground.setBackgroundResource(R.drawable.bg_attendance_card_both_glossy);
                    } else if (hasPunchIn) {
                        cardHolder.cardBackground.setBackgroundResource(R.drawable.bg_attendance_card_in_glossy);
                    } else if (hasPunchOut) {
                        cardHolder.cardBackground.setBackgroundResource(R.drawable.bg_attendance_card_out_glossy);
                    } else {
                        cardHolder.cardBackground.setBackgroundResource(R.drawable.bg_attendance_card_both_glossy);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        Animation animation = AnimationUtils.loadAnimation(context, R.anim.slide_in_left);
        animation.setStartOffset(30 * position);//Provide delay here
        holder.itemView.startAnimation(animation);
    }

    @Override
    public int getItemCount() {
        return adapterItems.size();
    }

    private void setFadeAnimation(View view) {
        AnimationSet set = new AnimationSet(true);
        Animation animation = new AlphaAnimation(0.0f, 1.0f);
        animation.setDuration(500);
        view.startAnimation(animation);
        animation = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, -1.0f, Animation.RELATIVE_TO_SELF, 0.0f
        );
        animation.setDuration(100);
        view.startAnimation(animation);
    }

    private String getCompleteAddressString(double LATITUDE, double LONGITUDE) {
        String strAdd = "";
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);
            if (addresses != null) {
                Address returnedAddress = addresses.get(0);
                StringBuilder strReturnedAddress = new StringBuilder("");

                for (int i = 0; i <= returnedAddress.getMaxAddressLineIndex(); i++) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n");
                }
                strAdd = strReturnedAddress.toString();
                Log.w("address", strReturnedAddress.toString());
            } else {
                Log.w("address", "No Address returned!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.w("address", "Canont get Address!");
        }
        return strAdd;
    }
}

