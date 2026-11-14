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
        ProjectsActivity.ProjectData project = projects.get(position);
        holder.name.setText(project.name);
        holder.count.setText(project.getDone() + "/" + project.tasks.size()
                + " tasks completed");
        holder.deadline.setText(project.deadline.isEmpty() ? "" : "Due " + project.deadline);
        GradientDrawable dot = new GradientDrawable();
        dot.setShape(GradientDrawable.OVAL);
        dot.setColor(project.color);
        holder.color.setBackground(dot);
        holder.progress.setProgress(project.getPercent());
        holder.itemView.setOnClickListener(v -> listener.onProjectClick(project));
        holder.menu.setOnClickListener(v -> listener.onMenuClick(holder.menu, project));
    }

    @Override
    public int getItemCount() {
        return projects.size();
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
