package com.example.attendance_system;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText etRegEmail, etRegPassword, etStudentId;
    private TextInputLayout tilStudentId;
    private Button btnRegister;
    private TextView tvGoToLogin;
    private RadioButton rbTeacher, rbStudent;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        try {
            mAuth = FirebaseAuth.getInstance();
        } catch (Exception e) {
            mAuth = null;
        }

        etRegEmail = findViewById(R.id.etRegEmail);
        etRegPassword = findViewById(R.id.etRegPassword);
        etStudentId = findViewById(R.id.etStudentId);
        tilStudentId = findViewById(R.id.tilStudentId);
        btnRegister = findViewById(R.id.btnRegister);
        tvGoToLogin = findViewById(R.id.tvGoToLogin);
        rbTeacher = findViewById(R.id.rbTeacher);
        rbStudent = findViewById(R.id.rbStudent);

        rbTeacher.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) tilStudentId.setVisibility(View.GONE);
        });

        rbStudent.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) tilStudentId.setVisibility(View.VISIBLE);
        });

        btnRegister.setOnClickListener(v -> registerUser());

        tvGoToLogin.setOnClickListener(v -> finish());
    }

    private void registerUser() {
        String email = etRegEmail.getText() != null ? etRegEmail.getText().toString().trim() : "";
        String password = etRegPassword.getText() != null ? etRegPassword.getText().toString().trim() : "";
        String role = rbTeacher.isChecked() ? "Teacher" : "Student";
        String studentId = etStudentId.getText() != null ? etStudentId.getText().toString().trim() : "";

        if (email.isEmpty()) {
            etRegEmail.setError("Email is required");
            etRegEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            etRegPassword.setError("Password is required");
            etRegPassword.requestFocus();
            return;
        }

        if ("Student".equals(role) && studentId.isEmpty()) {
            etStudentId.setError("Student Name / ID is required for unique identification");
            etStudentId.requestFocus();
            return;
        }

        if ("Teacher".equals(role)) {
            studentId = ""; // Teachers don't need student ID
        }

        SharedPreferences prefs = getSharedPreferences("AttendancePrefs", MODE_PRIVATE);
        prefs.edit()
                .putString("logged_email", email)
                .putString("user_role", role)
                .putString("student_id", studentId)
                .putBoolean("remember_me", true)
                .apply();

        Snackbar.make(btnRegister, "Registration Successful (" + role + ")!", Snackbar.LENGTH_SHORT).show();

        if (mAuth != null) {
            try {
                mAuth.createUserWithEmailAndPassword(email, password);
            } catch (Exception ignored) {
            }
        }

        Intent intent = new Intent(RegisterActivity.this, HomeActivity.class);
        startActivity(intent);
        finish();
    }
}
