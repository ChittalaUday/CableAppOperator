package com.example.cableapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class WorkerAdapter extends RecyclerView.Adapter<WorkerAdapter.WorkerViewHolder> {
    private List<Worker> workerList;
    private Context context;
    private OnWorkerClickListener onWorkerClickListener;

    public WorkerAdapter(Context context, List<Worker> workerList, OnWorkerClickListener onWorkerClickListener) {
        this.context = context;
        this.workerList = workerList;
        this.onWorkerClickListener = onWorkerClickListener;
    }

    @NonNull
    @Override
    public WorkerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.worker_item, parent, false);
        return new WorkerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WorkerViewHolder holder, int position) {
        Worker worker = workerList.get(position);
        holder.bind(worker);
    }

    @Override
    public int getItemCount() {
        return workerList.size();
    }

    class WorkerViewHolder extends RecyclerView.ViewHolder {
        TextView name, mobile, role;
        ImageView photo;
        Button editButton, deleteButton;

        WorkerViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            mobile = itemView.findViewById(R.id.mobile);
            role = itemView.findViewById(R.id.role);
            photo = itemView.findViewById(R.id.photo);
            editButton = itemView.findViewById(R.id.editButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);

            editButton.setOnClickListener(v -> onWorkerClickListener.onEditClick(getAdapterPosition()));
            deleteButton.setOnClickListener(v -> onWorkerClickListener.onDeleteClick(getAdapterPosition()));
        }

        void bind(Worker worker) {
            name.setText("Name: "+worker.getName());
            mobile.setText("Mobile No: "+worker.getMobile());
            role.setText("Role: "+worker.getRole());
            Glide.with(context).load(worker.getPhotoUrl()).into(photo);
        }
    }

    public interface OnWorkerClickListener {
        void onEditClick(int position);
        void onDeleteClick(int position);
    }
}
