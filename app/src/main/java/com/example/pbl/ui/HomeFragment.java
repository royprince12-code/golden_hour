package com.example.pbl.ui;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
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
    private ActivityResultLauncher<String[]> foregroundPermissionLauncher;
    private ActivityResultLauncher<String> backgroundPermissionLauncher;
    private ActivityResultLauncher<String> smsPermissionLauncher;
    private ActivityResultLauncher<Intent> gpsSettingsLauncher;
    private ActivityResultLauncher<Intent> appSettingsLauncher;
    private ActivityResultLauncher<Intent> overlaySettingsLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Register launcher for foreground permissions (Fine/Coarse Location, SMS, Notifications [Show in Lock Screen])
        foregroundPermissionLauncher = registerForActivityResult(
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
                        checkBackgroundLocation();
                    } else {
                        handlePermissionDenied();
                    }
                }
        );

        // Register launcher for Android 10+ background location permission
        backgroundPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        checkOverlayPermission();
                    } else {
                        Toast.makeText(requireContext(), "Background location access is recommended so crash sensing works while screen is off.", Toast.LENGTH_LONG).show();
                        checkOverlayPermission();
                    }
                }
        );

        // Register launcher for SMS permission when tapping SOS
        smsPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        Toast.makeText(requireContext(), "SMS permission granted! You can now send SOS alerts.", Toast.LENGTH_SHORT).show();
                        SosManager.triggerDangerSos(requireContext(), "Emergency Danger SOS button pressed by user.");
                    } else {
                        Toast.makeText(requireContext(), "SMS permission is required to dispatch emergency text warnings to your contacts.", Toast.LENGTH_LONG).show();
                    }
                }
        );

        // Register launcher for Overlay settings intent (Open New Window in the Background)
        overlaySettingsLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> checkGpsAndStartRideMode()
        );

        // Launcher for GPS settings intent
        gpsSettingsLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> verifyAndStartRideMode()
        );

        // Launcher for Application Settings intent
        appSettingsLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> verifyAndStartRideMode()
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

        checkPrivacyPolicyConsent();

        boolean isRunning = CrashDetectionService.isRunning();
        binding.switchRideMode.setChecked(isRunning);
        updateRideModeStatusUI(isRunning);

        binding.switchRideMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (!hasPrivacyPolicyConsent()) {
                    binding.switchRideMode.setChecked(false);
                    showPrivacyPolicyDialog(() -> verifyAndStartRideMode());
                } else {
                    verifyAndStartRideMode();
                }
            } else {
                CrashDetectionService.stopService(requireContext());
                updateRideModeStatusUI(false);
                Toast.makeText(requireContext(), "Ride Mode Deactivated", Toast.LENGTH_SHORT).show();
            }
        });

        binding.cardSos.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                smsPermissionLauncher.launch(Manifest.permission.SEND_SMS);
                Toast.makeText(requireContext(), "Please grant SMS permission to send emergency alerts.", Toast.LENGTH_LONG).show();
                return;
            }

            if (!hasPrivacyPolicyConsent()) {
                showPrivacyPolicyDialog(() -> SosManager.triggerDangerSos(requireContext(), "Emergency Danger SOS button pressed by user."));
            } else {
                SosManager.triggerDangerSos(requireContext(), "Emergency Danger SOS button pressed by user.");
            }
        });

        binding.cardFakeCall.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), FakeCallActivity.class);
            startActivity(intent);
        });

        binding.cardBystanderReport.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), ReportBystanderActivity.class);
            startActivity(intent);
        });
    }

    private void verifyAndStartRideMode() {
        // Step 1: Check foreground permissions (includes Show in Lock Screen / Notifications)
        List<String> missingPermissions = getMissingForegroundPermissions();

        if (!missingPermissions.isEmpty()) {
            explainAndRequestPermissions(missingPermissions);
        } else {
            // Step 2: Check background location permission
            checkBackgroundLocation();
        }
    }

    private List<String> getMissingForegroundPermissions() {
        List<String> requiredPermissions = new ArrayList<>();
        requiredPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        requiredPermissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        requiredPermissions.add(Manifest.permission.SEND_SMS);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requiredPermissions.add(Manifest.permission.POST_NOTIFICATIONS);
        }

        List<String> missing = new ArrayList<>();
        for (String perm : requiredPermissions) {
            if (ContextCompat.checkSelfPermission(requireContext(), perm) != PackageManager.PERMISSION_GRANTED) {
                missing.add(perm);
            }
        }
        return missing;
    }

    private void explainAndRequestPermissions(List<String> missingPermissions) {
        boolean showRationale = false;
        for (String perm : missingPermissions) {
            if (shouldShowRequestPermissionRationale(perm)) {
                showRationale = true;
                break;
            }
        }

        if (showRationale) {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Permissions Required for Ride Mode")
                    .setMessage("Ride Mode requires Location, SMS, and Notification permissions to detect vehicle crash impacts and send emergency alerts to your contacts.")
                    .setPositiveButton("Grant Permissions", (dialog, which) -> {
                        foregroundPermissionLauncher.launch(missingPermissions.toArray(new String[0]));
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> handlePermissionDenied())
                    .show();
        } else {
            foregroundPermissionLauncher.launch(missingPermissions.toArray(new String[0]));
        }
    }

    private void checkBackgroundLocation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                new AlertDialog.Builder(requireContext())
                        .setTitle("Background Location Access")
                        .setMessage("To detect crashes while your screen is locked or while using other apps, please select 'Allow all the time' for location access.")
                        .setPositiveButton("Grant Access", (dialog, which) -> {
                            backgroundPermissionLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION);
                        })
                        .setNegativeButton("Skip for Now", (dialog, which) -> {
                            checkOverlayPermission();
                        })
                        .show();
                return;
            }
        }
        checkOverlayPermission();
    }

    private void checkOverlayPermission() {
        // Permission 3: Open New Window in the Background ("Display pop-up windows while running in the background" / Draw Over Other Apps)
        if (!Settings.canDrawOverlays(requireContext())) {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Open New Window in Background (Pop-up Permission)")
                    .setMessage("To allow GoldenHour to pop up the emergency crash warning window over other apps or from the background, please enable 'Display pop-up windows while running in the background' (Draw over other apps).")
                    .setPositiveButton("Open Settings", (dialog, which) -> {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:" + requireContext().getPackageName()));
                        overlaySettingsLauncher.launch(intent);
                    })
                    .setNegativeButton("Skip for Now", (dialog, which) -> {
                        checkGpsAndStartRideMode();
                    })
                    .show();
            return;
        }
        checkGpsAndStartRideMode();
    }

    private void checkGpsAndStartRideMode() {
        LocationManager locationManager = (LocationManager) requireContext().getSystemService(Context.LOCATION_SERVICE);
        boolean isGpsEnabled = false;
        boolean isNetworkEnabled = false;

        if (locationManager != null) {
            isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        }

        if (!isGpsEnabled && !isNetworkEnabled) {
            if (binding != null) {
                binding.switchRideMode.setChecked(false);
                updateRideModeStatusUI(false);
            }
            new AlertDialog.Builder(requireContext())
                    .setTitle("Location Services Disabled")
                    .setMessage("Device Location (GPS) is turned off. GoldenHour requires Location Services enabled to pinpoint your position during emergencies.")
                    .setPositiveButton("Enable GPS in Settings", (dialog, which) -> {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        gpsSettingsLauncher.launch(intent);
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> {
                        Toast.makeText(requireContext(), "Ride Mode cannot start without Location Services enabled.", Toast.LENGTH_LONG).show();
                    })
                    .show();
        } else {
            enableRideModeService();
        }
    }

    private void enableRideModeService() {
        try {
            CrashDetectionService.startService(requireContext());
            if (binding != null) {
                binding.switchRideMode.setChecked(true);
                updateRideModeStatusUI(true);
            }
            Toast.makeText(requireContext(), "Ride Mode Activated: Real-time sensor monitoring active", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Failed to start Ride Mode: " + e.getMessage(), Toast.LENGTH_LONG).show();
            if (binding != null) {
                binding.switchRideMode.setChecked(false);
                updateRideModeStatusUI(false);
            }
        }
    }

    private void handlePermissionDenied() {
        if (binding != null) {
            binding.switchRideMode.setChecked(false);
            updateRideModeStatusUI(false);
        }

        boolean permanentlyDenied = false;
        List<String> missing = getMissingForegroundPermissions();
        for (String perm : missing) {
            if (!shouldShowRequestPermissionRationale(perm)) {
                permanentlyDenied = true;
                break;
            }
        }

        if (permanentlyDenied) {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Permissions Denied")
                    .setMessage("Required emergency permissions were permanently denied. Please enable Location, SMS, and Notification permissions in App Settings to use Ride Mode.")
                    .setPositiveButton("Open Settings", (dialog, which) -> {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", requireContext().getPackageName(), null);
                        intent.setData(uri);
                        appSettingsLauncher.launch(intent);
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        } else {
            Toast.makeText(requireContext(), "Ride Mode requires Location, SMS, and Notification permissions.", Toast.LENGTH_LONG).show();
        }
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

    private void updateRideModeStatusUI(boolean isEnabled) {
        if (binding == null) return;
        if (binding.switchRideMode.isChecked() != isEnabled) {
            binding.switchRideMode.setChecked(false);
        }
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
