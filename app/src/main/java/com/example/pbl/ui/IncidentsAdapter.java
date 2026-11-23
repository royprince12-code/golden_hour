package com.example.pbl.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pbl.R;
import com.example.pbl.model.Incident;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class IncidentsAdapter extends RecyclerView.Adapter<IncidentsAdapter.IncidentViewHolder> {

    private List<Incident> incidentList = new ArrayList<>();

    public void setIncidents(List<Incident> incidents) {
        this.incidentList = incidents;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public IncidentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_incident, parent, false);
        return new IncidentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull IncidentViewHolder holder, int position) {
        Incident incident = incidentList.get(position);

        holder.tvType.setText(incident.getType().replace("_", " "));
        holder.tvStatus.setText(incident.getStatus().replace("_", " "));
        holder.tvLocation.setText("Location: " + incident.getLocation());

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        holder.tvTime.setText("Time: " + sdf.format(new Date(incident.getTimestamp())));
        holder.tvDetails.setText("Note: " + (incident.getDetails() != null ? incident.getDetails() : "No extra info"));
    }

    @Override
    public int getItemCount() {
        return incidentList.size();
    }

    static class IncidentViewHolder extends RecyclerView.ViewHolder {
        TextView tvType, tvStatus, tvLocation, tvTime, tvDetails;

        public IncidentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvType = itemView.findViewById(R.id.tv_incident_type);
            tvStatus = itemView.findViewById(R.id.tv_incident_status);
            tvLocation = itemView.findViewById(R.id.tv_incident_location);
            tvTime = itemView.findViewById(R.id.tv_incident_time);
            tvDetails = itemView.findViewById(R.id.tv_incident_details);
        }
    }
}
