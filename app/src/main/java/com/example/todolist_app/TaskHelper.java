package com.example.todolist_app;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public final class TaskHelper {
    public static final String STATUS_NOT_STARTED = "Not Started";
    public static final String STATUS_IN_PROGRESS = "In Progress";
    public static final String STATUS_COMPLETED = "Completed";
    public static final String GROUP_OVERDUE = "Overdue";
    public static final String GROUP_TODAY = "Today";
    public static final String GROUP_UPCOMING = "Upcoming";

    public static final String[] CATEGORIES = {
            "Work", "Personal", "Health", "Finance", "Learning"
    };
    public static final int[] COLORS = {
            0xFF6051D8, 0xFF9634E8, 0xFF288438,
            0xFFFF8000, 0xFF008CC9
    };
    public static final String[] PROJECTS = {
            "Q4 Product Launch", "Website Redesign",
            "Fitness Goal", "Side Project"
    };

    private TaskHelper() {
    }

    public static String today(int plusDays) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, plusDays);
        return new SimpleDateFormat("yyyy-MM-dd", Locale.US)
                .format(calendar.getTime());
    }

    public static String formatDate(String date) {
        try {
            SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            SimpleDateFormat output = new SimpleDateFormat("MMM d, yyyy", Locale.ENGLISH);
            return output.format(input.parse(date));
        } catch (Exception exception) {
            return date == null ? "" : date;
        }
    }

    public static int categoryColor(String category) {
        for (int i = 0; i < CATEGORIES.length; i++) {
            if (CATEGORIES[i].equals(category)) return COLORS[i];
        }
        return 0xFF888888;
    }

    public static Task makeTask(String name, String description, String date,
                                String time, String category, String project,
                                String status) {
        Task task = new Task();
        task.setTitle(name);
        task.setDescription(description);
        task.setDate(parseDate(date));
        task.setTime(parseTime(time));
        task.setStatus(normalizeStatus(status));
        task.setComments(new ArrayList<>());
        return task;
    }

    public static List<Task> getTasks(Context context) {
        return getTasks(context, 0);
    }

    public static List<Task> getTasks(Context context, int userId) {
        DatabaseHelper helper = new DatabaseHelper(context);
        try {
                return new TaskDao(helper.getReadableDatabase()).findByUserId(userId);
        } finally {
            helper.close();
        }
    }

    public static Task find(Context context, int id) {
        DatabaseHelper helper = new DatabaseHelper(context);
        try {
            return new TaskDao(helper.getReadableDatabase()).findById(id);
        } finally {
            helper.close();
        }
    }

    public static Task find(Context context, int id, int userId) {
        Task task = find(context, id);
        return task != null && task.getUserId() == userId ? task : null;
    }

    public static void saveTask(Context context, Task task) {
        if (task == null) throw new IllegalArgumentException("Task is null");
        DatabaseHelper helper = new DatabaseHelper(context);
        try {
            new TaskDao(helper.getWritableDatabase()).save(task);
        } finally {
            helper.close();
        }
        NotificationScheduler.schedule(context, task);
    }

    public static void saveTask(Context context, Task task,
                                String categoryName, String projectName) {
            DatabaseHelper helper = new DatabaseHelper(context);
            SQLiteDatabase db = helper.getWritableDatabase();
            db.beginTransaction();
            try {
                TaskDao taskDao = new TaskDao(db);
                task.setProjectId(taskDao.findProjectId(projectName));
                taskDao.save(task);
                TaskCategoriesDao relationDao = new TaskCategoriesDao(db);
                relationDao.detachAllForTask(task.getId());
                int categoryId = taskDao.findCategoryId(categoryName);
                if (categoryId != 0) relationDao.attach(task.getId(), categoryId);
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
                helper.close();
        }
        NotificationScheduler.schedule(context, task);
    }

    public static void saveTasks(Context context, List<Task> tasks) {
        DatabaseHelper helper = new DatabaseHelper(context);
        SQLiteDatabase db = helper.getWritableDatabase();
        db.beginTransaction();
        try {
            TaskDao dao = new TaskDao(db);
            for (Task task : tasks) dao.save(task);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            helper.close();
        }
    }

    public static void deleteTask(Context context, int id) {
        NotificationScheduler.cancel(context, id);
        DatabaseHelper helper = new DatabaseHelper(context);
        try {
            new TaskDao(helper.getWritableDatabase()).delete(id);
        } finally {
            helper.close();
        }
    }

    public static boolean isDone(Task task) {
        return task != null && STATUS_COMPLETED.equals(task.getStatus());
    }

    public static String projectName(Context context, Task task) {
        DatabaseHelper helper = new DatabaseHelper(context);
        try {
            return new TaskDao(helper.getReadableDatabase())
                    .findProjectName(task.getProjectId());
        } finally {
            helper.close();
        }

    }

    public static int projectId(Context context, String name) {
        DatabaseHelper helper = new DatabaseHelper(context);
        try {
            return new TaskDao(helper.getReadableDatabase()).findProjectId(name);
        } finally {
            helper.close();
        }
    }

    public static int categoryId(Context context, String name) {
        DatabaseHelper helper = new DatabaseHelper(context);
        try {
            return new TaskDao(helper.getReadableDatabase()).findCategoryId(name);
        } finally {
            helper.close();
        }
    }

    public static List<String> categoryNames(Context context, Task task) {
        DatabaseHelper helper = new DatabaseHelper(context);
        try {
            return new TaskDao(helper.getReadableDatabase())
                    .findCategoryNames(task.getId());
        } finally {
            helper.close();
        }
    }

    public static boolean belongsToCategory(Context context, Task task,
                                            String category) {
        return categoryNames(context, task).contains(category);
    }

    public static void setCompleted(Task task, boolean completed) {
        setStatus(task, completed ? STATUS_COMPLETED : STATUS_NOT_STARTED);
    }

    public static void toggleCompleted(Task task) {
        setCompleted(task, !isDone(task));
    }

    public static void setStatus(Task task, String status) {
        if (task == null) throw new IllegalArgumentException("Task is null");
        task.setStatus(normalizeStatus(status));
    }

    public static void addComment(Context context, Task task, String comment) {
        if (task == null || comment == null || comment.trim().isEmpty()) return;
        if (task.getComments() == null) task.setComments(new ArrayList<>());
        task.getComments().add(comment.trim());
        saveTask(context, task);
    }

    public static String group(Task task) {
        if (task == null || task.getDate() == null) return GROUP_UPCOMING;
        String date = task.getDate().toString();
        if (date.compareTo(today(0)) < 0 && !isDone(task)) return GROUP_OVERDUE;
        if (date.compareTo(today(0)) <= 0) return GROUP_TODAY;
        return GROUP_UPCOMING;
    }

    private static String normalizeStatus(String status) {
        if (STATUS_COMPLETED.equals(status)) return STATUS_COMPLETED;
        if (STATUS_IN_PROGRESS.equals(status)) return STATUS_IN_PROGRESS;
        return STATUS_NOT_STARTED;
    }

    private static LocalDate parseDate(String date) {
        return date == null || date.isEmpty() ? null : LocalDate.parse(date);
    }

    private static LocalTime parseTime(String time) {
        if (time == null || time.isEmpty()) return LocalTime.of(9, 0);
        try {
            return LocalTime.parse(time);
        } catch (Exception ignored) {
            try {
                return LocalTime.parse(time,
                        java.time.format.DateTimeFormatter.ofPattern("h:mm a", Locale.US));
            } catch (Exception exception) {
                return LocalTime.of(9, 0);
            }
        }
    }
}
