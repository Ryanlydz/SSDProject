package com.example.ssdproject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.ssdproject.BuildConfig;
import com.google.android.gms.location.ActivityTransitionEvent;
import com.google.android.gms.location.ActivityTransitionResult;
import com.google.android.gms.location.DetectedActivity;

public class DetectedActivityReceiver extends BroadcastReceiver{

    static final String RECEIVER_ACTION = BuildConfig.APPLICATION_ID + ".com.example.ssdproject.DetectedActivityReceiver";

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