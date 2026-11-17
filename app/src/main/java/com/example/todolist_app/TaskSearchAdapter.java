package com.example.todolist_app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TaskSearchAdapter extends RecyclerView.Adapter<TaskSearchAdapter.ViewHolder> {
    public interface Listener {
        void onTaskClick(Task task);
    }

    private final List<Task> tasks;
    private final Listener listener;

    public TaskSearchAdapter(List<Task> tasks, Listener listener) {
        this.tasks = tasks;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task_search, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Task task = tasks.get(position);
        String date = task.getDate() == null ? "" :
                TaskHelper.formatDate(task.getDate().toString());
        holder.title.setText(task.getTitle());
        holder.info.setText(date + " · " +
                TaskHelper.projectName(holder.itemView.getContext(), task));
        holder.itemView.setOnClickListener(v -> listener.onTaskClick(task));
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView title;
        final TextView info;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.txtSearchTaskTitle);
            info = itemView.findViewById(R.id.txtSearchTaskInfo);
        }
    }
}
