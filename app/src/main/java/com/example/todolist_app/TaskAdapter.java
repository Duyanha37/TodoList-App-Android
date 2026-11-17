package com.example.todolist_app;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskViewHolder> {
    public interface Listener {
        void onTaskClick(Task task);
        void onTaskChecked(Task task, boolean checked);
        void onMenuClick(View anchor, Task task);
    }

    private final List<Task> tasks;
    private final Listener listener;
    private final int itemLayout;

    public TaskAdapter(List<Task> tasks, Listener listener) {
        this(tasks, listener, R.layout.item_task_home);
    }

    public TaskAdapter(List<Task> tasks, Listener listener, int itemLayout) {
        this.tasks = tasks;
        this.listener = listener;
        this.itemLayout = itemLayout;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                             int viewType) {
        View item = LayoutInflater.from(parent.getContext())
                .inflate(itemLayout, parent, false);
        return new TaskViewHolder(item);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder,
                                 int position) {
        Task task = tasks.get(position);
        boolean done = TaskHelper.isDone(task);
        String date = task.getDate() == null
                ? "" : TaskHelper.formatDate(task.getDate().toString());
        String project = TaskHelper.projectName(holder.itemView.getContext(), task);
        List<String> categories = TaskHelper.categoryNames(
                holder.itemView.getContext(), task);
        String category = categories.isEmpty() ? "" : categories.get(0);

        holder.name.setText(task.getTitle());
        holder.info.setText(date + " · " + project);
        holder.category.setText("●  " + category);
        holder.category.setTextColor(TaskHelper.categoryColor(category));

        holder.name.setPaintFlags(done
                ? holder.name.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG
                : holder.name.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
        holder.name.setTextColor(done ? 0xFF999999 : 0xFF111111);

        holder.check.setOnCheckedChangeListener(null);
        holder.check.setChecked(done);
        holder.check.setOnCheckedChangeListener((button, checked) ->
                listener.onTaskChecked(task, checked));

        holder.content.setOnClickListener(v -> listener.onTaskClick(task));
        holder.menu.setOnClickListener(v ->
                listener.onMenuClick(holder.menu, task));
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }
}
