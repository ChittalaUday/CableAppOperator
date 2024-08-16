package com.example.cableapp;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;


public class PlansActivity extends AppCompatActivity {
    private PlanRepository planRepository = new PlanRepository();
    private PlanAdapter cableTVAdapter, internetAdapter, apFiberAdapter;
    private List<Plan> cableTVPlans = new ArrayList<>();
    private List<Plan> internetPlans = new ArrayList<>();
    private List<Plan> apFiberPlans = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plans);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.bg_color));
        }

        setupRecyclerViews();
        loadPlans();

        FloatingActionButton addButton = findViewById(R.id.addButton);
        addButton.setOnClickListener(view -> showAddPlanDialog());
    }

    private void setupRecyclerViews() {
        RecyclerView cableTVRecyclerView = findViewById(R.id.cableTVRecyclerView);
        RecyclerView internetRecyclerView = findViewById(R.id.internetRecyclerView);
        RecyclerView apFiberRecyclerView = findViewById(R.id.apFiberRecyclerView);

        cableTVAdapter = new PlanAdapter(cableTVPlans, new PlanAdapter.OnItemClickListener() {
            @Override
            public void onEditClick(Plan plan) {
                showEditPlanDialog(plan);
            }

            @Override
            public void onDeleteClick(Plan plan) {
                deletePlan(plan);
            }
        });

        internetAdapter = new PlanAdapter(internetPlans, new PlanAdapter.OnItemClickListener() {
            @Override
            public void onEditClick(Plan plan) {
                showEditPlanDialog(plan);
            }

            @Override
            public void onDeleteClick(Plan plan) {
                deletePlan(plan);
            }
        });

        apFiberAdapter = new PlanAdapter(apFiberPlans, new PlanAdapter.OnItemClickListener() {
            @Override
            public void onEditClick(Plan plan) {
                showEditPlanDialog(plan);
            }

            @Override
            public void onDeleteClick(Plan plan) {
                deletePlan(plan);
            }
        });

        cableTVRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        internetRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        apFiberRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        cableTVRecyclerView.setAdapter(cableTVAdapter);
        internetRecyclerView.setAdapter(internetAdapter);
        apFiberRecyclerView.setAdapter(apFiberAdapter);
    }

    private void loadPlans() {
        planRepository.getPlans("Cable TV", plans -> {
            updateCableTVPlans(plans);
            Log.d("PlansActivity", "Loaded Cable TV plans: " + plans);
        }, e -> {
            handleError(e);
            Log.e("PlansActivity", "Error loading Cable TV plans", e);
        });

        planRepository.getPlans("Internet", plans -> {
            updateInternetPlans(plans);
            Log.d("PlansActivity", "Loaded Internet plans: " + plans);
        }, e -> {
            handleError(e);
            Log.e("PlansActivity", "Error loading Internet plans", e);
        });

        planRepository.getPlans("ApFiber", plans -> {
            updateApFiberPlans(plans);
            Log.d("PlansActivity", "Loaded ApFiber plans: " + plans);
        }, e -> {
            handleError(e);
            Log.e("PlansActivity", "Error loading ApFiber plans", e);
        });
    }

    private void updateCableTVPlans(List<Plan> plans) {
        cableTVPlans.clear();
        cableTVPlans.addAll(plans);
        cableTVAdapter.notifyDataSetChanged();
    }

    private void updateInternetPlans(List<Plan> plans) {
        internetPlans.clear();
        internetPlans.addAll(plans);
        internetAdapter.notifyDataSetChanged();
    }

    private void updateApFiberPlans(List<Plan> plans) {
        apFiberPlans.clear();
        apFiberPlans.addAll(plans);
        apFiberAdapter.notifyDataSetChanged();
    }

    private void handleError(Exception e) {
        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
    }

    private void showAddPlanDialog() {
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_plan, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add New Plan")
                .setView(dialogView)
                .setPositiveButton("Add", (dialog, which) -> {
                    EditText nameEditText = dialogView.findViewById(R.id.planNameEditText);
                    EditText priceEditText = dialogView.findViewById(R.id.planPriceEditText);
                    EditText detailsEditText = dialogView.findViewById(R.id.planDetailsEditText);
                    Spinner categorySpinner = dialogView.findViewById(R.id.categorySpinner);

                    String name = nameEditText.getText().toString().trim();
                    String price = priceEditText.getText().toString().trim();
                    String details = detailsEditText.getText().toString().trim();
                    String category = categorySpinner.getSelectedItem().toString();

                    // Validate input
                    if (name.isEmpty() || price.isEmpty() || details.isEmpty()) {
                        Toast.makeText(this, "Please fill in all details", Toast.LENGTH_LONG).show();
                        return;
                    }

                    // Create a new plan and add it to Firebase
                    Plan newPlan = new Plan(null, name, price, details, category); // Firebase generates ID
                    planRepository.addPlan(category, newPlan,
                            aVoid -> {
                                loadPlans(); // Reload plans to include the new one
                            },
                            this::handleError);
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        Spinner categorySpinner = dialogView.findViewById(R.id.categorySpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.plan_categories, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);

        builder.create().show();
    }

    private void showEditPlanDialog(Plan plan) {
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_plan, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Plan")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    EditText nameEditText = dialogView.findViewById(R.id.planNameEditText);
                    EditText priceEditText = dialogView.findViewById(R.id.planPriceEditText);
                    EditText detailsEditText = dialogView.findViewById(R.id.planDetailsEditText);
                    Spinner categorySpinner = dialogView.findViewById(R.id.categorySpinner);

                    String name = nameEditText.getText().toString().trim();
                    String price = priceEditText.getText().toString().trim();
                    String details = detailsEditText.getText().toString().trim();
                    String category = categorySpinner.getSelectedItem().toString();

                    // Validate input
                    if (name.isEmpty() || price.isEmpty() || details.isEmpty()) {
                        Toast.makeText(this, "Please fill in all details", Toast.LENGTH_LONG).show();
                        return;
                    }

                    // Update the plan object
                    plan.setName(name);
                    plan.setPrice(price);
                    plan.setDetails(details);
                    plan.setCategory(category);

                    // Update the plan in Firebase
                    planRepository.updatePlan(category, plan.getId(), plan,
                            aVoid -> {
                                loadPlans(); // Reload plans to reflect the changes
                            },
                            this::handleError);
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        // Populate dialog fields with current plan data
        EditText nameEditText = dialogView.findViewById(R.id.planNameEditText);
        EditText priceEditText = dialogView.findViewById(R.id.planPriceEditText);
        EditText detailsEditText = dialogView.findViewById(R.id.planDetailsEditText);
        Spinner categorySpinner = dialogView.findViewById(R.id.categorySpinner);

        nameEditText.setText(plan.getName());
        priceEditText.setText(plan.getPrice());
        detailsEditText.setText(plan.getDetails());

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.plan_categories, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);
        categorySpinner.setSelection(((ArrayAdapter<String>) categorySpinner.getAdapter()).getPosition(plan.getCategory()));

        builder.create().show();
    }

    private void deletePlan(Plan plan) {
        String category = plan.getCategory();
        planRepository.deletePlan(category, plan.getId(),
                aVoid -> {
                    loadPlans(); // Reload plans to reflect changes
                },
                this::handleError);
    }
}
