package com.example.cableapp;

import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.eazegraph.lib.charts.PieChart;
import org.eazegraph.lib.models.PieModel;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Environment;
import android.view.View;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.net.Uri;
import android.provider.MediaStore;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import com.opencsv.CSVWriter;
import java.io.OutputStream;
import java.util.Objects;

public class CustomerStatusReportActivity extends AppCompatActivity {

    TextView expired, active, deactive, total, others;
    Button downloadButton;
    private DatabaseReference databaseReference;
    private List<Customer> customerList = new ArrayList<>();
    PieChart pieChart;
    private static final int REQUEST_CODE_POST_NOTIFICATIONS = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_status_report);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.bg_color));
        }

        // Create notification channel
        createNotificationChannel();

        // Initialize views and set data
        databaseReference = FirebaseDatabase.getInstance().getReference();
        expired = findViewById(R.id.tvR);
        active = findViewById(R.id.tvPython);
        deactive = findViewById(R.id.tvCPP);
        total = findViewById(R.id.cutomer_total);
        others = findViewById(R.id.tvJava);
        pieChart = findViewById(R.id.piechart);

        downloadButton = findViewById(R.id.btn_download);

        setData();

        downloadButton.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                exportToCSVUsingMediaStore();
            } else {
                exportToCSV();
            }
        });
    }

    private void setData() {
        databaseReference.child("user-base/sscn/mandapeta").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int activeCount = 0;
                int expireCount = 0;
                int deactiveCount = 0;
                int othersCount = 0;
                int totalCount = 0;
                customerList.clear(); // Clear previous data

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    if (dataSnapshot.exists()) {
                        totalCount++;
                        Customer customer = dataSnapshot.getValue(Customer.class);
                        customerList.add(customer);
                        switch (Objects.requireNonNull(customer).getStatus().toLowerCase()) {
                            case "active":
                                activeCount++;
                                break;
                            case "deactive":
                                deactiveCount++;
                                break;
                            case "expired":
                                expireCount++;
                                break;
                            default:
                                othersCount++;
                                break;
                        }
                    }
                }
                total.setText(String.valueOf(totalCount));
                active.setText(String.valueOf(activeCount));
                deactive.setText(String.valueOf(deactiveCount));
                expired.setText(String.valueOf(expireCount));
                others.setText(String.valueOf(othersCount));
                getPieChart();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CustomerStatusReportActivity.this, "Failed to load data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void exportToCSVUsingMediaStore() {
        String fileName = "customer_data_report.csv";
        ContentValues values = new ContentValues();
        values.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
        values.put(MediaStore.Downloads.MIME_TYPE, "text/csv");
        values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

        Uri uri = getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);

        if (uri != null) {
            try (OutputStream outputStream = getContentResolver().openOutputStream(uri);
                 CSVWriter writer = new CSVWriter(new OutputStreamWriter(outputStream))) {
                // Write header
                String[] header = {"SerialNo", "CustomerId", "CustomerName", "ApartmentNo", "DoorNo", "Address", "Gmail", "MobileNo", "StbSerialNo", "ActivationDate", "ExpiryDate", "PackageId", "Status", "Due", "CreatedNo", "BoxType"};
                writer.writeNext(header);

                // Write data
                for (Customer customer : customerList) {
                    String[] data = {
                            customer.getSerialNo(),
                            customer.getCustomerId(),
                            customer.getCustomerName(),
                            customer.getApartmentNo(),
                            customer.getDoorNo(),
                            customer.getAddress(),
                            customer.getGmail(),
                            customer.getMobileNo(),
                            customer.getStbSerialNo(),
                            customer.getActivationNo(),
                            customer.getExpiryNo(),
                            customer.getPackageId(),
                            customer.getStatus(),
                            String.valueOf(customer.getDue()),
                            customer.getCreatedNo(),
                            customer.getBoxType()
                    };
                    writer.writeNext(data);
                }

                writer.flush();

                // Notify user about the download
                notifyUser("CSV Exported", "CSV file has been saved to Downloads.");
            } catch (IOException e) {
                e.printStackTrace();
                notifyUser("Export Failed", "Failed to export CSV file.");
            }
        } else {
            notifyUser("Export Failed", "Failed to create CSV file.");
        }
    }

    private void exportToCSV() {
        // Method for handling devices below Android 10
        String fileName = "customer_data_report.csv";
        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File file = new File(downloadsDir, fileName);

        // Prepare CSV content using OpenCSV
        try (CSVWriter writer = new CSVWriter(new FileWriter(file))) {
            // Write header
            String[] header = {"SerialNo", "CustomerId", "CustomerName", "ApartmentNo", "DoorNo", "Address", "Gmail", "MobileNo", "StbSerialNo", "ActivationDate", "ExpiryDate", "PackageId", "Status", "Due", "CreatedNo", "BoxType"};
            writer.writeNext(header);

            // Write data
            for (Customer customer : customerList) {
                String[] data = {
                        customer.getSerialNo(),
                        customer.getCustomerId(),
                        customer.getCustomerName(),
                        customer.getApartmentNo(),
                        customer.getDoorNo(),
                        customer.getAddress(),
                        customer.getGmail(),
                        customer.getMobileNo(),
                        customer.getStbSerialNo(),
                        customer.getActivationNo(),
                        customer.getExpiryNo(),
                        customer.getPackageId(),
                        customer.getStatus(),
                        String.valueOf(customer.getDue()),
                        customer.getCreatedNo(),
                        customer.getBoxType()
                };
                writer.writeNext(data);
            }

            // Notify user about the download
            notifyUser("CSV Exported", "CSV file has been saved to Downloads.");
        } catch (IOException e) {
            e.printStackTrace();
            notifyUser("Export Failed", "Failed to export CSV file.");
        }
    }

    private void notifyUser(String title, String message) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "YOUR_CHANNEL_ID")
                .setSmallIcon(R.drawable.ic_download)  // Change to your notification icon
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            // Request permission
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                    REQUEST_CODE_POST_NOTIFICATIONS);
        }
        notificationManager.notify(1, builder.build());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Export Channel";
            String description = "Channel for CSV export notifications";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("YOUR_CHANNEL_ID", name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void getPieChart(){


        // Set the data and color to the pie chart
        pieChart.addPieSlice(
                new PieModel(
                        "Expired",
                        Integer.parseInt(expired.getText().toString()),
                        Color.parseColor("#FFA726")));
        pieChart.addPieSlice(
                new PieModel(
                        "Active",
                        Integer.parseInt(active.getText().toString()),
                        Color.parseColor("#66BB6A")));
        pieChart.addPieSlice(
                new PieModel(
                        "Deactive",
                        Integer.parseInt(deactive.getText().toString()),
                        Color.parseColor("#EF5350")));
        pieChart.addPieSlice(
                new PieModel(
                        "Others",
                        Integer.parseInt(others.getText().toString()),
                        Color.parseColor("#29B6F6")));

        pieChart.startAnimation();
    }
}
