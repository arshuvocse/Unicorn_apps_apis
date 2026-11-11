package com.creatrix.unicorn.Activity.Attendance.Model;

import com.google.gson.annotations.SerializedName;

public class PunchWindow {
    @SerializedName("PunchInFrom")
    private String punchInFrom;

    @SerializedName("PunchInTo")
    private String punchInTo;

    @SerializedName("IsRestricted")
    private boolean isRestricted;

    @SerializedName("IsAllowedNow")
    private boolean isAllowedNow;

    public String getPunchInFrom() {
        return punchInFrom;
    }

    public String getPunchInTo() {
        return punchInTo;
    }

    public boolean isRestricted() {
        return isRestricted;
    }

    public boolean isAllowedNow() {
        return isAllowedNow;
    }
}
