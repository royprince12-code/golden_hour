package com.example.pbl.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.pbl.MainActivity;
import com.example.pbl.databinding.ActivityOnboardingBinding;
import com.example.pbl.model.OnboardingPage;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;

public class OnboardingActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "golden_hour_prefs";
    public static final String KEY_ONBOARDING_COMPLETED = "onboarding_completed";

    private ActivityOnboardingBinding binding;
    private List<OnboardingPage> pages;

    public static boolean isOnboardingCompleted(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false);
    }

    public static void setOnboardingCompleted(Context context, boolean completed) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETED, completed).apply();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOnboardingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupOnboardingPages();

        OnboardingAdapter adapter = new OnboardingAdapter(pages);
        binding.viewPagerOnboarding.setAdapter(adapter);

        new TabLayoutMediator(binding.tabLayoutDots, binding.viewPagerOnboarding, (tab, position) -> {
            // Indicator dots created automatically by tab selector
        }).attach();

        binding.viewPagerOnboarding.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                if (position == pages.size() - 1) {
                    binding.btnOnboardingAction.setText("GET STARTED");
                } else if (position == 0) {
                    binding.btnOnboardingAction.setText("AGREE & CONTINUE");
                } else {
                    binding.btnOnboardingAction.setText("NEXT");
                }
            }
        });

        binding.btnOnboardingAction.setOnClickListener(v -> {
            int current = binding.viewPagerOnboarding.getCurrentItem();
            if (current < pages.size() - 1) {
                binding.viewPagerOnboarding.setCurrentItem(current + 1);
            } else {
                setOnboardingCompleted(this, true);
                Intent intent = new Intent(OnboardingActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void setupOnboardingPages() {
        pages = new ArrayList<>();

        pages.add(new OnboardingPage(
                "Terms & Privacy Policy",
                "Please review the data & safety terms before proceeding",
                "1. Data Collection: Medical ID (Blood Group, Allergies, Conditions), Emergency Contacts, fine GPS location, and accelerometer motion sensor data.\n\n" +
                "2. Emergency Use Only: Your Medical ID and live location are shared only during critical crash/SOS events via warning SMS & hospital pre-alerts.\n\n" +
                "3. Offline Security: All information remains stored locally on your device in an offline database.",
                android.R.drawable.ic_dialog_info
        ));

        pages.add(new OnboardingPage(
                "Initial Instructions",
                "How GoldenHour keeps you safe in critical moments",
                "• Ride Mode: Toggle Ride Mode on before riding/driving to activate real-time accelerometer crash impact detection (4.0g threshold).\n\n" +
                "• Danger SOS: One-tap panic button for silent emergency alerts and live GPS SMS broadcast to emergency contacts.\n\n" +
                "• False Alarm Cancel: In case of a false alarm warning, tap 'I AM OK' and press Volume Up (+) on your phone to cancel safely.",
                android.R.drawable.ic_dialog_alert
        ));

        pages.add(new OnboardingPage(
                "Setup & Permissions",
                "You are ready to set up your Emergency Shield",
                "GoldenHour requires Location, SMS, and Notification permissions to detect crashes, send emergency SMS, and display lock-screen Medical ID cards.\n\n" +
                "Tap 'GET STARTED' to enter GoldenHour and configure your Medical ID Profile and Emergency Contacts.",
                android.R.drawable.ic_menu_compass
        ));
    }
}
