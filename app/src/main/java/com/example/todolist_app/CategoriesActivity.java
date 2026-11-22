package com.example.todolist_app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import android.database.sqlite.SQLiteDatabase;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class CategoriesActivity extends Activity {

    RecyclerView layoutCategories;
    List<Categories> categories = new ArrayList<Categories>();
    CategoryAdapter categoryAdapter;
    int userId;

    int[] colors = {
            0xFF4D95DF, 0xFFF0844C,
            0xFF55B968, 0xFFE85283,
            0xFFA357B9, 0xFFF7C641,
            0xFFED3535, 0xFF28A69B,
            0xFF7794A0, 0xFF917067
    };

    String[] icons = {
            "📚", "💼", "🏠", "🏃",
            "💰", "🎯", "🎨", "✈️",
            "🔬", "🎵", "🌱", "⚽"
    };

    int dp(int n) {
        return (int) (n * getResources()
                .getDisplayMetrics().density + 0.5f);
    }

    GradientDrawable circle(int color) {
        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.OVAL);
        bg.setColor(color);
        return bg;
    }

    GradientDrawable roundBg(int color, int radius) {
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(color);
        bg.setCornerRadius(dp(radius));
        return bg;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setStatusBarColor(Color.WHITE);
        getWindow().setNavigationBarColor(Color.BLACK);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        setContentView(R.layout.activity_categories);

        userId = getIntent().getIntExtra("userId", 0); //Get userID từ Intent
        findViewById(R.id.navProfile).setOnClickListener(v -> {
            Intent intent = new Intent(
                    CategoriesActivity.this, ProfileActivity.class);
            intent.putExtra("userId", userId);
            startActivity(intent);
        });

        findViewById(R.id.navHome).setOnClickListener(v -> {
            Intent intent = new Intent(
                    CategoriesActivity.this,
                    MainActivity.class);
            intent.putExtra("userId", userId);
            startActivity(intent);
            finish();
        });


        layoutCategories = findViewById(R.id.layoutCategories);
        categoryAdapter = new CategoryAdapter(categories,
                new CategoryAdapter.Listener() {
                    @Override
                    public void onCategoryClick(Categories category) {
                        Intent intent = new Intent(CategoriesActivity.this,
                                CategoryDetailsActivity.class);
                        intent.putExtra("categoryName", category.getName());
                        intent.putExtra("categoryIcon", category.getIcon());
                        intent.putExtra("categoryColor", category.getColor());
                        intent.putExtra("userId", userId);
                        startActivity(intent);
                    }

                    @Override
                    public void onMenuClick(View anchor, Categories category) {
                        showCategoryMenu(anchor, category);
                    }
                });
        layoutCategories.setLayoutManager(new LinearLayoutManager(this));
        layoutCategories.setAdapter(categoryAdapter);

        // Hai nút cộng
        findViewById(R.id.btnAddCategory)
                .setOnClickListener(v -> moDialog(null));

        findViewById(R.id.btnAddTopCategory)
                .setOnClickListener(v -> moDialog(null));

        findViewById(R.id.navProjects).setOnClickListener(v -> {
            Intent intent = new Intent(
                    CategoriesActivity.this,
                    ProjectsActivity.class);
            intent.putExtra("userId", userId);
            startActivity(intent);
        });

        findViewById(R.id.navCategories).setOnClickListener(v -> {
            hienThiCategories();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (layoutCategories != null) {
            docDuLieu();
            hienThiCategories();
        }
    }


    void capNhatCategoryTrongTasks(String tenCu, String tenMoi) {
        DatabaseHelper helper = new DatabaseHelper(this);
        new TaskDao(helper.getWritableDatabase())
                .updateCategory(tenCu, tenMoi);
        helper.close();
    }

    void luuDuLieu() {
        DatabaseHelper helper =
                new DatabaseHelper(this);

        try {
            CategoriesDao dao =
                    new CategoriesDao(helper.getWritableDatabase());
            for (Categories c : categories) {
                Categories category = new Categories();
                category.setId(c.getId());
                category.setUserId(userId);
                category.setName(c.getName());
                category.setIcon(c.getIcon());
                category.setColor(c.getColor());
                if (c.getId() == 0) {
                    c.setId((int) dao.insert(category));
                } else {
                    dao.update(category);
                }
            }
        } finally {
            helper.close();
        }
        // Đánh dấu đã khởi tạo Categories
        getSharedPreferences(
                "sqlite_category_setup", MODE_PRIVATE)
                .edit()
                .putBoolean("initialized_" + userId, true)
                .apply();
    }

    // Đọc dữ liệu
    void docDuLieu() {
        DatabaseHelper helper = new DatabaseHelper(this);

        try {
            categories.clear();
            CategoriesDao dao =
                    new CategoriesDao(helper.getReadableDatabase());
            for (Categories stored : dao.findByUserId(userId)) {
                String name = stored.getName();
                Categories category =
                        new Categories(name, stored.getIcon(),
                                stored.getColor());
                category.setId(stored.getId());
                categories.add(category);
            }
        } finally {
            helper.close();
        }

        // Chỉ tạo danh mục mẫu một lần khi chưa có dữ liệu
        SharedPreferences setup =
                getSharedPreferences(
                        "sqlite_category_setup", MODE_PRIVATE);

        if (!setup.getBoolean("initialized_" + userId, false)) {

            if (categories.isEmpty()) {
                taoDuLieuMau();
                luuDuLieu();
            }

            setup.edit()
                    .putBoolean("initialized_" + userId, true)
                    .apply();
        }
    }

    void taoDuLieuMau() {
        if(categories.isEmpty()){
            String[] defaultCategories = {
                    "Work", "Personal", "Shopping",
                    "Fitness", "Finance", "Travel"
            };

            for (int i = 0; i < defaultCategories.length; i++) {
                String name = defaultCategories[i];
                String icon = icons[i % icons.length];
                int color = colors[i % colors.length];

                Categories category =
                        new Categories(name, icon, color);
                categories.add(category);
            }
        }
    }


    void hienThiCategories() {
        categoryAdapter.notifyDataSetChanged();
    }

    void showCategoryMenu(View anchor, Categories category) {
        PopupMenu menu = new PopupMenu(this, anchor);
        menu.getMenu().add("Edit Category");
        menu.getMenu().add("Delete Category");
        menu.setOnMenuItemClickListener(choice -> {
            if (choice.getTitle().toString().equals("Edit Category")) {
                moDialog(category);
            } else {
                xoaCategory(category);
            }
            return true;
        });
        menu.show();
    }

    void taoTheCategory(Categories c) { // sao ku này chưa được gọi
        View item = getLayoutInflater().inflate(
                R.layout.item_category, layoutCategories, false);

        TextView txtIcon = item.findViewById(R.id.txtCategoryIcon);
        TextView txtName = item.findViewById(R.id.txtCategoryName);
        TextView txtCount = item.findViewById(R.id.txtCategoryCount);
        TextView btnMenu = item.findViewById(R.id.btnCategoryMenu);

        txtIcon.setText(c.getIcon());
        txtIcon.setBackground(circle(c.getColor()));
        txtName.setText(c.getName());
        txtCount.setText(TaskHelper.taskCountForCategory(this, c.getId()) + " tasks");


        layoutCategories.addView(item);

        item.setOnClickListener(v -> {
            Intent intent = new Intent(
                    CategoriesActivity.this,
                    CategoryDetailsActivity.class);

            intent.putExtra("categoryName", c.getName());
            intent.putExtra("categoryIcon", c.getIcon());
            intent.putExtra("categoryColor", c.getColor());

            intent.putExtra("userId", userId);
            startActivity(intent);
        });


        // Nhấn ba chấm
        btnMenu.setOnClickListener(v -> {
            PopupMenu menu = new PopupMenu(this, btnMenu);

            menu.getMenu().add("Edit Category");
            menu.getMenu().add("Delete Category");

            menu.setOnMenuItemClickListener(choice -> {
                String action = choice.getTitle().toString();

                if (action.equals("Edit Category")) {
                    moDialog(c);
                } else {
                    xoaCategory(c);
                }
                return true;
            });

            menu.show();
        });
    }

    void xoaCategory(Categories c) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Category")
                .setMessage("Delete " + c.getName() + "?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete", (d, w) -> {
                    categories.remove(c);
                    luuDuLieu();
                    hienThiCategories();

                    Toast.makeText(this,
                            "Category deleted",
                            Toast.LENGTH_SHORT).show();
                }).show();
    }

    void moDialog(Categories category) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_category);

        EditText edtName =
                dialog.findViewById(R.id.edtCategoryName);
        TextView txtTitle =
                dialog.findViewById(R.id.txtCategoryDialogTitle);
        TextView txtPreviewName =
                dialog.findViewById(R.id.txtPreviewCategory);
        TextView txtPreviewIcon =
                dialog.findViewById(R.id.txtPreviewIcon);
        TextView txtPreviewCount =
                dialog.findViewById(R.id.txtPreviewCount);

        GridLayout gridColors =
                dialog.findViewById(R.id.gridColors);
        GridLayout gridIcons =
                dialog.findViewById(R.id.gridIcons);

        Button btnSave =
                dialog.findViewById(R.id.btnSaveCategory);

        final int[] selectedColor = {colors[0]};
        final String[] selectedIcon = {icons[0]};

        // Nếu là Edit Category
        if (category != null) {
            txtTitle.setText("Edit Category");
            btnSave.setText("Save Changes");

            edtName.setText(category.getName());
            selectedColor[0] = category.getColor();
            selectedIcon[0] = category.getIcon();

            txtPreviewCount.setText(TaskHelper.taskCountForCategory(this, category.getId()) + (TaskHelper.taskCountForCategory(this, category.getId()) == 1 ? " task" : " tasks"));
        }

        // Cập nhật preview
        Runnable updatePreview = () -> {
            String name = edtName.getText().toString().trim();

            txtPreviewName.setText(
                    name.isEmpty() ? "Category name" : name);
            txtPreviewName.setTextColor(
                    name.isEmpty() ? 0xFFAAAAAA : Color.BLACK);

            txtPreviewIcon.setText(selectedIcon[0]);
            txtPreviewIcon.setBackground(
                    circle(selectedColor[0]));

            // Nút Create xám khi chưa nhập
            btnSave.setEnabled(!name.isEmpty());
            btnSave.setBackgroundTintList(
                    ColorStateList.valueOf(
                            name.isEmpty() ? 0xFFB5B5B5 : Color.BLACK));
        };

        edtName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(
                    CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(
                    CharSequence s, int start, int before, int count) {
                updatePreview.run();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // 10 màu
        Runnable drawColors = new Runnable() {
            @Override
            public void run() {
                gridColors.removeAllViews();

                int available = getResources()
                        .getDisplayMetrics().widthPixels - dp(40);
                int cell = available / 8;
                int size = Math.min(dp(36), cell - dp(4));

                for (int i = 0; i < colors.length; i++) {
                    int color = colors[i];

                    TextView swatch = new TextView(
                            CategoriesActivity.this);
                    swatch.setGravity(Gravity.CENTER);
                    swatch.setTextColor(Color.WHITE);
                    swatch.setTextSize(18);
                    swatch.setText(
                            color == selectedColor[0] ? "✓" : "");
                    swatch.setBackground(circle(color));

                    GridLayout.LayoutParams lp =
                            new GridLayout.LayoutParams();
                    lp.width = size;
                    lp.height = size;
                    lp.setMargins(dp(2), dp(2),
                            cell - size - dp(2), dp(5));
                    gridColors.addView(swatch, lp);

                    swatch.setOnClickListener(v -> {
                        selectedColor[0] = color;
                        run();
                        updatePreview.run();
                    });
                }
            }
        };

        // 12 icon
        Runnable drawIcons = new Runnable() {
            @Override
            public void run() {
                gridIcons.removeAllViews();

                int available = getResources()
                        .getDisplayMetrics().widthPixels - dp(40);
                int cell = available / 6;
                int width = cell - dp(7);

                for (String icon : icons) {
                    TextView iconView = new TextView(
                            CategoriesActivity.this);
                    iconView.setText(icon);
                    iconView.setTextSize(20);
                    iconView.setGravity(Gravity.CENTER);

                    boolean selected = icon.equals(selectedIcon[0]);

                    iconView.setBackground(roundBg(
                            selected ? Color.BLACK : 0xFFF5F5F5,
                            12));

                    GridLayout.LayoutParams lp =
                            new GridLayout.LayoutParams();
                    lp.width = width;
                    lp.height = dp(45);
                    lp.setMargins(dp(2), dp(3),
                            dp(5), dp(4));

                    gridIcons.addView(iconView, lp);

                    iconView.setOnClickListener(v -> {
                        selectedIcon[0] = icon;
                        run();
                        updatePreview.run();
                    });
                }
            }
        };

        drawColors.run();
        drawIcons.run();
        updatePreview.run();

        dialog.findViewById(R.id.btnCloseCategory)
                .setOnClickListener(v -> dialog.dismiss());

        // Nút Create / Save Changes
        btnSave.setOnClickListener(v -> {
            String name = edtName.getText().toString().trim();

            if (name.isEmpty()) {
                edtName.setError("Nhập tên category");
                return;
            }


            if (category == null) {
                categories.add(new Categories(
                        name, selectedIcon[0],
                        selectedColor[0]));

            } else {
                String tenCu = category.getName();
                category.setName(name);
                category.setIcon(selectedIcon[0]);
                category.setColor(selectedColor[0]);

                if (!tenCu.equals(name)) {
                    capNhatCategoryTrongTasks(tenCu, name);
                }
            }

            luuDuLieu();
            hienThiCategories();
            dialog.dismiss();
        });

        dialog.show();

        Window window = dialog.getWindow();

        if (window != null) {
            window.setBackgroundDrawableResource(
                    android.R.color.transparent);
            window.setLayout(-1, -2);
            window.setGravity(Gravity.BOTTOM);

            WindowManager.LayoutParams params =
                    window.getAttributes();
            params.dimAmount = 0.35f;
            window.addFlags(
                    WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            window.setAttributes(params);

            window.setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }
    }
}
