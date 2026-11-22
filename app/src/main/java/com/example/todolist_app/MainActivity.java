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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MainActivity extends Activity {

    RecyclerView layoutTaskList;
    TextView txtToday, txtUpcoming, txtOverdue;
    String filter = "All";
    List<Task> tasks;
    final List<Task> visibleTasks = new java.util.ArrayList<>();
    TaskAdapter taskAdapter;
    int userId;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_main);
        userId = getIntent().getIntExtra("userId", 0);

        layoutTaskList = findViewById(R.id.layoutTaskList);
        taskAdapter = new TaskAdapter(visibleTasks, //Gán mảng dữ liệu visibleTasks vào adapter để hiển thị
                new TaskAdapter.Listener() {
                    @Override
                    public void onTaskClick(Task task) {
                        Intent intent = new Intent(MainActivity.this,
                                TaskDetailsActivity.class);
                        intent.putExtra("taskId", task.getId());
                        intent.putExtra("userId", userId);
                        startActivity(intent);
                    }

                    @Override
                    public void onTaskChecked(Task task, boolean checked) {
                        TaskHelper.setCompleted(task, checked);
                        TaskHelper.saveTask(MainActivity.this, task);
                        showTasks();
                    }

                    @Override
                    public void onMenuClick(View anchor, Task task) {
                        showTaskMenu(anchor, task);
                    }
                });
        layoutTaskList.setLayoutManager(new LinearLayoutManager(this)); //Cài đặt cách hiển thị cho RecyclerView là LinearLayoutManager (hiển thị theo chiều dọc)
        layoutTaskList.setAdapter(taskAdapter); //Set adapter cho recyclerView
        txtToday = findViewById(R.id.txtTodayCount);
        txtUpcoming = findViewById(R.id.txtUpcomingCount);
        txtOverdue = findViewById(R.id.txtOverdueCount);
        // Gán event Addtask gọi TaskFormActivity
        findViewById(R.id.btnAddTask).setOnClickListener(v -> {
            Intent newitn = new Intent (MainActivity.this, TaskFormActivity.class);
            newitn.putExtra("userId", userId);
            startActivity(newitn);
        });

        // Gán event Search gọi TaskSearchActivity
        findViewById(R.id.btnSearch).setOnClickListener(v -> {
            Intent newitn = new Intent(MainActivity.this, TaskSearchActivity.class);
            newitn.putExtra("userId", userId);
            startActivity(newitn);
        });

        // Gán event Filter
        findViewById(R.id.btnFilter).setOnClickListener(v -> {
            String[] options = {
                    "All", "Not Started",
                    "In Progress", "Completed"
            };

            new AlertDialog.Builder(this)
                    .setTitle("Filter Tasks")
                    .setMultiChoiceItems(options, new boolean[options.length], //Cấu trúc cơ bản của setMultiChoice 1 mảng giá trị, 1 mảng giá trị đang được chọn là bool và 1 listener
                            (dialog, which, isChecked) -> { //truyền vào obj dialog và index which
                                filter = options[which]; //Lấy option theo index
                                dialog.dismiss(); //Đóng Dialog
                                showTasks(); //Gọi showTasks
                            }).show();
        });

        // Home -> Projects
        findViewById(R.id.navProjects).setOnClickListener(v -> {
            Intent newitn = new Intent(
                    MainActivity.this, ProjectsActivity.class);
            newitn.putExtra("userId", userId);
            startActivity(newitn);
        });

        // Home -> Categories
        findViewById(R.id.navCategories).setOnClickListener(v -> {
            Intent newitn = new Intent(
                    MainActivity.this, CategoriesActivity.class);
            newitn.putExtra("userId", userId);
            startActivity(newitn);
        });

        // Home -> Profile
        findViewById(R.id.navProfile).setOnClickListener(v -> {
            Intent newitn = new Intent(
                    MainActivity.this, ProfileActivity.class);
            newitn.putExtra("userId", userId);
            startActivity(newitn);
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
        tasks = TaskHelper.getTasks(this, userId);
        visibleTasks.clear();

        int today = 0;
        int upcoming = 0;
        int overdue = 0;

        for (int i = 0; i < tasks.size(); i++) {
            Task task = tasks.get(i);

            String group = TaskHelper.group(task);

            if (group.equals("Today")) today++;
            if (group.equals("Upcoming")) upcoming++;
            if (group.equals("Overdue")) overdue++;
        }

        txtToday.setText("Today\n" + today);
        txtUpcoming.setText("Upcoming\n" + upcoming);
        txtOverdue.setText("Overdue\n" + overdue);

        String[] groups = {TaskHelper.GROUP_OVERDUE,
                TaskHelper.GROUP_TODAY, TaskHelper.GROUP_UPCOMING};
        for (String group : groups) {
            for (Task task : tasks) {
                boolean matchesFilter = filter.equals("All") ||
                        filter.equals(task.getStatus());
                if (group.equals(TaskHelper.group(task)) && matchesFilter) {
                    visibleTasks.add(task);
                }
            }
        }
        taskAdapter.notifyDataSetChanged();
    }

    void showTaskMenu(View anchor, Task task) {
        PopupMenu popup = new PopupMenu(this, anchor);
        popup.getMenu().add(TaskHelper.isDone(task)
                ? "Mark as Not Started" : "Mark as Complete");
        popup.getMenu().add("Delete"); //Khi add luôn là add item có title là chuỗi trong ()
        //MenuItem item = popup.getMenu().add("Delete"); cách 2 gán luôn item vào 1 biến có thể dùng để gán sự kiện riêng
        popup.setOnMenuItemClickListener(choice -> {
            if (choice.getTitle().toString().equals("Delete")) {
                new AlertDialog.Builder(this)
                        .setTitle("Delete Task")
                        .setMessage("Delete " + task.getTitle() + "?")
                        .setNegativeButton("Cancel", null)
                        .setPositiveButton("Delete", (d, w) -> {
                            TaskHelper.deleteTask(this, task.getId());
                            showTasks();
                        }).show();
            } else {
                TaskHelper.toggleCompleted(task);
                TaskHelper.saveTask(this, task);
                showTasks();
            }
            return true;
        });
        popup.show();
    }

    // Hiển thị từng Task
    void addTask(LinearLayout parent, Task task) {

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

        int id = task.getId();
        boolean done = TaskHelper.isDone(task);

        name.setText(task.getTitle());

        info.setText(TaskHelper.formatDate(task.getDate().toString()) +
                " · " + TaskHelper.projectName(this, task));

        java.util.List<String> categories =
                TaskHelper.categoryNames(this, task);
        String cat = categories.isEmpty() ? "" : categories.get(0);

        category.setText("●  " + cat);
        category.setTextColor(TaskHelper.categoryColor(cat));

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
                TaskHelper.setCompleted(task, checked);
                TaskHelper.saveTask(this, task);
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
                                    task.getTitle() + "?")
                            .setNegativeButton("Cancel", null)
                            .setPositiveButton("Delete", (d, w) -> {
                                TaskHelper.deleteTask(this, id);
                                showTasks();
                            }).show();

                } else {

                    TaskHelper.toggleCompleted(task);
                    TaskHelper.saveTask(this, task);
                    showTasks();
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
                    intent.putExtra("userId", userId);
                    startActivity(intent);
                });

        parent.addView(item);
    }
}
