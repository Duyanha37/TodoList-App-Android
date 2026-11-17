package com.example.todolist_app;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

public class TaskNotificationService extends Service {
    private static final String CHANNEL_ID = "task_reminders";
    private static final int FOREGROUND_ID = 7000;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createChannel();
        int taskId = intent == null ? 0 :
                intent.getIntExtra(TaskAlarmReceiver.EXTRA_TASK_ID, 0);
        Task task = taskId == 0 ? null : TaskHelper.find(this, taskId);
        if (task == null || TaskHelper.isDone(task)) {
            stopSelf(startId);
            return START_NOT_STICKY;
        }

        boolean due = NotificationScheduler.ACTION_DUE.equals(
                intent.getAction());
        String title = due ? "Task due now" : "Task coming up";
        String text = due ? task.getTitle() :
                task.getTitle() + " is due in 15 minutes";

        Intent openTask = new Intent(this, TaskDetailsActivity.class)
                .putExtra("taskId", taskId)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(this, taskId,
                openTask, PendingIntent.FLAG_UPDATE_CURRENT |
                        PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(text)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
                .setContentIntent(contentIntent)
                .setAutoCancel(true)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setDefaults(NotificationCompat.DEFAULT_SOUND |
                        NotificationCompat.DEFAULT_VIBRATE)
                .build();

        startForeground(FOREGROUND_ID + taskId, notification);
        stopForeground(false);
        stopSelf(startId);
        return START_NOT_STICKY;
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID, "Task reminders",
                NotificationManager.IMPORTANCE_HIGH);
        channel.setDescription("Reminders before and at task time");
        channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        channel.enableVibration(true);
        getSystemService(NotificationManager.class)
                .createNotificationChannel(channel);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
