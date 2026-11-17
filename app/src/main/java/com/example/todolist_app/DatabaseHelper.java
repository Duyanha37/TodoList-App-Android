package com.example.todolist_app;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "todolist.db";
    private static final int DATABASE_VERSION = 4;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createUserTable = "CREATE TABLE User (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "fullname TEXT NOT NULL," +
                "password TEXT NOT NULL," +
                "email TEXT NOT NULL UNIQUE)";

        String createCategoriesTable = "CREATE TABLE Categories (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "userId INTEGER NOT NULL DEFAULT 0," +
                "name TEXT NOT NULL," +
                "UNIQUE(userId, name))";

        String createTaskTable = "CREATE TABLE Task (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "userId INTEGER NOT NULL DEFAULT 0," +
                "projectId INTEGER," +
                "title TEXT NOT NULL," +
                "description TEXT NOT NULL DEFAULT ''," +
                "comments TEXT NOT NULL DEFAULT '[]'," +
                "date TEXT NOT NULL," +
                "time TEXT NOT NULL DEFAULT '09:00'," +
                "priority TEXT NOT NULL DEFAULT 'Normal'," +
                "status TEXT NOT NULL DEFAULT 'Not Started'," +
                "FOREIGN KEY(projectId) REFERENCES Project(id) ON DELETE SET NULL)";

        String createProjectTable = "CREATE TABLE Project (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "userId INTEGER NOT NULL DEFAULT 0," +
                "name TEXT NOT NULL," +
                "UNIQUE(userId, name))";

        String createTaskCategoriesTable = "CREATE TABLE TaskCategories (" +
                "taskId INTEGER NOT NULL," +
                "categoryId INTEGER NOT NULL," +
                "PRIMARY KEY (taskId, categoryId)," +
                "FOREIGN KEY(taskId) REFERENCES Task(id) ON DELETE CASCADE," +
                "FOREIGN KEY(categoryId) REFERENCES Categories(id) ON DELETE CASCADE)";

        db.execSQL(createUserTable);
        db.execSQL(createCategoriesTable);
        db.execSQL(createTaskTable);
        db.execSQL(createProjectTable);
        db.execSQL(createTaskCategoriesTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 4) {
            db.execSQL("ALTER TABLE Task ADD COLUMN time TEXT NOT NULL DEFAULT '09:00'");
        }
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

}
