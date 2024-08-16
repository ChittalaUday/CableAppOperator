package com.example.cableapp;

import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.eazegraph.lib.charts.PieChart;
import org.eazegraph.lib.models.PieModel;

public class UnsolvedComplaintActivity extends AppCompatActivity {
    private MaterialToolbar toolbar;
    private RecyclerView recyclerView;
    private TextView noComplaintsText;
    private ComplaintHistoryAdapter complaintHistoryAdapter;
    private List<Complaint> complaintList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unsolved_complaint);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.bg_color));
        }

        toolbar = findViewById(R.id.un_solved_complaint_toolbar);
        recyclerView = findViewById(R.id.unsolved_recycler);
        noComplaintsText = findViewById(R.id.unsolved_complaints_text);

        complaintList = new ArrayList<>();
        complaintHistoryAdapter = new ComplaintHistoryAdapter(complaintList);
        recyclerView.setAdapter(complaintHistoryAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("complaints/mandapeta");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                complaintList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    for (DataSnapshot snapshot1 : dataSnapshot.getChildren()) {
                        if (snapshot1.exists()) {
                            Complaint complaint = snapshot1.getValue(Complaint.class);
                            if (complaint != null && !complaint.getStatus().equalsIgnoreCase("solved")) {
                                complaintList.add(complaint);
                            }
                        }
                    }
                }
                sortComplaintDescending();
                complaintHistoryAdapter.notifyDataSetChanged();
                noComplaintsText.setVisibility(complaintList.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });
    }

    private void sortComplaintDescending() {
        Collections.sort(complaintList, (complaint1, complaint2) ->
                Long.compare(complaint2.getTimestamp(), complaint1.getTimestamp())
        );
    }
}
