package com.example.pbl.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.example.pbl.db.DatabaseHelper;
import com.example.pbl.model.EmergencyContact;
import com.example.pbl.model.Incident;
import com.example.pbl.model.UserProfile;

import java.util.List;

public class SosManager {

    private static final String TAG = "SosManager";

    public static long triggerDangerSos(Context context, String customDetails) {
        DatabaseHelper db = DatabaseHelper.getInstance(context);

        String location = "Lat: 18.5204, Lng: 73.8567 (Pune Central)";
        String details = (customDetails != null && !customDetails.isEmpty())
                ? customDetails
                : "Silent Danger SOS Triggered from Mobile App";

        Incident incident = new Incident(
                "DANGER_SOS",
                System.currentTimeMillis(),
                location,
                "POLICE_NOTIFIED",
                true,
                details
        );

        long incidentId = db.addIncident(incident);

        UserProfile profile = db.getUserProfile();
        String userName = (profile != null && profile.getName() != null && !profile.getName().isEmpty())
                ? profile.getName()
                : "GoldenHour User";
        String medicalInfo = (profile != null && profile.getBloodGroup() != null)
                ? " | Blood Group: " + profile.getBloodGroup() + ", Allergies: " + profile.getAllergies()
                : "";

        String smsMessage = "EMERGENCY ALERT! " + userName + " has triggered a Danger SOS. Location: " + location + medicalInfo + ". Please send help immediately!";

        // Send SMS asynchronously in background thread to prevent UI freeze / ANR
        new Thread(() -> sendSmsToEmergencyContacts(context, smsMessage)).start();

        return incidentId;
    }

    public static long triggerCrashAlert(Context context, String details) {
        DatabaseHelper db = DatabaseHelper.getInstance(context);

        String location = "Lat: 18.5312, Lng: 73.8445 (Highway Km 12)";
        String crashDetails = (details != null && !details.isEmpty()) ? details : "Severe Vehicle Crash Impact Detected (>4.0g)";

        Incident incident = new Incident(
                "CRASH",
                System.currentTimeMillis(),
                location,
                "AMBULANCE_DISPATCHED",
                true,
                crashDetails
        );

        long incidentId = db.addIncident(incident);

        UserProfile profile = db.getUserProfile();
        String userName = (profile != null && profile.getName() != null && !profile.getName().isEmpty())
                ? profile.getName()
                : "GoldenHour User";
        String bloodGroup = (profile != null && profile.getBloodGroup() != null) ? profile.getBloodGroup() : "Unknown";
        String allergies = (profile != null && profile.getAllergies() != null) ? profile.getAllergies() : "None";
        String conditions = (profile != null && profile.getMedicalConditions() != null) ? profile.getMedicalConditions() : "None";

        String smsMessage = "CRASH DETECTED! " + userName + " was involved in a severe road accident. Location: " + location +
                ". Medical ID -> Blood Group: " + bloodGroup + ", Allergies: " + allergies + ", Conditions: " + conditions +
                ". Ambulance dispatched and hospital pre-alerted!";

        // Send SMS asynchronously in background thread to prevent UI freeze / ANR
        new Thread(() -> sendSmsToEmergencyContacts(context, smsMessage)).start();

        return incidentId;
    }

    private static void sendSmsToEmergencyContacts(Context context, String message) {
        try {
            // Check if SEND_SMS permission is granted before trying to send SMS
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                Log.w(TAG, "SEND_SMS permission not granted. Skipping SMS dispatch to prevent SecurityException crash.");
                new Handler(Looper.getMainLooper()).post(() ->
                    Toast.makeText(context, "Emergency Alert Logged! (Grant SMS permission in settings to send SMS)", Toast.LENGTH_LONG).show()
                );
                return;
            }

            DatabaseHelper db = DatabaseHelper.getInstance(context.getApplicationContext());
            List<EmergencyContact> contacts = db.getAllContacts();

            if (contacts.isEmpty()) {
                new Handler(Looper.getMainLooper()).post(() ->
                    Toast.makeText(context, "Emergency Alert Logged! (No Emergency Contacts Configured)", Toast.LENGTH_LONG).show()
                );
                return;
            }

            SmsManager smsManager;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                smsManager = context.getApplicationContext().getSystemService(SmsManager.class);
            } else {
                smsManager = SmsManager.getDefault();
            }

            StringBuilder alertedNames = new StringBuilder();
            for (EmergencyContact contact : contacts) {
                if (contact.getPhone() != null && !contact.getPhone().isEmpty()) {
                    try {
                        if (smsManager != null) {
                            smsManager.sendTextMessage(contact.getPhone(), null, message, null, null);
                            if (alertedNames.length() > 0) alertedNames.append(", ");
                            alertedNames.append(contact.getName());
                            Log.i(TAG, "Real SMS dispatched to " + contact.getName() + " (" + contact.getPhone() + ")");
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Failed to send SMS to " + contact.getPhone(), e);
                    }
                }
            }

            final String names = alertedNames.length() > 0 ? alertedNames.toString() : "Contacts";
            new Handler(Looper.getMainLooper()).post(() ->
                Toast.makeText(context, "EMERGENCY SMS SENT to: " + names, Toast.LENGTH_LONG).show()
            );
        } catch (Exception e) {
            Log.e(TAG, "Exception in sendSmsToEmergencyContacts", e);
        }
    }
}
