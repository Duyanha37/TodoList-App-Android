package com.example.todolist_app;

import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {
    public interface Listener {
        void onCategoryClick(Categories category);
        void onMenuClick(View anchor, Categories category);
    }

    private final List<Categories> categories;
    private final Listener listener;

    public CategoryAdapter(List<Categories> categories,
                           Listener listener) {
        this.categories = categories;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Categories category = categories.get(position);
        holder.icon.setText(category.getIcon());
        GradientDrawable background = new GradientDrawable();
        background.setShape(GradientDrawable.OVAL);
        background.setColor(category.getColor());
        holder.icon.setBackground(background);
        holder.name.setText(category.getName());
        holder.count.setText(String.valueOf(TaskHelper.taskCountForCategory(holder.itemView.getContext(), category.getId())));
        holder.itemView.setOnClickListener(v -> listener.onCategoryClick(category));
        holder.menu.setOnClickListener(v -> listener.onMenuClick(holder.menu, category));
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView icon, name, count, menu;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.txtCategoryIcon);
            name = itemView.findViewById(R.id.txtCategoryName);
            count = itemView.findViewById(R.id.txtCategoryCount);
            menu = itemView.findViewById(R.id.btnCategoryMenu);
        }
    }
}
