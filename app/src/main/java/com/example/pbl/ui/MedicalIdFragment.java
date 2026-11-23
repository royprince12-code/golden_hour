package com.example.pbl.ui;

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

        binding.btnSaveMedicalId.setOnClickListener(v -> saveUserProfile());
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
        }
    }

    private void saveUserProfile() {
        String name = binding.etName.getText() != null ? binding.etName.getText().toString().trim() : "";
        String phone = binding.etPhone.getText() != null ? binding.etPhone.getText().toString().trim() : "";
        String bloodGroup = binding.etBloodGroup.getText() != null ? binding.etBloodGroup.getText().toString().trim() : "";
        String allergies = binding.etAllergies.getText() != null ? binding.etAllergies.getText().toString().trim() : "";
        String conditions = binding.etConditions.getText() != null ? binding.etConditions.getText().toString().trim() : "";

        if (name.isEmpty() || phone.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter Name and Phone number", Toast.LENGTH_SHORT).show();
            return;
        }

        UserProfile profile = new UserProfile(name, phone, bloodGroup, allergies, conditions);
        DatabaseHelper db = DatabaseHelper.getInstance(requireContext());
        db.saveUserProfile(profile);

        Toast.makeText(requireContext(), "Medical ID saved successfully!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
