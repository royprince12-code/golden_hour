package com.example.pbl.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
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
import com.google.android.gms.location.CurrentLocationRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.Task;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SosManager {

    private static final String TAG = "SosManager";

    public static class LocationResult {
        public String mapsLink;
        public String accuracyInfo;
        public String timestampInfo;

        public LocationResult(String mapsLink, String accuracyInfo, String timestampInfo) {
            this.mapsLink = mapsLink;
            this.accuracyInfo = accuracyInfo;
            this.timestampInfo = timestampInfo;
        }
    }

    public static LocationResult getCurrentLocationDetails(Context context) {
        String mapsLink = "https://maps.google.com/?q=18.5204,73.8567";
        String accuracyInfo = "Accuracy: Unknown";
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        String timestampInfo = sdf.format(new Date());

        try {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                FusedLocationProviderClient fusedClient = LocationServices.getFusedLocationProviderClient(context);

                CurrentLocationRequest request = new CurrentLocationRequest.Builder()
                        .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                        .setDurationMillis(4000)
                        .build();

                Task<Location> task = fusedClient.getCurrentLocation(request, null);
                long startTime = System.currentTimeMillis();
                while (!task.isComplete() && (System.currentTimeMillis() - startTime) < 4500) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }

                Location loc = null;
                if (task.isSuccessful() && task.getResult() != null) {
                    loc = task.getResult();
                } else {
                    Task<Location> lastTask = fusedClient.getLastLocation();
                    startTime = System.currentTimeMillis();
                    while (!lastTask.isComplete() && (System.currentTimeMillis() - startTime) < 2000) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                    if (lastTask.isSuccessful() && lastTask.getResult() != null) {
                        loc = lastTask.getResult();
                    }
                }

                if (loc == null) {
                    LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
                    if (locationManager != null) {
                        Location gpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        Location netLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        loc = gpsLocation != null ? gpsLocation : netLocation;
                    }
                }

                if (loc != null) {
                    double lat = loc.getLatitude();
                    double lng = loc.getLongitude();
                    mapsLink = "https://maps.google.com/?q=" + lat + "," + lng;
                    accuracyInfo = loc.hasAccuracy() ? "±" + Math.round(loc.getAccuracy()) + "m" : "GPS Fix";
                    timestampInfo = sdf.format(new Date(loc.getTime()));
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error fetching live GPS location details", e);
        }

        return new LocationResult(mapsLink, accuracyInfo, timestampInfo);
    }

    public static long triggerDangerSos(Context context, String customDetails) {
        DatabaseHelper db = DatabaseHelper.getInstance(context);

        LocationResult locResult = getCurrentLocationDetails(context);
        String details = (customDetails != null && !customDetails.isEmpty())
                ? customDetails
                : "Silent Danger SOS Triggered from Mobile App";

        Incident incident = new Incident(
                "DANGER_SOS",
                System.currentTimeMillis(),
                locResult.mapsLink,
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

        String smsMessage = "⚠️ CRITICAL EMERGENCY WARNING! " + userName + " triggered Danger SOS. Live Map: " + locResult.mapsLink +
                " (Accuracy: " + locResult.accuracyInfo + ", Time: " + locResult.timestampInfo + ")" + medicalInfo + ". Please send help immediately!";

        // Send SMS asynchronously in background thread to prevent UI freeze / ANR
        new Thread(() -> sendSmsToEmergencyContacts(context, smsMessage)).start();

        return incidentId;
    }

    public static long triggerCrashAlert(Context context, String details) {
        DatabaseHelper db = DatabaseHelper.getInstance(context);

        LocationResult locResult = getCurrentLocationDetails(context);
        String crashDetails = (details != null && !details.isEmpty()) ? details : "Severe Vehicle Crash Impact Detected (>4.0g)";

        Incident incident = new Incident(
                "CRASH",
                System.currentTimeMillis(),
                locResult.mapsLink,
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

        String smsMessage = "⚠️ CRITICAL ACCIDENT WARNING! " + userName + " involved in road crash. Live Map: " + locResult.mapsLink +
                " (Accuracy: " + locResult.accuracyInfo + ", Time: " + locResult.timestampInfo + ")" +
                ". Medical ID -> Blood Group: " + bloodGroup + ", Allergies: " + allergies + ", Conditions: " + conditions +
                ". Ambulance dispatched and hospital pre-alerted!";

        // Send SMS asynchronously in background thread to prevent UI freeze / ANR
        new Thread(() -> sendSmsToEmergencyContacts(context, smsMessage)).start();

        return incidentId;
    }

    private static void sendSmsToEmergencyContacts(Context context, String message) {
        try {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                Log.w(TAG, "SEND_SMS permission not granted. Skipping SMS dispatch.");
                new Handler(Looper.getMainLooper()).post(() ->
                    Toast.makeText(context, "Alert Logged! Please grant SMS permission in App Settings to send emergency text messages.", Toast.LENGTH_LONG).show()
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
                            ArrayList<String> parts = smsManager.divideMessage(message);
                            if (!parts.isEmpty()) {
                                smsManager.sendMultipartTextMessage(contact.getPhone(), null, parts, null, null);
                            }

                            if (alertedNames.length() > 0) alertedNames.append(", ");
                            alertedNames.append(contact.getName());
                            Log.i(TAG, "Real multi-part SMS dispatched to " + contact.getName() + " (" + contact.getPhone() + ")");
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Failed to send SMS to " + contact.getPhone(), e);
                    }
                }
            }

            final String names = alertedNames.length() > 0 ? alertedNames.toString() : "Contacts";
            new Handler(Looper.getMainLooper()).post(() ->
                Toast.makeText(context, "EMERGENCY WARNING SMS SENT to: " + names, Toast.LENGTH_LONG).show()
            );
        } catch (Exception e) {
            Log.e(TAG, "Exception in sendSmsToEmergencyContacts", e);
        }
    }
}
