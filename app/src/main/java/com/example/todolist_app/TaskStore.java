package com.example.todolist_app;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.UUID;

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
        SharedPreferences pref = context.getSharedPreferences(
                "task_storage", Context.MODE_PRIVATE);

        if (!pref.contains("tasks")) {
            JSONArray sample = samples();
            saveTasks(context, sample);
            return sample;
        }

        try {
            return new JSONArray(pref.getString("tasks", "[]"));
        } catch (Exception e) {
            return new JSONArray();
        }
    }

    public static void saveTasks(Context context, JSONArray tasks) {
        context.getSharedPreferences(
                        "task_storage", Context.MODE_PRIVATE)
                .edit()
                .putString("tasks", tasks.toString())
                .apply();
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

    public static void saveTask(Context context, JSONObject task) {
        JSONArray tasks = getTasks(context);
        JSONArray result = new JSONArray();
        boolean found = false;

        for (int i = 0; i < tasks.length(); i++) {
            JSONObject old = tasks.optJSONObject(i);
            if (old == null) continue;

            if (old.optString("id").equals(task.optString("id"))) {
                result.put(task);
                found = true;
            } else {
                result.put(old);
            }
        }

        if (!found) result.put(task);
        saveTasks(context, result);
    }

    public static void deleteTask(Context context, String id) {
        JSONArray tasks = getTasks(context);
        JSONArray result = new JSONArray();

        for (int i = 0; i < tasks.length(); i++) {
            JSONObject task = tasks.optJSONObject(i);

            if (task != null &&
                    !id.equals(task.optString("id"))) {
                result.put(task);
            }
        }
        saveTasks(context, result);
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
