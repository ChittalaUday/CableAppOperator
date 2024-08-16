package com.example.cableapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.core.content.ContextCompat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PaymentAdapter extends RecyclerView.Adapter<PaymentAdapter.PaymentViewHolder> {
    private List<Payment> paymentList;
    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public PaymentAdapter(List<Payment> paymentList) {
        this.paymentList = paymentList;

    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    @NonNull
    @Override
    public PaymentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_payment, parent, false);
        return new PaymentViewHolder(itemView, onItemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull PaymentViewHolder holder, int position) {
        Payment payment = paymentList.get(position);

        if (payment.getPaidAmount() != 0) {
            holder.paidAmount.setText("+" + payment.getPaidAmount());
        }

        if (payment.getPaidAmount() > 0) {
            holder.paidAmount.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.status_active));
        }

        holder.newDue.setText("Remaining Due: " + payment.getNewDue());
        holder.timestamp.setText(formatTimestamp(payment.getTimestamp()));
        holder.paidTo.setText(payment.getPaidTo());
        holder.packageAmount.setText("Package Amount: " + payment.getPackageAmount());
        holder.status.setText(payment.getStatus());

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

    }

    @Override
    public int getItemCount() {
        return paymentList.size();
    }




    public static class PaymentViewHolder extends RecyclerView.ViewHolder {
        TextView paidAmount;
        TextView newDue;
        TextView timestamp;
        TextView status;
        TextView paidTo;
        TextView packageAmount;

        public PaymentViewHolder(@NonNull View itemView, final OnItemClickListener listener) {
            super(itemView);
            status = itemView.findViewById(R.id.item_payment_status_text);
            packageAmount = itemView.findViewById(R.id.item_package_amount);
            paidAmount = itemView.findViewById(R.id.item_paidAmount);
            newDue = itemView.findViewById(R.id.item_payment_newDue);
            timestamp = itemView.findViewById(R.id.item_payment_timestamp);
            paidTo = itemView.findViewById(R.id.item_payment_paidTo);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onItemClick(position);
                        }
                    }
                }
            });
        }
    }

    private String formatTimestamp(long timestamp) {
        return new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(new Date(timestamp));
    }
}
