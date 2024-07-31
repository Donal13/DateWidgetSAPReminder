package com.example.datewidgetapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.widget.RemoteViews;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class DateWidgetProvider extends AppWidgetProvider {
    // Action for starting the timer
    private static final String ACTION_START_TIMER = "com.example.datewidgetapp.ACTION_START_TIMER";

    // Called when the widget is updated
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    // Handles broadcast intents
    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (ACTION_START_TIMER.equals(intent.getAction())) {
            startTimerInMainActivity(context);
        }
    }

    // Updates the widget's UI
    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);

        Calendar calendar = Calendar.getInstance();

        // Set full date
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());
        String fullDate = dateFormat.format(calendar.getTime());
        views.setTextViewText(R.id.fullDateText, "Date: " + fullDate);

        // Set Julian date
        int dayOfYear = calendar.get(Calendar.DAY_OF_YEAR);
        int year = calendar.get(Calendar.YEAR) % 100;
        String julianDate = String.format(Locale.getDefault(), "%02d%03d", year, dayOfYear);
        views.setTextViewText(R.id.julianDateText, "Julian: " + julianDate);

        // Set SAP Reminder text
        views.setTextViewText(R.id.sapReminderText, "Don't forget to SAP");

        // Create an Intent to launch MainActivity when the widget is clicked
        Intent mainActivityIntent = new Intent(context, MainActivity.class);
        PendingIntent mainActivityPendingIntent = PendingIntent.getActivity(context, 0, mainActivityIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Set the pending intent to the entire RemoteViews
        views.setOnClickPendingIntent(R.id.widget_layout_root, mainActivityPendingIntent);

        // Set up the intent for the start timer button
        Intent startTimerIntent = new Intent(context, DateWidgetProvider.class);
        startTimerIntent.setAction(ACTION_START_TIMER);
        PendingIntent startTimerPendingIntent = PendingIntent.getBroadcast(context, 1, startTimerIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.startPipetteButton, startTimerPendingIntent);

        // Update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    // Starts the timer in MainActivity
    private static void startTimerInMainActivity(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setAction(ACTION_START_TIMER);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}