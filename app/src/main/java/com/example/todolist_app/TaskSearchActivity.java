package com.example.todolist_app;

import android.app.*;
import android.content.*;
import android.os.Bundle;
import android.text.*;
import android.view.*;
import android.widget.*;

import org.json.JSONArray;
import org.json.JSONObject;

public class TaskSearchActivity extends Activity {

    EditText edtSearch;
    LinearLayout layoutResults, layoutRecent;
    TextView txtEmpty;

    String categoryFilter = "All";

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_task_search);

        edtSearch = findViewById(R.id.edtSearch);
        layoutResults = findViewById(R.id.layoutSearchResults);
        layoutRecent = findViewById(R.id.layoutRecent);
        txtEmpty = findViewById(R.id.txtEmpty);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        String[] recent = {"Design system", "API docs", "Budget"};

        for (String word : recent) {
            TextView chip = new TextView(this);
            chip.setText("◷  " + word + "  ×");
            chip.setTextSize(12);
            chip.setPadding(12, 10, 12, 10);
            chip.setBackgroundResource(R.drawable.bg_outline);

            LinearLayout.LayoutParams lp =
                    new LinearLayout.LayoutParams(-2, -2);
            lp.rightMargin = 8;
            layoutRecent.addView(chip, lp);

            chip.setOnClickListener(v -> edtSearch.setText(word));
        }

        edtSearch.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(
                    CharSequence s, int start, int count, int after) {}

            public void onTextChanged(
                    CharSequence s, int start, int before, int count) {
                search();
            }

            public void afterTextChanged(Editable s) {}
        });

        findViewById(R.id.btnSearchFilter).setOnClickListener(v -> {
            String[] categories = {
                    "All", "Work", "Personal", "Health",
                    "Finance", "Learning"
            };

            new AlertDialog.Builder(this)
                    .setTitle("Filter by Category")
                    .setItems(categories, (d, which) -> {
                        categoryFilter = categories[which];
                        search();
                    }).show();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (layoutResults != null) search();
    }

    void search() {
        layoutResults.removeAllViews();

        String keyword = edtSearch.getText().toString()
                .trim().toLowerCase();

        if (keyword.isEmpty() && categoryFilter.equals("All")) {
            txtEmpty.setVisibility(View.VISIBLE);
            return;
        }

        JSONArray tasks = TaskStore.getTasks(this);
        int count = 0;

        for (int i = 0; i < tasks.length(); i++) {
            JSONObject task = tasks.optJSONObject(i);
            if (task == null) continue;

            String name = task.optString("name").toLowerCase();
            String description =
                    task.optString("description").toLowerCase();

            boolean matchesText = keyword.isEmpty() ||
                    name.contains(keyword) ||
                    description.contains(keyword);

            boolean matchesCategory = categoryFilter.equals("All") ||
                    task.optString("category").equals(categoryFilter);

            if (matchesText && matchesCategory) {
                TextView result = new TextView(this);
                result.setText(task.optString("name")
                        + "\n" + task.optString("time")
                        + " · " + task.optString("project"));
                result.setTextSize(14);
                result.setTextColor(0xFF111111);
                result.setPadding(18, 18, 18, 18);
                result.setBackgroundResource(R.drawable.bg_card);

                LinearLayout.LayoutParams lp =
                        new LinearLayout.LayoutParams(-1, -2);
                lp.bottomMargin = 10;
                layoutResults.addView(result, lp);

                String id = task.optString("id");

                result.setOnClickListener(v -> {
                    Intent intent = new Intent(
                            this, TaskDetailsActivity.class);
                    intent.putExtra("taskId", id);
                    startActivity(intent);
                });

                count++;
            }
        }

        txtEmpty.setVisibility(count == 0
                ? View.VISIBLE : View.GONE);

        txtEmpty.setText(count == 0
                ? "No matching tasks found"
                : "");
    }
}
