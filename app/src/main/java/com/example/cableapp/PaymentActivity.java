package com.example.cableapp;


import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class PaymentActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private PaymentHistoryAdapter paymentHistoryAdapter;
    private List<Payment> paymentList;
    private TextView noPaymentText;
    private Button datePickerButton;
    private Calendar selectedDate;
    private TextView sortAllText;
    private TextView sortDueText;
    private TextView sortPaidText, paidAmountText, dueAmountText;
    private MaterialToolbar toolbar;
    private ProgressBar progressBar;
    private ImageButton btnRefresh;

    private String selectedFilter = "All";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        noPaymentText = findViewById(R.id.payment_info_text);
        datePickerButton = findViewById(R.id.date_picker_button);

        sortAllText = findViewById(R.id.all_payments);
        sortDueText = findViewById(R.id.due_payments);
        sortPaidText = findViewById(R.id.paid_payments);
        toolbar = findViewById(R.id.payments_toolbar);
        progressBar = findViewById(R.id.payments_progressbar);

        paidAmountText = findViewById(R.id.paid_amount_text);
        dueAmountText = findViewById(R.id.dues_amount_text);

        btnRefresh = findViewById(R.id.btn_payments_refresh);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.bg_color));
        }


        paymentList = new ArrayList<>();
        paymentHistoryAdapter = new PaymentHistoryAdapter(paymentList);
        recyclerView.setAdapter(paymentHistoryAdapter);

        selectedDate = Calendar.getInstance();
        updateDatePickerButtonText();

        datePickerButton.setOnClickListener(v -> showDatePickerDialog());

        sortAllText.setOnClickListener(v -> updateFilter("All", sortAllText, R.drawable.filter_background_all_selected));
        sortDueText.setOnClickListener(v -> updateFilter("Due", sortDueText, R.drawable.filter_background_in_review_selected));
        sortPaidText.setOnClickListener(v -> updateFilter("Paid", sortPaidText, R.drawable.filter_background_solved_selected));

        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadPayments();
                progressBar.setVisibility(View.VISIBLE);
                noPaymentText.setVisibility(View.GONE);
            }
        });
        setSupportActionBar(toolbar);
        progressBar.setVisibility(View.VISIBLE);

        // Enable the Up button
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        loadPayments();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // Handle the back button press
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onRefreshClicked(View view) {
        // Perform refresh action when refresh button is clicked
        progressBar.setVisibility(View.VISIBLE);
        loadPayments();
        noPaymentText.setText(View.GONE);
    }

    private void showDatePickerDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    selectedDate.set(year, month, dayOfMonth);
                    updateDatePickerButtonText();
                    loadPayments();
                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void updateDatePickerButtonText() {
        Calendar today = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());

        if (isSameDate(selectedDate, today)) {
            datePickerButton.setText("Today");
        } else {
            today.add(Calendar.DAY_OF_YEAR, -1);
            if (isSameDate(selectedDate, today)) {
                datePickerButton.setText("Yesterday");
            } else {
                datePickerButton.setText(dateFormat.format(selectedDate.getTime()));
            }
        }
    }

    private void loadPayments() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("payments/mandapeta");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                paymentList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                        Payment payment = snapshot1.getValue(Payment.class);
                        if (payment != null && isSameDate(payment.getTimestamp(), selectedDate.getTimeInMillis())) {
                            paymentList.add(payment);
                        }
                    }
                }
                filterPayments(selectedFilter); // Use the selected filter
                updateAmount();
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(PaymentActivity.this, "Failed to load payments", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean isSameDate(long timestamp1, long timestamp2) {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTimeInMillis(timestamp1);
        cal2.setTimeInMillis(timestamp2);
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
                cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH);
    }

    private boolean isSameDate(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
                cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH);
    }

    private void filterPayments(String filter) {
        List<Payment> filteredList = new ArrayList<>();
        int allCount = 0;
        int dueCount = 0;
        int paidCount = 0;

        for (Payment payment : paymentList) {
            allCount++;
            if (payment.getStatus().equalsIgnoreCase("Due")) {
                dueCount++;
            } else if (payment.getStatus().equalsIgnoreCase("Paid")) {
                paidCount++;
            }

            if (filter.equals("All") || payment.getStatus().equalsIgnoreCase(filter)) {
                filteredList.add(payment);
            }
        }

        paymentHistoryAdapter.updatePaymentList(filteredList);
        noPaymentText.setVisibility(filteredList.isEmpty() ? View.VISIBLE : View.GONE);

        // Update sorting button texts with counts
        sortAllText.setText("All: " + allCount);
        sortDueText.setText("Due: " + dueCount);
        sortPaidText.setText("Paid: " + paidCount);

    }

    private void updateAmount() {
        int paidAmount = 0;
        int dueAmount = 0;
        for (Payment payment : paymentList) {
            paidAmount += payment.getPaidAmount();
            if (payment.getStatus().equalsIgnoreCase("due")) {
                dueAmount += payment.getPackageAmount();
            }
        }

        paidAmountText.setText("" + paidAmount);
        dueAmountText.setText("" + dueAmount);
    }


    private void updateFilter(String filter, TextView selectedText, int selectedDrawable) {
        if (!selectedFilter.equals(filter)) {
            selectedFilter = filter;
            setCategoryBackground(sortAllText, R.drawable.filter_background_all);
            setCategoryBackground(sortDueText, R.drawable.filter_background_in_review);
            setCategoryBackground(sortPaidText, R.drawable.filter_background_solved);
            setCategoryBackground(selectedText, selectedDrawable);
            filterPayments(filter);
        }
    }

    private void setCategoryBackground(TextView textView, int drawable) {
        textView.setBackgroundResource(drawable);
    }
}
