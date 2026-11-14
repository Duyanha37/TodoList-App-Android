package com.example.todolist_app;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class UserDao {
    private final SQLiteDatabase db;

    public UserDao(SQLiteDatabase db) {
        this.db = db;
    }

    public long insert(User user) {
        return db.insertOrThrow("User", null, values(user));
    }

    public int update(User user) {
        return db.update("User", values(user), "id = ?",
                new String[]{user.getId()});
    }

    public int delete(String id) {
        return db.delete("User", "id = ?", new String[]{id});
    }

    public User findById(String id) {
        try (Cursor cursor = db.query("User", null, "id = ?",
                new String[]{id}, null, null, null)) {
            return cursor.moveToFirst() ? fromCursor(cursor) : null;
        }
    }

    public User findByEmail(String email) {
        try (Cursor cursor = db.query("User", null, "email = ?",
                new String[]{email}, null, null, null)) {
            return cursor.moveToFirst() ? fromCursor(cursor) : null;
        }
    }

    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        try (Cursor cursor = db.query("User", null, null, null,
                null, null, "id ASC")) {
            while (cursor.moveToNext()) {
                users.add(fromCursor(cursor));
            }
        }
        return users;
    }

    public User Login(String email, String password) {
        try (Cursor cursor = db.query("User", null, "email = ? AND password = ?",
                new String[]{email, password}, null, null, null)) {
            return cursor.moveToFirst() ? fromCursor(cursor) : null;
        }
    }

    private ContentValues values(User user) {
        ContentValues values = new ContentValues();
        if (user.getId() == null || user.getId().isEmpty()) {
            values.putNull("id");
        } else {
            values.put("id", Long.parseLong(user.getId()));
        }
        values.put("fullname", user.getFullname());
        values.put("password", user.getPassword());
        values.put("email", user.getEmail());
        values.put("phone", user.getPhone());
        return values;
    }

    private User fromCursor(Cursor cursor) {
        User user = new User();
        user.setId(cursor.getString(cursor.getColumnIndexOrThrow("id")));
        user.setFullname(cursor.getString(cursor.getColumnIndexOrThrow("fullname")));
        user.setPassword(cursor.getString(cursor.getColumnIndexOrThrow("password")));
        user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow("email")));
        user.setPhone(cursor.getString(cursor.getColumnIndexOrThrow("phone")));
        return user;
    }
}
