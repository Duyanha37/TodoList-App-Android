package com.example.todolist_app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

public class CategoryDetailsActivity extends Activity {

    String categoryName = "Work";
    String categoryIcon = "💼";
    int categoryColor = 0xFF6051D8;
    String filter = "All";

    LinearLayout layoutTasks, layoutFilters;
    TextView txtStats;

    int dp(int n) {
        return (int) (n * getResources()
                .getDisplayMetrics().density + 0.5f);
    }

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_category_details);

        categoryName = getIntent().getStringExtra("categoryName");
        if (categoryName == null) categoryName = "Work";

        String icon = getIntent().getStringExtra("categoryIcon");
        if (icon != null) categoryIcon = icon;

        categoryColor = getIntent().getIntExtra(
                "categoryColor", categoryColor);

        layoutTasks = findViewById(R.id.layoutCategoryTasks);
        layoutFilters = findViewById(R.id.layoutCategoryFilters);
        txtStats = findViewById(R.id.txtCategoryStats);

        ((TextView) findViewById(R.id.txtCategoryTitle))
                .setText(categoryName);

        TextView bigIcon =
                findViewById(R.id.txtCategoryBigIcon);
        bigIcon.setText(categoryIcon);

        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.OVAL);
        bg.setColor(categoryColor);
        bigIcon.setBackground(bg);

        findViewById(R.id.btnBackCategory)
                .setOnClickListener(v -> finish());

        findViewById(R.id.btnAddCategoryTask)
                .setOnClickListener(v -> {
                    Intent intent = new Intent(
                            this, TaskFormActivity.class);
                    intent.putExtra("categoryName", categoryName);
                    startActivity(intent);
                });

        findViewById(R.id.btnCategoryOptions)
                .setOnClickListener(v -> {
                    PopupMenu menu = new PopupMenu(
                            this, findViewById(R.id.btnCategoryOptions));
                    menu.getMenu().add("View all tasks");
                    menu.getMenu().add("Add Task");

                    menu.setOnMenuItemClickListener(item -> {
                        if (item.getTitle().toString()
                                .equals("Add Task")) {
                            findViewById(R.id.btnAddCategoryTask)
                                    .performClick();
                        } else {
                            filter = "All";
                            drawFilters();
                            showTasks();
                        }
                        return true;
                    });
                    menu.show();
                });

        drawFilters();
        showTasks();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (layoutTasks != null) showTasks();
    }

    void drawFilters() {
        layoutFilters.removeAllViews();
        String[] filters = {
                "All", "Today", "Upcoming",
                "Overdue", "Completed"
        };

        for (String name : filters) {
            TextView button = new TextView(this);
            button.setText(name);
            button.setTextSize(12);
            button.setGravity(Gravity.CENTER);
            button.setPadding(dp(16), dp(10),
                    dp(16), dp(10));

            boolean selected = name.equals(filter);
            button.setTextColor(
                    selected ? Color.WHITE : 0xFF777777);
            button.setBackgroundResource(selected
                    ? R.drawable.bg_category_selected
                    : R.drawable.bg_outline);

            LinearLayout.LayoutParams lp =
                    new LinearLayout.LayoutParams(-2, -2);
            lp.rightMargin = dp(8);
            layoutFilters.addView(button, lp);

            button.setOnClickListener(v -> {
                filter = name;
                drawFilters();
                showTasks();
            });
        }
    }

    void showTasks() {
        layoutTasks.removeAllViews();

        JSONArray tasks = TaskStore.getTasks(this);
        int total = 0;
        int completed = 0;

        for (int i = 0; i < tasks.length(); i++) {
            JSONObject task = tasks.optJSONObject(i);
            if (task == null) continue;
            if (!categoryName.equals(
                    task.optString("category"))) continue;

            total++;
            if (TaskStore.isDone(task)) completed++;

            String group = TaskStore.group(task);
            boolean matches = filter.equals("All") ||
                    filter.equals(group) ||
                    (filter.equals("Completed") &&
                            TaskStore.isDone(task));

            if (matches) addTaskCard(task);
        }

        txtStats.setText(total + " tasks · " +
                completed + " completed");
    }

    void addTaskCard(JSONObject task) {
        View item = getLayoutInflater().inflate(
                R.layout.item_task_home,
                layoutTasks, false);

        CheckBox check = item.findViewById(R.id.checkTask);
        TextView name = item.findViewById(R.id.txtTaskName);
        TextView info = item.findViewById(R.id.txtTaskInfo);
        TextView category = item.findViewById(R.id.txtCategory);
        TextView menu = item.findViewById(R.id.btnTaskMenu);

        boolean done = TaskStore.isDone(task);
        String id = task.optString("id");

        name.setText(task.optString("name"));
        info.setText(task.optString("time") +
                " · " + task.optString("project"));
        category.setText("●  " + categoryName);
        category.setTextColor(categoryColor);

        check.setChecked(done);
        if (done) {
            name.setPaintFlags(name.getPaintFlags() |
                    Paint.STRIKE_THRU_TEXT_FLAG);
            name.setTextColor(0xFF999999);
        }

        check.setOnCheckedChangeListener((v, checked) -> {
            try {
                task.put("status", checked ?
                        "Completed" : "Not Started");
                TaskStore.saveTask(this, task);
                showTasks();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        menu.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(this, menu);
            popup.getMenu().add("Mark as Complete");
            popup.getMenu().add("Delete");

            popup.setOnMenuItemClickListener(choice -> {
                if (choice.getTitle().toString()
                        .equals("Delete")) {
                    new AlertDialog.Builder(this)
                            .setTitle("Delete Task")
                            .setMessage("Delete " +
                                    task.optString("name") + "?")
                            .setNegativeButton("Cancel", null)
                            .setPositiveButton("Delete", (d, w) -> {
                                TaskStore.deleteTask(this, id);
                                showTasks();
                            }).show();
                } else {
                    try {
                        task.put("status", "Completed");
                        TaskStore.saveTask(this, task);
                        showTasks();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return true;
            });
            popup.show();
        });

        item.findViewById(R.id.layoutTaskContent)
                .setOnClickListener(v -> {
                    Intent intent = new Intent(
                            this, TaskDetailsActivity.class);
                    intent.putExtra("taskId", id);
                    startActivity(intent);
                });

        layoutTasks.addView(item);
    }
}
