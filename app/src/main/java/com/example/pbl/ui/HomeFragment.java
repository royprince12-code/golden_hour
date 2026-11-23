package com.example.pbl.ui;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.pbl.databinding.FragmentHomeBinding;
import com.example.pbl.services.CrashDetectionService;
import com.example.pbl.utils.SosManager;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private static final String PREFS_NAME = "golden_hour_prefs";
    private static final String KEY_PRIVACY_ACCEPTED = "privacy_policy_accepted";

    private FragmentHomeBinding binding;
    private ActivityResultLauncher<String[]> permissionLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Register runtime permission launcher
        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                result -> {
                    boolean allGranted = true;
                    for (Boolean granted : result.values()) {
                        if (!granted) {
                            allGranted = false;
                            break;
                        }
                    }

                    if (allGranted) {
                        Toast.makeText(requireContext(), "All emergency permissions granted!", Toast.LENGTH_SHORT).show();
                        enableRideMode();
                    } else {
                        Toast.makeText(requireContext(), "Permissions denied. Location & Notifications are required for Ride Mode sensing.", Toast.LENGTH_LONG).show();
                        if (binding != null) {
                            binding.switchRideMode.setChecked(false);
                            updateRideModeStatusUI(false);
                        }
                    }
                }
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Check privacy policy on first launch
        checkPrivacyPolicyConsent();

        // Ride mode switch setup
        boolean isRunning = CrashDetectionService.isRunning();
        binding.switchRideMode.setChecked(isRunning);
        updateRideModeStatusUI(isRunning);

        binding.switchRideMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (!hasPrivacyPolicyConsent()) {
                    binding.switchRideMode.setChecked(false);
                    showPrivacyPolicyDialog(() -> checkAndRequestPermissions());
                } else {
                    checkAndRequestPermissions();
                }
            } else {
                CrashDetectionService.stopService(requireContext());
                updateRideModeStatusUI(false);
                Toast.makeText(requireContext(), "Ride Mode Deactivated", Toast.LENGTH_SHORT).show();
            }
        });

        // Danger SOS Button
        binding.cardSos.setOnClickListener(v -> {
            if (!hasPrivacyPolicyConsent()) {
                showPrivacyPolicyDialog(() -> SosManager.triggerDangerSos(requireContext(), "Emergency Danger SOS button pressed by user."));
            } else {
                SosManager.triggerDangerSos(requireContext(), "Emergency Danger SOS button pressed by user.");
            }
        });

        // Fake Call Tool
        binding.cardFakeCall.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), FakeCallActivity.class);
            startActivity(intent);
        });

        // Bystander Report Tool
        binding.cardBystanderReport.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), ReportBystanderActivity.class);
            startActivity(intent);
        });
    }

    private void checkPrivacyPolicyConsent() {
        if (!hasPrivacyPolicyConsent()) {
            showPrivacyPolicyDialog(null);
        }
    }

    private boolean hasPrivacyPolicyConsent() {
        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_PRIVACY_ACCEPTED, false);
    }

    private void showPrivacyPolicyDialog(@Nullable Runnable onAccepted) {
        new AlertDialog.Builder(requireContext())
                .setTitle("GoldenHour Privacy & Data Policy")
                .setMessage("GoldenHour requires access to emergency features to protect you in critical situations:\n\n" +
                        "1. Medical ID: Blood Group, Allergies & Conditions (Stored securely on device & shared only with emergency responder pre-alerts).\n" +
                        "2. Location & Sensors: Accesses fine GPS location & accelerometer sensor data to detect crash impacts in real-time.\n" +
                        "3. Emergency Contacts: Automatically dispatches SMS alerts & location updates during crash/SOS events.\n\n" +
                        "Your data remains private and stored locally on your device. Do you accept this Data Policy?")
                .setCancelable(false)
                .setPositiveButton("Accept & Continue", (dialog, which) -> {
                    SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                    prefs.edit().putBoolean(KEY_PRIVACY_ACCEPTED, true).apply();
                    Toast.makeText(requireContext(), "Privacy Policy Accepted.", Toast.LENGTH_SHORT).show();
                    if (onAccepted != null) {
                        onAccepted.run();
                    }
                })
                .setNegativeButton("Decline", (dialog, which) -> {
                    Toast.makeText(requireContext(), "Policy declined. Emergency sensing features will be disabled.", Toast.LENGTH_LONG).show();
                })
                .show();
    }

    private void checkAndRequestPermissions() {
        List<String> requiredPermissions = new ArrayList<>();
        requiredPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        requiredPermissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        requiredPermissions.add(Manifest.permission.SEND_SMS);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requiredPermissions.add(Manifest.permission.POST_NOTIFICATIONS);
        }

        List<String> missingPermissions = new ArrayList<>();
        for (String perm : requiredPermissions) {
            if (ContextCompat.checkSelfPermission(requireContext(), perm) != PackageManager.PERMISSION_GRANTED) {
                missingPermissions.add(perm);
            }
        }

        if (missingPermissions.isEmpty()) {
            enableRideMode();
        } else {
            permissionLauncher.launch(missingPermissions.toArray(new String[0]));
        }
    }

    private void enableRideMode() {
        CrashDetectionService.startService(requireContext());
        updateRideModeStatusUI(true);
        Toast.makeText(requireContext(), "Ride Mode Activated: Real-time sensor monitoring active", Toast.LENGTH_SHORT).show();
    }

    private void updateRideModeStatusUI(boolean isEnabled) {
        if (binding == null) return;
        if (isEnabled) {
            binding.tvRideModeStatus.setText("Sensor monitoring: Active (4.0g threshold)");
            binding.tvRideModeStatus.setTextColor(0xFF4ADE80); // Green
        } else {
            binding.tvRideModeStatus.setText("Sensor monitoring: Disabled");
            binding.tvRideModeStatus.setTextColor(0xFFE2E8F0);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
