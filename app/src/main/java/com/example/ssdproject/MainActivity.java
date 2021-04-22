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
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ToggleButton;

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
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private static final String CHANNEL_ID = "1";
    DetectedActivityReceiver receiver;
    PendingIntent pendingIntent;
    String selectedExercises[] = {"Initial"};
    Timer timer = new Timer("Activity Timer");
    final String[] strengthExercises = {
        "20 Jumping Jacks", "10 Push-Ups", "15 Sit Ups", "15 Crunches"
    };
    final String[] yogaExercises = {
            "30 sec Down Dog", "30 sec Cat Cow Pose", "20 sec Back Bend", " 25 sec Forward Fold"
    };


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

    }

    public void go_buttonClicked(View view) {
        // Array for selected exercises

        // Get toggle buttons
        ToggleButton strength_toggle = (ToggleButton) findViewById(R.id.strength_toggle);
        ToggleButton yoga_toggle = (ToggleButton) findViewById(R.id.yoga_toggle);
        TextView bothSelected = (TextView) findViewById(R.id.bothSelected);
        if(strength_toggle.isChecked()){
            selectedExercises = strengthExercises;
            bothSelected.setVisibility(View.INVISIBLE);
        }
        if(strength_toggle.isChecked() && yoga_toggle.isChecked()){
                bothSelected.setVisibility(View.VISIBLE);
            return;
        }
        if(yoga_toggle.isChecked()){
            selectedExercises = yogaExercises;
            bothSelected.setVisibility(View.INVISIBLE);
        }
        if(!yoga_toggle.isChecked() && !strength_toggle.isChecked()){
            bothSelected.setVisibility(View.VISIBLE);
            return;
        }
        // Hide go button, show stop button
        Button go_button = (Button) findViewById(R.id.go_button);
        go_button.setVisibility(View.INVISIBLE);
        Button stop_button = (Button) findViewById(R.id.stop_button);
        stop_button.setVisibility(View.VISIBLE);
        // Get progress bar and text view
        TextView monitoring_view = (TextView) findViewById(R.id.monitoring_view);
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
        TextView suggestedExercise_Test = (TextView) findViewById(R.id.suggestedExercise_text);


        // Set up timer
        TimerTask timerTask = new TimerTask() {
            public void run() {
                // Randomly select exercise from the list
                Random random = new Random();
                int random_int = random.nextInt(selectedExercises.length);
                String random_exercise = selectedExercises[random_int];
                // Set up notification
                NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                        .setSmallIcon(R.drawable.run)
                        .setContentTitle("It's time to Move!")
                        .setContentText("Do " + random_exercise + "!")
                        .setPriority(NotificationCompat.PRIORITY_MAX);

                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());

                // Display notification
                notificationManager.notify(1, builder.build());
                suggestedExercise_Test.setText("Suggested Exercise: " + random_exercise);
            }
        };
        long delay = 15000;
        long interval = 7000;
        timer.scheduleAtFixedRate(timerTask, delay, interval);

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
                    progressBar.setVisibility(View.VISIBLE);
                    monitoring_view.setVisibility(View.VISIBLE);
                    }
                }
        );

        task.addOnFailureListener(
                new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        Log.d("ActivityRecognition:","Transitions API registration has failed");
                        progressBar.setVisibility(View.INVISIBLE);
                        monitoring_view.setVisibility(View.INVISIBLE);
                    }
                }
        );
    }
    public void stop_buttonClicked(View view) {
        // Hide go button, show stop button
        Button go_button = (Button) findViewById(R.id.go_button);
        go_button.setVisibility(View.VISIBLE);
        Button stop_button = (Button) findViewById(R.id.stop_button);
        stop_button.setVisibility(View.INVISIBLE);
        // Show progress bar and textview
        TextView monitoring_view = (TextView) findViewById(R.id.monitoring_view);
        monitoring_view.setVisibility(View.INVISIBLE);
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);

        // Stop executing tasks from timer
        timer.purge();

        // Stop monitoring activity
        stopActivityRecognition();
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