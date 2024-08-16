package com.example.cableapp;

import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class WorkerListActivity extends AppCompatActivity implements WorkerAdapter.OnWorkerClickListener {
    private RecyclerView recyclerView;
    private FloatingActionButton fabAddWorker;
    private WorkerAdapter workerAdapter;
    private List<Worker> workerList;
    private FirebaseHelper firebaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_worker_list);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.bg_color));
        }


        recyclerView = findViewById(R.id.recyclerView);
        fabAddWorker = findViewById(R.id.fabAddWorker);
        workerList = new ArrayList<>();
        workerAdapter = new WorkerAdapter(this, workerList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(workerAdapter);

        firebaseHelper = new FirebaseHelper();
        loadWorkers();

        fabAddWorker.setOnClickListener(v -> {
            AddEditWorkerDialog dialog = new AddEditWorkerDialog();
            dialog.show(getSupportFragmentManager(), "AddEditWorkerDialog");
        });
    }

    private void loadWorkers() {
        firebaseHelper.getWorkers(task -> {
            if (task.isSuccessful()) {
                workerList.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Worker worker = document.toObject(Worker.class);
                    workerList.add(worker);
                }
                workerAdapter.notifyDataSetChanged();
            } else {
                // Handle error
            }
        });
    }

    @Override
    public void onEditClick(int position) {
        Worker worker = workerList.get(position);
        AddEditWorkerDialog dialog = new AddEditWorkerDialog();
        dialog.setWorker(worker);
        dialog.show(getSupportFragmentManager(), "AddEditWorkerDialog");
    }

    @Override
    public void onDeleteClick(int position) {
        Worker worker = workerList.get(position);
        firebaseHelper.deleteWorker(worker.getId(), task -> {
            if (task.isSuccessful()) {
                workerList.remove(position);
                workerAdapter.notifyItemRemoved(position);
            } else {
                // Handle error
            }
        });
    }
}
