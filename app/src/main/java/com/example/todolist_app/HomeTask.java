package com.example.todolist_app;

/**
 * View model for one task card on Home.
 *
 * This is intentionally separate from Task: later, map DB/API Task objects to
 * this class in one place without changing the RecyclerView layout.
 */
public class HomeTask {
    private final String section;
    private final String title;
    private final String time;
    private final String project;
    private final String category;
    private final int categoryColor;
    private boolean completed;

    public HomeTask(String section, String title, String time, String project,
                    String category, int categoryColor, boolean completed) {
        this.section = section;
        this.title = title;
        this.time = time;
        this.project = project;
        this.category = category;
        this.categoryColor = categoryColor;
        this.completed = completed;
    }

    public String getSection() { return section; }
    public String getTitle() { return title; }
    public String getTime() { return time; }
    public String getProject() { return project; }
    public String getCategory() { return category; }
    public int getCategoryColor() { return categoryColor; }
    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }
}
