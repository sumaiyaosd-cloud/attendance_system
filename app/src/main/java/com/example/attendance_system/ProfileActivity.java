package com.example.attendance_system;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

public class ProfileActivity extends AppCompatActivity {

    private TextInputEditText etProfileName, etProfileIdOrEmail, etDepartment, etSemester;
    private TextInputLayout tilDepartment, tilSemester;
    private Button btnSaveProfile, btnHelp, btnLogoutProfile;
    private SwitchCompat switchDarkMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        etProfileName = findViewById(R.id.etProfileName);
        etProfileIdOrEmail = findViewById(R.id.etProfileIdOrEmail);
        etDepartment = findViewById(R.id.etDepartment);
        etSemester = findViewById(R.id.etSemester);
        tilDepartment = findViewById(R.id.tilDepartment);
        tilSemester = findViewById(R.id.tilSemester);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);
        btnHelp = findViewById(R.id.btnHelp);
        btnLogoutProfile = findViewById(R.id.btnLogoutProfile);
        switchDarkMode = findViewById(R.id.switchDarkMode);

        SharedPreferences prefs = getSharedPreferences("AttendancePrefs", MODE_PRIVATE);
        String role = prefs.getString("user_role", "Teacher");
        boolean isDark = prefs.getBoolean("dark_mode", false);
        switchDarkMode.setChecked(isDark);

        if ("Student".equals(role)) {
            tilDepartment.setVisibility(View.VISIBLE);
            tilSemester.setVisibility(View.VISIBLE);
            etProfileName.setHint("Student Name");
            etProfileIdOrEmail.setHint("Student ID (e.g. CSE-101)");

            etProfileName.setText(prefs.getString("student_name", prefs.getString("student_id", "Student")));
            etProfileIdOrEmail.setText(prefs.getString("student_id", ""));
            etDepartment.setText(prefs.getString("student_dept", "CSE"));
            etSemester.setText(prefs.getString("student_semester", "8th Semester"));
        } else {
            tilDepartment.setVisibility(View.GONE);
            tilSemester.setVisibility(View.GONE);
            etProfileName.setHint("Teacher Name");
            etProfileIdOrEmail.setHint("Teacher Email");

            etProfileName.setText(prefs.getString("teacher_name", "Prof. Teacher"));
            etProfileIdOrEmail.setText(prefs.getString("logged_email", "teacher@university.edu"));
        }

        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("dark_mode", isChecked).apply();
            AppCompatDelegate.setDefaultNightMode(isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
        });

        btnSaveProfile.setOnClickListener(v -> {
            String name = etProfileName.getText() != null ? etProfileName.getText().toString().trim() : "";
            String idOrEmail = etProfileIdOrEmail.getText() != null ? etProfileIdOrEmail.getText().toString().trim() : "";
            if ("Student".equals(role)) {
                String dept = etDepartment.getText() != null ? etDepartment.getText().toString().trim() : "";
                String sem = etSemester.getText() != null ? etSemester.getText().toString().trim() : "";
                prefs.edit()
                        .putString("student_name", name)
                        .putString("student_id", idOrEmail)
                        .putString("student_dept", dept)
                        .putString("student_semester", sem)
                        .apply();
            } else {
                prefs.edit()
                        .putString("teacher_name", name)
                        .putString("logged_email", idOrEmail)
                        .apply();
            }
            Snackbar.make(btnSaveProfile, "Profile updated successfully!", Snackbar.LENGTH_SHORT).show();
        });

        btnHelp.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Help & Contact Support")
                    .setMessage("For technical assistance or support regarding the Attendance Management System, please contact:\n\nEmail: support@attendance.edu\nPhone: +8801234567890\nUniversity IT Department")
                    .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                    .show();
        });

        btnLogoutProfile.setOnClickListener(v -> {
            try {
                FirebaseAuth.getInstance().signOut();
            } catch (Exception ignored) {
            }
            prefs.edit().clear().apply();
            startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
            finishAffinity();
        });
    }
}
