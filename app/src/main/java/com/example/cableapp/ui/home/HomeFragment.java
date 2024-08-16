package com.example.cableapp.ui.home;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.cableapp.Complaint;
import com.example.cableapp.ComplaintActivity;
import com.example.cableapp.CustomerDetailsActivity;
import com.example.cableapp.FirebaseNotificationHelper;
import com.example.cableapp.Payment;
import com.example.cableapp.PaymentActivity;
import com.example.cableapp.R;
import com.example.cableapp.SendSignalAlertActivity;
import com.example.cableapp.WorkerListActivity;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.Timestamp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class HomeFragment extends Fragment {

    private Animation fabCloseAnim;
    private FloatingActionButton fabMain;
    private Animation fabOpenAnim;
    private FloatingActionButton fabOption1;
    private FloatingActionButton fabOption2;
    private FloatingActionButton fabOption3;
    public boolean isFabOpen = false;
    private TextView complaintsCount;
    private TextView duesCount;
    private TextView expiresCount;
    private ProgressBar complaintsProgress;
    private ProgressBar duesProgress;
    private ProgressBar expiresProgress;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        complaintsCount = view.findViewById(R.id.search_by_name).findViewById(R.id.complaints_count);
        duesCount = view.findViewById(R.id.search_by_serial_number).findViewById(R.id.dues_count);
        expiresCount = view.findViewById(R.id.search_by_mobile).findViewById(R.id.expires_count);
        complaintsProgress = view.findViewById(R.id.complaints_progress);
        duesProgress = view.findViewById(R.id.dues_progress);
        expiresProgress = view.findViewById(R.id.expires_progress);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        fetchExpiringAccountsCount(databaseReference);
        fetchDataAndSetProgress(databaseReference);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getActivity().getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.bg_color));
        }


        ((MaterialCardView) view.findViewById(R.id.home_customer_details)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                HomeFragment.this.startActivity(new Intent(HomeFragment.this.getActivity(), CustomerDetailsActivity.class));
            }
        });
        ((MaterialCardView) view.findViewById(R.id.home_complaints)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                HomeFragment.this.startActivity(new Intent(HomeFragment.this.getActivity(), ComplaintActivity.class));
            }
        });
        ((MaterialCardView) view.findViewById(R.id.home_dues)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                HomeFragment.this.startActivity(new Intent(HomeFragment.this.getContext(), PaymentActivity.class));
            }
        });
        ((MaterialCardView) view.findViewById(R.id.home_signal_alert)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                HomeFragment.this.startActivity(new Intent(HomeFragment.this.getContext(), SendSignalAlertActivity.class));
            }
        });

        ((TextView) view.findViewById(R.id.send_notification)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                 FirebaseNotificationHelper notificationHelper;
                notificationHelper = new FirebaseNotificationHelper(getContext());

                // Example: Send notification to a specific token
                notificationHelper.sendNotificationToToken(
                        "ctJWL4IXSQalyMtcwNCzfF:APA91bHqOS3BiTd1ycGLtQ2EBjr_gyQeAnUs-hu8Nd-fxz2qKCFHb2FXwxxwzGeW4zz7KgrG-f4erJoiZa8rw0S1tmPDItxXC3a3fVhRcq8cy1Fg3ONUKw2ozKKLOZqDiwmy_EC16DA0",
                        "Notification Title",
                        "This is a test notification",
                        null
                );

                notificationHelper.sendNotificationToAll(
                        "Notification Title",
                        "This is a test notification for batch",
                        null
                );
            }
        });


        this.fabMain = (FloatingActionButton) view.findViewById(R.id.fab_main);
        this.fabOption1 = (FloatingActionButton) view.findViewById(R.id.fab_option1);
        this.fabOption2 = (FloatingActionButton) view.findViewById(R.id.fab_option2);
        this.fabOption3 = (FloatingActionButton) view.findViewById(R.id.fab_option3);
        this.fabOpenAnim = AnimationUtils.loadAnimation(getContext(), R.anim.fab_show);
        this.fabCloseAnim = AnimationUtils.loadAnimation(getContext(), R.anim.fab_hide);
        this.fabMain.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (HomeFragment.this.isFabOpen) {
                    HomeFragment.this.closeFabMenu();
                } else {
                    HomeFragment.this.openFabMenu();
                }
            }
        });

        return view;
    }

    private void fetchExpiringAccountsCount(DatabaseReference databaseReference) {
        databaseReference.child("user-base/sscn/mandapeta/").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long maxCount=0;
                long count = 0;
                String todayDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    maxCount ++;
                    String expiryDate = userSnapshot.child("expiryNo").getValue(String.class);
                    if (expiryDate != null && expiryDate.equals(todayDate)) {
                        count++;
                    }
                }
                expiresCount.setText(String.valueOf(count));
                expiresProgress.setMax((int) maxCount); // Assuming max value is 100
                expiresProgress.setProgress((int) count); // Set progress value here
                animateCount(expiresCount, 0, (int) count);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle possible errors.
            }
        });
    }

    private void fetchDataAndSetProgress(DatabaseReference databaseReference) {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int maxCount = 0, dueMaxCount = 0;
                long complaintsSolved = 0;
                int duesCountNo = 0;

                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
                String todayDate = dateFormat.format(new Date());

                // Iterate through complaints
                for (DataSnapshot complaintSnapshot : snapshot.child("complaints/mandapeta").getChildren()) {
                    for (DataSnapshot dataSnapshot : complaintSnapshot.getChildren()) {
                        Complaint complaint = dataSnapshot.getValue(Complaint.class);
                        if (complaint != null && isTimestampToday(complaint.getTimestamp(), todayDate)) {
                            maxCount++;
                            if (complaint.getStatus().equalsIgnoreCase("solved")) {
                                complaintsSolved++;
                            }
                        }
                    }
                }
                complaintsCount.setText(String.valueOf(maxCount));
                complaintsProgress.setMax(maxCount);
                complaintsProgress.setProgress((int) complaintsSolved);
                animateCount(complaintsCount, 0, (int) maxCount);

                for (DataSnapshot paymentSnapshot : snapshot.child("payments/mandapeta").getChildren()) {
                    for (DataSnapshot dataSnapshot : paymentSnapshot.getChildren()) {
                        duesCountNo++;
                        Payment payment = dataSnapshot.getValue(Payment.class);
                        if (payment != null && payment.getStatus().equals("Due")) {
                            dueMaxCount++;
                        }
                    }
                }
                duesCount.setText(String.valueOf(dueMaxCount));
                duesProgress.setMax(duesCountNo); // Assuming max value is 100
                duesProgress.setProgress(dueMaxCount);
                animateCount(duesCount, 0, (int) dueMaxCount
                );
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle possible errors
            }
        });
    }

    private void animateCount(TextView textView, int start, int end) {
        ValueAnimator animator = ValueAnimator.ofInt(start, end);
        animator.setDuration(1500); // Duration of the animation in milliseconds
        animator.addUpdateListener(animation -> textView.setText(animation.getAnimatedValue().toString()));
        animator.start();
    }

    /* access modifiers changed from: private */
    public void openFabMenu() {
        this.isFabOpen = true;
        this.fabOption1.setVisibility(View.VISIBLE);
        this.fabOption2.setVisibility(View.VISIBLE);
        this.fabOption3.setVisibility(View.VISIBLE);
        this.fabOption1.startAnimation(this.fabOpenAnim);
        this.fabOption2.startAnimation(this.fabOpenAnim);
        this.fabOption3.startAnimation(this.fabOpenAnim);
        this.fabOption1.setClickable(true);
        this.fabOption2.setClickable(true);
        this.fabOption3.setClickable(true);
        this.fabMain.setImageResource(R.drawable.baseline_clear);
    }

    /* access modifiers changed from: private */
    public void closeFabMenu() {
        this.isFabOpen = false;
        this.fabOption1.startAnimation(this.fabCloseAnim);
        this.fabOption2.startAnimation(this.fabCloseAnim);
        this.fabOption3.startAnimation(this.fabCloseAnim);
        this.fabOption1.setClickable(false);
        this.fabOption2.setClickable(false);
        this.fabOption3.setClickable(false);
        this.fabOption1.setVisibility(View.GONE);
        this.fabOption2.setVisibility(View.GONE);
        this.fabOption3.setVisibility(View.GONE);
        this.fabMain.setImageResource(android.R.drawable.ic_input_add);
    }

    private boolean isTimestampToday(long timestampMillis, String todayDate) {
        // Convert timestamp to date string in "yyyyMMdd" format
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        String complaintDate = dateFormat.format(new Date(timestampMillis));

        // Check if the dates are the same
        return todayDate.equals(complaintDate);
    }
}
