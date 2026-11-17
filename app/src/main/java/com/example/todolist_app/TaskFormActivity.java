package com.example.todolist_app;

import android.app.*;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.*;
import android.view.*;
import android.widget.*;

import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.ArrayList;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class TaskFormActivity extends Activity {

    EditText edtName, edtDescription;
    TextView btnDate, btnTime, btnSaveTop;
    TextView statusTodo, statusProgress, statusDone;
    LinearLayout layoutCategories;
    Spinner spinnerProject;
    List<String> projectNames = new ArrayList<>();
    List<String> categoryNames = new ArrayList<>();
    Button btnSaveTask;

    int taskId = 0;
    String selectedDate = TaskHelper.today(0);
    String selectedTime = "9:00 AM";
    String selectedCategory = "Work";
    String selectedStatus = "Not Started";

    Task editingTask;
    int userId;

    int dp(int n) {
        return (int)(n * getResources()
                .getDisplayMetrics().density + 0.5f);
    }

    void loadProjects() {
        projectNames.clear();
        DatabaseHelper helper =
                new DatabaseHelper(this);
        try {
            ProjectDao dao = new ProjectDao(helper.getReadableDatabase());
            for (Project project : dao.findByUserId(userId)) {
                projectNames.add(project.getName());
            }
        } finally {
            helper.close();
        }

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        projectNames);
        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item); //Gán viewsource
        spinnerProject.setAdapter(adapter);
    }

    void loadCategories() { //Chưa rõ xử lý cái gì chỉ mỗi load
        categoryNames.clear();
        DatabaseHelper helper =
                new DatabaseHelper(this);
        try {
            CategoriesDao dao =
                    new CategoriesDao(helper.getReadableDatabase());
            for (Categories category : dao.findByUserId(userId)) {
                categoryNames.add(category.getName());
            }
        } finally {
            helper.close();
        }
    }

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_task_form);
        userId = getIntent().getIntExtra("userId", 0);

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

        taskId = getIntent().getIntExtra("taskId", 0);

        loadProjects();
        loadCategories();


        if (taskId != 0) {
            editingTask = TaskHelper.find(this, taskId, userId);

            if (editingTask == null) {
                finish();
                return;
            }

            ((TextView)findViewById(R.id.txtFormTitle))
                    .setText("Edit Task");

            edtName.setText(editingTask.getTitle());
            edtDescription.setText(
                    editingTask.getDescription());

            selectedDate = editingTask.getDate() == null
                    ? TaskHelper.today(0) : editingTask.getDate().toString();
            selectedTime = editingTask.getTime() == null
                    ? "9:00 AM"
                    : editingTask.getTime().format(
                    DateTimeFormatter.ofPattern("h:mm a", Locale.US));
            selectedStatus = editingTask.getStatus();


            String oldProject = TaskHelper.projectName(this, editingTask);
            if (!oldProject.isEmpty()) {
                int selectedIndex = -1; //Đặt vị trí chọn là -1 (không chọn gì)
                for (int i = 0; i < projectNames.size(); i++) {
                    if (projectNames.get(i).equals(oldProject)) {
                        selectedIndex = i;
                        break;
                    }
                }
                if(selectedIndex == -1) { //Nếu chưa tồn tại thì thêm mới
                    projectNames.add(oldProject);
                    ArrayAdapter<String> adapter =
                            (ArrayAdapter<String>) spinnerProject.getAdapter();
                    adapter.notifyDataSetChanged();
                    selectedIndex = projectNames.size() - 1;
                }
                spinnerProject.setSelection(selectedIndex);
            }
        }

        btnDate.setText("▢  " + TaskHelper.formatDate(selectedDate));
        btnTime.setText("◷  " + selectedTime);
        if (taskId == 0 &&
                getIntent().hasExtra("categoryName")) {
            selectedCategory =
                    getIntent().getStringExtra("categoryName");
        }
        // Khi mở Add Task từ Project Details
        if (taskId == 0 &&
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
                        TaskHelper.formatDate(selectedDate));

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
        return TaskHelper.categoryColor(categoryName);
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
            edtName.setError("Nhập tên Task");
            return;
        }
        String description = edtDescription.getText().toString().trim();
        String project = "";
        if (spinnerProject.getSelectedItem() != null) {
            project = spinnerProject.getSelectedItem().toString();
        }
        Task task;

        if (editingTask == null) {
            task = TaskHelper.makeTask(
                    name, description, selectedDate, selectedTime,
                    selectedCategory, project, selectedStatus);
            task.setUserId(userId);
        } else {
            task = editingTask;
            task.setTitle(name);
            task.setDescription(description);
            task.setDate(java.time.LocalDate.parse(selectedDate));
            task.setTime(LocalTime.parse(selectedTime,
                    DateTimeFormatter.ofPattern("h:mm a", Locale.US)));
            task.setStatus(selectedStatus);
        }

        TaskHelper.saveTask(this, task, selectedCategory, project);
        NotificationScheduler.schedule(this, task);
        finish();
    }
}
