package com.example.attendance_system;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity implements SensorEventListener {

    private RecyclerView recyclerView;
    private FloatingActionButton fabAdd;
    private Button btnCall, btnShare, btnWeb, btnLogout;
    private Toolbar toolbar;

    private AttendanceAdapter adapter;
    private List<AttendanceRecord> recordList;

    private DatabaseReference dbRef;
    private SensorManager sensorManager;
    private Sensor proximitySensor;
    private String userRole = "Teacher";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        recyclerView = findViewById(R.id.recyclerView);
        fabAdd = findViewById(R.id.fabAdd);
        btnCall = findViewById(R.id.btnCall);
        btnShare = findViewById(R.id.btnShare);
        btnWeb = findViewById(R.id.btnWeb);
        btnLogout = findViewById(R.id.btnLogout);
        toolbar = findViewById(R.id.toolbar);

        // Get user role from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("AttendancePrefs", MODE_PRIVATE);
        userRole = prefs.getString("user_role", "Teacher");

        if (toolbar != null) {
            toolbar.setTitle(userRole.equals("Teacher") ? "Teacher Dashboard (Attendance)" : "Student Dashboard (View Only)");
        }

        // If Student, hide Add FAB (Teachers can add/edit, Students can view)
        if ("Student".equals(userRole)) {
            fabAdd.setVisibility(View.GONE);
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recordList = new ArrayList<>();
        adapter = new AttendanceAdapter(this, recordList, new AttendanceAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(AttendanceRecord record) {
                showRecordDialog(record);
            }

            @Override
            public void onEditClick(AttendanceRecord record) {
                if ("Student".equals(userRole)) {
                    Toast.makeText(HomeActivity.this, "Students cannot edit attendance records", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(HomeActivity.this, AddEditActivity.class);
                intent.putExtra("id", record.getId());
                intent.putExtra("name", record.getStudentName());
                intent.putExtra("subject", record.getSubject());
                intent.putExtra("date", record.getDate());
                intent.putExtra("status", record.getStatus());
                intent.putExtra("photoUri", record.getPhotoUri());
                startActivity(intent);
            }

            @Override
            public void onDeleteClick(AttendanceRecord record) {
                if ("Student".equals(userRole)) {
                    Toast.makeText(HomeActivity.this, "Students cannot delete attendance records", Toast.LENGTH_SHORT).show();
                    return;
                }
                deleteRecord(record.getId());
            }
        });
        recyclerView.setAdapter(adapter);

        dbRef = FirebaseDatabase.getInstance().getReference("attendance_records");
        loadAttendanceData();

        fabAdd.setOnClickListener(v -> {
            if ("Student".equals(userRole)) {
                Toast.makeText(this, "Only teachers can add attendance records", Toast.LENGTH_SHORT).show();
                return;
            }
            startActivity(new Intent(HomeActivity.this, AddEditActivity.class));
        });

        // Feature 12: Implicit Intents
        btnCall.setOnClickListener(v -> {
            Intent callIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:0123456789"));
            startActivity(callIntent);
        });

        btnShare.setOnClickListener(v -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Attendance Report (" + userRole + ")");
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Check out the attendance records in Attendance Manager (" + userRole + " Dashboard).");
            startActivity(Intent.createChooser(shareIntent, "Share via"));
        });

        btnWeb.setOnClickListener(v -> {
            Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com"));
            startActivity(webIntent);
        });

        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            getSharedPreferences("AttendancePrefs", MODE_PRIVATE).edit().clear().apply();
            startActivity(new Intent(HomeActivity.this, LoginActivity.class));
            finish();
        });

        // Feature 11: Sensor Manager & SensorEventListener
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (proximitySensor != null) {
            sensorManager.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
            if (event.values[0] < proximitySensor.getMaximumRange()) {
                Toast.makeText(this, "Proximity Triggered (" + userRole + "): Near sensor!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not needed
    }

    private void loadAttendanceData() {
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                recordList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    AttendanceRecord record = ds.getValue(AttendanceRecord.class);
                    if (record != null) {
                        recordList.add(record);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(HomeActivity.this, "Failed to load data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showRecordDialog(AttendanceRecord record) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(record.getStudentName() + " (" + userRole + " View)")
                .setMessage("Subject: " + record.getSubject() + "\nDate: " + record.getDate() + "\nStatus: " + record.getStatus());

        if ("Teacher".equals(userRole)) {
            builder.setPositiveButton("Edit", (dialog, which) -> {
                Intent intent = new Intent(HomeActivity.this, AddEditActivity.class);
                intent.putExtra("id", record.getId());
                intent.putExtra("name", record.getStudentName());
                intent.putExtra("subject", record.getSubject());
                intent.putExtra("date", record.getDate());
                intent.putExtra("status", record.getStatus());
                intent.putExtra("photoUri", record.getPhotoUri());
                startActivity(intent);
            })
            .setNegativeButton("Delete", (dialog, which) -> deleteRecord(record.getId()));
        }

        builder.setNeutralButton("Close", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void deleteRecord(String id) {
        if ("Student".equals(userRole)) {
            Toast.makeText(this, "Students are not allowed to delete records", Toast.LENGTH_SHORT).show();
            return;
        }
        if (id != null && !id.isEmpty()) {
            dbRef.child(id).removeValue().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(HomeActivity.this, "Record deleted successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(HomeActivity.this, "Failed to delete record", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
