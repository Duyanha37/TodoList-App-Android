package com.example.todolist_app;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class CategoriesDao {
    private final SQLiteDatabase db;

    public CategoriesDao(SQLiteDatabase db) {
        this.db = db;
    } //Constructor nhận obj db đã được cấp quyền truy cập từ DatabaseHelper

    public long insert(Categories category) {
        ContentValues values = values(category); // Tạo ContentValues từ đối tượng category bằng function values
        values.remove("id");
        return db.insertOrThrow("Categories", null, values);
    }

    public int update(Categories category) {
        return db.update("Categories", values(category), "id = ?",
                new String[]{String.valueOf(category.getId())});
    }

    public int delete(int id) {
        return db.delete("Categories", "id = ?",
                new String[]{String.valueOf(id)});
    }

    public Categories findById(int id) {
        try (Cursor cursor = db.query("Categories", null, "id = ?",
                new String[]{String.valueOf(id)}, null, null, null)) { //Sử dụng query và các thuộc tính của nó
            return cursor.moveToFirst() ? fromCursor(cursor) : null; //Trả về obj Categories nếu moveToFirst là true (toán tử 3 ngôi)
        }
    }

    public List<Categories> findByUserId(int userId) {
        List<Categories> categories = new ArrayList<>();
        try (Cursor cursor = db.query("Categories", null, "userId = ?",
                new String[]{String.valueOf(userId)}, null, null, "id ASC")) {
            while (cursor.moveToNext()) {
                categories.add(fromCursor(cursor));
            }
        }
        return categories;
    }

    private ContentValues values(Categories category) {
        ContentValues values = new ContentValues();
        values.put("id", category.getId());
        values.put("userId", category.getUserId());
        values.put("name", category.getName());
        values.put("icon", category.getIcon());
        values.put("color", category.getColor());
        return values;
    }

    private Categories fromCursor(Cursor cursor) {
        Categories category = new Categories();
        category.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
        category.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow("userId")));
        category.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
        category.setIcon(cursor.getString(cursor.getColumnIndexOrThrow("icon")));
        category.setColor(cursor.getInt(cursor.getColumnIndexOrThrow("color")));
        return category;
    }
}
