package com.example.datewidgetapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;

import android.content.SharedPreferences;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    // Constants for timer, notifications, and preferences
    private static final String ACTION_START_TIMER = "com.example.datewidgetapp.ACTION_START_TIMER";
    private static final String CHANNEL_ID = "PipetteTimerChannel";
    private static final int NOTIFICATION_ID = 1;
    private static final long ALERT_BEFORE_END = 30000;
    private static final String PREFS_NAME = "SAPPrefs";
    private static final String SCRATCH_PAD_KEY = "ScratchPadText";

    // UI elements
    private TextView dateTextView, julianTextView, weekTextView, pipetteTimerText;
    private Button startStopButton, resetButton;
    private EditText sapScratchPad;

    // Timer variables
    private CountDownTimer countDownTimer;
    private boolean isTimerRunning = false;
    private long timeLeftInMillis = 270000; // 4 minutes 30 seconds

    // Shared preferences for saving scratch pad text
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI elements
        dateTextView = findViewById(R.id.dateTextView);
        julianTextView = findViewById(R.id.julianTextView);
        TextView sapReminderText = findViewById(R.id.sapReminderText);
        pipetteTimerText = findViewById(R.id.pipetteTimerText);
        startStopButton = findViewById(R.id.startStopButton);
        resetButton = findViewById(R.id.resetButton);

        // Update date information and timer text
        updateDateInfo();
        updateTimerText();

        // Set up button click listeners
        startStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isTimerRunning) {
                    pauseTimer();
                } else {
                    startTimer();
                }
            }
        });

        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetTimer();
            }
        });

        // Check if the app was launched to start the timer
        if (ACTION_START_TIMER.equals(getIntent().getAction())) {
            startTimer();
        }

        // Create notification channel for Android Oreo and above
        createNotificationChannel();

        // Request notification permission for Android 13 and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
            }
        }

        // Initialize scratch pad and load saved text
        sapScratchPad = findViewById(R.id.sapScratchPad);
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Load saved text
        String savedText = sharedPreferences.getString(SCRATCH_PAD_KEY, "");
        sapScratchPad.setText(savedText);

        // Set up text change listener to save scratch pad text
        sapScratchPad.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(SCRATCH_PAD_KEY, s.toString());
                editor.apply();
            }
        });

    }

    // Update date information from intent or generate current date
    private void updateDateInfo() {
        // Get date information from intent if available
        String fullDate = getIntent().getStringExtra("fullDate");
        String julianDate = getIntent().getStringExtra("julianDate");


        // If date information is not in the intent, generate it
        if (fullDate == null || julianDate == null) {
            Calendar calendar = Calendar.getInstance();

            // Full date
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());
            fullDate = dateFormat.format(calendar.getTime());

            // Julian date
            int dayOfYear = calendar.get(Calendar.DAY_OF_YEAR);
            int year = calendar.get(Calendar.YEAR) % 100;
            julianDate = String.format(Locale.getDefault(), "%02d%03d", year, dayOfYear);

        }

        // Display the information
        dateTextView.setText("Date: " + fullDate);
        julianTextView.setText("Julian: " + julianDate);

    }

    // Start the countdown timer
    private void startTimer() {
        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                updateTimerText();
            }

            @Override
            public void onFinish() {
                isTimerRunning = false;
                startStopButton.setText("Start");
                timeLeftInMillis = 0;
                updateTimerText();
            }
        }.start();

        isTimerRunning = true;
        startStopButton.setText("Pause");
    }

    // Pause the countdown timer
    private void pauseTimer() {
        countDownTimer.cancel();
        isTimerRunning = false;
        startStopButton.setText("Start");
    }

    // Reset the countdown timer
    private void resetTimer() {
        timeLeftInMillis = 270000;
        updateTimerText();
        if (isTimerRunning) {
            pauseTimer();
        }
    }

    // Create notification channel for Android Oreo and above
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Pipette Timer Channel";
            String description = "Channel for Pipette Timer Alerts";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    // Show notification when 30 seconds are left
    private void showAlertNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification) // Make sure you have this icon in your drawable resources
                .setContentTitle("Pipette Timer Alert")
                .setContentText("30 seconds remaining!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    // Update the timer text display
    private void updateTimerText() {
        int minutes = (int) (timeLeftInMillis / 1000) / 60;
        int seconds = (int) (timeLeftInMillis / 1000) % 60;
        String timeLeftFormatted = String.format(Locale.getDefault(), "%d:%02d", minutes, seconds);
        pipetteTimerText.setText("Pipette Timer: " + timeLeftFormatted);
    }

    // Clean up resources when the activity is destroyed
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}