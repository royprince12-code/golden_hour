package com.example.pbl.utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.example.pbl.db.DatabaseHelper;
import com.example.pbl.model.EmergencyContact;
import com.example.pbl.model.Incident;
import com.example.pbl.model.UserProfile;

import java.util.List;

public class SosManager {

    private static final String TAG = "SosManager";

    public static long triggerDangerSos(Context context, String customDetails) {
        DatabaseHelper db = DatabaseHelper.getInstance(context);

        // Get user location simulation
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

        // Simulate sending SMS & Push notifications to emergency contacts
        List<EmergencyContact> contacts = db.getAllContacts();
        UserProfile profile = db.getUserProfile();
        String userName = (profile != null && profile.getName() != null && !profile.getName().isEmpty())
                ? profile.getName()
                : "GoldenHour User";

        if (contacts.isEmpty()) {
            Toast.makeText(context, "SOS Alert Generated! (No Emergency Contacts Configured)", Toast.LENGTH_LONG).show();
        } else {
            StringBuilder contactNames = new StringBuilder();
            for (EmergencyContact contact : contacts) {
                if (contactNames.length() > 0) contactNames.append(", ");
                contactNames.append(contact.getName());
                Log.i(TAG, "Simulated SMS to " + contact.getName() + " (" + contact.getPhone() + "): EMERGENCY! " + userName + " triggered Danger SOS at " + location);
            }
            Toast.makeText(context, "DANGER SOS SENT! Alerted: " + contactNames.toString(), Toast.LENGTH_LONG).show();
        }

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

        // Dispatch ambulance & hospital pre-alert simulation
        List<EmergencyContact> contacts = db.getAllContacts();
        UserProfile profile = db.getUserProfile();

        String bloodGroup = (profile != null && profile.getBloodGroup() != null) ? profile.getBloodGroup() : "Unknown";
        Log.i(TAG, "CRASH PRE-ALERT SENT TO NEAREST HOSPITAL & AMBULANCE. Medical ID Blood Group: " + bloodGroup);

        Toast.makeText(context, "CRASH ALERT SENT! Ambulance Dispatched & Pre-Alert sent to Hospital.", Toast.LENGTH_LONG).show();

        return incidentId;
    }
}
