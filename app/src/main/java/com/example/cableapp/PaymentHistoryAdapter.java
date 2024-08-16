package com.example.cableapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PaymentHistoryAdapter extends RecyclerView.Adapter<PaymentHistoryAdapter.PaymentViewHolder> {

    private List<Payment> paymentList;
    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public PaymentHistoryAdapter(List<Payment> paymentList) {
        this.paymentList = paymentList;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public void updatePaymentList(List<Payment> filteredList) {
        this.paymentList = filteredList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PaymentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.payment_item, parent, false);
        return new PaymentViewHolder(itemView, onItemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull PaymentViewHolder holder, int position) {
        Payment payment = paymentList.get(position);
        holder.timestamp.setText(formatTimestamp(payment.getTimestamp()));
        holder.status.setText(payment.getStatus());
        holder.newDue.setText("New Due: " + payment.getNewDue());
        holder.packageAmount.setText("Package Amount: " + payment.getPackageAmount());
        holder.paidAmount.setText(String.valueOf( payment.getPaidAmount()));
        holder.paidTo.setText("Paid To: " + payment.getPaidTo());

        switch (payment.getStatus().toLowerCase()) {
            case "paid":
                holder.status.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.status_active));
                break;
            case "due":
                holder.status.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.status_deactive));
                break;
            default:
                holder.status.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.black));
                break;
        }

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("user-base/sscn/mandapeta").child(payment.getSerialNo());

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    Customer customer = snapshot.getValue(Customer.class);

                    holder.name.setText(customer.getCustomerName());
                    holder.address.setText(customer.getAddress());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (holder.paymentDetails.getVisibility() == View.VISIBLE) {
                    holder.paymentDetails.animate()
                            .alpha(0.0f)
                            .setDuration(300)
                            .withEndAction(new Runnable() {
                                @Override
                                public void run() {
                                    holder.paymentDetails.setVisibility(View.GONE);
                                }
                            });
                } else {
                    holder.paymentDetails.setAlpha(0.0f);
                    holder.paymentDetails.setVisibility(View.VISIBLE);
                    holder.paymentDetails.animate()
                            .alpha(1.0f)
                            .setDuration(300)
                            .setListener(null);
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return paymentList.size();
    }

    public static class PaymentViewHolder extends RecyclerView.ViewHolder {
        TextView  timestamp, status, newDue, packageAmount, paidAmount, paidTo,name,address;
        RelativeLayout paymentDetails,paymentCard;

        public PaymentViewHolder(@NonNull View itemView, final OnItemClickListener listener) {
            super(itemView);
            timestamp = itemView.findViewById(R.id.item_payment_timestamp);
            status = itemView.findViewById(R.id.item_payment_status_text);
            newDue = itemView.findViewById(R.id.item_payment_newDue);
            packageAmount = itemView.findViewById(R.id.item_package_amount);
            paidAmount = itemView.findViewById(R.id.item_paidAmount);
            paidTo = itemView.findViewById(R.id.item_payment_paidTo);
            paymentDetails = itemView.findViewById(R.id.payment_details_layout);
            paymentCard = itemView.findViewById(R.id.payment_card);
            name = itemView.findViewById(R.id.item_payment_name);
            address = itemView.findViewById(R.id.item_payment_address);

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
}
