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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ProjectDetailsActivity extends Activity {

    ProjectsActivity.ProjectData project;

    RecyclerView layoutTasks;
    final List<Task> visibleTasks = new ArrayList<>();
    TaskAdapter taskAdapter;
    TextView txtTitle, txtPercent, txtTotal, txtDone, txtPending;
    TextView filterAll, filterTodo, filterProgress, filterDone;

    String currentFilter = "All";
    int userId;

    int dp(int n) {
        return (int) (n * getResources()
                .getDisplayMetrics().density + 0.5f);
    }

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_project_details);
        userId = getIntent().getIntExtra("userId", 0);

        int position = getIntent().getIntExtra("position", -1);

        if (position < 0 ||
                position >= ProjectsActivity.projects.size()) {
            finish();
            return;
        }

        project = ProjectsActivity.projects.get(position);

        layoutTasks = findViewById(R.id.layoutTasks);
        taskAdapter = new TaskAdapter(visibleTasks,
                new TaskAdapter.Listener() {
                    public void onTaskClick(Task task) {
                        Intent intent = new Intent(ProjectDetailsActivity.this,
                                TaskDetailsActivity.class);
                        intent.putExtra("taskId", task.getId());
                        intent.putExtra("userId", userId);
                        startActivity(intent);
                    }

                    public void onTaskChecked(Task task, boolean checked) {
                        TaskHelper.setCompleted(task, checked);
                        TaskHelper.saveTask(ProjectDetailsActivity.this, task);
                        showDetails();
                    }

                    public void onMenuClick(View anchor, Task task) {
                        showTaskMenu(anchor, task);
                    }
                }, R.layout.item_task);
        layoutTasks.setLayoutManager(new LinearLayoutManager(this));
        layoutTasks.setAdapter(taskAdapter);
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
            intent.putExtra("userId", userId);
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
    List<Task> getProjectTasks() {
        List<Task> result = new ArrayList<>();
        for (Task task : TaskHelper.getTasks(this, userId)) {
            if (project.name.equals(TaskHelper.projectName(this, task))) {
                result.add(task);
            }
        }
        return result;
    }

    // Thống kê
    void showDetails() {

        txtTitle.setText(project.name);

        List<Task> tasks = getProjectTasks();

        int total = tasks.size();
        int completed = 0;

        for (Task task : tasks) {
            if (TaskHelper.isDone(task)) completed++;
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
        visibleTasks.clear();
        List<Task> tasks = getProjectTasks();

        for (Task task : tasks) {
            String status = task.getStatus();

            if (!currentFilter.equals("All") &&
                    !currentFilter.equals(status)) {
                continue;
            }

            visibleTasks.add(task);
        }
        taskAdapter.notifyDataSetChanged();
    }

    void showTaskMenu(View anchor, Task task) {
        PopupMenu menu = new PopupMenu(this, anchor);
        menu.getMenu().add("Edit Task");
        menu.getMenu().add("Not Started");
        menu.getMenu().add("In Progress");
        menu.getMenu().add("Completed");
        menu.getMenu().add("Delete Task");
        menu.setOnMenuItemClickListener(choice -> {
            String action = choice.getTitle().toString();
            if (action.equals("Edit Task")) {
                Intent intent = new Intent(this, TaskFormActivity.class);
                intent.putExtra("taskId", task.getId());
                intent.putExtra("userId", userId);
                startActivity(intent);
            } else if (action.equals("Delete Task")) {
                new AlertDialog.Builder(this)
                        .setTitle("Delete Task")
                        .setMessage("Delete " + task.getTitle() + "?")
                        .setNegativeButton("Cancel", null)
                        .setPositiveButton("Delete", (d, w) -> {
                            TaskHelper.deleteTask(this, task.getId());
                            showDetails();
                        }).show();
            } else {
                TaskHelper.setStatus(task, action);
                TaskHelper.saveTask(this, task);
                showDetails();
            }
            return true;
        });
        menu.show();
    }

    void addTaskCard(Task task) {

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

        int id = task.getId();
        String name = task.getTitle();
        java.util.List<String> categories =
                TaskHelper.categoryNames(this, task);
        String category = categories.isEmpty() ? "" : categories.get(0);

        boolean completed = TaskHelper.isDone(task);

        txtName.setText(name);

        txtTime.setText(
                TaskHelper.formatDate(task.getDate().toString()) +
                        " · " + project.name);

        txtCategory.setText("● " + category);
        txtCategory.setTextColor(
                TaskHelper.categoryColor(category));

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
                TaskHelper.setCompleted(task, checked);

                TaskHelper.saveTask(this, task);

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
                    intent.putExtra("userId", userId);
                    startActivity(intent);

                    return true;
                }

                if (action.equals("Delete Task")) {

                    new AlertDialog.Builder(this)
                            .setTitle("Delete Task")
                            .setMessage("Delete " + name + "?")
                            .setNegativeButton("Cancel", null)
                            .setPositiveButton("Delete", (d, w) -> {

                                TaskHelper.deleteTask(this, id);
                                showDetails();

                            }).show();

                    return true;
                }

                TaskHelper.setStatus(task, action);
                TaskHelper.saveTask(this, task);
                showDetails();

                return true;
            });

            menu.show();
        });

        // Nhấn vào Task để xem chi tiết
        item.setOnClickListener(v -> {

            Intent intent = new Intent(
                    this, TaskDetailsActivity.class);

            intent.putExtra("taskId", id);
            intent.putExtra("userId", userId);
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

                        }

                        project.name = newName;

                        ProjectsActivity.saveData(this, userId);

                        showDetails();
                        dialog.dismiss();
                    });
        });

        dialog.show();
    }

}
