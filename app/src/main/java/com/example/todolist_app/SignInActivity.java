package com.example.todolist_app;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class SignInActivity extends AppCompatActivity {
    private EditText edtEmail;
    private EditText edtPassword;
    private TextView tvEmailError;
    private TextView tvPasswordError;
    private ImageView ivPasswordToggle;
    private boolean passwordVisible;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signin);

        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        tvEmailError = findViewById(R.id.tvEmailError);
        tvPasswordError = findViewById(R.id.tvPasswordError);
        ivPasswordToggle = findViewById(R.id.ivPasswordToggle);
        Button btnLogin = findViewById(R.id.btnLogin);
        Button btnSignUp = findViewById(R.id.btnSignUp);
        ImageView btnBack = findViewById(R.id.btnBack);
        TextView tvForgotPassword = findViewById(R.id.tvForgotPassword);

        btnBack.setOnClickListener(v -> finish());
        btnSignUp.setOnClickListener(v -> startActivity(new Intent(this, SignUpActivity.class)));
        ivPasswordToggle.setOnClickListener(v -> togglePasswordVisibility());
        btnLogin.setOnClickListener(v -> Login());
        edtEmail.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                tvEmailError.setVisibility(TextView.GONE);
            }
        });
        edtPassword.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                tvPasswordError.setVisibility(TextView.GONE);
            }
        });
    }

    private void togglePasswordVisibility() {
        passwordVisible = !passwordVisible;
        int inputType = passwordVisible
                ? InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                : InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD; //Vì inputtype phải nhận nguyên 1 inputtype đầy đủ chứa class và dạng
        edtPassword.setInputType(inputType);
        edtPassword.setSelection(edtPassword.length());
        ivPasswordToggle.setImageResource(
                passwordVisible ? R.drawable.ic_visibility : R.drawable.ic_visibility_off);
        ivPasswordToggle.setContentDescription(
                passwordVisible ? "Hide password" : "Show password");
    }

    private void Login() {
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString();
        String regexEmail = "^[A-Za-z0-9]+@[A-Za-z0-9]+\\.[A-Za-z0-9]+$";
        String regexPassword = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$";
        boolean valid = true;

        tvEmailError.setVisibility(TextView.GONE);
        tvPasswordError.setVisibility(TextView.GONE);

        if (email.trim().isEmpty()) {
            showError(tvEmailError, "Email không được để trống.");
            valid = false;
        } else if (!email.matches(regexEmail)) {
            showError(tvEmailError, "Email không hợp lệ.");
            valid = false;
        }

        if (password.trim().isEmpty()) {
            showError(tvPasswordError, "Vui lòng nhập mật khẩu.");;
            valid = false;
        } else if (!password.matches(regexPassword)) {
            showError(tvPasswordError, "Mật khẩu phải chứa ít nhất 8 ký tự, bao gồm cả chữ và số.");
            valid = false;
        }

        if (!valid) {
            return;
        }
        //Chỉ mới kiểm tra valid dữ liệu chưa xử lý lấy dữ liệu để xác minh
        Toast.makeText(this, "Đăng nhập thành công.", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private void showError(TextView errorView, String message) { //Function hiển thị lỗi
        errorView.setText(message);
        errorView.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
        errorView.setVisibility(TextView.VISIBLE);
    }
}
