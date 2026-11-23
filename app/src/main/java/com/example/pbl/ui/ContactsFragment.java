package com.example.pbl.ui;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.pbl.databinding.FragmentContactsBinding;
import com.example.pbl.db.DatabaseHelper;
import com.example.pbl.model.EmergencyContact;

import java.util.List;

public class ContactsFragment extends Fragment implements ContactsAdapter.OnDeleteClickListener {

    private FragmentContactsBinding binding;
    private ContactsAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentContactsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.rvContacts.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new ContactsAdapter(this);
        binding.rvContacts.setAdapter(adapter);

        binding.fabAddContact.setOnClickListener(v -> showAddContactDialog());

        loadContacts();
    }

    private void loadContacts() {
        DatabaseHelper db = DatabaseHelper.getInstance(requireContext());
        List<EmergencyContact> contacts = db.getAllContacts();

        if (contacts.isEmpty()) {
            binding.tvEmptyContacts.setVisibility(View.VISIBLE);
            binding.rvContacts.setVisibility(View.GONE);
        } else {
            binding.tvEmptyContacts.setVisibility(View.GONE);
            binding.rvContacts.setVisibility(View.VISIBLE);
            adapter.setContacts(contacts);
        }
    }

    private void showAddContactDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Add Emergency Contact");

        LinearLayout container = new LinearLayout(requireContext());
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(50, 20, 50, 10);

        final EditText etName = new EditText(requireContext());
        etName.setHint("Contact Name");
        container.addView(etName);

        final EditText etPhone = new EditText(requireContext());
        etPhone.setHint("Phone Number");
        container.addView(etPhone);

        final EditText etRel = new EditText(requireContext());
        etRel.setHint("Relationship (e.g. Parent, Spouse)");
        container.addView(etRel);

        builder.setView(container);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String name = etName.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();
            String rel = etRel.getText().toString().trim();

            if (!name.isEmpty() && !phone.isEmpty()) {
                EmergencyContact contact = new EmergencyContact(name, phone, rel.isEmpty() ? "Emergency Contact" : rel);
                DatabaseHelper.getInstance(requireContext()).addContact(contact);
                Toast.makeText(requireContext(), "Contact added successfully!", Toast.LENGTH_SHORT).show();
                loadContacts();
            } else {
                Toast.makeText(requireContext(), "Name and phone are required", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    @Override
    public void onDeleteClick(EmergencyContact contact) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Contact")
                .setMessage("Are you sure you want to remove " + contact.getName() + " from emergency contacts?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    DatabaseHelper.getInstance(requireContext()).deleteContact(contact.getId());
                    loadContacts();
                    Toast.makeText(requireContext(), "Contact removed", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
