package com.example.todolist_app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class TaskAlarmReceiver extends BroadcastReceiver {
    public static final String EXTRA_TASK_ID = "taskId";

    @Override
    public void onReceive(Context context, Intent intent) {
        int taskId = intent.getIntExtra(EXTRA_TASK_ID, 0);
        if (taskId == 0 || !NotificationScheduler.notificationsEnabled(context)) {
            return;
        }

        Intent service = new Intent(context, TaskNotificationService.class)
                .setAction(intent.getAction())
                .putExtra(EXTRA_TASK_ID, taskId);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(service);
        } else {
            context.startService(service);
        }
    }
}
