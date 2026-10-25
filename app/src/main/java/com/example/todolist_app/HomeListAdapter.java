package com.example.todolist_app;

import android.graphics.drawable.GradientDrawable;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * RecyclerView adapter for Home.
 *
 * getItemViewType() lets one list render two row types:
 * 1) a section heading (Overdue, Today, Upcoming)
 * 2) a task card below that heading.
 *
 * Later, replace the sample list in MainActivity with DB results and keep this
 * adapter unchanged, as long as each task is converted to HomeListItem.task().
 */
public class HomeListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final List<HomeListItem> items;

    public HomeListAdapter(List<HomeListItem> items) {
        this.items = items;
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).getType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == HomeListItem.SECTION) {
            return new SectionViewHolder(inflater.inflate(
                    R.layout.item_task_section, parent, false));
        }
        return new TaskViewHolder(inflater.inflate(R.layout.item_task, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        HomeListItem item = items.get(position);
        if (holder instanceof SectionViewHolder) {
            ((SectionViewHolder) holder).title.setText(item.getSectionTitle());
        } else {
            ((TaskViewHolder) holder).bind(item.getTask());
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class SectionViewHolder extends RecyclerView.ViewHolder {
        final TextView title;
        SectionViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.sectionTitle);
        }
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        final CheckBox checkBox;
        final TextView title;
        final TextView meta;
        final TextView category;
        final View dot;

        TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.taskCheckBox);
            title = itemView.findViewById(R.id.taskTitle);
            meta = itemView.findViewById(R.id.taskMeta);
            category = itemView.findViewById(R.id.taskCategory);
            dot = itemView.findViewById(R.id.categoryDot);
        }

        void bind(HomeTask task) {
            title.setText(task.getTitle());
            meta.setText(task.getTime() + " · " + task.getProject());
            category.setText(task.getCategory());
            checkBox.setOnCheckedChangeListener(null);
            checkBox.setChecked(task.isCompleted());
            updateCompletedStyle(task.isCompleted());

            checkBox.setOnCheckedChangeListener((button, checked) -> {
                task.setCompleted(checked);
                updateCompletedStyle(checked);
            });

            GradientDrawable dotBackground = new GradientDrawable();
            dotBackground.setShape(GradientDrawable.OVAL);
            dotBackground.setColor(task.getCategoryColor());
            dot.setBackground(dotBackground);
        }

        private void updateCompletedStyle(boolean completed) {
            title.setPaintFlags(completed
                    ? title.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG
                    : title.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
            title.setTextColor(completed
                    ? itemView.getContext().getColor(R.color.text_secondary)
                    : itemView.getContext().getColor(R.color.text_primary));
        }
    }
}
