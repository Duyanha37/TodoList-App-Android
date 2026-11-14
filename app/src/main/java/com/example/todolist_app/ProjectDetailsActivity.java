package com.example.todolist_app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

public class ProjectDetailsActivity extends Activity {

    ProjectsActivity.ProjectData project;

    LinearLayout layoutTasks;
    TextView txtTitle, txtPercent, txtTotal, txtDone, txtPending;
    TextView filterAll, filterTodo, filterProgress, filterDone;

    String currentFilter = "All";

    int dp(int n) {
        return (int) (n * getResources()
                .getDisplayMetrics().density + 0.5f);
    }

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_project_details);

        int position = getIntent().getIntExtra("position", -1);

        if (position < 0 ||
                position >= ProjectsActivity.projects.size()) {
            finish();
            return;
        }

        project = ProjectsActivity.projects.get(position);

        layoutTasks = findViewById(R.id.layoutTasks);
        txtTitle = findViewById(R.id.txtTitle);
        txtPercent = findViewById(R.id.txtPercent);
        txtTotal = findViewById(R.id.txtTotal);
        txtDone = findViewById(R.id.txtDone);
        txtPending = findViewById(R.id.txtPending);

        filterAll = findViewById(R.id.filterAll);
        filterTodo = findViewById(R.id.filterTodo);
        filterProgress = findViewById(R.id.filterProgress);
        filterDone = findViewById(R.id.filterDone);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        findViewById(R.id.btnEdit)
                .setOnClickListener(v -> editProjectName());

        // Thêm Task bằng TaskFormActivity
        findViewById(R.id.btnAddTask).setOnClickListener(v -> {
            Intent intent = new Intent(
                    this, TaskFormActivity.class);

            intent.putExtra("projectName", project.name);
            startActivity(intent);
        });

        filterAll.setOnClickListener(v -> changeFilter("All"));

        filterTodo.setOnClickListener(v ->
                changeFilter("Not Started"));

        filterProgress.setOnClickListener(v ->
                changeFilter("In Progress"));

        filterDone.setOnClickListener(v ->
                changeFilter("Completed"));

        changeFilter("All");
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (project != null && layoutTasks != null) {
            showDetails();
        }
    }

    // Lấy Task của Project từ SQLite
    JSONArray getProjectTasks() {

        JSONArray allTasks = TaskStore.getTasks(this);
        JSONArray result = new JSONArray();

        for (int i = 0; i < allTasks.length(); i++) {
            JSONObject task = allTasks.optJSONObject(i);

            if (task == null) continue;

            if (project.name.equals(
                    task.optString("project"))) {
                result.put(task);
            }
        }

        return result;
    }

    // Thống kê
    void showDetails() {

        txtTitle.setText(project.name);

        JSONArray tasks = getProjectTasks();

        int total = tasks.length();
        int completed = 0;

        for (int i = 0; i < tasks.length(); i++) {
            JSONObject task = tasks.optJSONObject(i);

            if (task != null && TaskStore.isDone(task)) {
                completed++;
            }
        }

        int percent = total == 0
                ? 0 : completed * 100 / total;

        txtTotal.setText(total + "\nTotal");
        txtDone.setText(completed + "\nDone");
        txtPending.setText(
                (total - completed) + "\nPending");
        txtPercent.setText(percent + "%");

        showTasks();
    }

    // Bộ lọc
    void changeFilter(String filter) {

        currentFilter = filter;

        TextView[] buttons = {
                filterAll, filterTodo,
                filterProgress, filterDone
        };

        String[] values = {
                "All", "Not Started",
                "In Progress", "Completed"
        };

        for (int i = 0; i < buttons.length; i++) {

            boolean selected = values[i].equals(filter);

            buttons[i].setTextColor(
                    selected ? Color.WHITE : 0xFF888888);

            buttons[i].setBackgroundResource(
                    selected
                            ? R.drawable.bg_category_selected
                            : R.drawable.bg_outline);
        }

        showTasks();
    }

    void showTasks() {

        layoutTasks.removeAllViews();

        JSONArray tasks = getProjectTasks();

        for (int i = 0; i < tasks.length(); i++) {

            JSONObject task = tasks.optJSONObject(i);

            if (task == null) continue;

            String status = task.optString("status");

            if (!currentFilter.equals("All") &&
                    !currentFilter.equals(status)) {
                continue;
            }

            addTaskCard(task);
        }
    }

    void addTaskCard(JSONObject task) {

        View item = getLayoutInflater().inflate(
                R.layout.item_task, layoutTasks, false);

        TextView txtName =
                item.findViewById(R.id.txtTaskName);

        TextView txtTime =
                item.findViewById(R.id.txtTime);

        TextView txtCategory =
                item.findViewById(R.id.txtCategory);

        TextView btnMenu =
                item.findViewById(R.id.btnTaskMenu);

        CheckBox check =
                item.findViewById(R.id.checkTask);

        String id = task.optString("id");
        String name = task.optString("name");
        String category = task.optString("category");

        boolean completed = TaskStore.isDone(task);

        txtName.setText(name);

        txtTime.setText(
                task.optString("time") +
                        " · " + project.name);

        txtCategory.setText("● " + category);
        txtCategory.setTextColor(
                TaskStore.categoryColor(category));

        check.setChecked(completed);

        if (completed) {
            txtName.setPaintFlags(
                    txtName.getPaintFlags() |
                            Paint.STRIKE_THRU_TEXT_FLAG);

            txtName.setTextColor(0xFF999999);
        }

        // Đánh dấu hoàn thành, cập nhật SQLite
        check.setOnCheckedChangeListener((button, checked) -> {

            try {
                task.put("status",
                        checked ? "Completed" : "Not Started");

                TaskStore.saveTask(this, task);

                showDetails();

            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // Menu thao tác
        btnMenu.setOnClickListener(v -> {

            PopupMenu menu = new PopupMenu(this, btnMenu);

            menu.getMenu().add("Edit Task");
            menu.getMenu().add("Not Started");
            menu.getMenu().add("In Progress");
            menu.getMenu().add("Completed");
            menu.getMenu().add("Delete Task");

            menu.setOnMenuItemClickListener(choice -> {

                String action =
                        choice.getTitle().toString();

                if (action.equals("Edit Task")) {

                    Intent intent = new Intent(
                            this, TaskFormActivity.class);

                    intent.putExtra("taskId", id);
                    startActivity(intent);

                    return true;
                }

                if (action.equals("Delete Task")) {

                    new AlertDialog.Builder(this)
                            .setTitle("Delete Task")
                            .setMessage("Delete " + name + "?")
                            .setNegativeButton("Cancel", null)
                            .setPositiveButton("Delete", (d, w) -> {

                                TaskStore.deleteTask(this, id);
                                showDetails();

                            }).show();

                    return true;
                }

                try {
                    task.put("status", action);

                    TaskStore.saveTask(this, task);

                    showDetails();

                } catch (Exception e) {
                    e.printStackTrace();
                }

                return true;
            });

            menu.show();
        });

        // Nhấn vào Task để xem chi tiết
        item.setOnClickListener(v -> {

            Intent intent = new Intent(
                    this, TaskDetailsActivity.class);

            intent.putExtra("taskId", id);
            startActivity(intent);
        });

        layoutTasks.addView(item);
    }

    // Đổi tên Project và cập nhật các Task liên quan
    void editProjectName() {

        EditText edt = new EditText(this);
        edt.setSingleLine(true);
        edt.setText(project.name);

        edt.setPadding(
                dp(20), dp(12), dp(20), dp(12));

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Edit Project")
                .setView(edt)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Save", null)
                .create();

        dialog.setOnShowListener(d -> {

            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                    .setOnClickListener(v -> {

                        String newName =
                                edt.getText().toString().trim();

                        if (newName.isEmpty()) {
                            edt.setError("Enter project name");
                            return;
                        }

                        String oldName = project.name;

                        if (!oldName.equals(newName)) {

                            for (ProjectsActivity.ProjectData p :
                                    ProjectsActivity.projects) {

                                if (p != project &&
                                        p.name.equals(newName)) {
                                    edt.setError("Project already exists");
                                    return;
                                }
                            }

                            updateTaskProjectNames(oldName, newName);
                        }

                        project.name = newName;

                        ProjectsActivity.saveData(this);

                        showDetails();
                        dialog.dismiss();
                    });
        });

        dialog.show();
    }

    void updateTaskProjectNames(String oldName,
                                String newName) {

        JSONArray tasks = TaskStore.getTasks(this);

        for (int i = 0; i < tasks.length(); i++) {

            JSONObject task = tasks.optJSONObject(i);

            if (task == null) continue;

            if (oldName.equals(
                    task.optString("project"))) {

                try {
                    task.put("project", newName);
                    TaskStore.saveTask(this, task);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
