package com.example.todolist_app;

import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ProjectAdapter extends RecyclerView.Adapter<ProjectAdapter.ViewHolder> {
    public interface Listener {
        void onProjectClick(ProjectsActivity.ProjectData project);
        void onMenuClick(View anchor, ProjectsActivity.ProjectData project);
    }

    private final List<ProjectsActivity.ProjectData> projects;
    private final Listener listener;

    public ProjectAdapter(List<ProjectsActivity.ProjectData> projects, Listener listener) {
        this.projects = projects;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_project, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ProjectsActivity.ProjectData project = visibleProjects().get(position);
        holder.name.setText(project.name);
        int total = 0;
        int completed = 0;
        for (Task task : TaskHelper.getTasks(holder.itemView.getContext())) {
            if (project.name.equals(TaskHelper.projectName(
                    holder.itemView.getContext(), task))) {
                total++;
                if (TaskHelper.isDone(task)) {
                    completed++;
                }
            }
        }
        holder.count.setText(completed + "/" + total + " tasks completed");
        holder.deadline.setText(project.deadline.isEmpty() ? "" : "Due " + project.deadline);
        GradientDrawable dot = new GradientDrawable();
        dot.setShape(GradientDrawable.OVAL);
        dot.setColor(project.color);
        holder.color.setBackground(dot);
        holder.progress.setProgress(
                total == 0 ? 0 : completed * 100 / total);
        holder.itemView.setOnClickListener(v -> listener.onProjectClick(project));
        holder.menu.setOnClickListener(v -> listener.onMenuClick(holder.menu, project));
    }

    @Override
    public int getItemCount() {
        return visibleProjects().size();
    }

    private List<ProjectsActivity.ProjectData> visibleProjects() {
        List<ProjectsActivity.ProjectData> visible = new java.util.ArrayList<>();
        for (ProjectsActivity.ProjectData project : projects) {
            if (!project.archived) visible.add(project);
        }
        return visible;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView name, count, deadline, menu;
        final View color;
        final ProgressBar progress;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.txtProjectName);
            count = itemView.findViewById(R.id.txtCount);
            deadline = itemView.findViewById(R.id.txtDeadline);
            menu = itemView.findViewById(R.id.btnMenu);
            color = itemView.findViewById(R.id.projectColor);
            progress = itemView.findViewById(R.id.progressProject);
        }
    }
}
