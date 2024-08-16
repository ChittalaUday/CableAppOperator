package com.example.cableapp;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ComplaintActivity extends AppCompatActivity {
    private ComplaintHistoryAdapter complaintHistoryAdapter;
    private List<Complaint> complaintList;
    private TextView noComplaintText;
    private Button datePickerButton;
    private Calendar selectedDate;
    private String selectedFilter = "All";

    private Map<String, Integer> filterCategories;
    private TextView allCategoryText;
    private TextView pendingCategoryText;
    private TextView solvedCategoryText;
    private TextView inReviewCategoryText;
    private TextView inReviewPendingCountText;
    private MaterialToolbar complaintToolbar;
    private ProgressBar progressBar;
    private ImageButton btnRefresh;
    private ConstraintLayout totalComplaintLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complaint);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        noComplaintText = findViewById(R.id.complaint_info_text);
        datePickerButton = findViewById(R.id.date_picker_button);

        allCategoryText = findViewById(R.id.all_count_text);
        pendingCategoryText = findViewById(R.id.pending_count_text);
        solvedCategoryText = findViewById(R.id.solved_count_text);
        inReviewCategoryText = findViewById(R.id.in_review_count_text);
        inReviewPendingCountText = findViewById(R.id.unsolved_count_text);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.bg_color));
        }

        complaintToolbar = findViewById(R.id.complaint_toolbar);
        progressBar = findViewById(R.id.progressBar);
        btnRefresh = findViewById(R.id.btnRefresh);
        totalComplaintLayout = findViewById(R.id.total_unsolved);

        complaintList = new ArrayList<>();
        complaintHistoryAdapter = new ComplaintHistoryAdapter(complaintList);
        recyclerView.setAdapter(complaintHistoryAdapter);

        selectedDate = Calendar.getInstance();
        updateDatePickerButtonText();
        progressBar.setVisibility(View.VISIBLE);
        totalComplaintLayout.setOnClickListener(view -> {
            Intent intent = new Intent(ComplaintActivity.this, UnsolvedComplaintActivity.class);
            startActivity(intent);            });

        setSupportActionBar(complaintToolbar);

        // Enable the Up button
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        btnRefresh.setOnClickListener(v -> {
            // Perform refresh action
            loadComplaints(); // Example method for loading complaints
            progressBar.setVisibility(View.VISIBLE);
        });

        datePickerButton.setOnClickListener(v -> showDatePickerDialog());

        // Initialize filter categories
        filterCategories = new HashMap<>();
        filterCategories.put("All", 0); // Initial count for "All" is set to 0
        filterCategories.put("Pending", 0);
        filterCategories.put("Solved", 0);
        filterCategories.put("In Review", 0);

        loadComplaints();
        setCategoryBackground(allCategoryText, R.drawable.filter_background_all_selected);

        allCategoryText.setOnClickListener(v -> updateFilter("All", allCategoryText, R.drawable.filter_background_all_selected));
        pendingCategoryText.setOnClickListener(v -> updateFilter("Pending", pendingCategoryText, R.drawable.filter_background_pending_selected));
        solvedCategoryText.setOnClickListener(v -> updateFilter("Solved", solvedCategoryText, R.drawable.filter_background_solved_selected));
        inReviewCategoryText.setOnClickListener(v -> updateFilter("In Review", inReviewCategoryText, R.drawable.filter_background_in_review_selected));
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
        loadComplaints();
    }
    private void updateFilter(String filter, TextView selectedText, int selectedDrawable) {
        if (!selectedFilter.equals(filter)) {
            selectedFilter = filter;
            setCategoryBackground(allCategoryText, R.drawable.filter_background_all);
            setCategoryBackground(pendingCategoryText, R.drawable.filter_background_pending);
            setCategoryBackground(solvedCategoryText, R.drawable.filter_background_solved);
            setCategoryBackground(inReviewCategoryText, R.drawable.filter_background_in_review);
            setCategoryBackground(selectedText, selectedDrawable);
            filterComplaints();
        }
    }

    private void setCategoryBackground(TextView textView, int drawableRes) {
        textView.setBackground(ResourcesCompat.getDrawable(getResources(), drawableRes, null));
    }

    private void showDatePickerDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    selectedDate.set(year, month, dayOfMonth);
                    updateDatePickerButtonText();
                    loadComplaints();
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

    private void loadComplaints() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("complaints/mandapeta");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                complaintList.clear();
                resetFilterCategories();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                        Complaint complaint = snapshot1.getValue(Complaint.class);
                        if (complaint != null && isSameDate(complaint.getTimestamp(), selectedDate.getTimeInMillis())) {
                            complaintList.add(complaint);
                            updateFilterCategoryCount(complaint.getStatus());
                        }
                    }
                }
                sortComplaintDescending();
                filterComplaints(); // Filter complaints after loading
                updateFilterCategoryUI();
                progressBar.setVisibility(View.GONE);
                noComplaintText.setVisibility(complaintList.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ComplaintActivity.this, "Failed to load complaints", Toast.LENGTH_SHORT).show();
            }
        });

        countAllComplaints();
    }
    private void countAllComplaints() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("complaints/mandapeta");

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int inReviewCount = 0;
                int pendingCount = 0;

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                        Complaint complaint = snapshot1.getValue(Complaint.class);
                        if (complaint != null) {
                            if (complaint.getStatus().equalsIgnoreCase("In Review")) {
                                inReviewCount++;
                            } else if (complaint.getStatus().equalsIgnoreCase("Pending")) {
                                pendingCount++;
                            }
                        }
                    }
                }
                updateInReviewPendingCount(inReviewCount, pendingCount);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ComplaintActivity.this, "Failed to count complaints", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateInReviewPendingCount(int inReviewCount, int pendingCount) {
        inReviewPendingCountText.setText(String.valueOf (inReviewCount + pendingCount));
    }

    private void resetFilterCategories() {
        filterCategories.put("Pending", 0);
        filterCategories.put("Solved", 0);
        filterCategories.put("In Review", 0);
        filterCategories.put("All", 0);
    }

    private void updateFilterCategoryCount(String status) {
        filterCategories.put(status, filterCategories.get(status) + 1);
        filterCategories.put("All", filterCategories.get("All") + 1);
    }

    private void updateFilterCategoryUI() {
        allCategoryText.setText("All: "+String.valueOf(filterCategories.get("All")));
        pendingCategoryText.setText("Pending: "+String.valueOf(filterCategories.get("Pending")));
        solvedCategoryText.setText("Solved: "+String.valueOf(filterCategories.get("Solved")));
        inReviewCategoryText.setText("In Review: "+String.valueOf(filterCategories.get("In Review")));
    }

    private boolean isSameDate(long timestamp1, long timestamp2) {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTimeInMillis(timestamp1);
        cal2.setTimeInMillis(timestamp2);
        return isSameDate(cal1, cal2);
    }

    private boolean isSameDate(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
                cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH);
    }

    private void sortComplaintDescending() {
        Collections.sort(complaintList, (complaint1, complaint2) ->
                Long.compare(complaint2.getTimestamp(), complaint1.getTimestamp())
        );
    }

    private void filterComplaints() {
        List<Complaint> filteredList = new ArrayList<>();
        if (selectedFilter.equals("All")) {
            filteredList.addAll(complaintList);
        } else {
            for (Complaint complaint : complaintList) {
                if (complaint.getStatus().equalsIgnoreCase(selectedFilter)) {
                    filteredList.add(complaint);
                }
            }
        }
        complaintHistoryAdapter.updateComplaintList(filteredList);
        noComplaintText.setVisibility(filteredList.isEmpty() ? View.VISIBLE : View.GONE);
    }
}
