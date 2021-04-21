package com.example.ssdproject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityTransition;
import com.google.android.gms.location.ActivityTransitionRequest;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private static final String CHANNEL_ID = "1";
    DetectedActivityReceiver receiver;
    PendingIntent pendingIntent;

    public void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        createNotificationChannel();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Create a list of activities
        List<ActivityTransition> transitions = new ArrayList<>();

        //Walking
        transitions.add(
                new ActivityTransition.Builder()
                        .setActivityType(DetectedActivity.WALKING)
                        .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                        .build());
        //Running
        transitions.add(
                new ActivityTransition.Builder()
                        .setActivityType(DetectedActivity.RUNNING)
                        .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                        .build());
        // Cycling
        transitions.add(
                new ActivityTransition.Builder()
                        .setActivityType(DetectedActivity.ON_BICYCLE)
                        .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                        .build());

        // Create ActivityTransitionRequest object
        ActivityTransitionRequest request = new ActivityTransitionRequest(transitions);

        Intent intent = new Intent(DetectedActivityReceiver.RECEIVER_ACTION);

        pendingIntent = PendingIntent.getBroadcast(this, 0, intent,0);

         receiver = new DetectedActivityReceiver();

        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, new IntentFilter(DetectedActivityReceiver.RECEIVER_ACTION));


        Task<Void> task = ActivityRecognition.getClient(this)
                .requestActivityTransitionUpdates(request, pendingIntent);
        task.addOnSuccessListener(
                new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                       Log.d("ActivityRecognition:","Transitions API successfully registered");
                    }
                }
        );

        task.addOnFailureListener(
                new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        Log.d("ActivityRecognition:","Transitions API registration has failed");
                    }
                }
        );

        // Set up notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.run)
                .setContentTitle("It's time to Move!")
                .setContentText("Do 20 jumping jacks!")
                .setPriority(NotificationCompat.PRIORITY_MAX);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        // Set up timer
        TimerTask timerTask = new TimerTask() {
            public void run() {
                // Display notification
                notificationManager.notify(1, builder.build());
                System.out.println("Notification should have displayed!");
            }
        };
        Timer timer = new Timer("Activity Timer");
        long delay = 2000;
        timer.schedule(timerTask, delay);
    }

    private void stopActivityRecognition(){

        Task<Void> task = ActivityRecognition.getClient(this)
                .removeActivityTransitionUpdates(pendingIntent);

        task.addOnSuccessListener(
                new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        pendingIntent.cancel();
                        Log.d("ActivityRecognition","Transitions API successfully unregistered");
                    }
                }
        );

        task.addOnFailureListener(
                new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        Log.e("ActivityRegister", e.getMessage());
                    }
                }
        );

    }
}