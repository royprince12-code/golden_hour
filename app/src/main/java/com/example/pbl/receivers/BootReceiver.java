package com.example.pbl.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.pbl.utils.LockscreenMedicalHelper;

public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null && (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction()) ||
                Intent.ACTION_MY_PACKAGE_REPLACED.equals(intent.getAction()) ||
                "android.intent.action.QUICKBOOT_POWERON".equals(intent.getAction()))) {
            Log.i(TAG, "Device booted up. Re-evaluating GoldenHour persistent medical ID notification.");
            try {
                if (LockscreenMedicalHelper.isLockscreenMedicalEnabled(context)) {
                    LockscreenMedicalHelper.updateLockscreenMedicalDisplay(context);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error in BootReceiver onReceive", e);
            }
        }
    }
}
