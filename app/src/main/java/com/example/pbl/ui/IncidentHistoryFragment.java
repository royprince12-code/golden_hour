package com.example.pbl.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.pbl.databinding.FragmentIncidentHistoryBinding;
import com.example.pbl.db.DatabaseHelper;
import com.example.pbl.model.Incident;

import java.util.List;

public class IncidentHistoryFragment extends Fragment {

    private FragmentIncidentHistoryBinding binding;
    private IncidentsAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentIncidentHistoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.rvIncidents.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new IncidentsAdapter();
        binding.rvIncidents.setAdapter(adapter);

        loadIncidents();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadIncidents();
    }

    private void loadIncidents() {
        DatabaseHelper db = DatabaseHelper.getInstance(requireContext());
        List<Incident> incidents = db.getAllIncidents();

        if (incidents.isEmpty()) {
            binding.tvEmptyIncidents.setVisibility(View.VISIBLE);
            binding.rvIncidents.setVisibility(View.GONE);
        } else {
            binding.tvEmptyIncidents.setVisibility(View.GONE);
            binding.rvIncidents.setVisibility(View.VISIBLE);
            adapter.setIncidents(incidents);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
