package com.example.cableapp;

import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ComplaintAdapter extends RecyclerView.Adapter<ComplaintAdapter.ComplaintViewHolder> {
    private List<Complaint> complaintList;
    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public ComplaintAdapter(List<Complaint> complaintList) {
        this.complaintList = complaintList;
        sortComplaintsByTimestampDescending();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    @NonNull
    @Override
    public ComplaintViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_complaint, parent, false);
        return new ComplaintViewHolder(itemView, onItemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ComplaintViewHolder holder, int position) {
        Complaint complaint = complaintList.get(position);

        holder.timestamp.setText(formatTimestamp(complaint.getTimestamp()));
        holder.status.setText(complaint.getStatus());
        holder.solvedBy.setText(complaint.getSolvedBy());

        if ("collect bill".equalsIgnoreCase(complaint.getComplaintType()) && !complaint.getRemarks().isEmpty()) {
            holder.remarks.setText("Payment History:\nPaid: " + complaint.getRemarks().replace(",", "\nPaid: "));
        } else {
            holder.remarks.setText("Remarks: " + complaint.getRemarks());
        }

        if ("collect bill".equalsIgnoreCase(complaint.getComplaintType())) {
            String[] amounts = complaint.getComplaintDetails().split(",");
            holder.complaintDetails.setText("Amount Details:\nOld Due: " + amounts[1] + "\nPackage Amount: " + amounts[0]);
        } else {
            holder.complaintDetails.setText("Complaint Details:\n" + complaint.getComplaintDetails());
        }

        setStatusColor(holder, complaint.getStatus());

        holder.itemView.setOnClickListener(view -> {
            if ("quick complaint".equalsIgnoreCase(complaint.getComplaintType())) {
                openUpdateStatusDialog(view.getContext(), complaint);
            } else if ("collect bill".equalsIgnoreCase(complaint.getComplaintType())) {
                showComplaintPaymentDialog(view.getContext(), complaint);
            } else {
                view.setOnClickListener(null); // Clear the click listener for other types
            }
        });
    }

    private void setStatusColor(ComplaintViewHolder holder, String status) {
        int colorResId;
        switch (status.toLowerCase()) {
            case "solved":
                colorResId = R.color.status_active;
                break;
            case "pending":
                colorResId = R.color.status_due;
                break;
            case "in review":
                colorResId = R.color.status_expire;
                break;
            default:
                colorResId = android.R.color.black;
                break;
        }
        holder.status.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), colorResId));
    }

    @Override
    public int getItemCount() {
        return complaintList.size();
    }

    private void sortComplaintsByTimestampDescending() {
        Collections.sort(complaintList, (o1, o2) -> Long.compare(o2.getTimestamp(), o1.getTimestamp()));
    }

    public static class ComplaintViewHolder extends RecyclerView.ViewHolder {
        TextView complaintDetails, solvedBy, status, timestamp, remarks;

        public ComplaintViewHolder(@NonNull View itemView, final OnItemClickListener listener) {
            super(itemView);
            complaintDetails = itemView.findViewById(R.id.complaintDetails);
            timestamp = itemView.findViewById(R.id.timestamp);
            status = itemView.findViewById(R.id.registeredBy);
            solvedBy = itemView.findViewById(R.id.solvedBy);
            remarks = itemView.findViewById(R.id.complaint_remarks_1);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(position);
                }
            });
        }
    }

    private String formatTimestamp(long timestamp) {
        return new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(new Date(timestamp));
    }

    private void openUpdateStatusDialog(Context context, Complaint complaint) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Update Status");

        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_update_status, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.show();

        RadioGroup radioGroup = dialogView.findViewById(R.id.radioGroupStatus);
        EditText editTextRemarks = dialogView.findViewById(R.id.editTextRemarks);
        Button buttonSubmit = dialogView.findViewById(R.id.status_submit_button);

        buttonSubmit.setOnClickListener(v -> {
            int selectedRadioButtonId = radioGroup.getCheckedRadioButtonId();
            RadioButton selectedRadioButton = dialogView.findViewById(selectedRadioButtonId);
            String selectedStatus = selectedRadioButton.getText().toString();
            String remarks = editTextRemarks.getText().toString();

            DatabaseReference databaseReference = FirebaseDatabase.getInstance()
                    .getReference("complaints/mandapeta")
                    .child(complaint.getSerialNo());

            databaseReference.child(complaint.getComplaintId()).child("status").setValue(selectedStatus)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(context, "Status updated successfully", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    })
                    .addOnFailureListener(e -> Toast.makeText(context, "Failed to update status", Toast.LENGTH_SHORT).show());

            databaseReference.child(complaint.getComplaintId()).child("remarks").setValue(remarks)
                    .addOnSuccessListener(aVoid -> Toast.makeText(context, "Remarks updated successfully", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(context, "Failed to update remarks", Toast.LENGTH_SHORT).show());
        });

        builder.setNegativeButton("Cancel", (dialog2, which) -> dialog2.dismiss());
    }

    private void showComplaintPaymentDialog(Context context, Complaint complaint) {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.payment_dialog_box, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
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

        RadioGroup radio = dialogView.findViewById(R.id.default_amounts_group);
        TextView amountRadioText = dialogView.findViewById(R.id.payments_dialog_radio_text);
        TextView statusRadioText = dialogView.findViewById(R.id.payments_dialog_status_radio_text);
        RadioGroup radio2 = dialogView.findViewById(R.id.payment_status_group);
        statusRadioText.setVisibility(View.GONE);
        radio.setVisibility(View.GONE);
        radio2.setVisibility(View.GONE);
        amountRadioText.setText("Payment History\nPaid " + complaint.getRemarks().replace(",", "\nPaid "));

        DatabaseReference customerReference = FirebaseDatabase.getInstance()
                .getReference("user-base/sscn/mandapeta")
                .child(complaint.getSerialNo());

        customerReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Customer customer = dataSnapshot.getValue(Customer.class);
                    if (customer != null) {
                        float oldDue = parseOrZero(String.valueOf(customer.getDue()));

                        dialogTextDetails.setText("Complaint name: " + customer.getCustomerName() + "\nAddress: " + customer.getAddress());
                        dialogDue.setText("Due: ₹" + oldDue);

                        if (oldDue > 0) {
                            dialogDue.setTextColor(ContextCompat.getColor(context, R.color.status_deactive));
                        } else if (oldDue < 0) {
                            dialogDue.setTextColor(ContextCompat.getColor(context, R.color.status_active));
                        }

                        dialogAmountText.setText("Amount to Collect:\n" + complaint.getComplaintDetails());

                        dialogSubmit.setOnClickListener(v -> handlePaymentSubmission(dialogAmount, dialogAmountLayout, progressBar, complaint, customer, oldDue, context, dialog));
                        dialogCancel.setOnClickListener(v -> dialog.dismiss());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(context, "Unable to retrieve customer details.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handlePaymentSubmission(TextInputEditText dialogAmount, TextInputLayout dialogAmountLayout, ProgressBar progressBar, Complaint complaint, Customer customer, float oldDue, Context context, AlertDialog dialog) {
        String inputText = dialogAmount.getText().toString().trim();

        if (inputText.isEmpty()) {
            dialogAmountLayout.setError("Please fill in the details.");
            return;
        }

        int inputAmount = parseOrZero(inputText);

        if (inputAmount <= 0) {
            dialogAmountLayout.setError("Please enter a valid positive amount.");
            return;
        }

        try {
            String[] details = complaint.getComplaintDetails().split(",");
            int packageAmount = parseOrZero(details[0]);
            int previousDue = packageAmount + parseOrZero(details.length > 1 && details[1] != null ? details[1] : "0");

            String[] paidAmounts = complaint.getRemarks().split(",");
            for (String s : paidAmounts) {
                previousDue -= parseOrZero(s);
            }

            int newDue = previousDue - inputAmount;

            progressBar.setVisibility(View.VISIBLE);

            long timestamp = System.currentTimeMillis();

            Map<String, Object> paymentData = new HashMap<>();
            paymentData.put("serialNo", complaint.getSerialNo());
            paymentData.put("paidAmount", inputAmount);
            paymentData.put("newDue", newDue);
            paymentData.put("timestamp", timestamp);
            paymentData.put("paidTo", "");
            paymentData.put("status", "Paid");
            paymentData.put("packageAmount", packageAmount);

            String paymentLog = complaint.getRemarks().isEmpty() ? String.valueOf(inputAmount) : complaint.getRemarks() + "," + inputAmount;

            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("complaints/mandapeta");
            DatabaseReference paymentReference = FirebaseDatabase.getInstance().getReference("payments/mandapeta");
            DatabaseReference customerReference = FirebaseDatabase.getInstance()
                    .getReference("user-base/sscn/mandapeta")
                    .child(complaint.getSerialNo());

            String pushKey = databaseReference.child(complaint.getSerialNo()).push().getKey();
            if (pushKey != null) {
                paymentData.put("paymentId", pushKey);

                customerReference.child("due").setValue(newDue);

                databaseReference.child(complaint.getSerialNo()).child(complaint.getComplaintId()).child("status").setValue("Solved");
                databaseReference.child(complaint.getSerialNo()).child(complaint.getComplaintId()).child("remarks").setValue(paymentLog);

                paymentReference.child(complaint.getSerialNo()).child(pushKey).setValue(paymentData)
                        .addOnCompleteListener(task -> {
                            progressBar.setVisibility(View.GONE);

                            if (task.isSuccessful()) {
                                Toast.makeText(context, "Payment Updated: " + customer.getCustomerName(), Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            } else {
                                Toast.makeText(context, "Failed. Please try again.", Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(context, "Failed to generate payment ID. Please try again.", Toast.LENGTH_SHORT).show();
            }
        } catch (NumberFormatException e) {
            dialogAmountLayout.setError("Invalid amount entered.");
            Log.e("ComplaintAdapter", "Failed to parse amount in handlePaymentSubmission for complaint serialNo: " + complaint.getSerialNo() + ", input amount: " + inputText + ". Error: " + e.getMessage());
        }
    }

    private int parseOrZero(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
