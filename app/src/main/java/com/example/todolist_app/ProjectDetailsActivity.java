package com.example.todolist_app;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;

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

        findViewById(R.id.btnEdit).setOnClickListener(v ->
                editProjectName());

        findViewById(R.id.btnAddTask).setOnClickListener(v ->
                showTaskDialog(null));

        filterAll.setOnClickListener(v -> changeFilter("All"));
        filterTodo.setOnClickListener(v -> changeFilter("To Do"));
        filterProgress.setOnClickListener(v ->
                changeFilter("In Progress"));
        filterDone.setOnClickListener(v -> changeFilter("Done"));

        showDetails();
    }

    void showDetails() {
        txtTitle.setText(project.name);

        int total = project.tasks.size();
        int done = project.getDone();

        txtTotal.setText(total + "\nTotal");
        txtDone.setText(done + "\nDone");
        txtPending.setText((total - done) + "\nPending");
        txtPercent.setText(project.getPercent() + "%");

        showTasks();
    }

    void changeFilter(String filter) {
        currentFilter = filter;

        TextView[] buttons = {
                filterAll, filterTodo, filterProgress, filterDone
        };

        String[] names = {
                "All", "To Do", "In Progress", "Done"
        };

        for (int i = 0; i < buttons.length; i++) {
            boolean selected = names[i].equals(filter);

            buttons[i].setTextColor(
                    selected ? Color.WHITE : 0xFF888888);

            buttons[i].setBackgroundResource(
                    selected ? R.drawable.bg_circle
                            : R.drawable.bg_outline);
        }

        showTasks();
    }

    void showTasks() {
        layoutTasks.removeAllViews();

        for (ProjectsActivity.TaskData task : project.tasks) {
            String status = task.done ? "Done" : task.status;

            if (!currentFilter.equals("All") &&
                    !currentFilter.equals(status)) {
                continue;
            }

            addTaskCard(task);
        }
    }

    void addTaskCard(ProjectsActivity.TaskData task) {
        View item = getLayoutInflater().inflate(
                R.layout.item_task, layoutTasks, false);

        TextView txtName = item.findViewById(R.id.txtTaskName);
        TextView txtTime = item.findViewById(R.id.txtTime);
        TextView txtCategory = item.findViewById(R.id.txtCategory);
        TextView btnMenu = item.findViewById(R.id.btnTaskMenu);
        CheckBox check = item.findViewById(R.id.checkTask);

        txtName.setText(task.name);
        txtTime.setText(task.time + " · " + project.name);
        txtCategory.setText("● " + task.category);

        check.setChecked(task.done);

        if (task.done) {
            txtName.setPaintFlags(txtName.getPaintFlags()
                    | Paint.STRIKE_THRU_TEXT_FLAG);
            txtName.setTextColor(0xFF999999);
        }

        // Đánh dấu hoàn thành
        check.setOnCheckedChangeListener((button, checked) -> {
            task.done = checked;
            task.status = checked ? "Done" : "To Do";

            ProjectsActivity.saveData(this);
            showDetails();
        });

        btnMenu.setOnClickListener(v -> {
            PopupMenu menu = new PopupMenu(this, btnMenu);

            menu.getMenu().add("Edit Task");
            menu.getMenu().add("To Do");
            menu.getMenu().add("In Progress");
            menu.getMenu().add("Done");
            menu.getMenu().add("Delete Task");

            menu.setOnMenuItemClickListener(choice -> {
                String action = choice.getTitle().toString();

                if (action.equals("Edit Task")) {
                    showTaskDialog(task);
                    return true;
                }

                if (action.equals("Delete Task")) {
                    new AlertDialog.Builder(this)
                            .setTitle("Delete Task")
                            .setMessage("Delete " + task.name + "?")
                            .setNegativeButton("Cancel", null)
                            .setPositiveButton("Delete", (d, w) -> {
                                project.tasks.remove(task);
                                ProjectsActivity.saveData(this);
                                showDetails();
                            }).show();

                    return true;
                }

                task.status = action;
                task.done = action.equals("Done");

                ProjectsActivity.saveData(this);
                showDetails();
                return true;
            });

            menu.show();
        });

        layoutTasks.addView(item);
    }

    // Thêm hoặc sửa Task
    void showTaskDialog(ProjectsActivity.TaskData oldTask) {
        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(dp(20), dp(8), dp(20), 0);

        EditText edtName = new EditText(this);
        edtName.setHint("Task name");
        edtName.setSingleLine(true);
        form.addView(edtName);

        EditText edtTime = new EditText(this);
        edtTime.setHint("Time (e.g. 9:00 AM)");
        edtTime.setSingleLine(true);
        form.addView(edtTime);

        Spinner spinner = new Spinner(this);

        String[] categories = {
                "Work", "Finance", "Personal", "Other"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                categories);

        spinner.setAdapter(adapter);
        form.addView(spinner);

        if (oldTask != null) {
            edtName.setText(oldTask.name);
            edtTime.setText(oldTask.time);

            for (int i = 0; i < categories.length; i++) {
                if (categories[i].equals(oldTask.category)) {
                    spinner.setSelection(i);
                    break;
                }
            }
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(oldTask == null ? "Add Task" : "Edit Task")
                .setView(form)
                .setNegativeButton("Cancel", null)
                .setPositiveButton(
                        oldTask == null ? "Add" : "Save", null)
                .create();

        dialog.setOnShowListener(d -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                    .setOnClickListener(v -> {

                        String name = edtName.getText().toString().trim();

                        if (name.isEmpty()) {
                            edtName.setError("Enter task name");
                            return;
                        }

                        String time = edtTime.getText().toString().trim();
                        String category =
                                spinner.getSelectedItem().toString();

                        if (oldTask == null) {
                            project.tasks.add(
                                    new ProjectsActivity.TaskData(
                                            name, time, category, false));
                        } else {
                            oldTask.name = name;
                            oldTask.time = time;
                            oldTask.category = category;
                        }

                        ProjectsActivity.saveData(this);
                        currentFilter = "All";
                        changeFilter("All");
                        showDetails();
                        dialog.dismiss();
                    });
        });

        dialog.show();
    }

    // Sửa tên Project từ trang Details
    void editProjectName() {
        EditText edt = new EditText(this);
        edt.setSingleLine(true);
        edt.setText(project.name);
        edt.setPadding(dp(20), dp(12), dp(20), dp(12));

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Edit Project")
                .setView(edt)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Save", null)
                .create();

        dialog.setOnShowListener(d -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                    .setOnClickListener(v -> {
                        String name = edt.getText().toString().trim();

                        if (name.isEmpty()) {
                            edt.setError("Enter project name");
                            return;
                        }

                        project.name = name;
                        ProjectsActivity.saveData(this);
                        showDetails();
                        dialog.dismiss();
                    });
        });

        dialog.show();
    }
}
