package com.example.pbl.ui;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pbl.databinding.ActivityCrashCountdownBinding;
import com.example.pbl.utils.SosManager;

public class CrashCountdownActivity extends AppCompatActivity {

    private ActivityCrashCountdownBinding binding;
    private CountDownTimer countDownTimer;
    private Vibrator vibrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Allow screen to turn on and show on top of keyguard/lock screen during emergency
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
        } else {
            getWindow().addFlags(
                android.view.WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                android.view.WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                android.view.WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
            );
        }

        binding = ActivityCrashCountdownBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Load Medical ID from SQLite database to display on lock screen alert
        com.example.pbl.model.UserProfile profile = com.example.pbl.db.DatabaseHelper.getInstance(this).getUserProfile();
        if (profile != null) {
            String bg = (profile.getBloodGroup() != null && !profile.getBloodGroup().isEmpty()) ? profile.getBloodGroup() : "Not set";
            String alg = (profile.getAllergies() != null && !profile.getAllergies().isEmpty()) ? profile.getAllergies() : "None";
            binding.tvLockscreenMedicalId.setText("Medical ID: Blood Group " + bg + " | Allergies: " + alg);
        }

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        startVibration();

        startCountdown();

        binding.btnCancelCountdown.setOnClickListener(v -> {
            cancelCountdown();
            Toast.makeText(this, "False Alarm Cancelled. You are marked Safe.", Toast.LENGTH_LONG).show();
            finish();
        });

        binding.btnSendHelpNow.setOnClickListener(v -> {
            cancelCountdown();
            SosManager.triggerCrashAlert(this, "Immediate Crash Help Requested by User");
            finish();
        });
    }

    private void startCountdown() {
        countDownTimer = new CountDownTimer(10000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int secondsLeft = (int) (millisUntilFinished / 1000);
                if (binding != null) {
                    binding.tvCountdownNumber.setText(String.valueOf(secondsLeft));
                }
            }

            @Override
            public void onFinish() {
                if (binding != null) {
                    binding.tvCountdownNumber.setText("0");
                }
                stopVibration();
                SosManager.triggerCrashAlert(CrashCountdownActivity.this, "Critical Crash: User Non-Responsive during 10s Countdown");
                finish();
            }
        }.start();
    }

    private void cancelCountdown() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        stopVibration();
    }

    private void startVibration() {
        if (vibrator != null && vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(new long[]{0, 500, 200}, 0));
            } else {
                vibrator.vibrate(new long[]{0, 500, 200}, 0);
            }
        }
    }

    private void stopVibration() {
        if (vibrator != null) {
            vibrator.cancel();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelCountdown();
    }
}
