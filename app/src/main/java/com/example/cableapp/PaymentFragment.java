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
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class PaymentFragment extends Fragment {
    private RecyclerView recyclerView;
    private PaymentAdapter paymentAdapter;
    private List<Payment> paymentList;
    private Customer customer;

    private TextView noPaymentText;
    private DatabaseReference databaseReference;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_payment, container, false);

        recyclerView = view.findViewById(R.id.payment_fragment_recycler_view);
        noPaymentText = view.findViewById(R.id.payment_no_payments_text);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        paymentList = new ArrayList<>();
        paymentAdapter = new PaymentAdapter(paymentList);
        recyclerView.setAdapter(paymentAdapter);

        Intent intent = getActivity().getIntent();
        customer = (Customer) intent.getSerializableExtra("customer");

        databaseReference = FirebaseDatabase.getInstance().getReference("payments/mandapeta").child(customer.getSerialNo());

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                paymentList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Payment payment = snapshot.getValue(Payment.class);
                    paymentList.add(payment);
                }
                sortPaymentsDescending();
                paymentAdapter.notifyDataSetChanged();
                if (paymentList.isEmpty()) {
                    noPaymentText.setVisibility(View.VISIBLE);
                } else {
                    noPaymentText.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(),"Something Went Wrong!",Toast.LENGTH_SHORT).show();;
            }
        });

        return view;
    }
    public void sortPaymentsDescending() {
        if (paymentList == null || paymentList.isEmpty()) {
            System.out.println("Payment list is either null or empty.");
            return;
        }

        Collections.sort(paymentList, new Comparator<Payment>() {
            @Override
            public int compare(Payment payment1, Payment payment2) {
                long timestamp1 = payment1.getTimestamp();
                long timestamp2 = payment2.getTimestamp();
                return Long.compare(timestamp2, timestamp1);
            }
        });
    }

}
