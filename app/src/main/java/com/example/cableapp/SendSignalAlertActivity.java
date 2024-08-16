package com.example.cableapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import android.Manifest;
import android.util.Log;
import com.google.android.material.textfield.TextInputEditText;


public class SendSignalAlertActivity extends Activity {
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int PERMISSION_REQUEST_CODE = 100;

    private TextInputEditText titleEditText, descriptionEditText;
    private ImageView previewImage;
    private TextView previewTitle, previewDescription;
    private RadioGroup statusRadioGroup;
    private Button uploadImageButton, sendButton;
    private ProgressBar progressBar;

    private Uri imageUri;
    private FirebaseFirestore db;
    private FirebaseStorage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_signal_alert);

        // Initialize views
        titleEditText = findViewById(R.id.titleEditText);
        descriptionEditText = findViewById(R.id.descriptionEditText);
        previewImage = findViewById(R.id.previewImage);
        previewTitle = findViewById(R.id.previewTitle);
        previewDescription = findViewById(R.id.previewDescription);
        statusRadioGroup = findViewById(R.id.statusRadioGroup);
        uploadImageButton = findViewById(R.id.uploadImageButton);
        sendButton = findViewById(R.id.sendButton);
        progressBar = findViewById(R.id.progressBar);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.bg_color));
        }


        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        // Request necessary permissions if not already granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        }

        uploadImageButton.setOnClickListener(view -> openFileChooser());
        sendButton.setOnClickListener(view -> showConfirmationDialog());

        // Initially disable EditTexts
        titleEditText.setEnabled(false);
        descriptionEditText.setEnabled(false);

        // Listen for changes in the RadioGroup
        statusRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            updateRadioEditText();
            titleEditText.setEnabled(true);
            descriptionEditText.setEnabled(true);
        });

        // Listen for changes in text fields
        titleEditText.addTextChangedListener(createTextWatcher());
        descriptionEditText.addTextChangedListener(createTextWatcher());

        retrieveAndDisplayData();
    }

    private TextWatcher createTextWatcher() {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updatePreview();
            }

            @Override
            public void afterTextChanged(Editable s) { }
        };
    }

    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select an image"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("SendSignalAlertActivity", "onActivityResult called with requestCode: " + requestCode + ", resultCode: " + resultCode);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            previewImage.setImageURI(imageUri);
            previewImage.setVisibility(View.VISIBLE);
            Log.d("SendSignalAlertActivity", "Image selected: " + imageUri.toString());
        } else {
            previewImage.setVisibility(View.GONE);
        }

        updatePreview();
    }

    private void updateRadioEditText() {
        int checkedRadioButtonId = statusRadioGroup.getCheckedRadioButtonId();
        RadioButton statusRadioButton = findViewById(checkedRadioButtonId);

        String status = statusRadioButton != null ? statusRadioButton.getText().toString() : "No Status Selected";

        String title = titleEditText.getText().toString().trim();
        String description = descriptionEditText.getText().toString().trim();

        if (title.isEmpty() && description.isEmpty()) {
            if (status.equals("Disrupted")) {
                title = "Signal Alert";
                description = "Our services in your area are temporarily disrupted. We're actively resolving this and expect services to be back within 4 hours. Apologies for any inconvenience caused.";
            } else {
                title = "Signal Restored";
                description = "Our services in your area are restored. Thank you for your cooperation. Apologies for any inconvenience caused.";
            }
            titleEditText.setText(title);
            descriptionEditText.setText(description);
        }
        updatePreview();
    }

    private void updatePreview() {
        int checkedRadioButtonId = statusRadioGroup.getCheckedRadioButtonId();
        RadioButton statusRadioButton = findViewById(checkedRadioButtonId);

        String status = statusRadioButton != null ? statusRadioButton.getText().toString() : "No Status Selected";
        String title = titleEditText.getText().toString().trim();
        String description = descriptionEditText.getText().toString().trim();

        // Get the root layout
        View rootView = findViewById(R.id.signal_preview_layout);

        Drawable backgroundDrawable;
        if (status.equals("Disrupted")) {
            backgroundDrawable = ContextCompat.getDrawable(this, R.drawable.alert_card);
            if (title.isEmpty() && description.isEmpty()) {
                title = "Signal Alert";
                description = "Our services in your area are temporarily disrupted. We're actively resolving this and expect services to be back within 4 hours. Apologies for any inconvenience caused.";
            }
        } else if (status.equals("Restored")) {
            backgroundDrawable = ContextCompat.getDrawable(this, R.drawable.alert_live_card);
            if (title.isEmpty() && description.isEmpty()) {
                title = "Signal Restored";
                description = "Our services in your area are restored. Thank you for your cooperation. Apologies for any inconvenience caused.";
            }
        } else {
            backgroundDrawable = ContextCompat.getDrawable(this, R.drawable.alert_card);
        }

        // Apply background drawable
        if (backgroundDrawable != null) {
            rootView.setBackground(backgroundDrawable);
        }

        previewTitle.setText(title);
        previewDescription.setText(description);

        // Update image view visibility
        if (imageUri != null) {
            previewImage.setVisibility(View.VISIBLE);
            previewImage.setImageURI(imageUri);
        } else {
            previewImage.setVisibility(View.GONE);
        }
    }

    private void showConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Confirm")
                .setMessage("Are you sure you want to send this signal alert?")
                .setPositiveButton("Yes", (dialog, which) -> sendSignalAlert())
                .setNegativeButton("No", null)
                .show();
    }

    private void sendSignalAlert() {
        // Check if a status is selected
        if (!validateInput()) {
            Toast.makeText(this, "Please select a status before sending.", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        if (imageUri != null) {
            StorageReference fileReference = storage.getReference().child("uploads/" + System.currentTimeMillis() + ".jpg");
            fileReference.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageUrl = uri.toString();
                        Log.d("SendSignalAlertActivity", "Image URL: " + imageUrl);
                        saveAlertToDatabase(imageUrl);
                    }))
                    .addOnFailureListener(e -> {
                        Log.e("SendSignalAlertActivity", "Error Uploading Image", e);
                        handleFailure(e, "Error Uploading Image");
                    });
        } else {
            saveAlertToDatabase(null);
        }
    }

    private void saveAlertToDatabase(String imageUrl) {
        String title = titleEditText.getText().toString().trim();
        String description = descriptionEditText.getText().toString().trim();
        int checkedRadioButtonId = statusRadioGroup.getCheckedRadioButtonId();
        String status = checkedRadioButtonId != -1 ? ((RadioButton) findViewById(checkedRadioButtonId)).getText().toString() : "Unknown";

        Map<String, Object> alert = new HashMap<>();
        alert.put("title", title.isEmpty() ? (status.equals("Disrupted") ? "Signal Alert" : "Signal Restored") : title);
        alert.put("description", description.isEmpty() ? (status.equals("Disrupted") ? "Our services in your area are temporarily disrupted. We're actively resolving this and expect services to be back within 4 hours. Apologies for any inconvenience caused." : "Our services in your area are restored. Thank you for your cooperation. Apologies for any inconvenience caused.") : description);
        alert.put("status", status);
        alert.put("timestamp", FieldValue.serverTimestamp());
        if (imageUrl != null) {
            alert.put("imageUrl", imageUrl);
        }

        db.collection("signalAlerts").document("currentAlert")
                .set(alert)
                .addOnSuccessListener(documentReference -> {
                    progressBar.setVisibility(View.GONE);
                    retrieveAndDisplayData();
                    Toast.makeText(SendSignalAlertActivity.this, "Signal Alert Sent", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e("SendSignalAlertActivity", "Error Sending Alert", e);
                    handleFailure(e, "Error Sending Alert");
                    Toast.makeText(SendSignalAlertActivity.this, "Error occurred: " + e, Toast.LENGTH_SHORT).show();
                });
    }

    private void retrieveAndDisplayData() {
        db.collection("signalAlerts").document("currentAlert").get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String title = documentSnapshot.getString("title");
                        String description = documentSnapshot.getString("description");
                        String imageUrl = documentSnapshot.getString("imageUrl");
                        String status = documentSnapshot.getString("status");

                        // Update RadioGroup
                        RadioButton selectedRadioButton = findViewById(
                                "Disrupted".equals(status) ? R.id.disruptedRadioButton : R.id.restoredRadioButton);
                        if (selectedRadioButton != null) {
                            selectedRadioButton.setChecked(true);
                        }

                        // Update EditTexts
                        titleEditText.setText(title);
                        descriptionEditText.setText(description);

                        // Update Preview
                        updatePreview();

                        // Load image if available
                        if (imageUrl != null) {
                            Glide.with(this).load(imageUrl).into(previewImage);
                            previewImage.setVisibility(View.VISIBLE);
                            Log.d("SendSignalAlertActivity", "Image URL loaded: " + imageUrl);
                        } else {
                            previewImage.setVisibility(View.GONE);
                        }
                    } else {
                        // Handle case where no data exists
                        previewTitle.setText("No Data");
                        previewDescription.setText("");
                        previewImage.setImageDrawable(null);
                        previewImage.setVisibility(View.GONE);
                        findViewById(R.id.signal_preview_layout).setBackgroundResource(R.drawable.alert_card);
                    }
                    progressBar.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> {
                    Log.e("SendSignalAlertActivity", "Error Retrieving Data", e);
                    handleFailure(e, "Error Retrieving Data");
                });
    }

    private boolean validateInput() {
        if (statusRadioGroup.getCheckedRadioButtonId() == -1) {
            Toast.makeText(this, "Please select a status", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void handleFailure(Exception e, String message) {
        progressBar.setVisibility(View.GONE);
        Toast.makeText(SendSignalAlertActivity.this, message + ": " + e.getMessage(), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("SendSignalAlertActivity", "Permission granted");
            } else {
                Log.d("SendSignalAlertActivity", "Permission denied");
            }
        }
    }
}
