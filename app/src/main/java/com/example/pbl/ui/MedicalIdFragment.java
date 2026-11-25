package com.example.pbl.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.pbl.databinding.FragmentMedicalIdBinding;
import com.example.pbl.db.DatabaseHelper;
import com.example.pbl.model.UserProfile;
import com.example.pbl.utils.LockscreenMedicalHelper;

public class MedicalIdFragment extends Fragment {

    private FragmentMedicalIdBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMedicalIdBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        loadUserProfile();

        boolean isLockscreenEnabled = LockscreenMedicalHelper.isLockscreenMedicalEnabled(requireContext());
        binding.switchLockscreenMedical.setChecked(isLockscreenEnabled);

        binding.switchLockscreenMedical.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                showLockscreenConsentDialog();
            } else {
                LockscreenMedicalHelper.setLockscreenMedicalEnabled(requireContext(), false);
                Toast.makeText(requireContext(), "Lock Screen Medical ID disabled.", Toast.LENGTH_SHORT).show();
            }
        });

        binding.btnPreviewLockscreenCard.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), LockscreenMedicalCardActivity.class);
            startActivity(intent);
        });

        binding.btnSaveMedicalId.setOnClickListener(v -> saveUserProfile());
    }

    private void showLockscreenConsentDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Enable Lock Screen Medical Display?")
                .setMessage("Enabling this feature displays your Medical ID (Blood Group, Allergies, Emergency Contacts, Conditions) directly on your phone's lock screen.\n\n" +
                        "This allows first responders and paramedics to view your critical health information in an emergency without needing to unlock your device.\n\n" +
                        "Do you consent to showing your Medical ID on the lock screen?")
                .setPositiveButton("I Consent & Enable", (dialog, which) -> {
                    LockscreenMedicalHelper.setLockscreenMedicalEnabled(requireContext(), true);
                    Toast.makeText(requireContext(), "Lock Screen Medical ID display enabled!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    binding.switchLockscreenMedical.setChecked(false);
                })
                .setCancelable(false)
                .show();
    }

    private void loadUserProfile() {
        DatabaseHelper db = DatabaseHelper.getInstance(requireContext());
        UserProfile profile = db.getUserProfile();

        if (profile != null) {
            if (profile.getName() != null) binding.etName.setText(profile.getName());
            if (profile.getPhone() != null) binding.etPhone.setText(profile.getPhone());
            if (profile.getBloodGroup() != null) binding.etBloodGroup.setText(profile.getBloodGroup());
            if (profile.getAllergies() != null) binding.etAllergies.setText(profile.getAllergies());
            if (profile.getMedicalConditions() != null) binding.etConditions.setText(profile.getMedicalConditions());
            if (profile.getAge() != null) binding.etAge.setText(profile.getAge());
            if (profile.getHeightWeight() != null) binding.etHeightWeight.setText(profile.getHeightWeight());
            if (profile.getMedications() != null) binding.etMedications.setText(profile.getMedications());
            if (profile.getSurgeries() != null) binding.etSurgeries.setText(profile.getSurgeries());
            if (profile.getOrganDonor() != null) binding.etOrganDonor.setText(profile.getOrganDonor());
            if (profile.getNotes() != null) binding.etNotes.setText(profile.getNotes());
        }
    }

    private void saveUserProfile() {
        String name = binding.etName.getText() != null ? binding.etName.getText().toString().trim() : "";
        String phone = binding.etPhone.getText() != null ? binding.etPhone.getText().toString().trim() : "";
        String bloodGroup = binding.etBloodGroup.getText() != null ? binding.etBloodGroup.getText().toString().trim() : "";
        String allergies = binding.etAllergies.getText() != null ? binding.etAllergies.getText().toString().trim() : "";
        String conditions = binding.etConditions.getText() != null ? binding.etConditions.getText().toString().trim() : "";
        String age = binding.etAge.getText() != null ? binding.etAge.getText().toString().trim() : "";
        String heightWeight = binding.etHeightWeight.getText() != null ? binding.etHeightWeight.getText().toString().trim() : "";
        String medications = binding.etMedications.getText() != null ? binding.etMedications.getText().toString().trim() : "";
        String surgeries = binding.etSurgeries.getText() != null ? binding.etSurgeries.getText().toString().trim() : "";
        String organDonor = binding.etOrganDonor.getText() != null ? binding.etOrganDonor.getText().toString().trim() : "";
        String notes = binding.etNotes.getText() != null ? binding.etNotes.getText().toString().trim() : "";

        if (name.isEmpty() || phone.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter Name and Phone number", Toast.LENGTH_SHORT).show();
            return;
        }

        UserProfile profile = new UserProfile(name, phone, bloodGroup, allergies, conditions,
                age, heightWeight, medications, surgeries, organDonor, notes);

        DatabaseHelper db = DatabaseHelper.getInstance(requireContext());
        db.saveUserProfile(profile);

        // Automatically update the lock screen display
        LockscreenMedicalHelper.updateLockscreenMedicalDisplay(requireContext());

        Toast.makeText(requireContext(), "Medical ID saved successfully!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
