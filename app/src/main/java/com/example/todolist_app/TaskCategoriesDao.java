package com.example.todolist_app;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class TaskCategoriesDao {
    private final SQLiteDatabase db;

    public TaskCategoriesDao(SQLiteDatabase db) {
        this.db = db;
    }

    public long attach(int taskId, int categoryId) {
        ContentValues values = new ContentValues();
        values.put("taskId", taskId);
        values.put("categoryId", categoryId);
        return db.insertWithOnConflict("TaskCategories", null, values,
                SQLiteDatabase.CONFLICT_IGNORE);
    }

    public int detach(int taskId, int categoryId) {
        return db.delete("TaskCategories", "taskId = ? AND categoryId = ?",
                new String[]{String.valueOf(taskId), String.valueOf(categoryId)});
    }

    public int detachAllForTask(int taskId) {
        return db.delete("TaskCategories", "taskId = ?",
                new String[]{String.valueOf(taskId)});
    }

    public List<Integer> findCategoryIdsForTask(int taskId) {
        List<Integer> categoryIds = new ArrayList<>();
        try (Cursor cursor = db.query("TaskCategories",
                new String[]{"categoryId"}, "taskId = ?",
                new String[]{String.valueOf(taskId)}, null, null,
                "categoryId ASC")) {
            while (cursor.moveToNext()) {
                categoryIds.add(cursor.getInt(0));
            }
        }
        return categoryIds;
    }

    public List<Integer> findTaskIdsForCategory(int categoryId) {
        List<Integer> taskIds = new ArrayList<>();
        try (Cursor cursor = db.query("TaskCategories",
                new String[]{"taskId"}, "categoryId = ?",
                new String[]{String.valueOf(categoryId)}, null, null,
                "taskId ASC")) {
            while (cursor.moveToNext()) {
                taskIds.add(cursor.getInt(0));
            }
        }
        return taskIds;
    }
}
