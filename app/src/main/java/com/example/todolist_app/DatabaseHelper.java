package com.example.todolist_app;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "todolist.db";
    private static final int DATABASE_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createUserTable = "CREATE TABLE User (id INTEGER PRIMARY KEY AUTOINCREMENT, fullname TEXT, password TEXT, email TEXT, phone TEXT)";
        String createCategoriesTable = "CREATE TABLE Categories (id INTEGER PRIMARY KEY AUTOINCREMENT, userId INTEGER, name TEXT REFERENCES User(id) ON DELETE CASCADE)";
        String createTaskTable = "CREATE TABLE Task (id INTEGER PRIMARY KEY AUTOINCREMENT, userId INTEGER, projectId INTEGER, title TEXT, description TEXT, comments TEXT, date TEXT, priority TEXT, status TEXT REFERENCES Categories(id) REFERENCES User(id) ON DELETE CASCADE)";
        String createProjectTable = "CREATE TABLE Project (id INTEGER PRIMARY KEY AUTOINCREMENT, userId INTEGER, name TEXT REFERENCES User(id) ON DELETE CASCADE)";
        String createTaskCategoriesTable = "CREATE TABLE TaskCategories (taskId INTEGER REFERENCES Task(id) ON DELETE CASCADE, categoryId INTEGER REFERENCES Categories(id) ON DELETE CASCADE, PRIMARY KEY (taskId, categoryId))";

        db.execSQL(createUserTable);
        db.execSQL(createCategoriesTable);
        db.execSQL(createTaskTable);
        db.execSQL(createProjectTable);
        db.execSQL(createTaskCategoriesTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS User");
        db.execSQL("DROP TABLE IF EXISTS Categories");
        db.execSQL("DROP TABLE IF EXISTS Task");
        db.execSQL("DROP TABLE IF EXISTS Project");
        db.execSQL("DROP TABLE IF EXISTS TaskCategories");
        onCreate(db);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }
}
