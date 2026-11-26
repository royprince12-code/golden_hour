package com.example.pbl.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.pbl.MainActivity;
import com.example.pbl.R;
import com.example.pbl.databinding.ActivityOnboardingBinding;
import com.example.pbl.model.OnboardingPage;

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
        prefs.edit()
                .putBoolean(KEY_ONBOARDING_COMPLETED, completed)
                .putBoolean("privacy_policy_accepted", completed)
                .apply();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOnboardingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupOnboardingPages();

        OnboardingAdapter adapter = new OnboardingAdapter(pages);
        binding.viewPagerOnboarding.setAdapter(adapter);

        updateDots(0);

        binding.viewPagerOnboarding.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateDots(position);
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

    private void updateDots(int position) {
        android.view.View[] dots = new android.view.View[]{binding.dot0, binding.dot1, binding.dot2};
        float density = getResources().getDisplayMetrics().density;
        for (int i = 0; i < dots.length; i++) {
            ViewGroup.LayoutParams params = dots[i].getLayoutParams();
            if (i == position) {
                dots[i].setBackgroundResource(R.drawable.dot_active);
                params.width = (int) (24 * density);
            } else {
                dots[i].setBackgroundResource(R.drawable.dot_inactive);
                params.width = (int) (8 * density);
            }
            dots[i].setLayoutParams(params);
        }
    }

    private void setupOnboardingPages() {
        pages = new ArrayList<>();

        pages.add(new OnboardingPage(
                "Terms & Privacy Policy",
                "Complete data protection & privacy commitment",
                "1. Local Storage First: Your Medical ID and Emergency Contacts are stored offline in an encrypted database on your phone.\n\n" +
                "2. Emergency Use Only: Live GPS location & Medical ID details are shared exclusively during critical crash or SOS events.\n\n" +
                "3. Zero Data Selling: Your health and contact information is never uploaded or shared with third-party ad networks.",
                R.drawable.ic_terms_shield
        ));

        pages.add(new OnboardingPage(
                "Initial Instructions",
                "How GoldenHour protects you in critical moments",
                "• Ride Mode (Crash Sensing): Turn on Ride Mode before travel. Built-in motion sensors monitor for severe vehicle crash impact spikes (≥4.0g).\n\n" +
                "• Danger SOS: Tap the red DANGER SOS button anytime for instant, silent emergency alerts dispatched to your contacts.\n\n" +
                "• Two-Step Cancellation: If a false alarm warning pops up, tap 'I AM OK' on screen and press Volume Up (+) on your phone to cancel safely.",
                R.drawable.ic_instructions_siren
        ));

        pages.add(new OnboardingPage(
                "Permissions Setup",
                "Granting required emergency access to protect you",
                "• Location (GPS): Required to attach live Google Maps navigation links so emergency contacts and responders can pinpoint your location.\n\n" +
                "• SMS Dispatch: Required to send multi-part emergency text warnings containing your Medical ID during crashes or SOS triggers.\n\n" +
                "• Notifications: Required to display your persistent Lock Screen Medical ID Card and background crash detection status.",
                R.drawable.ic_rocket_launch
        ));
    }
}
