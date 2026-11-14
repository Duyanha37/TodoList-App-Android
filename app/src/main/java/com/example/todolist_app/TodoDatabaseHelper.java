package com.example.todolist_app;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.json.JSONArray;
import org.json.JSONObject;

public class TodoDatabaseHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "TodoListSQLite.db";
    public static final int DB_VERSION = 1;

    public static final String TABLE_TASKS = "tasks";
    public static final String TABLE_PROJECTS = "projects";
    public static final String TABLE_CATEGORIES = "categories";

    Context context;

    public TodoDatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.context = context.getApplicationContext();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // Bảng Tasks
        db.execSQL(
                "CREATE TABLE IF NOT EXISTS tasks (" +
                        "id TEXT PRIMARY KEY, " +
                        "name TEXT, " +
                        "description TEXT, " +
                        "date TEXT, " +
                        "time TEXT, " +
                        "category TEXT, " +
                        "project TEXT, " +
                        "status TEXT, " +
                        "comments TEXT)"
        );

        // Bảng Projects
        db.execSQL(
                "CREATE TABLE IF NOT EXISTS projects (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "name TEXT, " +
                        "color INTEGER, " +
                        "deadline TEXT, " +
                        "archived INTEGER, " +
                        "tasks_json TEXT)"
        );

        // Bảng Categories
        db.execSQL(
                "CREATE TABLE IF NOT EXISTS categories (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "name TEXT, " +
                        "icon TEXT, " +
                        "color INTEGER, " +
                        "tasks INTEGER)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db,
                          int oldVersion, int newVersion) {
        // Chưa thay đổi cấu trúc ở phiên bản 1.
        // Không xóa bảng để tránh mất dữ liệu.
        onCreate(db);
    }

    // Đếm số bản ghi trong bảng
    public int count(String table) {
        if (!TABLE_TASKS.equals(table) &&
                !TABLE_PROJECTS.equals(table) &&
                !TABLE_CATEGORIES.equals(table)) {
            throw new IllegalArgumentException("Invalid table");
        }

        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM " + table, null);

        int count = 0;

        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }

        cursor.close();
        return count;
    }

    // Chuyển dữ liệu SharedPreferences cũ sang SQLite
    // Mỗi loại dữ liệu chỉ nhập một lần.
    public void migrateLegacyData() {
        SharedPreferences flags = context.getSharedPreferences(
                "sqlite_migration_flags", Context.MODE_PRIVATE);

        migrateTasks(flags);
        migrateProjects(flags);
        migrateCategories(flags);
    }

    void migrateTasks(SharedPreferences flags) {
        if (flags.getBoolean("tasks_done", false)) return;

        SharedPreferences old = context.getSharedPreferences(
                "task_storage", Context.MODE_PRIVATE);

        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();

        try {
            if (old.contains("tasks") && count(TABLE_TASKS) == 0) {
                JSONArray array = new JSONArray(
                        old.getString("tasks", "[]"));

                for (int i = 0; i < array.length(); i++) {
                    JSONObject task = array.optJSONObject(i);

                    if (task != null) {
                        ContentValues values = new ContentValues();

                        values.put("id", task.optString("id"));
                        values.put("name", task.optString("name"));
                        values.put("description",
                                task.optString("description"));
                        values.put("date", task.optString("date"));
                        values.put("time", task.optString("time"));
                        values.put("category",
                                task.optString("category"));
                        values.put("project",
                                task.optString("project"));
                        values.put("status",
                                task.optString("status"));
                        values.put("comments",
                                task.optJSONArray("comments") == null
                                        ? "[]"
                                        : task.optJSONArray("comments")
                                        .toString());

                        db.insertWithOnConflict(
                                TABLE_TASKS, null, values,
                                SQLiteDatabase.CONFLICT_REPLACE);
                    }
                }
            }

            db.setTransactionSuccessful();
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Cannot migrate tasks", e);
        } finally {
            db.endTransaction();
        }

        flags.edit().putBoolean("tasks_done", true).apply();
    }

    void migrateProjects(SharedPreferences flags) {
        if (flags.getBoolean("projects_done", false)) return;

        SharedPreferences old = context.getSharedPreferences(
                "project_data", Context.MODE_PRIVATE);

        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();

        try {
            if (old.contains("projects") &&
                    count(TABLE_PROJECTS) == 0) {

                JSONArray array = new JSONArray(
                        old.getString("projects", "[]"));

                for (int i = 0; i < array.length(); i++) {
                    JSONObject p = array.optJSONObject(i);
                    if (p == null) continue;

                    ContentValues values = new ContentValues();
                    values.put("name", p.optString("name"));
                    values.put("color", p.optInt("color"));
                    values.put("deadline",
                            p.optString("deadline"));
                    values.put("archived",
                            p.optBoolean("archived") ? 1 : 0);
                    values.put("tasks_json",
                            p.optJSONArray("tasks") == null
                                    ? "[]"
                                    : p.optJSONArray("tasks").toString());

                    db.insert(TABLE_PROJECTS, null, values);
                }
            }

            db.setTransactionSuccessful();
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Cannot migrate projects", e);
        } finally {
            db.endTransaction();
        }

        flags.edit().putBoolean("projects_done", true).apply();
    }

    void migrateCategories(SharedPreferences flags) {
        if (flags.getBoolean("categories_done", false)) return;

        SharedPreferences old = context.getSharedPreferences(
                "category_data", Context.MODE_PRIVATE);

        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();

        try {
            if (old.contains("categories") &&
                    count(TABLE_CATEGORIES) == 0) {

                JSONArray array = new JSONArray(
                        old.getString("categories", "[]"));

                for (int i = 0; i < array.length(); i++) {
                    JSONObject c = array.optJSONObject(i);
                    if (c == null) continue;

                    ContentValues values = new ContentValues();
                    values.put("name", c.optString("name"));
                    values.put("icon", c.optString("icon"));
                    values.put("color", c.optInt("color"));
                    values.put("tasks", c.optInt("tasks"));

                    db.insert(TABLE_CATEGORIES, null, values);
                }
            }

            db.setTransactionSuccessful();
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Cannot migrate categories", e);
        } finally {
            db.endTransaction();
        }

        flags.edit().putBoolean("categories_done", true).apply();
    }
}
