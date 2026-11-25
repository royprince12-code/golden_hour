package com.example.pbl.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.pbl.R;
import com.example.pbl.databinding.ActivityReportBystanderBinding;
import com.example.pbl.db.DatabaseHelper;
import com.example.pbl.model.Incident;

public class ReportBystanderActivity extends AppCompatActivity {

    private ActivityReportBystanderBinding binding;
    private ActivityResultLauncher<Intent> cameraLauncher;
    private boolean hasPhoto = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityReportBystanderBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Bundle extras = result.getData().getExtras();
                        if (extras != null) {
                            Bitmap imageBitmap = (Bitmap) extras.get("data");
                            if (imageBitmap != null) {
                                binding.ivAccidentPhoto.setImageBitmap(imageBitmap);
                                binding.ivAccidentPhoto.setVisibility(View.VISIBLE);
                                hasPhoto = true;
                                Toast.makeText(this, "Accident photo attached!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
        );

        binding.btnCapturePhoto.setOnClickListener(v -> {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            try {
                cameraLauncher.launch(takePictureIntent);
            } catch (Exception e) {
                Toast.makeText(this, "Camera not available", Toast.LENGTH_SHORT).show();
            }
        });

        binding.btnSubmitBystander.setOnClickListener(v -> submitBystanderReport());
    }

    private void submitBystanderReport() {
        String location = binding.etBystanderLocation.getText() != null ? binding.etBystanderLocation.getText().toString().trim() : "";
        String details = binding.etBystanderDetails.getText() != null ? binding.etBystanderDetails.getText().toString().trim() : "";
        boolean isCritical = binding.rgSeverity.getCheckedRadioButtonId() == R.id.rb_critical;

        if (location.isEmpty()) {
            Toast.makeText(this, "Please enter accident location", Toast.LENGTH_SHORT).show();
            return;
        }

        String photoNote = hasPhoto ? " [Photo Evidence Attached]" : "";

        Incident incident = new Incident(
                "BYSTANDER_REPORT",
                System.currentTimeMillis(),
                location,
                "AMBULANCE_DISPATCHED",
                isCritical,
                "Bystander Witness Report: " + details + photoNote
        );

        DatabaseHelper.getInstance(this).addIncident(incident);

        Toast.makeText(this, "Accident Report Dispatched! Nearest Ambulance & Hospital notified.", Toast.LENGTH_LONG).show();
        finish();
    }
}
