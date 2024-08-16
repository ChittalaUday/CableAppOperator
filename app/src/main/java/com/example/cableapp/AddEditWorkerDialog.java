package com.example.cableapp;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;

import java.util.UUID;

public class AddEditWorkerDialog extends DialogFragment {
    private EditText nameEditText, mobileEditText, roleEditText;
    private ImageView photoImageView;
    private Uri photoUri;
    private Button saveButton;
    private Worker worker;
    private boolean isEditMode = false;
    private FirebaseHelper firebaseHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_add_edit_worker, container, false);
        nameEditText = view.findViewById(R.id.nameEditText);
        mobileEditText = view.findViewById(R.id.mobileEditText);
        roleEditText = view.findViewById(R.id.roleEditText);
        photoImageView = view.findViewById(R.id.photoImageView);
        saveButton = view.findViewById(R.id.saveButton);

        firebaseHelper = new FirebaseHelper();

        if (worker != null) {
            isEditMode = true;
            nameEditText.setText(worker.getName());
            mobileEditText.setText(worker.getMobile());
            roleEditText.setText(worker.getRole());
            Glide.with(getContext()).load(worker.getPhotoUrl()).into(photoImageView);
        }

        photoImageView.setOnClickListener(v -> {
            // Handle photo selection
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, 100);
        });

        saveButton.setOnClickListener(v -> {
            String name = nameEditText.getText().toString().trim();
            String mobile = mobileEditText.getText().toString().trim();
            String role = roleEditText.getText().toString().trim();

            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(mobile) || TextUtils.isEmpty(role)) {
                Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (photoUri == null && !isEditMode) {
                Toast.makeText(getContext(), "Please select a photo", Toast.LENGTH_SHORT).show();
                return;
            }

            if (isEditMode) {
                worker.setName(name);
                worker.setMobile(mobile);
                worker.setRole(role);
                firebaseHelper.updateWorker(worker, photoUri, task -> {
                    if (task.isSuccessful()) {
                        dismiss();
                    } else {
                        Toast.makeText(getContext(), "Failed to update worker", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                String id = UUID.randomUUID().toString();
                Worker newWorker = new Worker(id, name, null, mobile, role);
                firebaseHelper.addWorker(newWorker, photoUri, task -> {
                    if (task.isSuccessful()) {
                        dismiss();
                    } else {
                        Toast.makeText(getContext(), "Failed to add worker", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == Activity.RESULT_OK && data != null) {
            photoUri = data.getData();
            photoImageView.setImageURI(photoUri);
        }
    }

    public void setWorker(Worker worker) {
        this.worker = worker;
    }
}
