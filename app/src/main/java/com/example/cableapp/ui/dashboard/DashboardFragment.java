package com.example.cableapp.ui.dashboard;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.cableapp.Customer;
import com.example.cableapp.CustomerStatusReportActivity;
import com.example.cableapp.Payment;
import com.example.cableapp.PaymentDownloadActivity;
import com.example.cableapp.PlansActivity;
import com.example.cableapp.R;
import com.example.cableapp.WorkerListActivity;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import org.eazegraph.lib.charts.PieChart;
import org.eazegraph.lib.models.PieModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;

public class DashboardFragment extends Fragment {
    TextView expired, active, deactive, total,others;
    PieChart pieChart;
    private LineChart lineChart;
    private BarChart barChart;
    private DatabaseReference databaseReference;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getActivity().getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.bg_color));
        }

        ( view.findViewById(R.id.cardViewGraph)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                DashboardFragment.this.startActivity(new Intent(DashboardFragment.this.getActivity(), CustomerStatusReportActivity.class));
            }
        });
        ( view.findViewById(R.id.dashboard_payment_download)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                DashboardFragment.this.startActivity(new Intent(DashboardFragment.this.getActivity(), PaymentDownloadActivity.class));
            }
        });

        ( view.findViewById(R.id.dashboard_plans_details)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                DashboardFragment.this.startActivity(new Intent(DashboardFragment.this.getActivity(), PlansActivity.class));
            }
        });

        ( view.findViewById(R.id.home_workers_details)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                DashboardFragment.this.startActivity(new Intent(DashboardFragment.this.getActivity(), WorkerListActivity.class));
            }
        });
        databaseReference = FirebaseDatabase.getInstance().getReference();

        expired = view.findViewById(R.id.tvR);
        active = view.findViewById(R.id.tvPython);
        deactive = view.findViewById(R.id.tvCPP);
        total = view.findViewById(R.id.cutomer_total);
        pieChart = view.findViewById(R.id.piechart);
        others = view.findViewById(R.id.tvJava);

        setData();
        barChart = view.findViewById(R.id.barChart);

        // Retrieve data from Firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("payments/mandapeta");

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Payment> payments = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    for (DataSnapshot snapshot1:snapshot.getChildren()){

                        Payment payment = snapshot1.getValue(Payment.class);
                        payments.add(payment);
                    }
                }
                generateGraphs(payments);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.w("MainActivity", "Failed to read value.", error.toException());
            }
        });
        return view;

    }

    private void generateGraphs(List<Payment> payments) {
        Map<String, Float> dailyCollection = new TreeMap<>();
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Kolkata"));
        int currentDay = calendar.get(Calendar.DAY_OF_MONTH);
        int currentMonth = calendar.get(Calendar.MONTH);
        int currentYear = calendar.get(Calendar.YEAR);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM", Locale.getDefault());

        for (Payment payment : payments) {
            calendar.setTimeInMillis(payment.getTimestamp());
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            int month = calendar.get(Calendar.MONTH);
            int year = calendar.get(Calendar.YEAR);
            if (month == currentMonth && year == currentYear) {
                String dateKey = sdf.format(calendar.getTime());
                dailyCollection.put(dateKey, (float) (dailyCollection.getOrDefault(dateKey, 0f) + payment.getPaidAmount()));
            }
        }

        List<BarEntry> barEntries = new ArrayList<>();
        List<String> xAxisLabels = new ArrayList<>();
        Calendar startOfMonth = Calendar.getInstance(TimeZone.getTimeZone("Asia/Kolkata"));
        startOfMonth.set(currentYear, currentMonth, currentDay, 0, 0, 0);
        startOfMonth.set(Calendar.MILLISECOND, 0);
        Calendar endOfMonth = (Calendar) startOfMonth.clone();
        endOfMonth.add(Calendar.DAY_OF_MONTH, -5);

        int dayIndex = 0;
        while (!startOfMonth.before(endOfMonth)) {
            String dateKey = sdf.format(startOfMonth.getTime());
            xAxisLabels.add(displayFormat.format(startOfMonth.getTime()));
            float amount = dailyCollection.getOrDefault(dateKey, 0f);
            barEntries.add(new BarEntry(dayIndex++, amount));
            startOfMonth.add(Calendar.DAY_OF_MONTH, -1);
        }

        BarDataSet barDataSet = new BarDataSet(barEntries, "");
        BarData barData = new BarData(barDataSet);
        barChart.setData(barData);
        barChart.getLegend().setEnabled(false);
        barData.setValueTextColor(R.color.white);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(xAxisLabels));
        xAxis.setTextSize(10f);
        xAxis.setDrawAxisLine(true);
        xAxis.setDrawGridLines(false);
        barChart.setExtraBottomOffset(20f);

        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setGranularity(1f);
        barChart.getAxisRight().setEnabled(false);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setDrawLabels(true);
        leftAxis.setDrawAxisLine(true);
        leftAxis.setDrawGridLines(true);
        leftAxis.setAxisLineColor(Color.BLACK);
        leftAxis.setAxisLineWidth(1f);
        leftAxis.setTextSize(10f);

        xAxis.setAxisLineColor(Color.BLACK);
        xAxis.setAxisLineWidth(1f);
        xAxis.setDrawLabels(true);

        leftAxis.setAxisLineColor(Color.BLACK);
        leftAxis.setAxisLineWidth(1f);
        leftAxis.setTextSize(10f);

        barChart.getDescription().setEnabled(false);
        barDataSet.setValueTextColor(R.color.white);
        barDataSet.setValueTextSize(16f);

        barChart.setTouchEnabled(true);
        barChart.setDragEnabled(true);
        barChart.setScaleEnabled(true);
        barChart.animateY(2000);
        barChart.setExtraLeftOffset(20f);
        barChart.invalidate();
    }

    private void setData()
    {
        databaseReference.child("user-base/sscn/mandapeta").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int activeCount = 0;
                int expireCount = 0;
                int deactiveCount = 0;
                int othersCount = 0;
                int totalCount = 0;
                for (DataSnapshot dataSnapshot:snapshot.getChildren()){
                    if (dataSnapshot.exists()){
                        totalCount++;
                        Customer customer = dataSnapshot.getValue(Customer.class);
                        switch (customer.getStatus().toLowerCase()){
                            case "active":
                                activeCount++;
                                break;
                            case "deactive":
                                deactiveCount++;
                                break;
                            case "expired":
                                expireCount++;
                                break;
                            default:
                                othersCount++;
                                break;
                        }

                    }
                }
                total.setText(String.valueOf(totalCount));
                active.setText(String.valueOf(activeCount));
                deactive.setText(String.valueOf(deactiveCount));
                expired.setText(String.valueOf(expireCount));
                others.setText(String.valueOf(othersCount));
                getPieChart();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void getPieChart(){


        // Set the data and color to the pie chart
        pieChart.addPieSlice(
                new PieModel(
                        "Expired",
                        Integer.parseInt(expired.getText().toString()),
                        Color.parseColor("#FFA726")));
        pieChart.addPieSlice(
                new PieModel(
                        "Active",
                        Integer.parseInt(active.getText().toString()),
                        Color.parseColor("#66BB6A")));
        pieChart.addPieSlice(
                new PieModel(
                        "Deactive",
                        Integer.parseInt(deactive.getText().toString()),
                        Color.parseColor("#EF5350")));
        pieChart.addPieSlice(
                new PieModel(
                        "Others",
                        Integer.parseInt(others.getText().toString()),
                        Color.parseColor("#29B6F6")));

        pieChart.startAnimation();
    }
}