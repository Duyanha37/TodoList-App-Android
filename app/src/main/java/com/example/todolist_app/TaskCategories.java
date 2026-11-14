package com.example.todolist_app;

public class TaskCategories {
    private int taskID;
    private int categoryID;

    public int getTaskID() {
        return taskID;
    }

    public void setTaskID(int taskID) {
        this.taskID = taskID;
    }

    public int getCategoryID() {
        return categoryID;
    }

    public void setCategoryID(int categoryID) {
        this.categoryID = categoryID;
    }

    public TaskCategories(int taskID, int categoryID) {
        this.taskID = taskID;
        this.categoryID = categoryID;
    }
}
