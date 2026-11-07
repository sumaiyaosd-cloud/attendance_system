package com.example.attendance_system;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etPassword, etStudentId;
    private TextInputLayout tilStudentId;
    private Button btnLogin;
    private TextView tvGoToRegister, tvForgotPassword;
    private RadioButton rbLoginTeacher, rbLoginStudent;
    private CheckBox cbRememberMe;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        try {
            mAuth = FirebaseAuth.getInstance();
        } catch (Exception e) {
            mAuth = null;
        }

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etStudentId = findViewById(R.id.etStudentId);
        tilStudentId = findViewById(R.id.tilStudentId);
        btnLogin = findViewById(R.id.btnLogin);
        tvGoToRegister = findViewById(R.id.tvGoToRegister);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        rbLoginTeacher = findViewById(R.id.rbLoginTeacher);
        rbLoginStudent = findViewById(R.id.rbLoginStudent);
        cbRememberMe = findViewById(R.id.cbRememberMe);

        SharedPreferences prefs = getSharedPreferences("AttendancePrefs", MODE_PRIVATE);
        boolean remember = prefs.getBoolean("remember_me", false);
        if (remember) {
            String savedEmail = prefs.getString("logged_email", "");
            String savedRole = prefs.getString("user_role", "Teacher");
            String savedStudentId = prefs.getString("student_id", "");
            etEmail.setText(savedEmail);
            cbRememberMe.setChecked(true);
            if ("Student".equals(savedRole)) {
                rbLoginStudent.setChecked(true);
                tilStudentId.setVisibility(View.VISIBLE);
                etStudentId.setText(savedStudentId);
            } else {
                rbLoginTeacher.setChecked(true);
                tilStudentId.setVisibility(View.GONE);
            }
        }

        rbLoginTeacher.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) tilStudentId.setVisibility(View.GONE);
        });

        rbLoginStudent.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) tilStudentId.setVisibility(View.VISIBLE);
        });

        btnLogin.setOnClickListener(v -> loginUser());

        tvForgotPassword.setOnClickListener(v -> handleForgotPassword());

        tvGoToRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });
    }

    private void loginUser() {
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";
        String role = rbLoginTeacher.isChecked() ? "Teacher" : "Student";

        if (email.isEmpty()) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return;
        }

        // Verify if user is registered in UserStore
        UserStore.UserInfo userInfo = UserStore.verifyUser(this, email, password);

        // If not found in local UserStore, allow if pre-existing demo/fallback account or Firebase Auth works,
        // but for strict validation, if UserStore has users, require registration. To be user-friendly,
        // if UserStore returns null, we check if Firebase auth or allow a default demo account, OR enforce registration.
        // The user specifically requested: "random kono pass diye or email diye jeno kaw student/teacher dash e duika jaite na pare ager reg kore then oi passm or mail diye dukte parbe"
        
        if (userInfo == null) {
            // Check if Firebase Auth can sign in
            if (mAuth != null) {
                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                proceedLogin(email, role, email.split("@")[0], password);
                            } else {
                                Snackbar.make(btnLogin, "Account not found or incorrect password! Please register first.", Snackbar.LENGTH_LONG).show();
                            }
                        });
                return;
            } else {
                Snackbar.make(btnLogin, "Account not found or incorrect password! Please register first.", Snackbar.LENGTH_LONG).show();
                return;
            }
        } else {
            proceedLogin(userInfo.email, userInfo.role, userInfo.studentId, password);
        }
    }

    private void proceedLogin(String email, String role, String studentId, String password) {
        if (studentId == null || studentId.isEmpty()) {
            studentId = email.split("@")[0];
        }

        boolean rememberMe = cbRememberMe.isChecked();
        SharedPreferences prefs = getSharedPreferences("AttendancePrefs", MODE_PRIVATE);
        prefs.edit()
                .putString("logged_email", email)
                .putString("user_role", role)
                .putString("student_id", studentId)
                .putString("teacher_name", email.contains("@") ? email.split("@")[0] : email)
                .putBoolean("remember_me", rememberMe)
                .apply();

        Snackbar.make(btnLogin, "Login Successful (" + role + ")!", Snackbar.LENGTH_SHORT).show();

        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
        startActivity(intent);
        finish();
    }

    private void handleForgotPassword() {
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        if (email.isEmpty()) {
            etEmail.setError("Enter your email to reset password");
            etEmail.requestFocus();
            return;
        }

        if (mAuth != null) {
            try {
                mAuth.sendPasswordResetEmail(email)
                        .addOnCompleteListener(task -> {
                            Snackbar.make(btnLogin, "Password reset email sent!", Snackbar.LENGTH_LONG).show();
                        });
                return;
            } catch (Exception ignored) {
            }
        }
        Snackbar.make(btnLogin, "Password reset email simulated for: " + email, Snackbar.LENGTH_LONG).show();
    }
}
