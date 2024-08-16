package com.example.cableapp;

import android.app.DatePickerDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.FileProvider;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.app.DatePickerDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.opencsv.CSVWriter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;


public class PaymentDownloadActivity extends AppCompatActivity {

    private DatePickerDialog datePickerStartDialog, datePickerEndDialog;
    private Button btnSelectStartDate, btnSelectEndDate, downloadButton;
    private TextView notification;
    private Calendar startDate, endDate;
    private List<Payment> paymentList = new ArrayList<>();
    private DatabaseReference databaseReference;
    private static final int REQUEST_CODE_POST_NOTIFICATIONS = 100;
    private static final String CHANNEL_ID = "EXPORT_CHANNEL_ID";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_download);

        // Initialize views
        btnSelectStartDate = findViewById(R.id.btnSelectStartDate);
        btnSelectEndDate = findViewById(R.id.btnSelectEndDate);
        downloadButton = findViewById(R.id.btn_download);
        notification = findViewById(R.id.tvNotification);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.bg_color));
        }


        databaseReference = FirebaseDatabase.getInstance().getReference();

        createNotificationChannel();

        btnSelectStartDate.setOnClickListener(v -> showDatePickerDialog(true));
        btnSelectEndDate.setOnClickListener(v -> showDatePickerDialog(false));
        downloadButton.setOnClickListener(v -> {
            if (startDate != null && endDate != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    fetchAndExportPaymentsUsingMediaStore(startDate, endDate);
                } else {
                    fetchAndExportPayments(startDate, endDate);
                }
            } else {
                Toast.makeText(this, "Please select both start and end dates", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDatePickerDialog(boolean isStartDate) {
        final Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            Calendar selectedDate = Calendar.getInstance();
            selectedDate.set(year, month, dayOfMonth);
            if (isStartDate) {
                startDate = selectedDate;
                btnSelectStartDate.setText(formatDate(selectedDate));
            } else {
                endDate = selectedDate;
                btnSelectEndDate.setText(formatDate(selectedDate));
            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        datePickerDialog.show();
    }

    private String formatDate(Calendar date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(date.getTime());
    }

    private void fetchAndExportPaymentsUsingMediaStore(Calendar startDate, Calendar endDate) {
        databaseReference.child("payments/mandapeta").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                paymentList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                        Payment payment = childSnapshot.getValue(Payment.class);
                        if (payment != null) {
                            Calendar paymentDate = Calendar.getInstance();
                            paymentDate.setTimeInMillis(payment.getTimestamp());

                            if (isDateInRange(paymentDate, startDate, endDate)) {
                                paymentList.add(payment);
                            }
                        }
                    }
                }
                exportToCSVUsingMediaStore();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(PaymentDownloadActivity.this, "Failed to load data", Toast.LENGTH_SHORT).show();
            }
        });
    }


    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void exportToCSVUsingMediaStore() {
        String fileName = "payment_data_report.csv";
        ContentValues values = new ContentValues();
        values.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
        values.put(MediaStore.Downloads.MIME_TYPE, "text/csv");
        values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

        Uri uri = getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);

        if (uri != null) {
            try (OutputStream outputStream = getContentResolver().openOutputStream(uri);
                 CSVWriter writer = new CSVWriter(new OutputStreamWriter(outputStream))) {
                // Write header
                String[] header = {"PaymentId", "SerialNo", "Status", "Timestamp", "PackageAmount", "PaidAmount"};
                writer.writeNext(header);

                // Write data
                for (Payment payment : paymentList) {
                    Date date = new Date(payment.getTimestamp());

                    // Define the desired date format
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    String[] data = {
                            payment.getPaymentId(),
                            payment.getSerialNo(),
                            payment.getStatus(),
                            sdf.format(date),
                            String.valueOf(payment.getPackageAmount()),
                            String.valueOf(payment.getPaidAmount())
                    };
                    writer.writeNext(data);
                }

                writer.flush();
                notifyUser("CSV Exported", "CSV file has been saved to Downloads.");
            } catch (IOException e) {
                e.printStackTrace();
                notifyUser("Export Failed", "Failed to export CSV file.");
            }
        } else {
            notifyUser("Export Failed", "Failed to create CSV file.");
        }
    }


    private void fetchAndExportPayments(Calendar startDate, Calendar endDate) {
        databaseReference.child("payments/mandapeta").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                paymentList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                        Payment payment = childSnapshot.getValue(Payment.class);
                        if (payment != null) {
                            Calendar paymentDate = Calendar.getInstance();
                            paymentDate.setTimeInMillis(payment.getTimestamp());

                            if (isDateInRange(paymentDate, startDate, endDate)) {
                                paymentList.add(payment);
                            }
                        }
                    }
                }
                exportToCSV();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(PaymentDownloadActivity.this, "Failed to load data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean isDateInRange(Calendar date, Calendar startDate, Calendar endDate) {
        return !date.before(startDate) && !date.after(endDate);
    }

    private void exportToCSV() {
        String fileName = "payment_data_report.csv";
        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File file = new File(downloadsDir, fileName);

        try (CSVWriter writer = new CSVWriter(new FileWriter(file))) {
            // Write header
            String[] header = {"PaymentId", "SerialNo", "Status", "Timestamp", "PackageAmount", "PaidAmount"};
            writer.writeNext(header);

            // Write data
            for (Payment payment : paymentList) {
                String[] data = {
                        payment.getPaymentId(),
                        payment.getSerialNo(),
                        payment.getStatus(),
                        String.valueOf(payment.getTimestamp()),
                        String.valueOf(payment.getPackageAmount()),
                        String.valueOf(payment.getPaidAmount())
                };

                writer.writeNext(data);
            }

            writer.flush();
            notifyUser("CSV Exported", "CSV file has been saved to Downloads.");
        } catch (IOException e) {
            e.printStackTrace();
            notifyUser("Export Failed", "Failed to export CSV file.");
        }
    }

    private void notifyUser(String title, String message) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_download)
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
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
