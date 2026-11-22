package com.example.todolist_app;

import java.util.ArrayList;
import java.util.List;

public class Categories {
    private int id;
    private int userId;
    private String name;
    private String icon;
    private int color;

    public Categories() {
    }

    public Categories(String name, String icon, int color) {
        this.name = name;
        this.icon = icon;
        this.color = color;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }
}
