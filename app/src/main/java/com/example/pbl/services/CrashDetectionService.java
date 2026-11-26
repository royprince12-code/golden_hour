package com.example.pbl.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.pbl.MainActivity;
import com.example.pbl.R;
import com.example.pbl.db.DatabaseHelper;
import com.example.pbl.model.UserProfile;
import com.example.pbl.ui.CrashCountdownActivity;

public class CrashDetectionService extends Service implements SensorEventListener {

    private static final String TAG = "CrashDetectionService";
    private static final String CHANNEL_ID = "golden_hour_crash_detection";
    private static final int NOTIFICATION_ID = 1001;

    // Thresholds specified in report: 4g impact threshold
    private static final float CRASH_THRESHOLD_G = 4.0f;
    private static final float CRASH_THRESHOLD_MS2 = CRASH_THRESHOLD_G * SensorManager.GRAVITY_EARTH;

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private static boolean isServiceRunning = false;
    private long lastTriggerTime = 0;

    public static boolean isRunning() {
        return isServiceRunning;
    }

    public static void startService(Context context) {
        try {
            Intent intent = new Intent(context, CrashDetectionService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent);
            } else {
                context.startService(intent);
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to start CrashDetectionService", e);
        }
    }

    public static void stopService(Context context) {
        try {
            Intent intent = new Intent(context, CrashDetectionService.class);
            context.stopService(intent);
        } catch (Exception e) {
            Log.e(TAG, "Failed to stop CrashDetectionService", e);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            if (sensorManager != null) {
                accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            }
            createNotificationChannel();
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate", e);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            isServiceRunning = true;
            Notification notification = createNotification();
            startForeground(NOTIFICATION_ID, notification);

            if (accelerometer != null && sensorManager != null) {
                sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in onStartCommand", e);
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isServiceRunning = false;
        try {
            if (sensorManager != null) {
                sensorManager.unregisterListener(this);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in onDestroy", e);
        }
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        try {
            Intent restartServiceIntent = new Intent(getApplicationContext(), CrashDetectionService.class);
            restartServiceIntent.setPackage(getPackageName());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                getApplicationContext().startForegroundService(restartServiceIntent);
            } else {
                getApplicationContext().startService(restartServiceIntent);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in onTaskRemoved", e);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        try {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];

                float acceleration = (float) Math.sqrt(x * x + y * y + z * z);

                // Check if acceleration spike exceeds 4g (39.2 m/s^2)
                if (acceleration >= CRASH_THRESHOLD_MS2) {
                    long currentTime = System.currentTimeMillis();
                    // Prevent multiple triggers within 10 seconds
                    if (currentTime - lastTriggerTime > 10000) {
                        lastTriggerTime = currentTime;
                        Log.w(TAG, "IMPACT SPIKE DETECTED: " + acceleration + " m/s^2 (" + (acceleration / SensorManager.GRAVITY_EARTH) + "g)");
                        triggerCrashCountdown();
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in onSensorChanged", e);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    private void triggerCrashCountdown() {
        try {
            PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
            if (powerManager != null) {
                PowerManager.WakeLock wakeLock = powerManager.newWakeLock(
                        PowerManager.FULL_WAKE_LOCK |
                        PowerManager.ACQUIRE_CAUSES_WAKEUP |
                        PowerManager.ON_AFTER_RELEASE,
                        "GoldenHour:CrashEmergencyWakeLock"
                );
                wakeLock.acquire(15000); // Hold for 15 seconds to ensure CPU and screen wake up
            }

            Intent intent = new Intent(this, CrashCountdownActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error launching CrashCountdownActivity", e);
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Crash Detection Service",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Monitors phone sensors for crash impact spikes in Ride Mode");
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private Notification createNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, notificationIntent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        String medicalSummary = "Ride Mode: Real-time crash detection active";
        try {
            UserProfile profile = DatabaseHelper.getInstance(this).getUserProfile();
            if (profile != null && profile.getBloodGroup() != null && !profile.getBloodGroup().isEmpty()) {
                medicalSummary = "Blood Group: " + profile.getBloodGroup() + " | Medical ID active";
            }
        } catch (Exception e) {
            Log.e(TAG, "Error reading user profile for notification", e);
        }

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("GoldenHour Protection Active")
                .setContentText(medicalSummary)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setOngoing(true)
                .build();
    }
}
