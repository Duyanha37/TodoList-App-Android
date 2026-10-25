package com.example.todolist_app;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        RecyclerView taskRecyclerView = findViewById(R.id.taskRecyclerView);
        taskRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        taskRecyclerView.setAdapter(new HomeListAdapter(createSampleItems()));

        setupSummaryCards();
        setupBottomNavigation();
        findViewById(R.id.btnSearch).setOnClickListener(v ->
                Toast.makeText(this, "Search sẽ được bổ sung.", Toast.LENGTH_SHORT).show());
        findViewById(R.id.btnFilter).setOnClickListener(v ->
                Toast.makeText(this, "Filter sẽ được bổ sung.", Toast.LENGTH_SHORT).show());
        findViewById(R.id.btnAddTask).setOnClickListener(v ->
                Toast.makeText(this, "Màn hình tạo task sẽ được bổ sung.", Toast.LENGTH_SHORT).show());
    }

    /**
     * Sample data only. Later replace this method with repository/DB data.
     * RecyclerView is appropriate here because it creates only visible rows,
     * reuses row views and supports a long task list efficiently.
     */
    private List<HomeListItem> createSampleItems() {
        List<HomeListItem> items = new ArrayList<>();
        items.add(HomeListItem.section("Overdue"));
        items.add(HomeListItem.task(new HomeTask(
                "Overdue", "Fix login page bug", "5:00 PM", "Website Redesign",
                "Work", getColor(R.color.category_work), false)));
        items.add(HomeListItem.task(new HomeTask(
                "Overdue", "Pay electricity bill", "11:59 PM", "Q4 Product Launch",
                "Finance", getColor(R.color.category_finance), false)));

        items.add(HomeListItem.section("Today"));
        items.add(HomeListItem.task(new HomeTask(
                "Today", "Review design mockups", "10:00 AM", "Website Redesign",
                "Work", getColor(R.color.category_work), false)));
        items.add(HomeListItem.task(new HomeTask(
                "Today", "Team standup meeting", "9:00 AM", "Q4 Product Launch",
                "Work", getColor(R.color.category_work), true)));
        items.add(HomeListItem.task(new HomeTask(
                "Today", "Morning run – 5km", "7:00 AM", "Fitness Goal",
                "Health", getColor(R.color.category_health), false)));

        items.add(HomeListItem.section("Upcoming"));
        items.add(HomeListItem.task(new HomeTask(
                "Upcoming", "Prepare weekly report", "Tomorrow", "Website Redesign",
                "Work", getColor(R.color.category_work), false)));
        return items;
    }

    private void setupSummaryCards() {
        LinearLayout content = findViewById(R.id.homeContent);
        int[] counts = {2, 5, 2};
        String[] labels = {"Today", "Upcoming", "Overdue"};
        for (int i = 0; i < 3; i++) {
            View card = content.findViewWithTag(labels[i].toLowerCase());
            if (card == null) {
                continue;
            }
            ((TextView) card.findViewById(R.id.summaryLabel)).setText(labels[i]);
            TextView count = card.findViewById(R.id.summaryCount);
            count.setText(String.valueOf(counts[i]));
            if (i == 2) {
                count.setTextColor(getColor(android.R.color.holo_red_dark));
            }
        }
    }

    private void setupBottomNavigation() {
        LinearLayout navigation = findViewById(R.id.bottomNavigation);
        int[] icons = {R.drawable.ic_home, R.drawable.ic_folder,
                R.drawable.ic_tag, R.drawable.ic_profile};
        String[] labels = {"Home", "Projects", "Categories", "Profile"};

        for (int i = 0; i < navigation.getChildCount(); i++) {
            View item = navigation.getChildAt(i);
            ImageView icon = item.findViewById(R.id.navIcon);
            TextView label = item.findViewById(R.id.navLabel);
            icon.setImageResource(icons[i]);
            label.setText(labels[i]);
            if (i == 0) {
                icon.setColorFilter(getColor(R.color.primary_black));
                label.setTextColor(getColor(R.color.text_primary));
            }
            final int selectedTab = i;
            item.setOnClickListener(v -> Toast.makeText(this,
                    labels[selectedTab] + " sẽ được bổ sung.", Toast.LENGTH_SHORT).show());
        }
    }
}
