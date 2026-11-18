package com.example.todolist_app;

import android.content.BroadcastReceiver;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.content.ContextCompat;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class TaskAlarmReceiver extends BroadcastReceiver {

    public static final String EXTRA_TASK_ID = "taskId";

    private static final String CHANNEL_ID = "task_reminders";

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent == null) {
            return;
        }

        int taskId = intent.getIntExtra(EXTRA_TASK_ID, 0);

        if (taskId == 0) {
            return;
        }

        // Lấy thông tin Task
        Task task = TaskHelper.find(context, taskId);

        if (task == null || TaskHelper.isDone(task)) {
            return;
        }

        // Kiểm tra thông báo của đúng tài khoản
        if (!NotificationScheduler.notificationsEnabled(
                context, task.getUserId())) {
            return;
        }

        // Xác định loại thông báo
        boolean due = NotificationScheduler.ACTION_DUE.equals(
                intent.getAction());

        String title = due ? "Task due now" : "Task coming up";

        String text = due ? task.getTitle()
                : task.getTitle() + " is due in 15 minutes";

        // Tạo Notification Channel
        createChannel(context);

        // Khi nhấn thông báo sẽ mở chi tiết Task
        Intent openTask = new Intent(context, TaskDetailsActivity.class);
        openTask.putExtra("taskId", taskId);
        openTask.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_CLEAR_TOP);

        int notificationId = taskId * 2 + (due ? 1 : 0);

        PendingIntent contentIntent = PendingIntent.getActivity(
                context,
                notificationId,
                openTask,
                PendingIntent.FLAG_UPDATE_CURRENT |
                        PendingIntent.FLAG_IMMUTABLE
        );

        // Tạo thông báo
        Notification notification =
                new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(android.R.drawable.ic_dialog_info)
                        .setContentTitle(title)
                        .setContentText(text)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(text))
                        .setContentIntent(contentIntent)
                        .setAutoCancel(true)
                        .setCategory(NotificationCompat.CATEGORY_REMINDER)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                        .setDefaults(NotificationCompat.DEFAULT_SOUND |
                                NotificationCompat.DEFAULT_VIBRATE)
                        .build();

        // Hiển thị thông báo trực tiếp, không chạy Foreground Service
        NotificationManagerCompat.from(context).notify(
                notificationId, notification);
    }

    private void createChannel(Context context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Task reminders",
                    NotificationManager.IMPORTANCE_HIGH
            );

            channel.setDescription(
                    "Reminders before and at task time");

            channel.enableVibration(true);

            NotificationManager manager =
                    context.getSystemService(NotificationManager.class);

            manager.createNotificationChannel(channel);
        }
    }
}
