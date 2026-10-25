package com.example.todolist_app;

/**
 * One row in the Home RecyclerView. A row is either a section header or a task.
 */
public class HomeListItem {
    public static final int SECTION = 0;
    public static final int TASK = 1;

    private final int type;
    private final String sectionTitle;
    private final HomeTask task;

    private HomeListItem(int type, String sectionTitle, HomeTask task) {
        this.type = type;
        this.sectionTitle = sectionTitle;
        this.task = task;
    }

    public static HomeListItem section(String title) {
        return new HomeListItem(SECTION, title, null);
    }

    public static HomeListItem task(HomeTask task) {
        return new HomeListItem(TASK, null, task);
    }

    public int getType() { return type; }
    public String getSectionTitle() { return sectionTitle; }
    public HomeTask getTask() { return task; }
}
