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
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.pbl.MainActivity;
import com.example.pbl.R;
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
        Intent intent = new Intent(context, CrashDetectionService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }

    public static void stopService(Context context) {
        Intent intent = new Intent(context, CrashDetectionService.class);
        context.stopService(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }

        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        isServiceRunning = true;
        Notification notification = createNotification();
        startForeground(NOTIFICATION_ID, notification);

        if (accelerometer != null && sensorManager != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isServiceRunning = false;
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
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
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    private void triggerCrashCountdown() {
        Intent intent = new Intent(this, CrashCountdownActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Crash Detection Service",
                    NotificationManager.IMPORTANCE_LOW
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

        com.example.pbl.model.UserProfile profile = com.example.pbl.db.DatabaseHelper.getInstance(this).getUserProfile();
        String medicalSummary = (profile != null && profile.getBloodGroup() != null)
                ? "Blood Group: " + profile.getBloodGroup() + " | Medical ID active"
                : "Ride Mode: Real-time crash detection active";

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("GoldenHour Protection (Lock Screen Medical ID)")
                .setContentText(medicalSummary)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setOngoing(true)
                .build();
    }
}
