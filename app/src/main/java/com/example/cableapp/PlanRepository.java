package com.example.cableapp;
// PlanRepository.java
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class PlanRepository {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public void addPlan(String category, Plan plan, OnSuccessListener<Void> successListener, OnFailureListener failureListener) {
        CollectionReference plansRef = db.collection("plans").document(category).collection("plans");
        DocumentReference newPlanRef = plansRef.document(); // Firebase generates a unique ID here

        // Set the ID of the plan object
        plan.setId(newPlanRef.getId());

        newPlanRef.set(plan)
                .addOnSuccessListener(aVoid -> successListener.onSuccess(null))
                .addOnFailureListener(failureListener);
    }

    public void getPlans(String category, OnCompleteListener<List<Plan>> completeListener, OnFailureListener failureListener) {
        CollectionReference plansRef = db.collection("plans").document(category).collection("plans");
        plansRef.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Plan> plans = task.getResult().toObjects(Plan.class);
                        completeListener.onComplete(plans);
                    } else {
                        failureListener.onFailure(task.getException());
                    }
                })
                .addOnFailureListener(failureListener);
    }

    public void updatePlan(String category, String planId, Plan updatedPlan, OnSuccessListener<Void> successListener, OnFailureListener failureListener) {
        DocumentReference planRef = db.collection("plans").document(category).collection("plans").document(planId);

        // Update the plan data
        planRef.set(updatedPlan)
                .addOnSuccessListener(successListener)
                .addOnFailureListener(failureListener);
    }

    public void deletePlan(String category, String planId, OnSuccessListener<Void> successListener, OnFailureListener failureListener) {
        DocumentReference planRef = db.collection("plans").document(category).collection("plans").document(planId);

        // Delete the plan document
        planRef.delete()
                .addOnSuccessListener(successListener)
                .addOnFailureListener(failureListener);
    }

    public interface OnCompleteListener<T> {
        void onComplete(T result);
    }
}
