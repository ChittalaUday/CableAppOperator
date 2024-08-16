package com.example.cableapp;

import android.net.Uri;
import androidx.annotation.NonNull;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class FirebaseHelper {
    private FirebaseFirestore db;
    private FirebaseStorage storage;

    public FirebaseHelper() {
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
    }

    public void addWorker(Worker worker, Uri photoUri, OnCompleteListener<Void> onCompleteListener) {
        if (photoUri != null) {
            StorageReference photoRef = storage.getReference().child("worker_photos/" + worker.getId() + ".jpg");
            photoRef.putFile(photoUri)
                    .addOnSuccessListener(taskSnapshot -> photoRef.getDownloadUrl()
                            .addOnSuccessListener(uri -> {
                                worker.setPhotoUrl(uri.toString());
                                db.collection("workers").document(worker.getId()).set(worker)
                                        .addOnCompleteListener(onCompleteListener);
                            })
                    );
        } else {
            db.collection("workers").document(worker.getId()).set(worker)
                    .addOnCompleteListener(onCompleteListener);
        }
    }

    public void getWorkers(OnCompleteListener<QuerySnapshot> onCompleteListener) {
        db.collection("workers").get().addOnCompleteListener(onCompleteListener);
    }

    public void updateWorker(Worker worker, Uri photoUri, OnCompleteListener<Void> onCompleteListener) {
        if (photoUri != null) {
            StorageReference photoRef = storage.getReference().child("worker_photos/" + worker.getId() + ".jpg");
            photoRef.putFile(photoUri)
                    .addOnSuccessListener(taskSnapshot -> photoRef.getDownloadUrl()
                            .addOnSuccessListener(uri -> {
                                worker.setPhotoUrl(uri.toString());
                                db.collection("workers").document(worker.getId()).set(worker)
                                        .addOnCompleteListener(onCompleteListener);
                            })
                    );
        } else {
            db.collection("workers").document(worker.getId()).set(worker)
                    .addOnCompleteListener(onCompleteListener);
        }
    }

    public void deleteWorker(String workerId, OnCompleteListener<Void> onCompleteListener) {
        db.collection("workers").document(workerId).delete().addOnCompleteListener(onCompleteListener);
    }
}
