package com.example.todolist_app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class ProjectsActivity extends Activity {

    public static ArrayList<ProjectData> projects = new ArrayList<>();

    RecyclerView layoutProjects;
    ProjectAdapter projectAdapter;
    int userId;

    int[] colors = {
            0xFF4D95DF, 0xFFF0844C, 0xFF55B968,
            0xFFE85283, 0xFFA357B9, 0xFFF7C641,
            0xFFED3535, 0xFF28A69B, 0xFF7794A0,
            0xFF917067
    };

    // Thông tin Project
    public static class ProjectData {
        public int id;
        public String name, deadline;
        public int color;
        public boolean archived = false;

        ProjectData(String name, int color, String deadline) {
            this.name = name;
            this.color = color;
            this.deadline = deadline;
        }
    }

    int dp(int n) {
        return (int) (n * getResources()
                .getDisplayMetrics().density + 0.5f);
    }

    GradientDrawable circle(int color) {
        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.OVAL);
        bg.setColor(color);
        return bg;
    }


    public static void loadData(Context context, int userId) {
        projects.clear();
        DatabaseHelper helper =
                new DatabaseHelper(context);
        try {
            ProjectDao dao = new ProjectDao(helper.getReadableDatabase());
            for (Project stored : dao.findByUserId(userId)) {
                int index = projects.size();
                ProjectData project = new ProjectData(
                        stored.getName(),
                        TaskHelper.COLORS[index % TaskHelper.COLORS.length],
                        "");
                project.id = stored.getId();
                projects.add(project);
            }
        } finally {
            helper.close();
        }
        // Chỉ tạo Project mẫu trong lần đầu khởi tạo
        SharedPreferences setup = context.getSharedPreferences(
                "sqlite_project_setup",
                Context.MODE_PRIVATE);

        if (!setup.getBoolean("initialized_" + userId, false)) {

            if (projects.isEmpty()) {
                createSamples();
                saveData(context, userId);
            }

            setup.edit()
                    .putBoolean("initialized_" + userId, true)
                    .apply();
        }
    }


    public static void saveData(Context context, int userId) {
        DatabaseHelper helper =
                new DatabaseHelper(context);
        try {
            ProjectDao dao = new ProjectDao(helper.getWritableDatabase());
            for (ProjectData p : projects) {
                Project project = new Project();
                project.setId(p.id);
                project.setUserId(userId);
                project.setName(p.name);
                if (p.id == 0) {
                    p.id = (int) dao.insert(project);
                } else {
                    dao.update(project);
                }
            }
        } finally {
            helper.close();
        }
        context.getSharedPreferences(
                        "sqlite_project_setup",
                        Context.MODE_PRIVATE)
                .edit()
                .putBoolean("initialized_" + userId, true)
                .apply();
    }


    // Bốn Project mẫu
    static void createSamples() {
        ProjectData p1 = new ProjectData(
                "Q4 Product Launch", 0xFF6051D8, "Sep 16");

        projects.add(p1);

        ProjectData p2 = new ProjectData(
                "Website Redesign", 0xFF9634E8, "Sep 13");
        projects.add(p2);

        ProjectData p3 = new ProjectData(
                "Fitness Goal", 0xFF288438, "Sep 14");
        projects.add(p3);

        ProjectData p4 = new ProjectData(
                "Side Project", 0xFF008CC9, "Sep 18");
        projects.add(p4);
    }

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);

        getWindow().setStatusBarColor(Color.WHITE);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        setContentView(R.layout.activity_projects);
        userId = getIntent().getIntExtra("userId", 0);

        View navHome = findViewById(R.id.navHome);

        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                Intent intent = new Intent(
                        ProjectsActivity.this,
                        MainActivity.class);

                intent.putExtra("userId", userId);
                startActivity(intent);
                finish();
            });
        }


        View navCategories = findViewById(R.id.navCategories);
        if (navCategories != null) {
            navCategories.setOnClickListener(v -> {
                Intent intent = new Intent(
                        ProjectsActivity.this,
                        CategoriesActivity.class);
                intent.putExtra("userId", userId);
                startActivity(intent);
                finish();
            });
        }

        View navProfile = findViewById(R.id.navProfile);
        if (navProfile != null) {
            navProfile.setOnClickListener(v -> {
                Intent intent = new Intent(
                        ProjectsActivity.this,
                        ProfileActivity.class);
                intent.putExtra("userId", userId);
                startActivity(intent);
                finish();
            });
        }

        layoutProjects = findViewById(R.id.layoutProjects);
        projectAdapter = new ProjectAdapter(projects,
                new ProjectAdapter.Listener() {
                    @Override
                    public void onProjectClick(ProjectData project) {
                        Intent intent = new Intent(ProjectsActivity.this,
                                ProjectDetailsActivity.class);
                        intent.putExtra("position", projects.indexOf(project));
                        intent.putExtra("userId", userId);
                        startActivity(intent);
                    }

                    @Override
                    public void onMenuClick(View anchor, ProjectData project) {
                        showProjectMenu(anchor, project);
                    }
                }, userId);
        layoutProjects.setLayoutManager(new LinearLayoutManager(this));
        layoutProjects.setAdapter(projectAdapter);
        findViewById(R.id.btnAdd).setOnClickListener(v ->
                showProjectDialog(null));
        findViewById(R.id.btnAddTop).setOnClickListener(v ->
                showProjectDialog(null));
        // Nhấn giữ nút cộng trên để khôi phục Project
        findViewById(R.id.btnAddTop).setOnLongClickListener(v -> {
            showArchived();
            return true;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (layoutProjects != null) {
            loadData(this, userId);
            showProjects();
        }
    }


    // Hiển thị các project chưa Archive
    void showProjects() {
        projectAdapter.notifyDataSetChanged();
    }

    void showProjectMenu(View anchor, ProjectData p) {
        PopupMenu menu = new PopupMenu(this, anchor);
        menu.getMenu().add("Edit");
        menu.getMenu().add("Archive");
        menu.getMenu().add("Delete");
        menu.setOnMenuItemClickListener(choice -> {
            String action = choice.getTitle().toString();
            if (action.equals("Edit")) {
                showProjectDialog(p);
            } else if (action.equals("Archive")) {
                new AlertDialog.Builder(this)
                        .setTitle("Archive Project")
                        .setMessage("Archive " + p.name + "?")
                        .setNegativeButton("Cancel", null)
                        .setPositiveButton("Archive", (d, w) -> {
                            p.archived = true;
                            saveData(this, userId);
                            showProjects();
                        }).show();
            } else {
                new AlertDialog.Builder(this)
                        .setTitle("Delete Project")
                        .setMessage("Delete " + p.name +
                                "? Tasks will remain in Home.")
                        .setNegativeButton("Cancel", null)
                        .setPositiveButton("Delete", (d, w) -> {
                            projects.remove(p);
                            DatabaseHelper helper = new DatabaseHelper(this);
                            try {
                                new ProjectDao(helper.getWritableDatabase())
                                        .delete(p.id);
                            } finally {
                                helper.close();
                            }
                            showProjects();
                        }).show();
            }
            return true;
        });
        menu.show();
    }

    // Thống kê Task trong bảng SQLite chung với Home
    int[] thongKeTask(String projectName) {
        java.util.List<Task> tasks = TaskHelper.getTasks(this, userId);
        int total = 0;
        int completed = 0;

        for (Task task : tasks) {
            if (projectName.equals(TaskHelper.projectName(this, task))) {
                total++;
                if (TaskHelper.isDone(task)) completed++;
            }
        }
        return new int[]{total, completed};
    }

    boolean trungTenProject(String name, ProjectData except) {
        for (ProjectData p : projects) {
            if (p != except && p.name.equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    // Tạo một thẻ project
    void addProjectCard(ProjectData p) {
        View item = getLayoutInflater().inflate(
                R.layout.item_project, layoutProjects, false);

        TextView txtName = item.findViewById(R.id.txtProjectName);
        TextView txtCount = item.findViewById(R.id.txtCount);
        TextView txtDeadline = item.findViewById(R.id.txtDeadline);
        TextView btnMenu = item.findViewById(R.id.btnMenu);
        View dot = item.findViewById(R.id.projectColor);
        ProgressBar progress = item.findViewById(R.id.progressProject);

        txtName.setText(p.name);
        int[] stats = thongKeTask(p.name);
        int total = stats[0];
        int completed = stats[1];
        txtCount.setText(completed + "/" +
                total + " tasks completed");


        txtDeadline.setText(p.deadline.isEmpty()
                ? "" : "Due " + p.deadline);

        dot.setBackground(circle(p.color));
        progress.setProgress(
                total == 0 ? 0 : completed * 100 / total);

        layoutProjects.addView(item);

        // Mở Project Details
        item.setOnClickListener(v -> {
            Intent intent = new Intent(
                    this, ProjectDetailsActivity.class);

            intent.putExtra("position", projects.indexOf(p));
            intent.putExtra("userId", userId);
            startActivity(intent);
        });

        // Menu Edit / Archive / Delete
        btnMenu.setOnClickListener(v -> {
            PopupMenu menu = new PopupMenu(this, btnMenu);

            menu.getMenu().add("Edit");
            menu.getMenu().add("Archive");
            menu.getMenu().add("Delete");

            menu.setOnMenuItemClickListener(choice -> {
                String action = choice.getTitle().toString();

                if (action.equals("Edit")) {
                    showProjectDialog(p);
                }

                if (action.equals("Archive")) {
                    new AlertDialog.Builder(this)
                            .setTitle("Archive Project")
                            .setMessage("Archive " + p.name + "?")
                            .setNegativeButton("Cancel", null)
                            .setPositiveButton("Archive", (d, w) -> {
                                p.archived = true;
                                saveData(this, userId);
                                showProjects();
                            }).show();
                }

                if (action.equals("Delete")) {
                    new AlertDialog.Builder(this)
                            .setTitle("Delete Project")
                            .setMessage("Delete " + p.name +
                                    "? Tasks will remain in Home.")
                            .setNegativeButton("Cancel", null)
                            .setPositiveButton("Delete", (d, w) -> {
                                projects.remove(p);
                                DatabaseHelper helper = new DatabaseHelper(this);
                                try {
                                    new ProjectDao(helper.getWritableDatabase())
                                            .delete(p.id);
                                } finally {
                                    helper.close();
                                }
                                showProjects();
                            }).show();
                }

                return true;
            });

            menu.show();
        });
    }

    // Cửa sổ Add / Edit
    void showProjectDialog(ProjectData oldProject) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_add_project);

        EditText edtName =
                dialog.findViewById(R.id.edtProjectName);
        TextView txtTitle =
                dialog.findViewById(R.id.txtDialogTitle);
        TextView txtPreview =
                dialog.findViewById(R.id.txtPreviewName);
        TextView txtPreviewCount =
                dialog.findViewById(R.id.txtPreviewCount);
        TextView previewIcon =
                dialog.findViewById(R.id.previewIcon);
        TextView btnDeadline =
                dialog.findViewById(R.id.btnDeadline);
        LinearLayout layoutColors =
                dialog.findViewById(R.id.layoutColors);
        Button btnCreate =
                dialog.findViewById(R.id.btnCreate);

        final int[] selectedColor = {colors[0]};
        final String[] selectedDate = {""};

        if (oldProject != null) {
            txtTitle.setText("Edit Project");
            btnCreate.setText("Save Changes");

            edtName.setText(oldProject.name);
            selectedColor[0] = oldProject.color;
            selectedDate[0] = oldProject.deadline;

            txtPreviewCount.setText(
                    thongKeTask(oldProject.name)[0] + " tasks   ›");

            if (!selectedDate[0].isEmpty()) {
                btnDeadline.setText(selectedDate[0]);
            }
        }

        // Tên nhập thay đổi thì preview thay đổi
        Runnable updatePreview = () -> {
            String name = edtName.getText().toString().trim();

            txtPreview.setText(
                    name.isEmpty() ? "Project name" : name);
            txtPreview.setTextColor(
                    name.isEmpty() ? 0xFFAAAAAA : Color.BLACK);

            previewIcon.setBackground(
                    circle(selectedColor[0]));

            btnCreate.setEnabled(!name.isEmpty());
            btnCreate.setBackgroundTintList(
                    ColorStateList.valueOf(
                            name.isEmpty() ? 0xFFB5B5B5 : Color.BLACK));
        };

        edtName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(
                    CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(
                    CharSequence s, int start, int before, int count) {
                updatePreview.run();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Vẽ bảng màu, nhấn được
        Runnable drawColors = new Runnable() {
            @Override
            public void run() {
                layoutColors.removeAllViews();

                for (int row = 0; row < 2; row++) {
                    LinearLayout line = new LinearLayout(
                            ProjectsActivity.this);
                    layoutColors.addView(line);

                    int start = row == 0 ? 0 : 8;
                    int end = row == 0 ? 8 : 10;

                    int available = getResources()
                            .getDisplayMetrics().widthPixels - dp(40);
                    int cell = available / 8;
                    int size = Math.min(dp(36), cell - dp(4));

                    for (int i = start; i < end; i++) {
                        int color = colors[i];

                        TextView swatch = new TextView(
                                ProjectsActivity.this);
                        swatch.setGravity(Gravity.CENTER);
                        swatch.setTextSize(18);
                        swatch.setTextColor(Color.WHITE);
                        swatch.setText(
                                selectedColor[0] == color ? "✓" : "");
                        swatch.setBackground(circle(color));

                        LinearLayout.LayoutParams lp =
                                new LinearLayout.LayoutParams(size, size);

                        lp.rightMargin = cell - size;
                        lp.bottomMargin = dp(8);
                        line.addView(swatch, lp);

                        swatch.setOnClickListener(v -> {
                            selectedColor[0] = color;
                            run();
                            updatePreview.run();
                        });
                    }
                }
            }
        };

        drawColors.run();
        updatePreview.run();

        // Chọn ngày deadline
        btnDeadline.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();

            DatePickerDialog picker = new DatePickerDialog(
                    this, (view, year, month, day) -> {
                Calendar selected = Calendar.getInstance();
                selected.set(year, month, day);

                SimpleDateFormat format = new SimpleDateFormat(
                        "MMM d, yyyy", Locale.ENGLISH);

                selectedDate[0] =
                        format.format(selected.getTime());
                btnDeadline.setText(selectedDate[0]);

            }, cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH));

            picker.show();
        });

        dialog.findViewById(R.id.btnClose)
                .setOnClickListener(v -> dialog.dismiss());

        // Nút lưu
        btnCreate.setOnClickListener(v -> {
            String name = edtName.getText().toString().trim();

            if (name.isEmpty()) {
                edtName.setError("Enter project name");
                return;
            }

            if (trungTenProject(name, oldProject)) {
                edtName.setError("Project already exists");
                return;
            }

            if (oldProject == null) {
                projects.add(new ProjectData(
                        name, selectedColor[0], selectedDate[0]));
                saveData(this, userId);
            } else {
                String oldName = oldProject.name;
                oldProject.name = name;
                oldProject.color = selectedColor[0];
                oldProject.deadline = selectedDate[0];

                saveData(this, userId);
            }

            showProjects();
            dialog.dismiss();
        });

        dialog.show();

        Window window = dialog.getWindow();

        if (window != null) {
            window.setBackgroundDrawableResource(
                    android.R.color.transparent);
            window.setLayout(-1, -2);
            window.setGravity(Gravity.BOTTOM);
            window.setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }
    }

    // Xem và khôi phục Archived Project
    void showArchived() {
        ArrayList<ProjectData> archived = new ArrayList<>();
        ArrayList<String> names = new ArrayList<>();

        for (ProjectData p : projects) {
            if (p.archived) {
                archived.add(p);
                names.add(p.name);
            }
        }

        if (archived.isEmpty()) {
            Toast.makeText(this,
                    "No archived projects",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Archived Projects")
                .setItems(names.toArray(new String[0]), (d, which) -> {
                    ProjectData p = archived.get(which);

                    new AlertDialog.Builder(this)
                            .setTitle("Restore Project")
                            .setMessage("Restore " + p.name + "?")
                            .setNegativeButton("Cancel", null)
                            .setPositiveButton("Restore", (a, b) -> {
                                p.archived = false;
                                saveData(this, userId);
                                showProjects();
                            }).show();
                }).show();
    }
}
