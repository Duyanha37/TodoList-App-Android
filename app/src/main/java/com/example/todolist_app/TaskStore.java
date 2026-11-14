package com.example.todolist_app;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.UUID;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class TaskStore {

    public static final String[] CATEGORIES = {
            "Work", "Personal", "Health", "Finance", "Learning"
    };

    public static final int[] COLORS = {
            0xFF6051D8, 0xFF9634E8,
            0xFF288438, 0xFFFF8000, 0xFF008CC9
    };

    public static final String[] PROJECTS = {
            "Q4 Product Launch", "Website Redesign",
            "Fitness Goal", "Side Project"
    };

    public static String today(int plusDays) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, plusDays);
        return new SimpleDateFormat(
                "yyyy-MM-dd", Locale.US).format(cal.getTime());
    }

    public static String formatDate(String date) {
        try {
            SimpleDateFormat input =
                    new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            SimpleDateFormat output =
                    new SimpleDateFormat("MMM d, yyyy", Locale.ENGLISH);
            return output.format(input.parse(date));
        } catch (Exception e) {
            return date;
        }
    }

    public static int categoryColor(String category) {
        for (int i = 0; i < CATEGORIES.length; i++) {
            if (CATEGORIES[i].equals(category)) return COLORS[i];
        }
        return 0xFF888888;
    }

    public static JSONObject makeTask(
            String name, String description,
            String date, String time,
            String category, String project,
            String status) {

        JSONObject task = new JSONObject();

        try {
            task.put("id", UUID.randomUUID().toString());
            task.put("name", name);
            task.put("description", description);
            task.put("date", date);
            task.put("time", time);
            task.put("category", category);
            task.put("project", project);
            task.put("status", status);
            task.put("comments", new JSONArray());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return task;
    }


    public static JSONArray getTasks(Context context) {
        TodoDatabaseHelper helper =
                new TodoDatabaseHelper(context);
        // Chuyển dữ liệu cũ khi sử dụng lần đầu
        helper.migrateLegacyData();
        SQLiteDatabase db = helper.getReadableDatabase();
        JSONArray tasks = new JSONArray();
        Cursor cursor = db.rawQuery(
                "SELECT * FROM tasks ORDER BY rowid ASC",
                null);

        try {
            while (cursor.moveToNext()) {
                JSONObject task = new JSONObject();
                task.put("id",
                        cursor.getString(
                                cursor.getColumnIndexOrThrow("id")));
                task.put("name",
                        cursor.getString(
                                cursor.getColumnIndexOrThrow("name")));
                task.put("description",
                        cursor.getString(
                                cursor.getColumnIndexOrThrow("description")));
                task.put("date",
                        cursor.getString(
                                cursor.getColumnIndexOrThrow("date")));
                task.put("time",
                        cursor.getString(
                                cursor.getColumnIndexOrThrow("time")));
                task.put("category",
                        cursor.getString(
                                cursor.getColumnIndexOrThrow("category")));
                task.put("project",
                        cursor.getString(
                                cursor.getColumnIndexOrThrow("project")));
                task.put("status",
                        cursor.getString(
                                cursor.getColumnIndexOrThrow("status")));
                String comments = cursor.getString(
                        cursor.getColumnIndexOrThrow("comments"));
                task.put("comments",
                        new JSONArray(comments == null
                                ? "[]" : comments));
                tasks.put(task);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cursor.close();
            helper.close();
        }
        // Tạo dữ liệu mẫu đúng một lần nếu chưa có task
        SharedPreferences setup = context.getSharedPreferences(
                "sqlite_task_setup", Context.MODE_PRIVATE);
        if (!setup.getBoolean("initialized", false)) {
            if (tasks.length() == 0) {
                tasks = samples();
                saveTasks(context, tasks);
            }
            setup.edit()
                    .putBoolean("initialized", true)
                    .apply();
        }
        return tasks;
    }

    public static JSONObject find(Context context, String id) {

        JSONArray tasks = getTasks(context);

        for (int i = 0; i < tasks.length(); i++) {

            JSONObject task = tasks.optJSONObject(i);

            if (task != null &&
                    id.equals(task.optString("id"))) {
                return task;
            }
        }

        return null;
    }


    public static void saveTasks(
            Context context, JSONArray tasks) {
        TodoDatabaseHelper helper =
                new TodoDatabaseHelper(context);
        SQLiteDatabase db = helper.getWritableDatabase();
        db.beginTransaction();

        try {
            db.delete("tasks", null, null);
            for (int i = 0; i < tasks.length(); i++) {
                JSONObject task = tasks.optJSONObject(i);
                if (task == null) continue;
                ContentValues values = taskValues(task);
                db.insertWithOnConflict(
                        "tasks", null, values,
                        SQLiteDatabase.CONFLICT_REPLACE);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            helper.close();
        }
    }


    private static ContentValues taskValues(JSONObject task) {
        ContentValues values = new ContentValues();
        values.put("id", task.optString("id"));
        values.put("name", task.optString("name"));
        values.put("description",
                task.optString("description"));
        values.put("date", task.optString("date"));
        values.put("time", task.optString("time"));
        values.put("category", task.optString("category"));
        values.put("project", task.optString("project"));
        values.put("status", task.optString("status"));
        JSONArray comments = task.optJSONArray("comments");
        values.put("comments",
                comments == null ? "[]" : comments.toString());
        return values;
    }


    public static void saveTask(
            Context context, JSONObject task) {

        TodoDatabaseHelper helper =
                new TodoDatabaseHelper(context);

        // Đảm bảo dữ liệu cũ đã chuyển trước khi ghi mới
        helper.migrateLegacyData();
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = taskValues(task);
        db.insertWithOnConflict(
                "tasks",
                null,
                values,
                SQLiteDatabase.CONFLICT_REPLACE);
        helper.close();
    }



    public static void deleteTask(Context context, String id) {
        TodoDatabaseHelper helper =
                new TodoDatabaseHelper(context);
        helper.migrateLegacyData();
        SQLiteDatabase db = helper.getWritableDatabase();
        db.delete(
                "tasks",
                "id = ?",
                new String[]{id});
        helper.close();
    }

    public static boolean isDone(JSONObject task) {
        return "Completed".equals(task.optString("status"));
    }

    public static String group(JSONObject task) {
        String date = task.optString("date");
        String now = today(0);

        if (date.compareTo(now) < 0 && !isDone(task)) {
            return "Overdue";
        }
        if (date.compareTo(now) <= 0) {
            return "Today";
        }
        return "Upcoming";
    }

    private static JSONArray samples() {
        JSONArray a = new JSONArray();

        a.put(makeTask("Fix login page bug",
                "Session token not persisting",
                today(-1), "5:00 PM",
                "Work", "Website Redesign", "In Progress"));

        a.put(makeTask("Pay electricity bill", "",
                today(-1), "11:59 PM",
                "Finance", "Q4 Product Launch", "Not Started"));

        a.put(makeTask("Review design mockups", "",
                today(0), "10:00 AM",
                "Work", "Website Redesign", "Not Started"));

        a.put(makeTask("Team standup meeting", "",
                today(0), "9:00 AM",
                "Work", "Q4 Product Launch", "Completed"));

        a.put(makeTask("Morning run – 5km", "",
                today(0), "7:00 AM",
                "Health", "Fitness Goal", "Not Started"));

        a.put(makeTask("Write API documentation", "",
                today(1), "2:00 PM",
                "Work", "Side Project", "Not Started"));

        a.put(makeTask("Monthly budget review", "",
                today(2), "7:00 PM",
                "Finance", "Q4 Product Launch", "Not Started"));

        a.put(makeTask("Read System Design book", "",
                today(3), "8:00 PM",
                "Learning", "Side Project", "Not Started"));

        a.put(makeTask("Update project roadmap", "",
                today(4), "11:00 AM",
                "Work", "Q4 Product Launch", "Not Started"));

        a.put(makeTask("Gym session – strength", "",
                today(5), "6:00 PM",
                "Health", "Fitness Goal", "Not Started"));

        return a;
    }
}
