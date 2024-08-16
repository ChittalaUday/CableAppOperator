package com.example.cableapp;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.circularreveal.cardview.CircularRevealCardView;

import java.util.List;

public class CustomerAdapter extends RecyclerView.Adapter<CustomerAdapter.CustomerViewHolder> {
    private List<Customer> customerList;
    private Context context;

    public static class CustomerViewHolder extends RecyclerView.ViewHolder {
        public TextView boxType, createdOn, cId, address, status, mobileNo, name, serialNo, sNo,due,complaint;
        public CardView cardView;

        public CustomerViewHolder(View view) {
            super(view);
            cardView = view.findViewById(R.id.card_view);
            boxType = view.findViewById(R.id.box_type);
            createdOn = view.findViewById(R.id.created_on);
            cId = view.findViewById(R.id.c_id);
            address = view.findViewById(R.id.address);
            status = view.findViewById(R.id.status);
            mobileNo = view.findViewById(R.id.mobile_no);
            name = view.findViewById(R.id.name);
            serialNo = view.findViewById(R.id.serial_no);
            sNo = view.findViewById(R.id.s_no);
        }
    }

    public CustomerAdapter(Context context, List<Customer> customerList) {
        this.context = context;
        this.customerList = customerList;
    }

    @Override
    public CustomerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.customer_card, parent, false);
        return new CustomerViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull CustomerViewHolder holder, int position) {
        Customer customer = customerList.get(position);
        holder.boxType.setText(customer.getBoxType());
        holder.createdOn.setText(customer.getCreatedNo());
        holder.cId.setText(customer.getCustomerId());
        holder.address.setText(customer.getAddress());
        holder.status.setText(customer.getStatus());
        holder.mobileNo.setText(customer.getMobileNo());
        holder.name.setText(customer.getCustomerName());
        holder.serialNo.setText(customer.getStbSerialNo());
        holder.sNo.setText(customer.getSerialNo());

        switch (customer.getStatus().toLowerCase()) {
            case "active":
                holder.status.setTextColor(ContextCompat.getColor(context, R.color.status_active));
                break;
            case "deactive":
                holder.status.setTextColor(ContextCompat.getColor(context, R.color.status_deactive));
                break;
            case "deactivate":
                holder.status.setTextColor(ContextCompat.getColor(context, R.color.status_deactive));
                break;
            case "due":
                holder.status.setTextColor(ContextCompat.getColor(context,R.color.status_due));
            case "expired":
                holder.status.setTextColor(ContextCompat.getColor(context, R.color.status_expire));
                break;
            default:
                holder.status.setTextColor(ContextCompat.getColor(context, android.R.color.black));
                break;
        }
        
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, CustomerActivity.class);
                intent.putExtra("customer", customer);

                context.startActivity(intent);
            }
        });

        holder.cardView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                String allText = "Name: " + holder.name.getText().toString() + "\n" +
                        "Serial No: " + holder.serialNo.getText().toString() + "\n" +
                        "Box Type: " + holder.boxType.getText().toString() + "\n" +
                        "Created on: " + holder.createdOn.getText().toString() + "\n" +
                        "Customer ID: " + holder.cId.getText().toString() + "\n" +
                        "Status: " + holder.status.getText().toString() + "\n" +
                        "Address: " + holder.address.getText().toString() + "\n" +
                        "Mobile No: " + holder.mobileNo.getText().toString();

                ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Card Data", allText);
                clipboard.setPrimaryClip(clip);

                Toast.makeText(context, "Card content copied to clipboard", Toast.LENGTH_SHORT).show();
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return customerList.size();
    }
}
