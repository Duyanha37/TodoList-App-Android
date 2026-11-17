package com.example.todolist_app;

import android.app.*;
import android.content.*;
import android.os.Bundle;
import android.text.*;
import android.view.*;
import android.widget.*;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TaskSearchActivity extends Activity {

    EditText edtSearch;
    RecyclerView layoutResults;
    LinearLayout layoutRecent;
    final List<Task> searchResults = new java.util.ArrayList<>();
    TaskSearchAdapter searchAdapter;
    TextView txtEmpty;

    String categoryFilter = "All";

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_task_search);

        edtSearch = findViewById(R.id.edtSearch);
        layoutResults = findViewById(R.id.layoutSearchResults);
        searchAdapter = new TaskSearchAdapter(searchResults,
                task -> {
                    Intent intent = new Intent(this, TaskDetailsActivity.class);
                    intent.putExtra("taskId", task.getId());
                    startActivity(intent);
                });
        layoutResults.setLayoutManager(new LinearLayoutManager(this));
        layoutResults.setAdapter(searchAdapter);
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
        searchResults.clear();

        String keyword = edtSearch.getText().toString()
                .trim().toLowerCase();

        if (keyword.isEmpty() && categoryFilter.equals("All")) {
            txtEmpty.setVisibility(View.VISIBLE);
            searchAdapter.notifyDataSetChanged();
            return;
        }

        List<Task> tasks = TaskHelper.getTasks(this);
        int count = 0;

        for (Task task : tasks) {

            String name = task.getTitle().toLowerCase();
            String description = task.getDescription() == null
                    ? "" : task.getDescription().toLowerCase();

            boolean matchesText = keyword.isEmpty() ||
                    name.contains(keyword) ||
                    description.contains(keyword);

            boolean matchesCategory = categoryFilter.equals("All") ||
                    TaskHelper.belongsToCategory(this, task, categoryFilter);

            if (matchesText && matchesCategory) {
                searchResults.add(task);
                count++;
            }
        }

        txtEmpty.setVisibility(count == 0
                ? View.VISIBLE : View.GONE);

        txtEmpty.setText(count == 0
                ? "No matching tasks found"
                : "");
        searchAdapter.notifyDataSetChanged();
    }
}
