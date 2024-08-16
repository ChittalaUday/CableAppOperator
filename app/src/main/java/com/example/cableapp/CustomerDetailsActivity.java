package com.example.cableapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class CustomerDetailsActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private CustomerAdapter customerAdapter;
    private List<Customer> customerList;
    private DatabaseReference databaseReference;
    private TextInputEditText searchField;
    private LinearLayout searchByName, searchBySerialNumber, searchByMobile, searchByAddress;
    private List<Customer> originalCustomerList;
    private ProgressBar progressBar;
    private TextView customerCount;
    private String currentSearchType = "Name";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_details);

        MaterialToolbar toolbar = findViewById(R.id.customer_details_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.bg_color));
        }


        recyclerView = findViewById(R.id.recycler_view);
        searchField = findViewById(R.id.search_field);
        searchByName = findViewById(R.id.search_by_name);
        searchBySerialNumber = findViewById(R.id.search_by_serial_number);
        searchByMobile = findViewById(R.id.search_by_mobile);
        searchByAddress = findViewById(R.id.search_by_address);
        progressBar = findViewById(R.id.customer_details_progress_bar);
        customerCount = findViewById(R.id.customer_count);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        customerList = new ArrayList<>();
        customerAdapter = new CustomerAdapter(this, customerList);
        recyclerView.setAdapter(customerAdapter);
        progressBar.setVisibility(View.VISIBLE);

        databaseReference = FirebaseDatabase.getInstance().getReference("user-base/sscn/mandapeta");
        fetchAllCustomers();

        searchField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchCustomers(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        searchByName.setOnClickListener(view -> setCurrentSearchType("Name"));
        searchBySerialNumber.setOnClickListener(view -> setCurrentSearchType("Serial Number"));
        searchByMobile.setOnClickListener(view -> setCurrentSearchType("Mobile"));
        searchByAddress.setOnClickListener(view -> setCurrentSearchType("Address"));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_filter, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else if (item.getItemId() == R.id.filter_active) {
            filterCustomersByStatus("Active");
        } else if (item.getItemId() == R.id.filter_deactive) {
            filterCustomersByStatus("Deactive");
        } else if (item.getItemId() == R.id.filter_expired) {
            filterCustomersByStatus("Expired");
        }
        return super.onOptionsItemSelected(item);
    }

    private void setCurrentSearchType(String searchType) {
        currentSearchType = searchType;
        searchField.setHint("Search by " + searchType);
    }

    private void filterCustomersByStatus(String status) {
        List<Customer> filteredList = new ArrayList<>();
        for (Customer customer : originalCustomerList) {
            if (customer.getStatus().equalsIgnoreCase(status)) {
                filteredList.add(customer);
            }
        }
        customerList.clear();
        customerList.addAll(filteredList);
        customerAdapter.notifyDataSetChanged();
        customerCount.setText("Customer Count: " + customerList.size());
    }

    private void fetchAllCustomers() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                customerList.clear();
                originalCustomerList = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Customer customer = snapshot.getValue(Customer.class);
                    if (customer != null) {
                        customerList.add(customer);
                        originalCustomerList.add(customer);
                    }
                }
                customerCount.setText("Customer Count: " + customerList.size());
                customerAdapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(CustomerDetailsActivity.this, "Failed to load data", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void searchCustomers(String query) {
        customerList.clear();
        for (Customer customer : originalCustomerList) {
            if (customerMatchesQuery(customer, query, currentSearchType)) {
                customerList.add(customer);
            }
        }
        customerCount.setText("Customer Count: " + customerList.size());
        customerAdapter.notifyDataSetChanged();
    }

    private boolean customerMatchesQuery(Customer customer, String query, String searchType) {
        switch (searchType) {
            case "Name":
                return customer.getCustomerName().toLowerCase().contains(query.toLowerCase());
            case "Serial Number":
                return customer.getStbSerialNo().toLowerCase().contains(query.toLowerCase());
            case "Mobile":
                return customer.getMobileNo().toLowerCase().contains(query.toLowerCase());
            case "Address":
                return customer.getAddress().toLowerCase().contains(query.toLowerCase());
            default:
                return false;
        }
    }
}
