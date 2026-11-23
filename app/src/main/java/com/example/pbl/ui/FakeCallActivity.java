package com.example.pbl.ui;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFakeCallBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnDeclineCall.setOnClickListener(v -> finish());

        binding.btnAnswerCall.setOnClickListener(v -> answerCall());

        binding.btnEndCall.setOnClickListener(v -> endCall());
    }

    private void answerCall() {
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
        if (timerHandler != null && timerRunnable != null) {
            timerHandler.removeCallbacks(timerRunnable);
        }
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        endCall();
    }
}
