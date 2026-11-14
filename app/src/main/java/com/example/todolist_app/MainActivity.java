package com.example.todolist_app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

public class MainActivity extends Activity {

    LinearLayout layoutTaskList;
    TextView txtToday, txtUpcoming, txtOverdue;
    String filter = "All";

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_main);

        layoutTaskList = findViewById(R.id.layoutTaskList);
        txtToday = findViewById(R.id.txtTodayCount);
        txtUpcoming = findViewById(R.id.txtUpcomingCount);
        txtOverdue = findViewById(R.id.txtOverdueCount);

        // Nút Add Task
        findViewById(R.id.btnAddTask).setOnClickListener(v -> {
            Intent intent = new Intent(
                    MainActivity.this, TaskFormActivity.class);
            startActivity(intent);
        });

        // Nút Search
        findViewById(R.id.btnSearch).setOnClickListener(v -> {
            Intent intent = new Intent(
                    MainActivity.this, TaskSearchActivity.class);
            startActivity(intent);
        });

        // Nút Filter
        findViewById(R.id.btnFilter).setOnClickListener(v -> {
            String[] options = {
                    "All", "Not Started",
                    "In Progress", "Completed"
            };

            new AlertDialog.Builder(this)
                    .setTitle("Filter Tasks")
                    .setSingleChoiceItems(options, -1,
                            (dialog, which) -> {
                                filter = options[which];
                                dialog.dismiss();
                                showTasks();
                            }).show();
        });

        // Home -> Projects
        findViewById(R.id.navProjects).setOnClickListener(v -> {
            Intent intent = new Intent(
                    MainActivity.this, ProjectsActivity.class);
            startActivity(intent);
        });

        // Home -> Categories
        findViewById(R.id.navCategories).setOnClickListener(v -> {
            Intent intent = new Intent(
                    MainActivity.this, CategoriesActivity.class);
            startActivity(intent);
        });

        // Home -> Profile
        findViewById(R.id.navProfile).setOnClickListener(v -> {
            Intent intent = new Intent(
                    MainActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

        showTasks();
    }


    @Override
    protected void onResume() {
        super.onResume();

        if (layoutTaskList != null) {
            showTasks();
        }
    }

    // Hiển thị danh sách Task
    void showTasks() {
        layoutTaskList.removeAllViews();

        JSONArray tasks = TaskStore.getTasks(this);

        int today = 0;
        int upcoming = 0;
        int overdue = 0;

        for (int i = 0; i < tasks.length(); i++) {
            JSONObject task = tasks.optJSONObject(i);

            if (task == null) continue;

            String group = TaskStore.group(task);

            if (group.equals("Today")) today++;
            if (group.equals("Upcoming")) upcoming++;
            if (group.equals("Overdue")) overdue++;
        }

        txtToday.setText("Today\n" + today);
        txtUpcoming.setText("Upcoming\n" + upcoming);
        txtOverdue.setText("Overdue\n" + overdue);

        String[] groups = {
                "Overdue", "Today", "Upcoming"
        };

        for (String group : groups) {

            LinearLayout section = new LinearLayout(this);
            section.setOrientation(LinearLayout.VERTICAL);

            int shown = 0;

            for (int i = 0; i < tasks.length(); i++) {
                JSONObject task = tasks.optJSONObject(i);

                if (task == null) continue;

                boolean matchesFilter =
                        filter.equals("All") ||
                                filter.equals(task.optString("status"));

                if (TaskStore.group(task).equals(group)
                        && matchesFilter) {

                    if (shown == 0) {
                        TextView heading = new TextView(this);

                        heading.setText(group);
                        heading.setTextSize(14);
                        heading.setTypeface(null,
                                android.graphics.Typeface.BOLD);

                        heading.setTextColor(
                                group.equals("Overdue")
                                        ? Color.RED : Color.BLACK);

                        heading.setPadding(0, 18, 0, 12);

                        section.addView(heading);
                    }

                    addTask(section, task);
                    shown++;
                }
            }

            layoutTaskList.addView(section);
        }
    }

    // Hiển thị từng Task
    void addTask(LinearLayout parent, JSONObject task) {

        View item = getLayoutInflater().inflate(
                R.layout.item_task_home, parent, false);

        CheckBox check =
                item.findViewById(R.id.checkTask);

        TextView name =
                item.findViewById(R.id.txtTaskName);

        TextView info =
                item.findViewById(R.id.txtTaskInfo);

        TextView category =
                item.findViewById(R.id.txtCategory);

        TextView menu =
                item.findViewById(R.id.btnTaskMenu);

        String id = task.optString("id");
        boolean done = TaskStore.isDone(task);

        name.setText(task.optString("name"));

        info.setText(task.optString("time") + " · " +
                task.optString("project"));

        String cat = task.optString("category");

        category.setText("●  " + cat);
        category.setTextColor(TaskStore.categoryColor(cat));

        check.setChecked(done);

        // Gạch ngang Task đã hoàn thành
        if (done) {
            name.setPaintFlags(
                    name.getPaintFlags() |
                            Paint.STRIKE_THRU_TEXT_FLAG);

            name.setTextColor(0xFF999999);
        }

        // Tick hoàn thành Task
        check.setOnCheckedChangeListener((button, checked) -> {
            try {
                task.put("status",
                        checked ? "Completed" : "Not Started");

                TaskStore.saveTask(this, task);
                showTasks();

            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // Menu ba chấm
        menu.setOnClickListener(v -> {

            PopupMenu popup = new PopupMenu(this, menu);

            popup.getMenu().add(done
                    ? "Mark as Not Started"
                    : "Mark as Complete");

            popup.getMenu().add("Delete");

            popup.setOnMenuItemClickListener(choice -> {

                String action = choice.getTitle().toString();

                if (action.equals("Delete")) {

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
                        task.put("status",
                                done ? "Not Started" : "Completed");

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

        // Nhấn vào Task -> Task Details
        item.findViewById(R.id.layoutTaskContent)
                .setOnClickListener(v -> {

                    Intent intent = new Intent(
                            MainActivity.this,
                            TaskDetailsActivity.class);

                    intent.putExtra("taskId", id);
                    startActivity(intent);
                });

        parent.addView(item);
    }
}
