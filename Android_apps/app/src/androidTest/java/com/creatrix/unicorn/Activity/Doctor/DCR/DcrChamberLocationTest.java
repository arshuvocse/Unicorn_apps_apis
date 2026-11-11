package com.creatrix.unicorn.Activity.Doctor.DCR;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.util.Log;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.creatrix.unicorn.Model.DcrSM;
import com.creatrix.unicorn.Model.ResultInfo;
import com.creatrix.unicorn.Network.ApiDoctorCall;
import com.creatrix.unicorn.Network.RetrofitClientInstance;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;

import retrofit2.Response;

/**
 * Posts DCRs from the phone through the app's own Retrofit client, built the way AddDCRActivity.SubmitDCR
 * builds them, to check the chamber-location rule end to end (phone -> API -> SP -> DB).
 * Needs local test data: MIO 43 day-wise tour plan for 31-Oct-2026 with doctor 260 / chamber 2907.
 * The PC side then checks tblDoctorChemberDetail for chamber 2907. Results: adb logcat -d -s UITEST
 */
@RunWith(AndroidJUnit4.class)
public class DcrChamberLocationTest {
    private static final String TAG = "UITEST";

    private static DcrSM dcr(String shift, String lat, String lon) {
        DcrSM a = new DcrSM();
        a.setDoctorId(260);
        a.setDcrDate("31-Oct-2026");
        a.setEntryTime(shift.equals("Morning") ? "10:15 AM" : "06:15 PM");
        a.setTourPlanTypeId(2);
        a.setDocTPDetailsId(0);
        a.setChamberId(2907);
        a.setShift(shift);
        a.setIsMorning(shift.equals("Morning") ? "1" : "0");
        a.setRemarks("UITEST chamber location");
        a.setSessionUser("43");
        a.setGiftList(new ArrayList<>());
        a.setSampleList(new ArrayList<>());
        a.setDoctorBrand(new ArrayList<>());
        a.setAempList(new ArrayList<>());
        a.setType("DCR");
        a.setEntryDate_Apps("31-Oct-2026");
        a.setLatitude(lat);
        a.setLongitude(lon);
        a.setStreetAddress("UITEST");
        return a;
    }

    private static ResultInfo save(DcrSM a) throws Exception {
        Response<ResultInfo> r = RetrofitClientInstance.getRetrofitInstance().create(ApiDoctorCall.class).SaveDcr(a).execute();
        Log.i(TAG, "SaveDcr shift=" + a.getShift() + " lat=" + a.getLatitude() + " -> HTTP " + r.code()
                + " success=" + (r.body() == null ? null : r.body().getSuccess())
                + " msg=" + (r.body() == null ? null : r.body().getMsd()));
        return r.body();
    }

    @Test
    public void firstDcrFillsChamberLocation_secondDoesNotOverwrite() throws Exception {
        ResultInfo first = save(dcr("Morning", "24.374512", "88.601234"));
        assertNotNull(first);
        assertTrue("first DCR not saved: " + first.getMsd(), Boolean.TRUE.equals(first.getSuccess()));
        ResultInfo second = save(dcr("Evening", "22.999999", "91.888888"));
        assertNotNull(second);
        Log.i(TAG, "second DCR saved=" + second.getSuccess() + " (chamber location must stay 24.374512, 88.601234)");
    }
}
