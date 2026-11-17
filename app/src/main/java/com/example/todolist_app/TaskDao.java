package com.example.todolist_app;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.json.JSONArray;
import org.json.JSONException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class TaskDao {
    private final SQLiteDatabase db;

    public TaskDao(SQLiteDatabase db) {
        this.db = db;
    }

    public long insert(Task task) {
        long id = db.insertOrThrow("Task", null, values(task));
        task.setId((int) id);
        return id;
    }

    public void save(Task task) { //Gom Insert Update kiểm tra task đã tồn tại chưa để xử lý
        if (task.getId() == 0 || update(task) == 0) insert(task);
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

    public List<Task> findAll() {
        return find(null, null);
    }

    public List<Task> findByUserId(int userId) {
        return find("userId = ?", new String[]{String.valueOf(userId)});
    }

    public List<Task> findByProjectId(int projectId) {
        return find("projectId = ?", new String[]{String.valueOf(projectId)});
    }

    public List<Task> findByCategory(String category) {
        String sql = "SELECT t.* FROM Task t " +
                "INNER JOIN TaskCategories tc ON tc.taskId = t.id " +
                "INNER JOIN Categories c ON c.id = tc.categoryId " +
                "WHERE c.name = ? ORDER BY t.id ASC";
        return findRaw(sql, new String[]{category});
    }

    public List<Task> search(String keyword, String category) {
        String value = "%" + (keyword == null ? "" : keyword) + "%";
        if (category != null && !category.equals("All")) {
            String sql = "SELECT t.* FROM Task t " +
                    "INNER JOIN TaskCategories tc ON tc.taskId = t.id " +
                    "INNER JOIN Categories c ON c.id = tc.categoryId " +
                    "WHERE (t.title LIKE ? OR t.description LIKE ?) " +
                    "AND c.name = ? ORDER BY t.id ASC";
            return findRaw(sql, new String[]{value, value, category});
        }
        return find("(title LIKE ? OR description LIKE ?)",
                new String[]{value, value});
    }

    public int updateCategory(String oldName, String newName) {
        ContentValues values = new ContentValues();
        values.put("name", newName);
        return db.update("Categories", values, "name = ?",
                new String[]{oldName});
    }

    public int updateProject(String oldName, String newName) {
        ContentValues values = new ContentValues();
        values.put("name", newName);
        return db.update("Project", values, "name = ?",
                new String[]{oldName});
    }

    public String findProjectName(int projectId) {
        if (projectId == 0) return "";
        try (Cursor cursor = db.query("Project", new String[]{"name"},
                "id = ?", new String[]{String.valueOf(projectId)},
                null, null, null)) {
            return cursor.moveToFirst() ? cursor.getString(0) : "";
        }
    }

    public int findProjectId(String name) {
        if (name == null || name.isEmpty()) return 0;
        try (Cursor cursor = db.query("Project", new String[]{"id"},
                "name = ?", new String[]{name}, null, null, null)) {
            return cursor.moveToFirst() ? cursor.getInt(0) : 0;
        }
    }

    public int findCategoryId(String name) {
        if (name == null || name.isEmpty()) return 0;
        try (Cursor cursor = db.query("Categories", new String[]{"id"},
                "name = ?", new String[]{name}, null, null, null)) {
            return cursor.moveToFirst() ? cursor.getInt(0) : 0;
        }
    }

    public List<String> findCategoryNames(int taskId) {
        List<String> names = new ArrayList<>();
        String sql = "SELECT c.name FROM Categories c " +
                "INNER JOIN TaskCategories tc ON tc.categoryId = c.id " +
                "WHERE tc.taskId = ? ORDER BY c.id ASC";
        try (Cursor cursor = db.rawQuery(sql,
                new String[]{String.valueOf(taskId)})) {
            while (cursor.moveToNext()) names.add(cursor.getString(0));
        }
        return names;
    }

    private List<Task> find(String selection, String[] args) {
        List<Task> tasks = new ArrayList<>();
        try (Cursor cursor = db.query("Task", null, selection, args,
                null, null, "id ASC")) {
            while (cursor.moveToNext()) tasks.add(fromCursor(cursor));
        }
        return tasks;
    }

    private List<Task> findRaw(String sql, String[] args) {
        List<Task> tasks = new ArrayList<>();
        try (Cursor cursor = db.rawQuery(sql, args)) {
            while (cursor.moveToNext()) tasks.add(fromCursor(cursor));
        }
        return tasks;
    }

    private ContentValues values(Task task) {
        ContentValues values = new ContentValues();
        if (task.getId() != 0) values.put("id", task.getId());
        values.put("userId", task.getUserId());
        if (task.getProjectId() == 0) values.putNull("projectId");
        else values.put("projectId", task.getProjectId());
        values.put("title", task.getTitle());
        values.put("description", task.getDescription());
        values.put("comments", commentsToJson(task.getComments()));
        values.put("date", task.getDate() == null ? null : task.getDate().toString());
        values.put("time", task.getTime() == null ? "09:00" : task.getTime().toString());
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
        String time = cursor.getString(cursor.getColumnIndexOrThrow("time"));
        task.setTime(time == null || time.isEmpty() ? LocalTime.of(9, 0) : LocalTime.parse(time));
        task.setPriority(cursor.getString(cursor.getColumnIndexOrThrow("priority")));
        task.setStatus(cursor.getString(cursor.getColumnIndexOrThrow("status")));
        return task;
    }

    private String commentsToJson(ArrayList<String> comments) {
        return comments == null ? "[]" : new JSONArray(comments).toString();
    }

    private ArrayList<String> commentsFromJson(String json) {
        ArrayList<String> comments = new ArrayList<>();
        if (json == null || json.isEmpty()) return comments;
        try {
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) comments.add(array.optString(i));
        } catch (JSONException exception) {
            throw new IllegalStateException("Invalid task comments JSON", exception);
        }
        return comments;
    }
}
