package com.example.pbl.ui;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pbl.R;
import com.example.pbl.databinding.ActivityReportBystanderBinding;
import com.example.pbl.db.DatabaseHelper;
import com.example.pbl.model.Incident;

public class ReportBystanderActivity extends AppCompatActivity {

    private ActivityReportBystanderBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityReportBystanderBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

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

        Incident incident = new Incident(
                "BYSTANDER_REPORT",
                System.currentTimeMillis(),
                location,
                "AMBULANCE_DISPATCHED",
                isCritical,
                "Bystander Witness Report: " + details
        );

        DatabaseHelper.getInstance(this).addIncident(incident);

        Toast.makeText(this, "Accident Report Dispatched! Nearest Ambulance & Hospital notified.", Toast.LENGTH_LONG).show();
        finish();
    }
}
