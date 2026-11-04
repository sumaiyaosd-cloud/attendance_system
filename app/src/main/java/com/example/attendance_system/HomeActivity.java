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
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class HomeActivity extends AppCompatActivity implements SensorEventListener {

    private RecyclerView recyclerView;
    private FloatingActionButton fabAdd;
    private Button btnCall, btnShare, btnWeb, btnLogout, btnTakeAttendance, btnFilterPresent, btnFilterAll;
    private Toolbar toolbar;
    private TextView tvWelcomeTeacher, tvTotalCourses, tvTotalStudents, tvTodayPresent, tvTodayAbsent, tvOverallPercentage, tvSummaryTitle, tvEligibilityWarning;
    private EditText etSearch;
    private Spinner spinnerCourseFilter;

    private AttendanceAdapter adapter;
    private List<AttendanceRecord> recordList;
    private List<AttendanceRecord> fullRecordList;
    private List<Course> courseList;

    private DatabaseReference dbRef;
    private SensorManager sensorManager;
    private Sensor proximitySensor;
    private String userRole = "Teacher";
    private String studentId = "";
    private String userEmail = "";
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
        btnTakeAttendance = findViewById(R.id.btnTakeAttendance);
        btnFilterPresent = findViewById(R.id.btnFilterPresent);
        btnFilterAll = findViewById(R.id.btnFilterAll);
        toolbar = findViewById(R.id.toolbar);
        tvWelcomeTeacher = findViewById(R.id.tvWelcomeTeacher);
        tvTotalCourses = findViewById(R.id.tvTotalCourses);
        tvTotalStudents = findViewById(R.id.tvTotalStudents);
        tvTodayPresent = findViewById(R.id.tvTodayPresent);
        tvTodayAbsent = findViewById(R.id.tvTodayAbsent);
        tvOverallPercentage = findViewById(R.id.tvOverallPercentage);
        tvSummaryTitle = findViewById(R.id.tvSummaryTitle);
        tvEligibilityWarning = findViewById(R.id.tvEligibilityWarning);
        etSearch = findViewById(R.id.etSearch);
        spinnerCourseFilter = findViewById(R.id.spinnerCourseFilter);

        SharedPreferences prefs = getSharedPreferences("AttendancePrefs", MODE_PRIVATE);
        userRole = prefs.getString("user_role", "Teacher");
        studentId = prefs.getString("student_id", "");
        userEmail = prefs.getString("logged_email", "Teacher Profile");

        if (toolbar != null) {
            toolbar.setTitle(userRole.equals("Teacher") ? "Teacher Dashboard" : "Student Dashboard");
        }

        if ("Student".equals(userRole)) {
            fabAdd.setVisibility(View.GONE);
            btnTakeAttendance.setVisibility(View.GONE);
            if (spinnerCourseFilter != null) spinnerCourseFilter.setVisibility(View.GONE);
            etSearch.setHint("Search my subjects...");
        } else {
            courseList = CourseStore.getCourses(this);
            List<String> courseNames = new ArrayList<>();
            courseNames.add("All Courses");
            for (Course c : courseList) {
                courseNames.add(c.getCourseName());
            }
            ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, courseNames);
            spinnerCourseFilter.setAdapter(spinnerAdapter);
            spinnerCourseFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (position == 0) {
                        loadDataIntoAdapter(fullRecordList);
                    } else {
                        String selectedCourse = courseList.get(position - 1).getCourseName();
                        filterByCourse(selectedCourse);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });
        }

        if (tvWelcomeTeacher != null) {
            tvWelcomeTeacher.setText("Welcome, " + (userRole.equals("Teacher") ? userEmail : studentId));
        }

        View profileHeaderLayout = findViewById(R.id.profileHeaderLayout);
        if (profileHeaderLayout != null) {
            profileHeaderLayout.setOnClickListener(v -> {
                startActivity(new Intent(HomeActivity.this, ProfileActivity.class));
            });
        }

        recordList = new ArrayList<>();
        fullRecordList = new ArrayList<>();

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AttendanceAdapter(this, recordList, fullRecordList, new AttendanceAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(AttendanceRecord record) {
                showRecordDialog(record);
            }

            @Override
            public void onEditClick(AttendanceRecord record) {
                if ("Student".equals(userRole)) {
                    Snackbar.make(recyclerView, "Students cannot edit attendance records", Snackbar.LENGTH_SHORT).show();
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
                    Snackbar.make(recyclerView, "Students cannot delete attendance records", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                deleteRecord(record.getId());
            }
        });
        recyclerView.setAdapter(adapter);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterRecords(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnFilterPresent.setOnClickListener(v -> filterByStatus("Present"));
        btnFilterAll.setOnClickListener(v -> {
            etSearch.setText("");
            loadDataIntoAdapter(fullRecordList);
        });

        btnTakeAttendance.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, AddEditActivity.class));
        });

        try {
            dbRef = FirebaseDatabase.getInstance().getReference("attendance_records");
            loadAttendanceData();
        } catch (Exception e) {
            isFirebaseConnected = false;
            loadLocalAttendanceData();
        }

        fabAdd.setOnClickListener(v -> {
            if ("Student".equals(userRole)) {
                Snackbar.make(recyclerView, "Only teachers can add attendance records", Snackbar.LENGTH_SHORT).show();
                return;
            }
            startActivity(new Intent(HomeActivity.this, AddEditActivity.class));
        });

        btnCall.setOnClickListener(v -> {
            Intent callIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:0123456789"));
            startActivity(callIntent);
        });

        btnShare.setOnClickListener(v -> {
            int total = recordList.size();
            int present = 0;
            for (AttendanceRecord r : recordList) {
                if (r.getStatus() != null && (r.getStatus().equalsIgnoreCase("Present") || r.getStatus().equalsIgnoreCase("P"))) {
                    present++;
                }
            }
            StringBuilder csv = new StringBuilder("ID,Student,Subject,Date,Status\n");
            for (AttendanceRecord r : recordList) {
                csv.append(r.getId()).append(",").append(r.getStudentName()).append(",").append(r.getSubject()).append(",").append(r.getDate()).append(",").append(r.getStatus()).append("\n");
            }
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Attendance CSV Report (" + userRole + ")");
            shareIntent.putExtra(Intent.EXTRA_TEXT, csv.toString());
            startActivity(Intent.createChooser(shareIntent, "Export CSV Report via"));
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
                Snackbar.make(recyclerView, "Proximity Triggered (" + userRole + "): Near sensor!", Snackbar.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    private void filterRecords(String query) {
        List<AttendanceRecord> filtered = new ArrayList<>();
        for (AttendanceRecord r : fullRecordList) {
            String matchField = "Teacher".equals(userRole) ? r.getStudentName() : r.getSubject();
            if (matchField != null && matchField.toLowerCase().contains(query.toLowerCase())) {
                filtered.add(r);
            }
        }
        loadDataIntoAdapter(filtered);
    }

    private void filterByStatus(String status) {
        List<AttendanceRecord> filtered = new ArrayList<>();
        for (AttendanceRecord r : fullRecordList) {
            if (r.getStatus() != null && r.getStatus().equalsIgnoreCase(status)) {
                filtered.add(r);
            }
        }
        loadDataIntoAdapter(filtered);
    }

    private void filterByCourse(String courseName) {
        List<AttendanceRecord> filtered = new ArrayList<>();
        for (AttendanceRecord r : fullRecordList) {
            if (r.getSubject() != null && r.getSubject().equalsIgnoreCase(courseName)) {
                filtered.add(r);
            }
        }
        loadDataIntoAdapter(filtered);
    }

    private void loadDataIntoAdapter(List<AttendanceRecord> list) {
        recordList.clear();
        recordList.addAll(list);
        adapter.notifyDataSetChanged();
        updateSummary();
    }

    private void updateSummary() {
        int totalRecords = fullRecordList != null ? fullRecordList.size() : 0;
        int present = 0;
        Set<String> uniqueStudents = new HashSet<>();
        String todayStr = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        int todayPresent = 0;
        int todayAbsent = 0;

        if (fullRecordList != null) {
            for (AttendanceRecord r : fullRecordList) {
                if (r.getStudentName() != null) uniqueStudents.add(r.getStudentName().trim().toLowerCase());
                String status = r.getStatus() != null ? r.getStatus().toLowerCase() : "";
                boolean isPresent = status.contains("present") || status.contains("p");
                if (isPresent) {
                    present++;
                }

                if (r.getDate() != null && r.getDate().equals(todayStr)) {
                    if (isPresent) todayPresent++;
                    else todayAbsent++;
                }
            }
        }
        double percentage = totalRecords > 0 ? ((double) present / totalRecords) * 100 : 0.0;

        if (tvTotalCourses != null) tvTotalCourses.setText(String.valueOf(CourseStore.getCourses(this).size()));
        if (tvTotalStudents != null) tvTotalStudents.setText(String.valueOf(uniqueStudents.size()));
        if (tvTodayPresent != null) tvTodayPresent.setText(String.valueOf(todayPresent));
        if (tvTodayAbsent != null) tvTodayAbsent.setText(String.valueOf(todayAbsent));
        if (tvOverallPercentage != null) tvOverallPercentage.setText(String.format("%.1f%%", percentage));

        if (tvSummaryTitle != null) {
            tvSummaryTitle.setText(userRole.equals("Teacher") ? "Teacher Attendance Overview & Analytics" : "My Attendance Status (" + studentId + ")");
        }

        if (tvEligibilityWarning != null) {
            if (totalRecords > 0 && percentage < 75.0) {
                tvEligibilityWarning.setVisibility(View.VISIBLE);
                tvEligibilityWarning.setText("Warning: Attendance is " + String.format("%.1f", percentage) + "% (Below 75% eligibility requirement!)");
            } else {
                tvEligibilityWarning.setVisibility(View.GONE);
            }
        }
    }

    private void loadAttendanceData() {
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<AttendanceRecord> allRecords = new ArrayList<>();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    AttendanceRecord record = ds.getValue(AttendanceRecord.class);
                    if (record != null) {
                        allRecords.add(record);
                    }
                }
                List<AttendanceRecord> localList = AttendanceLocalStore.getRecords(HomeActivity.this);
                for (AttendanceRecord lr : localList) {
                    boolean alreadyExists = false;
                    for (AttendanceRecord r : allRecords) {
                        if (r.getId().equals(lr.getId())) {
                            alreadyExists = true;
                            break;
                        }
                    }
                    if (!alreadyExists) {
                        allRecords.add(lr);
                    }
                }

                processRecordsForRole(allRecords);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                loadLocalAttendanceData();
            }
        });
    }

    private void loadLocalAttendanceData() {
        List<AttendanceRecord> allRecords = AttendanceLocalStore.getRecords(this);
        processRecordsForRole(allRecords);
    }

    private void processRecordsForRole(List<AttendanceRecord> allRecords) {
        fullRecordList.clear();
        if ("Student".equals(userRole) && !studentId.isEmpty()) {
            for (AttendanceRecord r : allRecords) {
                if (r.getStudentName() != null && r.getStudentName().toLowerCase().contains(studentId.toLowerCase())) {
                    fullRecordList.add(r);
                }
            }
        } else {
            fullRecordList.addAll(allRecords);
        }
        loadDataIntoAdapter(fullRecordList);
    }

    private void showRecordDialog(AttendanceRecord record) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(record.getStudentName() + " (" + userRole + " View)")
                .setMessage("Course: " + record.getSubject() + "\nDate: " + record.getDate() + "\nStatus: " + record.getStatus());

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
            Snackbar.make(recyclerView, "Students are not allowed to delete records", Snackbar.LENGTH_SHORT).show();
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
        Snackbar.make(recyclerView, "Record deleted successfully", Snackbar.LENGTH_SHORT).show();
    }
}
