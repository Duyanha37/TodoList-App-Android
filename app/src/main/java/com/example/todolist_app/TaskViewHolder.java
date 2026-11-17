package com.example.todolist_app;

import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class TaskViewHolder extends RecyclerView.ViewHolder {
    final CheckBox check;
    final TextView name;
    final TextView info;
    final TextView category;
    final TextView menu;
    final View content;

    public TaskViewHolder(@NonNull View itemView) {
        super(itemView);
        check = itemView.findViewById(R.id.checkTask);
        name = itemView.findViewById(R.id.txtTaskName);
        TextView taskInfo = itemView.findViewById(R.id.txtTaskInfo);
        info = taskInfo != null ? taskInfo : itemView.findViewById(R.id.txtTime);
        category = itemView.findViewById(R.id.txtCategory);
        menu = itemView.findViewById(R.id.btnTaskMenu);
        View taskContent = itemView.findViewById(R.id.layoutTaskContent);
        content = taskContent != null ? taskContent : itemView;
    }
}
