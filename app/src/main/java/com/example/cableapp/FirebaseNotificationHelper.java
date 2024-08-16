package com.example.cableapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;

import java.util.HashMap;
import java.util.Map;

public class FirebaseNotificationHelper {

    private static final String TAG = "FirebaseNotificationHelper";
    private static final String CHANNEL_ID = "CableMitraNotifications"; // Unique identifier for the notification channel.
    private static final int NOTIFICATION_ID = 1;

    private Context context;

    public FirebaseNotificationHelper(Context context) {
        this.context = context;
        createNotificationChannel();
    }

    // Method to send notification to a specific token
    public void sendNotificationToToken(String token, String title, String description, String imageUrl) {
        FirebaseMessaging fm = FirebaseMessaging.getInstance();

        Map<String, String> data = new HashMap<>();
        data.put("title", title);
        data.put("description", description);
        if (imageUrl != null) {
            data.put("imageUrl", imageUrl);
        }

        RemoteMessage message = new RemoteMessage.Builder(token)
                .setMessageId(Integer.toString(NOTIFICATION_ID))
                .addData("title", title)
                .addData("description", description)
                .build();

        fm.send(message);
        showNotification(title, description);


    }

    // Method to send notification to all tokens in the database
    public void sendNotificationToAll(String title, String description, String imageUrl) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users_fcm_token");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    for (DataSnapshot tokenSnapshot : snapshot.getChildren()) {
                        String token = tokenSnapshot.getValue(String.class);
                        if (token != null && !token.isEmpty()) {
                            sendNotificationToToken(token, title, description, imageUrl);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Failed to read tokens from database", databaseError.toException());
            }
        });
    }

    // Method to show a notification locally on the device
    private void showNotification(String title, String description) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notifications_black_24dp)
                .setContentTitle(title)
                .setContentText(description)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    // Method to create a notification channel for Android O and above
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "General Notifications"; // The user-visible name of the channel.
            String description = "Includes all general notifications"; // The user-visible description of the channel.
            int importance = NotificationManager.IMPORTANCE_DEFAULT; // The importance level for this channel.
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
