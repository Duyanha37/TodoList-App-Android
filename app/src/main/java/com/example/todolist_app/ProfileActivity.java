package com.example.todolist_app;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ProfileActivity extends Activity {

    SharedPreferences pref;
    TextView txtName, txtEmail;
    TextView txtNotificationValue, txtThemeValue;
    int userId;

    int dp(int n) {
        return (int) (n * getResources()
                .getDisplayMetrics().density + 0.5f);
    }

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_profile);
        userId = getIntent().getIntExtra("userId", 0);

        pref = getSharedPreferences("profile_data_" + userId, MODE_PRIVATE);

        txtName = findViewById(R.id.txtProfileName);
        txtEmail = findViewById(R.id.txtProfileEmail);

        // Thiết lập 5 dòng cài đặt
        setupRow(R.id.rowEditProfile, "✎",
                "Edit Profile", "");

        setupRow(R.id.rowNotifications, "♧",
                "Notifications", "");

        setupRow(R.id.rowTheme, "●",
                "App Theme", "");

        setupRow(R.id.rowHelp, "ⓘ",
                "Help & Support", "");

        setupRow(R.id.rowAbout, "ⓘ",
                "About", "");

        View notifications =
                findViewById(R.id.rowNotifications);
        View theme = findViewById(R.id.rowTheme);

        txtNotificationValue =
                notifications.findViewById(R.id.txtRowValue);
        txtThemeValue =
                theme.findViewById(R.id.txtRowValue);

        showProfile();
        showSettings();

        // Edit Profile
        findViewById(R.id.rowEditProfile)
                .setOnClickListener(v -> editProfile());

        // Notifications
        findViewById(R.id.rowNotifications)
                .setOnClickListener(v -> changeNotifications());

        // App Theme
        findViewById(R.id.rowTheme)
                .setOnClickListener(v -> changeTheme());

        // Help
        findViewById(R.id.rowHelp)
                .setOnClickListener(v ->
                        new AlertDialog.Builder(this)
                                .setTitle("Hỗ trợ")
                                .setMessage(
                                        "Sử dụng Home để quản lý công việc.\n\n" +
                                                "Sử dụng Projects để quản lý dự án.\n\n" +
                                                "Sử dụng Categories để quản lý danh mục.")
                                .setPositiveButton("OK", null)
                                .show());

        // About
        findViewById(R.id.rowAbout)
                .setOnClickListener(v ->
                        new AlertDialog.Builder(this)
                                .setTitle("Về ứng dụng")
                                .setMessage(
                                        "TodoList\nAndroid Task Manager")
                                .setPositiveButton("OK", null)
                                .show());

        // Log Out: chưa có hệ thống phiên đăng nhập
        findViewById(R.id.btnLogout)
                .setOnClickListener(v ->
                        new AlertDialog.Builder(this)
                                .setTitle("Log Out")
                                .setMessage(
                                        "Chức năng đăng xuất sẽ được kết nối " +
                                                "khi nhóm hoàn thiện đăng nhập.")
                                .setPositiveButton("OK", null)
                                .show());

        // Bottom Navigation
        findViewById(R.id.navHome)
                .setOnClickListener(v ->
                        open(MainActivity.class));

        findViewById(R.id.navProjects)
                .setOnClickListener(v ->
                        open(ProjectsActivity.class));

        findViewById(R.id.navCategories)
                .setOnClickListener(v ->
                        open(CategoriesActivity.class));
    }

    void setupRow(int id, String icon,
                  String title, String value) {

        View row = findViewById(id);

        TextView txtIcon =
                row.findViewById(R.id.txtRowIcon);
        TextView txtTitle =
                row.findViewById(R.id.txtRowTitle);
        TextView txtValue =
                row.findViewById(R.id.txtRowValue);

        txtIcon.setText(icon);
        txtTitle.setText(title);
        txtValue.setText(value);
    }

    void open(Class<?> activity) {
        Intent intent = new Intent(this, activity);
        intent.putExtra("userId", userId);
        startActivity(intent);
        finish();
    }

    void showProfile() {
        DatabaseHelper helper = new DatabaseHelper(this);
        try {
            User user = new UserDao(helper.getReadableDatabase())
                    .findById(String.valueOf(userId));
            if (user != null) {
                txtName.setText(user.getFullname());
                txtEmail.setText(user.getEmail());
            } else {
                txtName.setText(pref.getString("name", ""));
                txtEmail.setText(pref.getString("email", ""));
            }
        } finally {
            helper.close();
        }
    }

    void showSettings() {
        boolean enabled = pref.getBoolean(
                "notifications", false);

        txtNotificationValue.setText(
                enabled ? "On" : "Off");

        txtThemeValue.setText(pref.getString(
                "theme", "Light"));
    }

    // Sửa tên và email
    void editProfile() {

        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(dp(20), dp(8),
                dp(20), dp(8));

        EditText edtName = new EditText(this);
        edtName.setHint("Full name");
        edtName.setSingleLine(true);
        edtName.setText(txtName.getText());

        EditText edtEmail = new EditText(this);
        edtEmail.setHint("Email");
        edtEmail.setSingleLine(true);
        edtEmail.setInputType(
                android.text.InputType.TYPE_CLASS_TEXT |
                        android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        edtEmail.setText(txtEmail.getText());

        form.addView(edtName);
        form.addView(edtEmail);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Edit Profile")
                .setView(form)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Save", null)
                .create();

        dialog.setOnShowListener(d -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                    .setOnClickListener(v -> {

                        String name = edtName.getText()
                                .toString().trim();

                        String email = edtEmail.getText()
                                .toString().trim();

                        if (name.isEmpty()) {
                            edtName.setError("Enter your name");
                            return;
                        }

                        if (!android.util.Patterns.EMAIL_ADDRESS
                                .matcher(email).matches()) {
                            edtEmail.setError("Invalid email");
                            return;
                        }

                        DatabaseHelper helper = new DatabaseHelper(this);
                        try {
                            User user = new UserDao(helper.getWritableDatabase())
                                    .findById(String.valueOf(userId));
                            if (user != null) {
                                user.setFullname(name);
                                user.setEmail(email);
                                new UserDao(helper.getWritableDatabase()).update(user);
                            }
                        } finally {
                            helper.close();
                        }

                        showProfile();
                        dialog.dismiss();
                    });
        });

        dialog.show();
    }

    // Bật/tắt cài đặt thông báo
    void changeNotifications() {
        boolean enabled = pref.getBoolean(
                "notifications", false);

        new AlertDialog.Builder(this)
                .setTitle("Notifications")
                .setMessage(enabled
                        ? "Disable task notifications?"
                        : "Enable task notifications?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton(
                        enabled ? "Disable" : "Enable",
                        (d, w) -> {

                            if (enabled) {
                                NotificationScheduler.cancelAll(this, userId);
                                pref.edit()
                                        .putBoolean("notifications", false)
                                        .apply();
                                showSettings();
                            } else {
                                requestNotificationPermission();
                            }
                        })
                .show();
    }

    void requestNotificationPermission() {

        if (Build.VERSION.SDK_INT >= 33 &&
                checkSelfPermission(
                        Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{
                            Manifest.permission.POST_NOTIFICATIONS
                    },
                    100);

        } else {
            pref.edit()
                    .putBoolean("notifications", true)
                    .apply();

            NotificationScheduler.rescheduleAll(this, userId);
            showSettings();
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            String[] permissions,
            int[] grantResults) {

        super.onRequestPermissionsResult(
                requestCode, permissions, grantResults);

        if (requestCode == 100) {

            boolean granted =
                    grantResults.length > 0 &&
                            grantResults[0] ==
                                    PackageManager.PERMISSION_GRANTED;

            pref.edit()
                    .putBoolean("notifications", granted)
                    .apply();

            if (granted) {
                NotificationScheduler.rescheduleAll(this, userId);
            }
            showSettings();

            if (!granted) {
                Toast.makeText(this,
                        "Notification permission was denied",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Lưu lựa chọn theme, chưa áp dụng toàn app
    void changeTheme() {

        String[] themes = {"Light", "Dark"};

        String current = pref.getString(
                "theme", "Light");

        int selected = current.equals("Dark") ? 1 : 0;

        new AlertDialog.Builder(this)
                .setTitle("App Theme")
                .setSingleChoiceItems(
                        themes, selected,
                        (dialog, which) -> {

                            pref.edit()
                                    .putString("theme", themes[which])
                                    .apply();

                            showSettings();
                            dialog.dismiss();

                            Toast.makeText(this,
                                    "Theme preference saved",
                                    Toast.LENGTH_SHORT).show();
                        })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
