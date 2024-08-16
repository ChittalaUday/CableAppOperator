package com.example.cableapp;

// PlanAdapter.java
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

// PlanAdapter.java
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class PlanAdapter extends RecyclerView.Adapter<PlanAdapter.PlanViewHolder> {

    private List<Plan> plans;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onEditClick(Plan plan);
        void onDeleteClick(Plan plan);
    }

    public PlanAdapter(List<Plan> plans, OnItemClickListener listener) {
        this.plans = plans;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PlanViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.plan_item, parent, false);
        return new PlanViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlanViewHolder holder, int position) {
        Plan plan = plans.get(position);
        holder.nameTextView.setText(plan.getName());
        holder.priceTextView.setText(plan.getPrice());
        holder.detailsTextView.setText(plan.getDetails());

        holder.editButton.setOnClickListener(v -> listener.onEditClick(plan));
        holder.deleteButton.setOnClickListener(v -> listener.onDeleteClick(plan));
    }

    @Override
    public int getItemCount() {
        return plans.size();
    }

    public static class PlanViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        TextView priceTextView;
        TextView detailsTextView;
        Button editButton;
        Button deleteButton;

        public PlanViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            priceTextView = itemView.findViewById(R.id.priceTextView);
            detailsTextView = itemView.findViewById(R.id.detailsTextView);
            editButton = itemView.findViewById(R.id.editButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
}
