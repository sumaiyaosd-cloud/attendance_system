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
import android.widget.TextView;
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
    private TextView tvTotalCount, tvPresentCount, tvAbsentCount, tvSummaryTitle;

    private AttendanceAdapter adapter;
    private List<AttendanceRecord> recordList;

    private DatabaseReference dbRef;
    private SensorManager sensorManager;
    private Sensor proximitySensor;
    private String userRole = "Teacher";
    private boolean isFirebaseConnected = true;

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
        tvTotalCount = findViewById(R.id.tvTotalCount);
        tvPresentCount = findViewById(R.id.tvPresentCount);
        tvAbsentCount = findViewById(R.id.tvAbsentCount);
        tvSummaryTitle = findViewById(R.id.tvSummaryTitle);

        SharedPreferences prefs = getSharedPreferences("AttendancePrefs", MODE_PRIVATE);
        userRole = prefs.getString("user_role", "Teacher");

        if (toolbar != null) {
            toolbar.setTitle(userRole.equals("Teacher") ? "Teacher Dashboard" : "Student Dashboard");
        }

        if ("Student".equals(userRole)) {
            fabAdd.setVisibility(View.GONE);
        }

        updateSummary();

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

        try {
            dbRef = FirebaseDatabase.getInstance().getReference("attendance_records");
            loadAttendanceData();
        } catch (Exception e) {
            isFirebaseConnected = false;
            loadLocalAttendanceData();
        }

        fabAdd.setOnClickListener(v -> {
            if ("Student".equals(userRole)) {
                Toast.makeText(this, "Only teachers can add attendance records", Toast.LENGTH_SHORT).show();
                return;
            }
            startActivity(new Intent(HomeActivity.this, AddEditActivity.class));
        });

        btnCall.setOnClickListener(v -> {
            Intent callIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:0123456789"));
            startActivity(callIntent);
        });

        btnShare.setOnClickListener(v -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Attendance Report (" + userRole + ")");
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Check out attendance records in Attendance Manager (" + userRole + " Dashboard). Total: " + recordList.size());
            startActivity(Intent.createChooser(shareIntent, "Share via"));
        });

        btnWeb.setOnClickListener(v -> {
            Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com"));
            startActivity(webIntent);
        });

        btnLogout.setOnClickListener(v -> {
            try {
                FirebaseAuth.getInstance().signOut();
            } catch (Exception ignored) {
            }
            getSharedPreferences("AttendancePrefs", MODE_PRIVATE).edit().clear().apply();
            startActivity(new Intent(HomeActivity.this, LoginActivity.class));
            finish();
        });

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isFirebaseConnected) {
            loadLocalAttendanceData();
        }
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
    }

    private void updateSummary() {
        int total = recordList != null ? recordList.size() : 0;
        int present = 0;
        int absent = 0;
        if (recordList != null) {
            for (AttendanceRecord r : recordList) {
                String status = r.getStatus() != null ? r.getStatus().toLowerCase() : "";
                if (status.contains("present") || status.contains("p")) {
                    present++;
                } else {
                    absent++;
                }
            }
        }
        if (tvTotalCount != null) tvTotalCount.setText(String.valueOf(total));
        if (tvPresentCount != null) tvPresentCount.setText(String.valueOf(present));
        if (tvAbsentCount != null) tvAbsentCount.setText(String.valueOf(absent));
        if (tvSummaryTitle != null) {
            tvSummaryTitle.setText(userRole.equals("Teacher") ? "Teacher Attendance Overview" : "Student Attendance Status");
        }
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
                List<AttendanceRecord> localList = AttendanceLocalStore.getRecords(HomeActivity.this);
                for (AttendanceRecord lr : localList) {
                    boolean alreadyExists = false;
                    for (AttendanceRecord r : recordList) {
                        if (r.getId().equals(lr.getId())) {
                            alreadyExists = true;
                            break;
                        }
                    }
                    if (!alreadyExists) {
                        recordList.add(lr);
                    }
                }
                adapter.notifyDataSetChanged();
                updateSummary();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                loadLocalAttendanceData();
            }
        });
    }

    private void loadLocalAttendanceData() {
        recordList.clear();
        recordList.addAll(AttendanceLocalStore.getRecords(this));
        adapter.notifyDataSetChanged();
        updateSummary();
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
        AttendanceLocalStore.deleteRecord(this, id);
        if (dbRef != null) {
            try {
                dbRef.child(id).removeValue();
            } catch (Exception ignored) {
            }
        }
        loadLocalAttendanceData();
        Toast.makeText(HomeActivity.this, "Record deleted successfully", Toast.LENGTH_SHORT).show();
    }
}
