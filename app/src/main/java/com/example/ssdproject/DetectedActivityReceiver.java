package com.example.ssdproject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.ssdproject.BuildConfig;
import com.google.android.gms.location.ActivityTransitionEvent;
import com.google.android.gms.location.ActivityTransitionResult;
import com.google.android.gms.location.DetectedActivity;

public class DetectedActivityReceiver extends BroadcastReceiver{

    static final String RECEIVER_ACTION = BuildConfig.APPLICATION_ID + ".com.example.ssdproject.DetectedActivityReceiver";
    private static final String CHANNEL_ID = "activityNotifier" ;

    @Override
    public void onReceive(Context context, Intent intent) {

        if (RECEIVER_ACTION == intent.getAction()) {
            Log.d("DetectedActivityReceive", "Received an unsupported action.");
            return;
        }

        if (ActivityTransitionResult.hasResult(intent)) {

            ActivityTransitionResult result = ActivityTransitionResult.extractResult(intent);
            
            for (ActivityTransitionEvent event : result.getTransitionEvents()) {
                int activity = event.getActivityType();
                int transition = event.getTransitionType();
                String message = "Transition: " + activity + " " + transition;

                // Send notification
                NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(R.drawable.strength)
                        .setContentTitle("It's time to Move!")
                        .setContentText("Do 20 jumping jacks!")
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);

                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

                // Display notification
                notificationManager.notify(1, builder.build());
            }
        }
    }

    private String activityType(int activity){
        switch(activity){
            case DetectedActivity.WALKING:
                return "Walking";
            case DetectedActivity.RUNNING:
                return "Running";
            case DetectedActivity.ON_BICYCLE:
                return "Cycling";
            default:
                 return "Activity type unknown?";
        }

    }
}
