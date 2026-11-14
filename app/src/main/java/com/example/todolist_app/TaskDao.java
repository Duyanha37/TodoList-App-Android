package com.example.todolist_app;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.json.JSONArray;
import org.json.JSONException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TaskDao {
    private final SQLiteDatabase db;

    public TaskDao(SQLiteDatabase db) {
        this.db = db;
    }

    public long insert(Task task) {
        ContentValues values = values(task);
        values.remove("id");
        return db.insertOrThrow("Task", null, values);
    }

    public int update(Task task) {
        return db.update("Task", values(task), "id = ?",
                new String[]{String.valueOf(task.getId())});
    }

    public int delete(int id) {
        return db.delete("Task", "id = ?",
                new String[]{String.valueOf(id)});
    }

    public Task findById(int id) {
        try (Cursor cursor = db.query("Task", null, "id = ?",
                new String[]{String.valueOf(id)}, null, null, null)) {
            return cursor.moveToFirst() ? fromCursor(cursor) : null;
        }
    }

    public List<Task> findByUserId(int userId) {
        return find("userId = ?", new String[]{String.valueOf(userId)});
    }

    public List<Task> findByProjectId(int projectId) {
        return find("projectId = ?", new String[]{String.valueOf(projectId)});
    }

    private List<Task> find(String selection, String[] args) {
        List<Task> tasks = new ArrayList<>();
        try (Cursor cursor = db.query("Task", null, selection, args,
                null, null, "id ASC")) {
            while (cursor.moveToNext()) {
                tasks.add(fromCursor(cursor));
            }
        }
        return tasks;
    }

    private ContentValues values(Task task) {
        ContentValues values = new ContentValues();
        values.put("id", task.getId());
        values.put("userId", task.getUserId());
        if (task.getProjectId() == 0) {
            values.putNull("projectId");
        } else {
            values.put("projectId", task.getProjectId());
        }
        values.put("title", task.getTitle());
        values.put("description", task.getDescription());
        values.put("comments", commentsToJson(task.getComments()));
        values.put("date", task.getDate() == null ? null : task.getDate().toString());
        values.put("priority", task.getPriority());
        values.put("status", task.getStatus());
        return values;
    }

    private Task fromCursor(Cursor cursor) {
        Task task = new Task();
        task.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
        task.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow("userId")));
        int projectIndex = cursor.getColumnIndexOrThrow("projectId");
        task.setProjectId(cursor.isNull(projectIndex) ? 0 : cursor.getInt(projectIndex));
        task.setTitle(cursor.getString(cursor.getColumnIndexOrThrow("title")));
        task.setDescription(cursor.getString(
                cursor.getColumnIndexOrThrow("description")));
        task.setComments(commentsFromJson(cursor.getString(
                cursor.getColumnIndexOrThrow("comments"))));
        String date = cursor.getString(cursor.getColumnIndexOrThrow("date"));
        task.setDate(date == null || date.isEmpty() ? null : LocalDate.parse(date));
        task.setPriority(cursor.getString(cursor.getColumnIndexOrThrow("priority")));
        task.setStatus(cursor.getString(cursor.getColumnIndexOrThrow("status")));
        return task;
    }

    private String commentsToJson(ArrayList<String> comments) {
        return comments == null ? "[]" : new JSONArray(comments).toString();
    }

    private ArrayList<String> commentsFromJson(String json) {
        ArrayList<String> comments = new ArrayList<>();
        if (json == null || json.isEmpty()) {
            return comments;
        }
        try {
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                comments.add(array.optString(i));
            }
        } catch (JSONException exception) {
            throw new IllegalStateException("Invalid task comments JSON", exception);
        }
        return comments;
    }
}
