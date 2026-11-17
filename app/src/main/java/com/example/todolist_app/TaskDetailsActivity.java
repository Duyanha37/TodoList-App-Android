package com.example.todolist_app;

import android.app.*;
import android.content.*;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.*;
import java.util.ArrayList;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
public class TaskDetailsActivity extends Activity {

    int taskId;
    Task task;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_task_details);

        taskId = getIntent().getIntExtra("taskId", 0);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        findViewById(R.id.btnEdit).setOnClickListener(v -> {
            Intent intent = new Intent(this, TaskFormActivity.class);
            intent.putExtra("taskId", taskId);
            startActivity(intent);
        });

        findViewById(R.id.btnComplete).setOnClickListener(v -> {
            try {
                boolean done = TaskHelper.isDone(task);
                TaskHelper.setCompleted(task, !done);
                TaskHelper.saveTask(this, task);
                showDetails();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        findViewById(R.id.btnDelete).setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Delete Task")
                    .setMessage("Delete this task?")
                    .setNegativeButton("Cancel", null)
                    .setPositiveButton("Delete", (d, w) -> {
                        TaskHelper.deleteTask(this, taskId);
                        finish();
                    }).show();
        });

        findViewById(R.id.btnComments).setOnClickListener(v ->
                showComments());
    }

    @Override
    protected void onResume() {
        super.onResume();
        task = TaskHelper.find(this, taskId);

        if (task == null) {
            finish();
        } else {
            showDetails();
        }
    }

    void showDetails() {
        ((TextView)findViewById(R.id.txtTaskTitle))
                .setText(task.getTitle());

        boolean overdue = TaskHelper.group(task).equals("Overdue");
        boolean done = TaskHelper.isDone(task);

        TextView status = findViewById(R.id.txtStatus);
        status.setText(overdue ? "Overdue" :
                done ? "Completed" : task.getStatus());
        status.setTextColor(overdue ? Color.RED : Color.BLACK);

        TextView date = findViewById(R.id.txtDateTime);
        String time = task.getTime() == null ? "" : task.getTime().format(
                DateTimeFormatter.ofPattern("h:mm a", Locale.US));
        date.setText("◷  " +
                TaskHelper.formatDate(task.getDate() == null
                        ? "" : task.getDate().toString()) +
                (time.isEmpty() ? "" : " · " + time));
        date.setTextColor(overdue ? Color.RED : 0xFF888888);

        ((TextView)findViewById(R.id.txtCategoryProject))
                .setText("●  " + firstCategory()
                        + "      ▢  " + TaskHelper.projectName(this, task));

        ((TextView)findViewById(R.id.txtDescription))
                .setText(task.getDescription() == null ||
                        task.getDescription().isEmpty()
                        ? "No description" :
                        task.getDescription());

        int count = task.getComments() == null ? 0 : task.getComments().size();

        ((TextView)findViewById(R.id.txtCommentCount))
                .setText(count + " comments");

        ((Button)findViewById(R.id.btnComplete))
                .setText(done
                        ? "Mark as Not Started"
                        : "✓  Mark as Complete");
    }

    void showComments() {
        StringBuilder text = new StringBuilder();

        ArrayList<String> comments = task.getComments();
        if (comments == null) comments = new ArrayList<>();
        for (String comment : comments) {
            text.append("• ")
                    .append(comment)
                    .append("\n\n");
        }

        EditText input = new EditText(this);
        input.setHint("Write a comment...");
        input.setSingleLine(false);

        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(30, 20, 30, 10);

        TextView history = new TextView(this);
        history.setText(text.length() == 0
                ? "No comments yet" : text.toString());
        history.setTextSize(14);
        form.addView(history);
        form.addView(input);

        new AlertDialog.Builder(this)
                .setTitle("Comments")
                .setView(form)
                .setNegativeButton("Close", null)
                .setPositiveButton("Add", (d, w) -> {
                    String comment =
                            input.getText().toString().trim();

                    if (!comment.isEmpty()) {
                        TaskHelper.addComment(this, task, comment);
                        showDetails();
                    }
                }).show();
    }

    String firstCategory() {
        java.util.List<String> categories =
                TaskHelper.categoryNames(this, task);
        return categories.isEmpty() ? "" : categories.get(0);
    }
}
