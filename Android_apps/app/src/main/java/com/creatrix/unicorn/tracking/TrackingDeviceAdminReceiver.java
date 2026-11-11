package com.creatrix.unicorn.tracking;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Entry point that lets this app be enrolled as the Device Owner of a company-owned, fully managed
 * device. Provision it on a factory-reset device with:
 * <pre>adb shell dpm set-device-owner com.creatrix.unicorn/.tracking.TrackingDeviceAdminReceiver</pre>
 * (or through an EMM / zero-touch enrollment that names this component as the admin). Once enrolled,
 * {@link TrackingDevicePolicy} applies the organisation's policy.
 */
public class TrackingDeviceAdminReceiver extends DeviceAdminReceiver {

    @Override
    public void onEnabled(Context context, Intent intent) {
        super.onEnabled(context, intent);
        TrackingDevicePolicy.apply(context);
    }

    @Override
    public void onProfileProvisioningComplete(Context context, Intent intent) {
        super.onProfileProvisioningComplete(context, intent);
        TrackingDevicePolicy.apply(context);
    }
}
