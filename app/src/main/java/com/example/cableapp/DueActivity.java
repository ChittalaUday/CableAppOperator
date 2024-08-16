package com.example.cableapp;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class DueActivity extends AppCompatActivity {
    private List<Due> dueList;
    private TextView name, serialNo, boxType, createdOn, cId, flatNo, dNo, mobileNo, oldDue;
    private TextInputEditText dueAmount, dueRemark;
    private String key, parentKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_due);

        MaterialToolbar toolbar = findViewById(R.id.due_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.bg_color));
        }


        oldDue = findViewById(R.id.old_due);
        name = findViewById(R.id.name);
        serialNo = findViewById(R.id.serial_no);
        boxType = findViewById(R.id.box_type);
        createdOn = findViewById(R.id.created_on);
        cId = findViewById(R.id.c_id);
        flatNo = findViewById(R.id.flat_no);
        dNo = findViewById(R.id.d_no);
        mobileNo = findViewById(R.id.mobile_no);

        dueAmount = findViewById(R.id.due_amount);
        dueRemark = findViewById(R.id.due_remark_amount);

        RecyclerView recyclerView = findViewById(R.id.due_history_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        dueList = new ArrayList<>();
        DueHistoryAdapter adapter = new DueHistoryAdapter(dueList);
        recyclerView.setAdapter(adapter);

        Intent intent = getIntent();
        Customer customer = (Customer) intent.getSerializableExtra("customer");
        key = intent.getStringExtra("key");
        parentKey = intent.getStringExtra("parentKey");

        if (customer != null) {
            name.setText(customer.getCustomerName());
            serialNo.setText(customer.getStbSerialNo());
            boxType.setText(customer.getBoxType());
            createdOn.setText(customer.getCreatedNo());
            cId.setText(customer.getCustomerId());
            dNo.setText(customer.getDoorNo());
            mobileNo.setText(customer.getMobileNo());
        }

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("dues/mandapeta").child(parentKey).child(key);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                dueList.clear();
                double totalDueAmount = 0.0; // Variable to hold total due amount
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    if (snapshot.exists()){
                        Due due = snapshot.getValue(Due.class);
                        if (due != null) {
                            dueList.add(due);
                            if (!due.isPaid()){
                                totalDueAmount += due.getAmount();
                            }
                        }
                    }
                }
                adapter.notifyDataSetChanged();
                // Set the total due amount to the oldDue TextView
                oldDue.setText(String.valueOf(totalDueAmount));
                if (dueList.isEmpty()) {
                    // Show a message indicating no data
                    Toast.makeText(DueActivity.this, "No due data available", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Show error message if data retrieval is cancelled
                Toast.makeText(DueActivity.this, "Failed to retrieve due data", Toast.LENGTH_SHORT).show();
            }
        });

        MaterialButton saveButton = findViewById(R.id.save_button);
        saveButton.setOnClickListener(v -> saveDueData());
    }

    private void saveDueData() {
        String amount = dueAmount.getText().toString().trim();
        String remark = dueRemark.getText().toString().trim();

        if (amount.isEmpty()) {
            Toast.makeText(this, "Please enter the due amount", Toast.LENGTH_SHORT).show();
            return;
        }

        if (remark.isEmpty()){
            remark = "None";
        }
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("dues/mandapeta").child(parentKey).child(key);

        String dueId = databaseReference.push().getKey();
        if (dueId != null) {
            Due due = new Due(Double.parseDouble(amount), remark, false, ServerValue.TIMESTAMP,0);

            databaseReference.child(dueId).setValue(due)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(DueActivity.this, "Due amount saved", Toast.LENGTH_SHORT).show();

                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(DueActivity.this, "Failed to save due amount", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
