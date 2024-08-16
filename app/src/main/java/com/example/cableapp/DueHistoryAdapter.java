package com.example.cableapp;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DueHistoryAdapter extends RecyclerView.Adapter<DueHistoryAdapter.DueHistoryViewHolder> {

    private List<Due> dueList;


    public DueHistoryAdapter(List<Due> dueList) {
        this.dueList = dueList;
    }

    @NonNull
    @Override
    public DueHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_due_history, parent, false);
        return new DueHistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DueHistoryViewHolder holder, int position) {
        Due due = dueList.get(position);

        holder.amountTextView.setText("Amount: " + due.getAmount());
        holder.remarkTextView.setText("Remark: " + due.getRemark());

        // Convert timestamp to human-readable date and time
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault());
        String formattedTime = sdf.format(new Date((long) due.getTimestamp()));
        holder.timeTextView.setText("Time: " + formattedTime);

        holder.paidTextView.setText("Paid: " + (due.isPaid()?"Yes":"No"));
        if (due.getPaidOnTimeStamp().toString().equals("0")){
            holder.paidOnTextView.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return dueList.size();
    }

    static class DueHistoryViewHolder extends RecyclerView.ViewHolder {
        TextView amountTextView, remarkTextView, timeTextView, paidTextView,paidOnTextView;

        DueHistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            paidOnTextView = itemView.findViewById(R.id.paid_on_text_view);
            paidTextView = itemView.findViewById(R.id.paid_text_view);
            amountTextView = itemView.findViewById(R.id.amount_text_view);
            remarkTextView = itemView.findViewById(R.id.remark_text_view);
            timeTextView = itemView.findViewById(R.id.time_text_view);
        }

    }
}
