package com.example.pbl.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pbl.R;
import com.example.pbl.model.EmergencyContact;

import java.util.ArrayList;
import java.util.List;

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ContactViewHolder> {

    public interface OnDeleteClickListener {
        void onDeleteClick(EmergencyContact contact);
    }

    private List<EmergencyContact> contactList = new ArrayList<>();
    private final OnDeleteClickListener deleteClickListener;

    public ContactsAdapter(OnDeleteClickListener deleteClickListener) {
        this.deleteClickListener = deleteClickListener;
    }

    public void setContacts(List<EmergencyContact> contacts) {
        this.contactList = contacts;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contact, parent, false);
        return new ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        EmergencyContact contact = contactList.get(position);
        holder.tvName.setText(contact.getName());
        holder.tvPhone.setText(contact.getPhone());
        holder.tvRelationship.setText("Relationship: " + (contact.getRelationship() != null ? contact.getRelationship() : "Emergency Contact"));

        holder.btnDelete.setOnClickListener(v -> {
            if (deleteClickListener != null) {
                deleteClickListener.onDeleteClick(contact);
            }
        });
    }

    @Override
    public int getItemCount() {
        return contactList.size();
    }

    static class ContactViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPhone, tvRelationship;
        ImageButton btnDelete;

        public ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_contact_name);
            tvPhone = itemView.findViewById(R.id.tv_contact_phone);
            tvRelationship = itemView.findViewById(R.id.tv_contact_relationship);
            btnDelete = itemView.findViewById(R.id.btn_delete_contact);
        }
    }
}
