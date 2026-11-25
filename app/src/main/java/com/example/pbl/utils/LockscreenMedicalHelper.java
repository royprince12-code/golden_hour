package com.example.pbl.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.pbl.db.DatabaseHelper;
import com.example.pbl.model.EmergencyContact;
import com.example.pbl.model.UserProfile;

import java.util.List;

public class LockscreenMedicalHelper {

    private static final String TAG = "LockscreenMedicalHelper";
    private static final String PREFS_NAME = "golden_hour_prefs";
    public static final String KEY_LOCKSCREEN_MEDICAL_ENABLED = "lockscreen_medical_enabled";
    private static final String CHANNEL_ID = "golden_hour_medical_id_channel";
    private static final int NOTIFICATION_ID = 2002;

    public static boolean isLockscreenMedicalEnabled(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_LOCKSCREEN_MEDICAL_ENABLED, false);
    }

    public static void setLockscreenMedicalEnabled(Context context, boolean enabled) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_LOCKSCREEN_MEDICAL_ENABLED, enabled).apply();
        updateLockscreenMedicalDisplay(context);
    }

    public static void updateLockscreenMedicalDisplay(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager == null) return;

        if (!isLockscreenMedicalEnabled(context)) {
            notificationManager.cancel(NOTIFICATION_ID);
            Log.i(TAG, "Lockscreen Medical ID Display disabled by user.");
            return;
        }

        DatabaseHelper db = DatabaseHelper.getInstance(context);
        UserProfile profile = db.getUserProfile();
        List<EmergencyContact> contacts = db.getAllContacts();

        if (profile == null) {
            notificationManager.cancel(NOTIFICATION_ID);
            return;
        }

        createNotificationChannel(notificationManager);

        String name = safeVal(profile.getName(), "Not specified");
        String age = safeVal(profile.getAge(), "N/A");
        String bloodGroup = safeVal(profile.getBloodGroup(), "UNKNOWN");
        String htWt = safeVal(profile.getHeightWeight(), "N/A");
        String conditions = safeVal(profile.getMedicalConditions(), "None reported");
        String allergies = safeVal(profile.getAllergies(), "None reported");
        String medications = safeVal(profile.getMedications(), "None");
        String surgeries = safeVal(profile.getSurgeries(), "None");
        String organDonor = safeVal(profile.getOrganDonor(), "N/A");
        String notes = safeVal(profile.getNotes(), "None");

        StringBuilder contactSummary = new StringBuilder();
        if (!contacts.isEmpty()) {
            for (EmergencyContact contact : contacts) {
                if (contactSummary.length() > 0) contactSummary.append(" | ");
                contactSummary.append(contact.getName()).append(" (").append(contact.getPhone()).append(")");
            }
        } else {
            contactSummary.append("No contacts saved");
        }

        StringBuilder cardText = new StringBuilder();
        cardText.append("🩸 BLOOD GROUP: ").append(bloodGroup.toUpperCase()).append("\n");
        cardText.append("👤 Patient: ").append(name).append(" (Age: ").append(age).append(" | Ht/Wt: ").append(htWt).append(")\n");
        cardText.append("⚠️ Conditions: ").append(conditions).append("\n");
        cardText.append("🚫 Allergies: ").append(allergies).append("\n");
        cardText.append("💊 Medications: ").append(medications).append("\n");
        cardText.append("🏥 Surgeries: ").append(surgeries).append("\n");
        cardText.append("🫀 Organ Donor: ").append(organDonor).append("\n");
        cardText.append("📞 Emergency Contacts: ").append(contactSummary.toString()).append("\n");
        cardText.append("📝 Notes: ").append(notes);

        // Inert PendingIntent: Tapping the notification does NOT open or redirect to the application
        PendingIntent inertIntent = PendingIntent.getActivity(
                context, 0, new Intent(), PendingIntent.FLAG_IMMUTABLE
        );

        Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle("🩺 EMERGENCY MEDICAL ID")
                .setContentText("Blood Group: " + bloodGroup + " | Patient: " + name)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(cardText.toString()))
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentIntent(inertIntent)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setOngoing(true)
                .build();

        try {
            notificationManager.notify(NOTIFICATION_ID, notification);
            Log.i(TAG, "Lockscreen Medical ID Card updated successfully.");
        } catch (Exception e) {
            Log.e(TAG, "Error posting Lockscreen Medical ID notification", e);
        }
    }

    private static String safeVal(String value, String fallback) {
        if (value != null && !value.trim().isEmpty()) {
            return value.trim();
        }
        return fallback;
    }

    private static void createNotificationChannel(NotificationManager manager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Lockscreen Medical ID Card",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Displays full emergency medical information panel directly on the Android lock screen");
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            manager.createNotificationChannel(channel);
        }
    }
}
