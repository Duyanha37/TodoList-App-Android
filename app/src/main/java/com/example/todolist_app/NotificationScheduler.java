package com.example.todolist_app;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.Manifest;
import android.os.Build;

import java.time.LocalDateTime;
import java.time.ZoneId;

public final class NotificationScheduler {
    public static final String ACTION_REMINDER =
            "com.example.todolist_app.ACTION_TASK_REMINDER";
    public static final String ACTION_DUE =
            "com.example.todolist_app.ACTION_TASK_DUE";
    public static final int REMINDER_MINUTES = 15;

    private NotificationScheduler() {
    }

    public static void schedule(Context context, Task task) {
        cancel(context, task.getId());
        if (!notificationsEnabled(context) || task.getId() == 0 ||
                task.getDate() == null || task.getTime() == null ||
                TaskHelper.isDone(task)) {
            return;
        }

        LocalDateTime due = LocalDateTime.of(task.getDate(), task.getTime());
        long dueMillis = due.atZone(ZoneId.systemDefault())
                .toInstant().toEpochMilli();
        long reminderMillis = due.minusMinutes(REMINDER_MINUTES)
                .atZone(ZoneId.systemDefault())
                .toInstant().toEpochMilli();
        long now = System.currentTimeMillis();

        if (reminderMillis > now) {
            setAlarm(context, reminderMillis, task.getId(), ACTION_REMINDER,
                    reminderRequestCode(task.getId()));
        }
        if (dueMillis > now) {
            setAlarm(context, dueMillis, task.getId(), ACTION_DUE,
                    dueRequestCode(task.getId()));
        }
    }

    public static void cancel(Context context, int taskId) {
        AlarmManager alarms = (AlarmManager) context.getSystemService(
                Context.ALARM_SERVICE);
        alarms.cancel(pendingIntent(context, taskId, ACTION_REMINDER,
                reminderRequestCode(taskId)));
        alarms.cancel(pendingIntent(context, taskId, ACTION_DUE,
                dueRequestCode(taskId)));
    }

    public static void rescheduleAll(Context context) {
        if (!notificationsEnabled(context)) return;
        for (Task task : TaskHelper.getTasks(context)) schedule(context, task);
    }

    public static void cancelAll(Context context) {
        for (Task task : TaskHelper.getTasks(context)) cancel(context, task.getId());
    }

    public static boolean notificationsEnabled(Context context) {
        boolean enabled = context.getSharedPreferences(
                "profile_data", Context.MODE_PRIVATE)
                .getBoolean("notifications", false);
        return enabled && (Build.VERSION.SDK_INT < 33 ||
                context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                        == PackageManager.PERMISSION_GRANTED);
    }

    private static void setAlarm(Context context, long time, int taskId,
                                 String action, int requestCode) {
        AlarmManager alarms = (AlarmManager) context.getSystemService(
                Context.ALARM_SERVICE);
        PendingIntent operation = pendingIntent(context, taskId, action, requestCode);
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarms.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP, time, operation);
            } else {
                alarms.setExact(AlarmManager.RTC_WAKEUP, time, operation);
            }
        } catch (SecurityException exception) {
            alarms.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP, time, operation);
        }
    }

    private static PendingIntent pendingIntent(Context context, int taskId,
                                               String action, int requestCode) {
        Intent intent = new Intent(context, TaskAlarmReceiver.class)
                .setAction(action)
                .putExtra(TaskAlarmReceiver.EXTRA_TASK_ID, taskId);
        return PendingIntent.getBroadcast(context, requestCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT |
                        PendingIntent.FLAG_IMMUTABLE);
    }

    private static int reminderRequestCode(int taskId) {
        return taskId * 2;
    }

    private static int dueRequestCode(int taskId) {
        return taskId * 2 + 1;
    }
}
