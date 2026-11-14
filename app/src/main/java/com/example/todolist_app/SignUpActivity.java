package com.example.todolist_app;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class SignUpActivity extends AppCompatActivity {
    private EditText edtFullName;
    private EditText edtEmail;
    private EditText edtPassword;
    private EditText edtConfirmPassword;
    private TextView tvNameError;
    private TextView tvEmailError;
    private TextView tvPasswordError;
    private TextView tvConfirmPasswordError;
    private boolean passwordVisible;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signup);

        edtFullName = findViewById(R.id.edtFullName);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);
        tvNameError = findViewById(R.id.tvNameError);
        tvEmailError = findViewById(R.id.tvEmailError);
        tvPasswordError = findViewById(R.id.tvPasswordError);
        tvConfirmPasswordError = findViewById(R.id.tvConfirmPasswordError);

        ImageView btnBack = findViewById(R.id.btnBack);
        ImageView ivPasswordToggle = findViewById(R.id.ivPasswordToggle);
        Button btnSignUp = findViewById(R.id.btnSignUp);
        Button btnLogin = findViewById(R.id.btnLogin);

        btnBack.setOnClickListener(v -> finish());
        btnLogin.setOnClickListener(v -> finish());
        ivPasswordToggle.setOnClickListener(v -> togglePasswordVisibility(ivPasswordToggle));
        btnSignUp.setOnClickListener(v -> SignUp());
    }

    private void togglePasswordVisibility(ImageView toggleView) {
        passwordVisible = !passwordVisible;
        int inputType = passwordVisible
                ? InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                : InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD;
        edtPassword.setInputType(inputType);
        edtPassword.setSelection(edtPassword.length());
        toggleView.setImageResource(
                passwordVisible ? R.drawable.ic_visibility : R.drawable.ic_visibility_off);
        toggleView.setContentDescription(
                passwordVisible ? "Hiện mật khẩu" : "Ẩn mật khẩu");
    }

    private void SignUp() {
        String fullName = edtFullName.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString();
        String confirmPassword = edtConfirmPassword.getText().toString();
        String regexFullname = "^[a-zA-ZÀ-ỹ\\s]+$";
        String regexEmail = "^[A-Za-z0-9]+@[A-Za-z0-9]+\\.[A-Za-z0-9]+$";
        String regexPassword = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$";
        boolean valid = true;

        hideErrors();

        if (fullName.trim().isEmpty()) {
            showError(tvNameError, "Mời nhập tên của bạn.");
            valid = false;
        } else if (!fullName.matches(regexFullname)) {
            showError(tvNameError, "Tên chỉ được chứa chữ cái và khoảng trắng.");
            valid = false;
        }

        if (email.trim().isEmpty()) {
            showError(tvEmailError, "Mời nhập email của bạn.");
            valid = false;
        } else if (!email.matches(regexEmail)) {
            showError(tvEmailError, "Email không hợp lệ.");
            valid = false;
        }

        if (password.trim().isEmpty()) {
            showError(tvPasswordError, "Mời nhập mật khẩu.");
            valid = false;
        } else if (!password.matches(regexPassword)) {
            showError(tvPasswordError, "Mật khẩu phải chứa ít nhất 8 ký tự, bao gồm cả chữ và số.");
            valid = false;
        }

        if (confirmPassword.trim().isEmpty()) {
            showError(tvConfirmPasswordError, "Mời nhập lại mật khẩu.");
            valid = false;
        } else if (!password.equals(confirmPassword)) {
            showError(tvConfirmPasswordError, "Mật khẩu xác nhận không khớp.");
            valid = false;
        }

        if (!valid) {
            return;
        }

        SQLiteDatabase db = new DatabaseHelper(this).getWritableDatabase();
        UserDao userDao = new UserDao(db);
        if(userDao.findByEmail(email.toLowerCase()) != null) {
            showError(tvEmailError, "Email đã được sử dụng.");
            db.close();
            return;
        } else {
            User user = new User();
            user.setFullname(fullName);
            user.setEmail(email.toLowerCase());
            user.setPassword(password);
            userDao.insert(user);
            db.close();
        }
        Toast.makeText(this, "Đăng ký thành công!.", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void hideErrors() {
        tvNameError.setVisibility(TextView.GONE);
        tvEmailError.setVisibility(TextView.GONE);
        tvPasswordError.setVisibility(TextView.GONE);
        tvConfirmPasswordError.setVisibility(TextView.GONE);
    }

    private void showError(TextView errorView, String message) {
        errorView.setText(message);
        errorView.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
        errorView.setVisibility(TextView.VISIBLE);
    }
}
