package com.example.todolist_app;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class ProjectDao {
    private final SQLiteDatabase db;

    public ProjectDao(SQLiteDatabase db) {
        this.db = db;
    }

    public long insert(Project project) {
        ContentValues values = values(project);
        values.remove("id");
        return db.insertOrThrow("Project", null, values);
    }

    public int update(Project project) {
        return db.update("Project", values(project), "id = ?",
                new String[]{String.valueOf(project.getId())});
    }

    public int delete(int id) {
        return db.delete("Project", "id = ?",
                new String[]{String.valueOf(id)});
    }

    public Project findById(int id) {
        try (Cursor cursor = db.query("Project", null, "id = ?",
                new String[]{String.valueOf(id)}, null, null, null)) {
            return cursor.moveToFirst() ? fromCursor(cursor) : null;
        }
    }

    public List<Project> findByUserId(int userId) {
        List<Project> projects = new ArrayList<>();
        try (Cursor cursor = db.query("Project", null, "userId = ?",
                new String[]{String.valueOf(userId)}, null, null, "id ASC")) {
            while (cursor.moveToNext()) {
                projects.add(fromCursor(cursor));
            }
        }
        return projects;
    }

    private ContentValues values(Project project) {
        ContentValues values = new ContentValues();
        values.put("id", project.getId());
        values.put("userId", project.getUserId());
        values.put("name", project.getName());
        return values;
    }

    private Project fromCursor(Cursor cursor) {
        Project project = new Project();
        project.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
        project.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow("userId")));
        project.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
        return project;
    }
}
