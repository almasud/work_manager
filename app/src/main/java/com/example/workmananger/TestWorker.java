package com.example.workmananger;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Data;
import androidx.work.ForegroundInfo;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.UUID;

public class TestWorker extends Worker {
    private static final String TAG = "TestWorker";
    public static final int NOTIFICATION_ID_ONE_TIME_REQUEST = 1;
    public static final int NOTIFICATION_ID_PERIODIC_REQUEST = 2;
    public static final int NOTIFICATION_ID_ONE_TIME_FOREGROUND_REQUEST = 3;
    private static final String CHANNEL_ID_WORK_MANAGER = "Channel_ID_Work_Manager";
    public static final String NOTIFICATION_TITLE = "Notification_Title";
    public static final String NOTIFICATION_BODY = "Notification_Body";
    public static final String NOTIFICATION_ID = "Notification_ID";
    public static final String PROGRESS = "Progress";

    public TestWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        createNotificationChannel(
                CHANNEL_ID_WORK_MANAGER, getApplicationContext().getResources().getString(R.string.work_manager)
        );
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "doWork: is called");
        try {
            // code to be executed
            // Get input data from work request
            Data inputData = getInputData();
            String notificationTitle = inputData.getString(NOTIFICATION_TITLE);
            String notificationBody = inputData.getString(NOTIFICATION_BODY);
            int notificationId = inputData.getInt(NOTIFICATION_ID, 0);
            // Show the input data in notification
            if (notificationId == NOTIFICATION_ID_ONE_TIME_FOREGROUND_REQUEST) {
                setForegroundAsync(
                        createWorkerForegroundInfo(notificationTitle, notificationBody, notificationId, getId())
                );
                int maxProgress = 100;
                while (true) {
                    try {
                        Thread.sleep(10000);
                        setProgressAsync(new Data.Builder().putInt(PROGRESS, 0).build());
//                    showNotificationWithProgress(notificationTitle, notificationBody, i, maxProgress, notificationId);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                showNotification(notificationTitle, notificationBody, notificationId);
                int maxProgress = 10;
                for (int i=0; i < maxProgress; i++) {
                    try {
                        Thread.sleep(1000);
                        setProgressAsync(new Data.Builder().putInt(PROGRESS, i).build());
                        showNotificationWithProgress(notificationTitle, notificationBody, i, maxProgress, notificationId);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                finishProgress(notificationTitle, "Work completed", notificationId);
            }

            return Result.success();
        } catch (Throwable throwable) {
            // clean up and log
            return Result.failure();
        }
    }

    private ForegroundInfo createWorkerForegroundInfo(String title, String body, int notificationId, UUID workRequestId) {
        // Used to cancel the work
        PendingIntent cancelPendingIntent = WorkManager.getInstance(getApplicationContext()).createCancelPendingIntent(workRequestId);

        // Prepare a pending intent
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID_WORK_MANAGER)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(title)
                        .setTicker(title)
                        .setContentText(body)
                        .setContentIntent(pendingIntent)
                        .setOngoing(true)
                        .addAction(
                                R.drawable.ic_baseline_delete,
                                getApplicationContext().getResources().getString(R.string.cancel),
                                cancelPendingIntent
                        );

        return new ForegroundInfo(notificationId, notificationBuilder.build());
    }

    public void showNotification(String title, String body, int notificationId) {
        Log.d(TAG, "showNotification: is called");

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(getApplicationContext());
        // Prepare a pending intent
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);
        // Notification builder
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID_WORK_MANAGER)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(title)
                        .setTicker(title)
                        .setContentText(body)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true);

        notificationManagerCompat.notify(notificationId, notificationBuilder.build());
    }

    private void showNotificationWithProgress(String title, String body, int progress, int maxProgress, int notificationId) {
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(getApplicationContext());
        // Notification builder
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID_WORK_MANAGER)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(title)
                        .setTicker(title)
                        .setContentText(body)
                        .setOngoing(true)
                .setProgress(maxProgress, progress, false);
        notificationManagerCompat.notify(notificationId, notificationBuilder.build());
    }

    public void finishProgress(String title, String body, int notificationId) {
        Log.d(TAG, "finishProgress: is called");

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(getApplicationContext());
        // Prepare a pending intent
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);
        // Notification builder
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID_WORK_MANAGER)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(title)
                        .setTicker(title)
                        .setContentText(body)
                        .setProgress(0, 0, false)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true);

        notificationManagerCompat.notify(notificationId, notificationBuilder.build());
    }

    private void createNotificationChannel(String channelId, String name) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(
                    channelId, name, NotificationManager.IMPORTANCE_DEFAULT
            );

            notificationChannel.setLightColor(Color.GREEN);
            // Set the created channel in notification manager
            NotificationManager notificationManager = getApplicationContext().getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(notificationChannel);
                Log.d(TAG, "createDownloaderNotificationChannel: Notification channel is created.");
            }
        }
    }
}
