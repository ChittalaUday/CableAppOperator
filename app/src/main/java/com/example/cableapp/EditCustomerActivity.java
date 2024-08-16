package com.example.cableapp;

import android.app.DatePickerDialog;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.DatePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class EditCustomerActivity extends AppCompatActivity {
    private TextInputEditText boxType, createdOn, cId, dNo, flatNo, mobileNo, name, serialNo, sNo;
    private TextInputLayout createdOnLayout;
    private Customer customer;
    private String customerKey, parentKey;
    private DatabaseReference databaseReference;
    private Calendar calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_customer);

        MaterialToolbar toolbar = findViewById(R.id.edit_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.bg_color));
        }


        boxType = findViewById(R.id.box_type);
        createdOn = findViewById(R.id.created_on);
        cId = findViewById(R.id.c_id);
        dNo = findViewById(R.id.d_no);
        flatNo = findViewById(R.id.flat_no);
        mobileNo = findViewById(R.id.mobile_no);
        name = findViewById(R.id.name);
        serialNo = findViewById(R.id.serial_no);
        sNo = findViewById(R.id.s_no);
        createdOnLayout = findViewById(R.id.created_on_layout);


        customer = (Customer) getIntent().getSerializableExtra("customer");

        if (customer != null) {
            boxType.setText(customer.getBoxType());
            createdOn.setText(customer.getCreatedNo());
            cId.setText(customer.getCustomerId());
            dNo.setText(customer.getDoorNo());
            flatNo.setText(customer.getApartmentNo());
            mobileNo.setText(customer.getMobileNo());
            name.setText(customer.getCustomerName());
            serialNo.setText(customer.getStbSerialNo());
            sNo.setText(customer.getSerialNo());
        }

        databaseReference = FirebaseDatabase.getInstance().getReference("user-base/sscn/mandapeta");

        // Set up the date picker for createdOn field
        calendar = Calendar.getInstance();
        createdOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(EditCustomerActivity.this, dateSetListener,
                        calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        findViewById(R.id.btn_save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveCustomerData();
            }
        });
    }

    private DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateCreatedOnField();
        }
    };

    private void updateCreatedOnField() {
        String myFormat = "dd-MM-yyyy"; // Define your format
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        createdOn.setText(sdf.format(calendar.getTime()));
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveCustomerData() {
        customer.setBoxType(boxType.getText().toString());
        customer.setCreatedNo(createdOn.getText().toString());
        customer.setCustomerId(cId.getText().toString());
        customer.setDoorNo(dNo.getText().toString());
        customer.setApartmentNo(flatNo.getText().toString());
        customer.setMobileNo(mobileNo.getText().toString());
        customer.setCustomerName(name.getText().toString());
        customer.setStbSerialNo(serialNo.getText().toString());
        customer.setSerialNo(sNo.getText().toString());

        // Update the customer data in Firebase
        databaseReference.child(customer.getSerialNo()).setValue(customer)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(EditCustomerActivity.this, "Customer updated successfully", Toast.LENGTH_SHORT).show();
                            finish(); // Close the activity after saving
                        } else {
                            Toast.makeText(EditCustomerActivity.this, "Failed to update customer", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
