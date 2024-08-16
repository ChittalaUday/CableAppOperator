package com.example.cableapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ComplaintFragment extends Fragment {
    private RecyclerView recyclerView;
    private ComplaintAdapter complaintAdapter;
    private List<Complaint> complaintList;
    private Customer customer;

    private TextView noComplaintText;
    private DatabaseReference databaseReference;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_complaint, container, false);

        recyclerView = view.findViewById(R.id.complaint_fragment_recycler_view);
        noComplaintText = view.findViewById(R.id.fragment_complaint_text);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize complaintList and complaintAdapter
        complaintList = new ArrayList<>();

        complaintAdapter = new ComplaintAdapter(complaintList);
        recyclerView.setAdapter(complaintAdapter);

        Intent intent = getActivity().getIntent();
        customer = (Customer) intent.getSerializableExtra("customer");

        databaseReference = FirebaseDatabase.getInstance().getReference("complaints/mandapeta").child(customer.getSerialNo());

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                complaintList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Complaint complaint = snapshot.getValue(Complaint.class);
                    complaintList.add(complaint);

                }
                sortComplaintDescending();
                complaintAdapter.notifyDataSetChanged();
                if (complaintList.isEmpty()) {
                    noComplaintText.setVisibility(View.VISIBLE);
                } else {
                    noComplaintText.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle possible errors
            }
        });

        return view;
    }

    public void sortComplaintDescending() {
        if (complaintList == null || complaintList.isEmpty()) {
            System.out.println("Payment list is either null or empty.");
            return;
        }

        Collections.sort(complaintList, new Comparator<Complaint>() {
            @Override
            public int compare(Complaint complaint1, Complaint comlpaint2) {
                long timestamp1 = complaint1.getTimestamp();
                long timestamp2 = comlpaint2.getTimestamp();
                return Long.compare(timestamp2, timestamp1);
            }
        });


    }



}