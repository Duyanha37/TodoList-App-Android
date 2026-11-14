package com.example.todolist_app;

import android.app.*;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.content.*;
import android.text.*;
import android.view.*;
import android.widget.*;

import org.json.JSONObject;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.util.Locale;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;

public class TaskFormActivity extends Activity {

    EditText edtName, edtDescription;
    TextView btnDate, btnTime, btnSaveTop;
    TextView statusTodo, statusProgress, statusDone;
    LinearLayout layoutCategories;
    Spinner spinnerProject;
    ArrayList<String> projectNames = new ArrayList<>();
    ArrayList<String> categoryNames = new ArrayList<>();
    Button btnSaveTask;

    String taskId = null;
    String selectedDate = TaskStore.today(0);
    String selectedTime = "9:00 AM";
    String selectedCategory = "Work";
    String selectedStatus = "Not Started";

    JSONObject editingTask;

    int dp(int n) {
        return (int)(n * getResources()
                .getDisplayMetrics().density + 0.5f);
    }

    void loadProjects() {
        projectNames.clear();
        TodoDatabaseHelper helper =
                new TodoDatabaseHelper(this);
        helper.migrateLegacyData();
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT name FROM projects " +
                        "WHERE archived = 0 ORDER BY id ASC",
                null);

        try {
            while (cursor.moveToNext()) {
                projectNames.add(cursor.getString(0));
            }
        } finally {
            cursor.close();
            helper.close();
        }

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        projectNames);
        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        spinnerProject.setAdapter(adapter);
    }

    void loadCategories() {
        categoryNames.clear();
        TodoDatabaseHelper helper =
                new TodoDatabaseHelper(this);
        helper.migrateLegacyData();
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT name FROM categories ORDER BY id ASC",
                null);
        try {
            while (cursor.moveToNext()) {
                categoryNames.add(cursor.getString(0));
            }
        } finally {
            cursor.close();
            helper.close();
        }
    }

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_task_form);

        edtName = findViewById(R.id.edtTaskName);
        edtDescription = findViewById(R.id.edtDescription);
        btnDate = findViewById(R.id.btnDate);
        btnTime = findViewById(R.id.btnTime);
        btnSaveTop = findViewById(R.id.btnSaveTop);
        btnSaveTask = findViewById(R.id.btnSaveTask);
        layoutCategories = findViewById(R.id.layoutCategories);
        spinnerProject = findViewById(R.id.spinnerProject);

        statusTodo = findViewById(R.id.statusTodo);
        statusProgress = findViewById(R.id.statusProgress);
        statusDone = findViewById(R.id.statusDone);

        taskId = getIntent().getStringExtra("taskId");

        loadProjects();
        loadCategories();


        if (taskId != null) {
            editingTask = TaskStore.find(this, taskId);

            if (editingTask == null) {
                finish();
                return;
            }

            ((TextView)findViewById(R.id.txtFormTitle))
                    .setText("Edit Task");

            edtName.setText(editingTask.optString("name"));
            edtDescription.setText(
                    editingTask.optString("description"));

            selectedDate = editingTask.optString("date");
            selectedTime = editingTask.optString("time");
            selectedCategory = editingTask.optString("category");
            selectedStatus = editingTask.optString("status");


            String oldProject = editingTask.optString("project");
            if (!oldProject.isEmpty() &&
                    !projectNames.contains(oldProject)) {
                projectNames.add(oldProject);
                ArrayAdapter<String> adapter =
                        (ArrayAdapter<String>)
                                spinnerProject.getAdapter();
                adapter.notifyDataSetChanged();
            }
            for (int i = 0; i < projectNames.size(); i++) {
                if (projectNames.get(i).equals(oldProject)) {
                    spinnerProject.setSelection(i);
                    break;
                }
            }

        }
        btnDate.setText("▢  " + TaskStore.formatDate(selectedDate));
        btnTime.setText("◷  " + selectedTime);
        if (taskId == null &&
                getIntent().hasExtra("categoryName")) {
            selectedCategory =
                    getIntent().getStringExtra("categoryName");
        }
        // Khi mở Add Task từ Project Details
        if (taskId == null &&
                getIntent().hasExtra("projectName")) {
            String projectName =
                    getIntent().getStringExtra("projectName");
            for (int i = 0; i < projectNames.size(); i++) {

                if (projectNames.get(i).equals(projectName)) {
                    spinnerProject.setSelection(i);
                    break;
                }
            }
        }
        // Giữ danh mục cũ khi sửa Task
        if (!selectedCategory.isEmpty() &&
                !categoryNames.contains(selectedCategory)) {
            categoryNames.add(selectedCategory);
        }
        drawCategories();
        drawStatus();
        updateSaveButton();

        edtName.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(
                    CharSequence s, int start, int count, int after) {}

            public void onTextChanged(
                    CharSequence s, int start, int before, int count) {
                updateSaveButton();
            }

            public void afterTextChanged(Editable s) {}
        });

        btnDate.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();

            try {
                cal.setTime(new SimpleDateFormat(
                        "yyyy-MM-dd", Locale.US).parse(selectedDate));
            } catch (Exception e) {
                e.printStackTrace();
            }

            DatePickerDialog picker = new DatePickerDialog(
                    this, (view, year, month, day) -> {
                Calendar chosen = Calendar.getInstance();
                chosen.set(year, month, day);

                selectedDate = new SimpleDateFormat(
                        "yyyy-MM-dd", Locale.US)
                        .format(chosen.getTime());

                btnDate.setText("▢  " +
                        TaskStore.formatDate(selectedDate));

            }, cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH));

            picker.show();
        });

        btnTime.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();

            new TimePickerDialog(this, (view, hour, minute) -> {
                Calendar chosen = Calendar.getInstance();
                chosen.set(Calendar.HOUR_OF_DAY, hour);
                chosen.set(Calendar.MINUTE, minute);

                selectedTime = new SimpleDateFormat(
                        "h:mm a", Locale.US)
                        .format(chosen.getTime());

                btnTime.setText("◷  " + selectedTime);

            }, cal.get(Calendar.HOUR_OF_DAY),
                    cal.get(Calendar.MINUTE), false).show();
        });

        statusTodo.setOnClickListener(v -> {
            selectedStatus = "Not Started";
            drawStatus();
        });

        statusProgress.setOnClickListener(v -> {
            selectedStatus = "In Progress";
            drawStatus();
        });

        statusDone.setOnClickListener(v -> {
            selectedStatus = "Completed";
            drawStatus();
        });

        btnSaveTask.setOnClickListener(v -> saveTask());
        btnSaveTop.setOnClickListener(v -> saveTask());

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    void updateSaveButton() {
        boolean valid = !edtName.getText().toString().trim().isEmpty();

        btnSaveTask.setEnabled(valid);
        btnSaveTop.setEnabled(valid);

        btnSaveTask.setBackgroundTintList(
                ColorStateList.valueOf(
                        valid ? Color.BLACK : 0xFFB5B5B5));

        btnSaveTop.setTextColor(
                valid ? Color.BLACK : 0xFFB5B5B5);
    }


    void drawCategories() {
        layoutCategories.removeAllViews();
        for (String category : categoryNames) {
            boolean selected =
                    category.equals(selectedCategory);
            TextView chip = new TextView(this);
            chip.setText("●  " + category);
            chip.setTextSize(13);
            chip.setGravity(Gravity.CENTER);
            chip.setPadding(
                    dp(14), dp(9), dp(14), dp(9));
            chip.setTextColor(
                    selected ? Color.WHITE :
                            getCategoryColor(category));
            chip.setBackgroundResource(
                    selected
                            ? R.drawable.bg_category_selected
                            : R.drawable.bg_outline);
            LinearLayout.LayoutParams lp =
                    new LinearLayout.LayoutParams(-2, -2);
            lp.rightMargin = dp(8);
            layoutCategories.addView(chip, lp);
            chip.setOnClickListener(v -> {
                selectedCategory = category;
                drawCategories();
            });
        }
    }

    int getCategoryColor(String categoryName) {
        TodoDatabaseHelper helper =
                new TodoDatabaseHelper(this);
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT color FROM categories WHERE name = ?",
                new String[]{categoryName});
        int color = 0xFF888888;
        try {
            if (cursor.moveToFirst()) {
                color = cursor.getInt(0);
            }
        } finally {
            cursor.close();
            helper.close();
        }
        return color;
    }


    void drawStatus() {
        TextView[] buttons = {
                statusTodo, statusProgress, statusDone
        };

        String[] values = {
                "Not Started", "In Progress", "Completed"
        };

        for (int i = 0; i < buttons.length; i++) {
            boolean selected = values[i].equals(selectedStatus);

            buttons[i].setTextColor(
                    selected ? Color.WHITE : 0xFF888888);

            buttons[i].setBackgroundResource(
                    selected ? R.drawable.bg_status_selected
                            : R.drawable.bg_status_outline);
        }
    }

    void saveTask() {
        String name = edtName.getText().toString().trim();
        if (name.isEmpty()) {
            edtName.setError("Enter task name");
            return;
        }
        String description = edtDescription.getText().toString().trim();
        String project = "";
        if (spinnerProject.getSelectedItem() != null) {
            project = spinnerProject.getSelectedItem().toString();
        }
        JSONObject task;

        if (editingTask == null) {
            task = TaskStore.makeTask(
                    name, description, selectedDate, selectedTime,
                    selectedCategory, project, selectedStatus);
        } else {
            task = editingTask;
            try {
                task.put("name", name);
                task.put("description", description);
                task.put("date", selectedDate);
                task.put("time", selectedTime);
                task.put("category", selectedCategory);
                task.put("project", project);
                task.put("status", selectedStatus);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        TaskStore.saveTask(this, task);
        finish();
    }
}
