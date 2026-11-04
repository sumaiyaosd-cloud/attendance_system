package com.example.attendance_system;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

public class ProfileActivity extends AppCompatActivity {

    private TextInputEditText etTeacherName, etTeacherEmail;
    private Button btnSaveProfile, btnHelp, btnLogoutProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        etTeacherName = findViewById(R.id.etTeacherName);
        etTeacherEmail = findViewById(R.id.etTeacherEmail);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);
        btnHelp = findViewById(R.id.btnHelp);
        btnLogoutProfile = findViewById(R.id.btnLogoutProfile);

        SharedPreferences prefs = getSharedPreferences("AttendancePrefs", MODE_PRIVATE);
        String name = prefs.getString("teacher_name", "Prof. Teacher");
        String email = prefs.getString("logged_email", "teacher@university.edu");

        etTeacherName.setText(name);
        etTeacherEmail.setText(email);

        btnSaveProfile.setOnClickListener(v -> {
            String newName = etTeacherName.getText() != null ? etTeacherName.getText().toString().trim() : "";
            String newEmail = etTeacherEmail.getText() != null ? etTeacherEmail.getText().toString().trim() : "";
            prefs.edit().putString("teacher_name", newName).putString("logged_email", newEmail).apply();
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
