package com.example.pbl.ui;

import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pbl.databinding.ActivityFakeCallBinding;

import java.util.Locale;

public class FakeCallActivity extends AppCompatActivity {

    private ActivityFakeCallBinding binding;
    private Handler timerHandler;
    private Runnable timerRunnable;
    private int secondsActive = 0;
    private Ringtone ringtone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFakeCallBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        startRingtone();

        binding.btnDeclineCall.setOnClickListener(v -> {
            stopRingtone();
            finish();
        });

        binding.btnAnswerCall.setOnClickListener(v -> answerCall());

        binding.btnEndCall.setOnClickListener(v -> endCall());
    }

    private void startRingtone() {
        try {
            Uri ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            if (ringtoneUri == null) {
                ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            }
            ringtone = RingtoneManager.getRingtone(getApplicationContext(), ringtoneUri);
            if (ringtone != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ringtone.setLooping(true);
                }
                ringtone.play();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopRingtone() {
        try {
            if (ringtone != null && ringtone.isPlaying()) {
                ringtone.stop();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void answerCall() {
        stopRingtone();

        binding.layoutCallControls.setVisibility(View.GONE);
        binding.btnEndCall.setVisibility(View.VISIBLE);
        binding.tvCallStatus.setText("00:00");

        timerHandler = new Handler(Looper.getMainLooper());
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                secondsActive++;
                int mins = secondsActive / 60;
                int secs = secondsActive % 60;
                binding.tvCallStatus.setText(String.format(Locale.getDefault(), "%02d:%02d", mins, secs));
                timerHandler.postDelayed(this, 1000);
            }
        };
        timerHandler.postDelayed(timerRunnable, 1000);
    }

    private void endCall() {
        stopRingtone();
        if (timerHandler != null && timerRunnable != null) {
            timerHandler.removeCallbacks(timerRunnable);
        }
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopRingtone();
        endCall();
    }
}
