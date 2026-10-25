package com.example.todolist_app;

import android.app.*;
import android.content.*;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.*;
import org.json.JSONArray;
import org.json.JSONObject;

public class TaskDetailsActivity extends Activity {

    String taskId;
    JSONObject task;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_task_details);

        taskId = getIntent().getStringExtra("taskId");

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        findViewById(R.id.btnEdit).setOnClickListener(v -> {
            Intent intent = new Intent(this, TaskFormActivity.class);
            intent.putExtra("taskId", taskId);
            startActivity(intent);
        });

        findViewById(R.id.btnComplete).setOnClickListener(v -> {
            try {
                boolean done = TaskStore.isDone(task);
                task.put("status",
                        done ? "Not Started" : "Completed");
                TaskStore.saveTask(this, task);
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
                        TaskStore.deleteTask(this, taskId);
                        finish();
                    }).show();
        });

        findViewById(R.id.btnComments).setOnClickListener(v ->
                showComments());
    }

    @Override
    protected void onResume() {
        super.onResume();
        task = TaskStore.find(this, taskId);

        if (task == null) {
            finish();
        } else {
            showDetails();
        }
    }

    void showDetails() {
        ((TextView)findViewById(R.id.txtTaskTitle))
                .setText(task.optString("name"));

        boolean overdue = TaskStore.group(task).equals("Overdue");
        boolean done = TaskStore.isDone(task);

        TextView status = findViewById(R.id.txtStatus);
        status.setText(overdue ? "Overdue" :
                done ? "Completed" : task.optString("status"));
        status.setTextColor(overdue ? Color.RED : Color.BLACK);

        TextView date = findViewById(R.id.txtDateTime);
        date.setText("◷  " +
                TaskStore.formatDate(task.optString("date")) +
                " · " + task.optString("time"));
        date.setTextColor(overdue ? Color.RED : 0xFF888888);

        ((TextView)findViewById(R.id.txtCategoryProject))
                .setText("●  " + task.optString("category")
                        + "      ▢  " + task.optString("project"));

        ((TextView)findViewById(R.id.txtDescription))
                .setText(task.optString("description").isEmpty()
                        ? "No description" :
                        task.optString("description"));

        JSONArray comments = task.optJSONArray("comments");
        int count = comments == null ? 0 : comments.length();

        ((TextView)findViewById(R.id.txtCommentCount))
                .setText(count + " comments");

        ((Button)findViewById(R.id.btnComplete))
                .setText(done
                        ? "Mark as Not Started"
                        : "✓  Mark as Complete");
    }

    void showComments() {
        JSONArray comments = task.optJSONArray("comments");

        if (comments == null) {
            comments = new JSONArray();
        }

        final JSONArray savedComments = comments;
        StringBuilder text = new StringBuilder();

        for (int i = 0; i < comments.length(); i++) {
            text.append("• ")
                    .append(comments.optString(i))
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
                        savedComments.put(comment);

                        try {
                            task.put("comments", savedComments);
                            TaskStore.saveTask(this, task);
                            showDetails();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).show();
    }
}
