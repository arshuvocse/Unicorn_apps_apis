package com.creatrix.unicorn.Activity.Team.Model;

import com.google.gson.annotations.SerializedName;

public class LiveTrackingLocation {
    @SerializedName("EmpMasterCode")
    private String empMasterCode;

    @SerializedName("EmpName")
    private String empName;

    @SerializedName("DesigName")
    private String desigName;

    @SerializedName("DisplayName")
    private String displayName;

    @SerializedName("LatValue")
    private String latValue;

    @SerializedName("LongValue")
    private String longValue;

    @SerializedName("AddressName")
    private String addressName;

    @SerializedName(value = "TrackTime", alternate = {"trackTime"})
    private String trackTime;

    public String getEmpMasterCode() {
        return empMasterCode;
    }

    public String getEmpName() {
        return empName;
    }

    public String getDesigName() {
        return desigName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getLatValue() {
        return latValue;
    }

    public String getLongValue() {
        return longValue;
    }

    public String getAddressName() {
        return addressName;
    }

    public String getTrackTime() {
        return trackTime;
    }
}
