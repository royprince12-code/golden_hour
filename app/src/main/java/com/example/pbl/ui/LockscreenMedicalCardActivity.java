package com.example.pbl.ui;

import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pbl.databinding.ActivityLockscreenMedicalCardBinding;
import com.example.pbl.db.DatabaseHelper;
import com.example.pbl.model.EmergencyContact;
import com.example.pbl.model.UserProfile;

import java.util.List;

public class LockscreenMedicalCardActivity extends AppCompatActivity {

    private ActivityLockscreenMedicalCardBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Allow display over lock screen when launched as an emergency activity
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
        } else {
            getWindow().addFlags(
                android.view.WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                android.view.WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            );
        }

        binding = ActivityLockscreenMedicalCardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnClosePreview.setOnClickListener(v -> finish());

        loadMedicalCardData();
    }

    private void loadMedicalCardData() {
        DatabaseHelper db = DatabaseHelper.getInstance(this);
        UserProfile profile = db.getUserProfile();
        List<EmergencyContact> contacts = db.getAllContacts();

        if (profile != null) {
            binding.tvPreviewBloodGroup.setText(safeVal(profile.getBloodGroup(), "UNKNOWN"));
            binding.tvPreviewNameAge.setText("Patient: " + safeVal(profile.getName(), "Anonymous") +
                    " (Age: " + safeVal(profile.getAge(), "N/A") + " | Ht/Wt: " + safeVal(profile.getHeightWeight(), "N/A") + ")");
            binding.tvPreviewConditions.setText(safeVal(profile.getMedicalConditions(), "None reported"));
            binding.tvPreviewAllergies.setText(safeVal(profile.getAllergies(), "None reported"));
            binding.tvPreviewMedsSurgeries.setText("Meds: " + safeVal(profile.getMedications(), "None") +
                    " | Surgeries: " + safeVal(profile.getSurgeries(), "None"));
            binding.tvPreviewDonorNotes.setText("Organ Donor: " + safeVal(profile.getOrganDonor(), "N/A") +
                    " | Notes: " + safeVal(profile.getNotes(), "None"));
        }

        StringBuilder contactSummary = new StringBuilder();
        if (contacts != null && !contacts.isEmpty()) {
            for (EmergencyContact contact : contacts) {
                if (contactSummary.length() > 0) contactSummary.append("\n");
                contactSummary.append("• ").append(contact.getName()).append(" (").append(contact.getPhone()).append(")");
            }
            binding.tvPreviewContacts.setText(contactSummary.toString());
        } else {
            binding.tvPreviewContacts.setText("No emergency contacts configured.");
        }
    }

    private String safeVal(String val, String fallback) {
        if (val != null && !val.trim().isEmpty()) {
            return val.trim();
        }
        return fallback;
    }
}
