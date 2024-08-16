package com.example.cableapp;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;


public class CustomerActivity extends AppCompatActivity {
    private MaterialToolbar toolbar;
    private Customer customer;
    private String customerKey,parentKey;
    private DatabaseReference databaseReference;
    private TextView name, serialNo, boxType, createdOn,status,due, cId, address, expiry, mobileNo, packageId,stb;

    private TextView buttonTab1, buttonTab2;

    private FloatingActionButton fabMain, fabOption1, fabOption2, fabOption3;
    private boolean isFabOpen = false;
    private Animation fabOpenAnim, fabCloseAnim;

    private FirebaseNotificationHelper notificationHelper;
    private DatabaseReference notificationDataReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.bg_color));
        }


        customer = (Customer) getIntent().getSerializableExtra("customer");
        customerKey = getIntent().getStringExtra("key");
        parentKey = getIntent().getStringExtra("parentKey");
        notificationHelper = new FirebaseNotificationHelper(this);
        notificationDataReference = FirebaseDatabase.getInstance().getReference("users_fcm_token");

        packageId = findViewById(R.id.package_id);
        stb = findViewById(R.id.box_id);
        name = findViewById(R.id.name);
        serialNo = findViewById(R.id.serial_no);
        boxType = findViewById(R.id.box_type);
        createdOn = findViewById(R.id.created_on);
        cId = findViewById(R.id.c_id);
        address = findViewById(R.id.address);
        expiry = findViewById(R.id.expire);
        mobileNo = findViewById(R.id.mobile_no);
        status = findViewById(R.id.status);
        due = findViewById(R.id.dueString);

        if (customer != null) {
            name.setText(customer.getCustomerName());
            serialNo.setText(customer.getSerialNo());
            boxType.setText(customer.getBoxType());
            createdOn.setText(customer.getCreatedNo());
            cId.setText(customer.getCustomerId());
            address.setText(customer.getAddress());
            expiry.setText(customer.getExpiryNo());
            mobileNo.setText(customer.getMobileNo());
            status.setText(customer.getStatus());
            due.setText("₹"+String.valueOf(customer.getDue()));
            packageId.setText(customer.getPackageId());
            stb.setText(customer.getStbSerialNo());
            statusColorChange();

        }

        // Set up the toolbar
        toolbar = findViewById(R.id.customer_toolbar);
        setSupportActionBar(toolbar);

        // Enable the Up button

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        buttonTab1 = findViewById(R.id.button_tab1);
        buttonTab2 = findViewById(R.id.button_tab2);

        buttonTab1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadFragment(new ComplaintFragment());
                animateTabTransition(buttonTab1, buttonTab2);
            }
        });

        buttonTab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadFragment(new PaymentFragment());
                animateTabTransition(buttonTab2, buttonTab1);
            }
        });

        loadFragment(new ComplaintFragment());
        animateTabTransition(buttonTab1, buttonTab2);

        fabMain = findViewById(R.id.fab_main);
        fabOption1 = findViewById(R.id.fab_option1);
        fabOption2 = findViewById(R.id.fab_option2);
        fabOption3 = findViewById(R.id.fab_option3);

        fabOpenAnim = AnimationUtils.loadAnimation(this, R.anim.fab_show);
        fabCloseAnim = AnimationUtils.loadAnimation(this, R.anim.fab_hide);

        fabMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isFabOpen) {

                    closeFabMenu();
                } else {
                    openFabMenu();
                }
            }
        });

        fabOption1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showChangeStatusDialog();
            }
        });

        fabOption2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPaymentDialog();
            }
        });

        fabOption3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showComplaintDialog();
            }
        });

    }

    private void statusColorChange(){
        switch (customer.getStatus().toLowerCase()) {
            case "active":
                status.setTextColor(ContextCompat.getColor(this, R.color.status_active));
                break;
            case "deactive":
                status.setTextColor(ContextCompat.getColor(this, R.color.status_deactive));
                break;
            case "expired":
                status.setTextColor(ContextCompat.getColor(this, R.color.status_expire));
                break;
            case "due":
                status.setTextColor(ContextCompat.getColor(this, R.color.status_due));
                break;
            default:
                status.setTextColor(ContextCompat.getColor(this, android.R.color.black));
                break;
        }
    }

    private void showChangeStatusDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.status_dialog_box, null);
        builder.setView(dialogView);

        RadioGroup statusRadioGroup = dialogView.findViewById(R.id.status_radio_group);
        ProgressBar progressBar = dialogView.findViewById(R.id.status_progress);

        builder.setTitle("Change Status")
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int selectedId = statusRadioGroup.getCheckedRadioButtonId();
                        if (selectedId != -1) {
                            RadioButton selectedRadioButton = statusRadioGroup.findViewById(selectedId);
                            String selectedStatus = selectedRadioButton.getText().toString();
                            DatabaseReference databaseReference2 = FirebaseDatabase.getInstance().getReference("user-base/sscn/mandapeta");

                            // Handle status change based on selectedStatus
                            switch (selectedStatus) {
                                case "Active":
                                    databaseReference2.child(customer.getSerialNo()).child("STATUS").setValue("Active");
                                    break;
                                case "Deactive":
                                    databaseReference2.child(customer.getSerialNo()).child("STATUS").setValue("Deactive");
                                    break;
                                case "Terminate":
                                    databaseReference2.child(customer.getSerialNo()).child("STATUS").setValue("Terminated");
                                    break;
                                default:
                                    break;
                            }
                            Toast.makeText(CustomerActivity.this, customer.getCustomerName()+" Status changed.", Toast.LENGTH_SHORT).show();
                            customer.setStatus(selectedStatus);
                            status.setText(selectedStatus);
                            statusColorChange();
                        }
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }


    private void showPaymentDialog() {
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.payment_dialog_box, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        dialog.show();

        TextView dialogTextDetails = dialogView.findViewById(R.id.payment_dialog_details);
        TextView dialogDue = dialogView.findViewById(R.id.payment_old_due);
        TextView dialogAmountText = dialogView.findViewById(R.id.payments_amount_paid);
        TextInputEditText dialogAmount = dialogView.findViewById(R.id.payment_amount_input);
        TextInputLayout dialogAmountLayout = dialogView.findViewById(R.id.payment_amount_input_layout);
        Button dialogCancel = dialogView.findViewById(R.id.payment_dialog_cancel_button);
        Button dialogSubmit = dialogView.findViewById(R.id.payment_dialog_button);
        ProgressBar progressBar = dialogView.findViewById(R.id.payment_progress);
        RadioGroup defaultAmountsGroup = dialogView.findViewById(R.id.default_amounts_group);
        RadioButton amountCustom = dialogView.findViewById(R.id.amount_custom);
        TextInputLayout customAmountInputLayout = dialogView.findViewById(R.id.custom_amount_input_layout);
        TextInputEditText customAmountInput = dialogView.findViewById(R.id.custom_amount_input);
        RadioGroup paymentStatusGroup = dialogView.findViewById(R.id.payment_status_group);



        dialogTextDetails.setText("Name: " + customer.getCustomerName() + "\nAddress: " + customer.getAddress());

        float oldDue = customer.getDue();
        dialogDue.setText("Due: ₹" + oldDue);
        if (oldDue > 0) {
            dialogDue.setTextColor(ContextCompat.getColor(this, R.color.status_deactive));
        } else if (0>oldDue) {
            dialogDue.setTextColor(ContextCompat.getColor(this, R.color.status_active));
        }

        paymentStatusGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.radio_set_due) {
                    dialogAmountLayout.setVisibility(View.GONE);
                } else {
                    dialogAmountLayout.setVisibility(View.VISIBLE);

                }
            }
        });

        defaultAmountsGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.amount_custom) {
                    customAmountInputLayout.setVisibility(View.VISIBLE);


                } else {
                    RadioButton selectedButton = dialogView.findViewById(defaultAmountsGroup.getCheckedRadioButtonId());
                    Float amount = Float.parseFloat(selectedButton.getText().toString().replace("₹", "").trim());
                    dialogAmountText.setText("Amount: ₹"+(amount+customer.getDue()));
                    customAmountInputLayout.setVisibility(View.GONE);
                }
            }
        });

        customAmountInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().isEmpty()) {
                    float customAmount = Float.parseFloat(s.toString())+customer.getDue();
                    dialogAmountText.setText("Amount Paid: ₹" + customAmount); // Update amount paid text
                }
            }
        });


        dialogSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String inputText = dialogAmount.getText().toString();
                int selectedStatusId = paymentStatusGroup.getCheckedRadioButtonId();
                boolean isSetAsActive = selectedStatusId == R.id.radio_activate;

                if (isSetAsActive && inputText.isEmpty()) {
                    dialogAmountLayout.setError("Please fill in the details.");
                    return;
                }

                int selectedId = defaultAmountsGroup.getCheckedRadioButtonId();
                if (selectedId == -1) {
                    Toast.makeText(CustomerActivity.this, "Please select an amount.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (selectedStatusId == -1) {
                    Toast.makeText(CustomerActivity.this, "Please select Status.", Toast.LENGTH_SHORT).show();
                    return;
                }

                float inputAmount = isSetAsActive ? Float.parseFloat(inputText) : 0;
                float selectedAmount = 0;

                if (amountCustom.isChecked()) {
                    String customInputText = customAmountInput.getText().toString();
                    if (customInputText.isEmpty()) {
                        customAmountInputLayout.setError("Please enter a custom amount.");
                        return;
                    }
                    selectedAmount = Float.parseFloat(customInputText);
                } else {
                    selectedId = defaultAmountsGroup.getCheckedRadioButtonId();
                    RadioButton selectedButton = dialogView.findViewById(selectedId);
                    selectedAmount = Float.parseFloat(selectedButton.getText().toString().replace("₹", "").trim());
                }

                // Calculate new due amount
                float totalDue = oldDue + selectedAmount;
                float newDue = totalDue - inputAmount;

                RadioButton selectedRadioStatus = dialogView.findViewById(paymentStatusGroup.getCheckedRadioButtonId());
                String selectedStatus = selectedRadioStatus.getText().toString();

                // Show progress bar
                progressBar.setVisibility(View.VISIBLE);

                // Get the current timestamp
                long timestamp = System.currentTimeMillis();

                // Create a payment object or a Map to save in Firebase
                Map<String, Object> paymentData = new HashMap<>();
                paymentData.put("serialNo", customer.getSerialNo());
                paymentData.put("paidAmount", inputAmount);
                paymentData.put("packageAmount",selectedAmount);
                paymentData.put("newDue", newDue);
                paymentData.put("timestamp", timestamp);
                paymentData.put("paidTo", "");
                paymentData.put("status", selectedStatus);

                // Save to Firebase using customer's serial number and a random key
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("payments/mandapeta");
                DatabaseReference databaseReference2 = FirebaseDatabase.getInstance().getReference("user-base/sscn/mandapeta");
                String pushKey = databaseReference.child(customer.getSerialNo()).push().getKey();
                if (pushKey != null) {
                    paymentData.put("paymentId", pushKey);
                    databaseReference2.child(customer.getSerialNo()).child("due").setValue(newDue);
                    databaseReference.child(customer.getSerialNo()).child(pushKey).setValue(paymentData)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    // Hide progress bar
                                    progressBar.setVisibility(View.GONE);

                                    if (task.isSuccessful()) {
                                        Toast.makeText(CustomerActivity.this, "Payment Updated: " + customer.getCustomerName(), Toast.LENGTH_SHORT).show();
                                        dialog.dismiss();
                                        due.setText("₹" + String.valueOf(newDue));
                                        customer.setDue((int) newDue);

                                    } else {
                                        Toast.makeText(CustomerActivity.this, "Failed. Please try again.", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                } else {
                    // Hide progress bar
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(CustomerActivity.this, "Failed to generate payment ID. Please try again.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        dialogCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
    }

    private void showComplaintDialog() {
        // Inflate the custom layout
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.complaint_dialog_box, null);

        // Create the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);



        // Customize the dialog
        TextView dialogText = dialogView.findViewById(R.id.dialog_details);
        TextInputEditText dialogInput = dialogView.findViewById(R.id.dialog_input);
        TextInputLayout dialogInputLayout = dialogView.findViewById(R.id.dialog_input_layout);
        RadioGroup complaintTypeGroup = dialogView.findViewById(R.id.complaint_type_group);
        TextView paymentsDialogRadioText = dialogView.findViewById(R.id.payments_dialog_radio_text);
        RadioGroup complaintDialogAmountsGroup = dialogView.findViewById(R.id.complaint_dialog_amounts_group);
        TextInputLayout customAmountInputLayout = dialogView.findViewById(R.id.complaint_dialog_custom_amount_input_layout);
        TextInputEditText customAmountInput = dialogView.findViewById(R.id.complaint_dialog_custom_amount_input);
        Button dialogButton = dialogView.findViewById(R.id.dialog_button);
        Button dialogCancelButton = dialogView.findViewById(R.id.dialog_cancel_button);
        ProgressBar progressBar = dialogView.findViewById(R.id.progress_bar);
        AlertDialog dialog = builder.create();
        dialog.show();

        // Assuming customer is an instance of a Customer class with appropriate getters
        dialogText.setText("Name: " + customer.getCustomerName() + "\nAddress: " + customer.getAddress());

        // Set RadioGroup listener to show/hide additional fields based on selection
        complaintTypeGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.collect_bill) {
                    paymentsDialogRadioText.setVisibility(View.VISIBLE);
                    complaintDialogAmountsGroup.setVisibility(View.VISIBLE);
                    dialogInputLayout.setVisibility(View.GONE);
                    if (complaintDialogAmountsGroup.getCheckedRadioButtonId() == R.id.complaint_dialog_amount_custom) {
                        customAmountInputLayout.setVisibility(View.VISIBLE);
                    } else {
                        customAmountInputLayout.setVisibility(View.GONE);
                    }
                } else {
                    paymentsDialogRadioText.setVisibility(View.GONE);
                    complaintDialogAmountsGroup.setVisibility(View.GONE);
                    dialogInputLayout.setVisibility(View.VISIBLE);
                    customAmountInputLayout.setVisibility(View.GONE);
                }
            }
        });

        complaintDialogAmountsGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.complaint_dialog_amount_custom) {
                    customAmountInputLayout.setVisibility(View.VISIBLE);
                } else {
                    customAmountInputLayout.setVisibility(View.GONE);
                }
            }
        });

        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String inputText = dialogInput.getText().toString();

                // If Collect Bill is selected, validate additional fields
                int selectedId = complaintTypeGroup.getCheckedRadioButtonId();
                if (selectedId == R.id.collect_bill) {
                    int amountSelectedId = complaintDialogAmountsGroup.getCheckedRadioButtonId();
                    if (amountSelectedId == R.id.complaint_dialog_amount_custom) {
                        inputText = customAmountInput.getText().toString().replace("₹", "").trim();

                        if (inputText.isEmpty()) {
                            customAmountInputLayout.setError("Please enter the custom amount.");
                            return;
                        }
                    } else {
                        RadioButton selectedAmountButton = dialogView.findViewById(amountSelectedId);
                        inputText = selectedAmountButton.getText().toString().replace("₹", "").trim();
                    }

                    inputText += "," + (String.valueOf(customer.getDue()).equals("null") ? "0" : String.valueOf(customer.getDue()));
                } else {
                    if (inputText.isEmpty()) {
                        dialogInputLayout.setError("Please fill in the details.");
                        return;
                    }
                }

                // Show progress bar
                progressBar.setVisibility(View.VISIBLE);

                long timestamp = System.currentTimeMillis();

                // Determine the type of complaint
                String complaintType = (selectedId == R.id.quick_complaint) ? "Quick Complaint" : "Collect Bill";

                // Create a complaint object or a Map to save in Firebase
                Map<String, Object> complaintData = new HashMap<>();
                complaintData.put("serialNo", customer.getSerialNo());
                complaintData.put("complaintDetails", inputText);
                complaintData.put("timestamp", timestamp);
                complaintData.put("status", "In Review");
                complaintData.put("solvedOn", "");
                complaintData.put("solvedBy", "");
                complaintData.put("remarks","");
                complaintData.put("complaintType", complaintType);

                // Save to Firebase using customer's serial number and a random key
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("complaints/mandapeta");
                String pushKey = databaseReference.child(customer.getSerialNo()).push().getKey();
                if (pushKey != null) {
                    complaintData.put("complaintId", pushKey);
                    databaseReference.child(customer.getSerialNo()).child(pushKey).setValue(complaintData)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    // Hide progress bar
                                    progressBar.setVisibility(View.GONE);
                                    if (task.isSuccessful()) {
                                        Toast.makeText(CustomerActivity.this, "Complaint Registered: " + customer.getCustomerName(), Toast.LENGTH_SHORT).show();
                                        dialog.dismiss();
                                    } else {
                                        Toast.makeText(CustomerActivity.this, "Failed. Please try again.", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                } else {
                    // Hide progress bar
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(CustomerActivity.this, "Failed to generate complaint ID. Please try again.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Show the dialog
        dialogCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
    }

    private void openFabMenu() {
        isFabOpen = true;
        fabOption1.setVisibility(View.VISIBLE);
        fabOption2.setVisibility(View.VISIBLE);
        fabOption3.setVisibility(View.VISIBLE);
        fabOption1.startAnimation(fabOpenAnim);
        fabOption2.startAnimation(fabOpenAnim);
        fabOption3.startAnimation(fabOpenAnim);
        fabOption1.setClickable(true);
        fabOption2.setClickable(true);
        fabOption3.setClickable(true);
        fabMain.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
    }

    private void closeFabMenu() {
        isFabOpen = false;
        fabOption1.startAnimation(fabCloseAnim);
        fabOption2.startAnimation(fabCloseAnim);
        fabOption3.startAnimation(fabCloseAnim);
        fabOption1.setClickable(false);
        fabOption2.setClickable(false);
        fabOption3.setClickable(false);
        fabOption1.setVisibility(View.GONE);
        fabOption2.setVisibility(View.GONE);
        fabOption3.setVisibility(View.GONE);
        fabMain.setImageResource(android.R.drawable.ic_input_add);
    }
        private void loadFragment(Fragment fragment) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.container, fragment);
            fragmentTransaction.commit();
        }

        private void animateTabTransition(TextView activeButton, TextView inactiveButton) {
            int colorFrom = Color.parseColor("#FFFFFF");
            int colorTo = Color.parseColor("#6200EE");
            int textColorFrom = Color.parseColor("#6200EE");
            int textColorTo = Color.parseColor("#FFFFFF");

            // Change background color
            ObjectAnimator colorFade = ObjectAnimator.ofArgb(activeButton, "backgroundColor", colorFrom, colorTo);
            ObjectAnimator colorFadeOut = ObjectAnimator.ofArgb(inactiveButton, "backgroundColor", colorTo, colorFrom);

            // Change text color
            ObjectAnimator textColorFade = ObjectAnimator.ofArgb(activeButton, "textColor", textColorFrom, textColorTo);
            ObjectAnimator textColorFadeOut = ObjectAnimator.ofArgb(inactiveButton, "textColor", textColorTo, textColorFrom);

            // Scale animations
            ObjectAnimator scaleUpX = ObjectAnimator.ofFloat(activeButton, "scaleX", 0.9f, 1.0f);
            ObjectAnimator scaleUpY = ObjectAnimator.ofFloat(activeButton, "scaleY", 0.9f, 1.0f);
            ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(inactiveButton, "scaleX", 1.0f, 0.9f);
            ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(inactiveButton, "scaleY", 1.0f, 0.9f);

            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(colorFade, colorFadeOut, textColorFade, textColorFadeOut, scaleUpX, scaleUpY, scaleDownX, scaleDownY);
            animatorSet.setDuration(300);
            animatorSet.start();
        }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_edit) {
            Intent intent = new Intent(this, EditCustomerActivity.class);
            intent.putExtra("customer", customer);

            startActivity(intent);
        } else{
            // Handle the Up button action
            onBackPressed();
        }
        return true;
    }
}
