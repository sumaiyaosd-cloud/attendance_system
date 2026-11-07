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
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
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

    // Teacher views
    private RecyclerView recyclerView;
    private FloatingActionButton fabAdd;
    private Button btnCall, btnShare, btnWeb, btnLogout, btnTakeAttendance, btnFilterPresent, btnFilterAll, btnNotice, btnRoutine, btnViewSchedule, btnAddCourse;
    private Toolbar toolbar;
    private TextView tvWelcomeTeacher, tvTotalCourses, tvTotalStudents, tvTodayPresent, tvTodayAbsent, tvTodayDateBanner;
    private EditText etSearch;
    private Spinner spinnerCourseFilter;
    private CardView cardTodaySchedule;

    // Student dashboard views
    private TextView tvStudentFullName;
    private RecyclerView recyclerViewStudentCourses;
    private Button btnStudentLogout, btnStudentRoutine, btnStudentReport, btnStudentNotifications, btnStudentDownload;
    private View cardStudentProfile, cardStudentTodaySchedule;

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

        // CRITICAL: Initialize lists FIRST before any layout inflation or data loading
        recordList = new ArrayList<>();
        fullRecordList = new ArrayList<>();

        SharedPreferences prefs = getSharedPreferences("AttendancePrefs", MODE_PRIVATE);
        userRole = prefs.getString("user_role", "Teacher");
        studentId = prefs.getString("student_id", "");
        userEmail = prefs.getString("logged_email", "Teacher Profile");

        if ("Student".equals(userRole)) {
            setContentView(R.layout.activity_student_dashboard);
            initStudentDashboard();
        } else {
            setContentView(R.layout.activity_home);
            initTeacherDashboard();
        }

        try {
            dbRef = FirebaseDatabase.getInstance().getReference("attendance_records");
            loadAttendanceData();
        } catch (Exception e) {
            isFirebaseConnected = false;
            loadLocalAttendanceData();
        }

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        }
    }

    private void initStudentDashboard() {
        tvStudentFullName = findViewById(R.id.tvStudentFullName);
        TextView tvStudentProfileInfo = findViewById(R.id.tvStudentProfileInfo);
        recyclerViewStudentCourses = findViewById(R.id.recyclerViewStudentCourses);
        btnStudentLogout = findViewById(R.id.btnStudentLogout);
        btnStudentRoutine = findViewById(R.id.btnStudentRoutine);
        btnStudentReport = findViewById(R.id.btnStudentReport);
        btnStudentNotifications = findViewById(R.id.btnStudentNotifications);
        btnStudentDownload = findViewById(R.id.btnStudentDownload);
        cardStudentProfile = findViewById(R.id.cardStudentProfile);
        cardStudentTodaySchedule = findViewById(R.id.cardStudentTodaySchedule);

        SharedPreferences prefs = getSharedPreferences("AttendancePrefs", MODE_PRIVATE);
        String studentName = prefs.getString("student_name", studentId.isEmpty() ? "Student User" : studentId);
        String dept = prefs.getString("student_dept", "CSE");
        String sem = prefs.getString("student_semester", "8th Semester");

        if (tvStudentFullName != null) {
            tvStudentFullName.setText("Welcome, " + studentName);
        }
        if (tvStudentProfileInfo != null) {
            tvStudentProfileInfo.setText("ID: " + studentId + " | Dept: " + dept + " | " + sem + " (Tap to Edit Profile)");
        }

        if (cardStudentProfile != null) {
            cardStudentProfile.setOnClickListener(v -> {
                startActivity(new Intent(HomeActivity.this, ProfileActivity.class));
            });
        }

        if (cardStudentTodaySchedule != null) {
            cardStudentTodaySchedule.setOnClickListener(v -> {
                List<TodayScheduleStore.ScheduledClass> todayClasses = TodayScheduleStore.getTodayClasses();
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < todayClasses.size(); i++) {
                    TodayScheduleStore.ScheduledClass sc = todayClasses.get(i);
                    sb.append((i + 1)).append(". ").append(sc.courseName).append(" (").append(sc.courseCode).append(")\nTime: ").append(sc.time).append(" | Room: ").append(sc.room).append("\n\n");
                }
                new AlertDialog.Builder(this)
                        .setTitle("Remaining Classes Today")
                        .setMessage(sb.length() > 0 ? sb.toString().trim() : "No classes scheduled for today.")
                        .setPositiveButton("Close", (dialog, which) -> dialog.dismiss())
                        .show();
            });
        }

        if (recyclerViewStudentCourses != null) {
            recyclerViewStudentCourses.setLayoutManager(new LinearLayoutManager(this));
        }

        if (btnStudentRoutine != null) {
            btnStudentRoutine.setOnClickListener(v -> {
                new AlertDialog.Builder(this)
                        .setTitle("Class Schedule")
                        .setMessage(RoutineStore.getWeeklyRoutine())
                        .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                        .show();
            });
        }

        if (btnStudentReport != null) {
            btnStudentReport.setOnClickListener(v -> {
                int total = fullRecordList != null ? fullRecordList.size() : 0;
                int present = 0;
                int absent = 0;
                int late = 0;
                for (AttendanceRecord r : fullRecordList) {
                    String st = r.getStatus() != null ? r.getStatus().toLowerCase() : "";
                    if (st.contains("present") || st.contains("p")) present++;
                    else if (st.contains("late")) late++;
                    else absent++;
                }
                double pct = total > 0 ? ((double) present / total) * 100 : 0.0;
                String reportMsg = "Monthly & Semester Attendance Summary:\n\n" +
                        "Total Classes: " + total + "\n" +
                        "Present: " + present + "\n" +
                        "Absent: " + absent + "\n" +
                        "Late: " + late + "\n" +
                        "Overall Attendance Rate: " + String.format(Locale.getDefault(), "%.1f", pct) + "%\n" +
                        "Eligibility Status: " + (pct >= 75.0 ? "ELIGIBLE ✅" : "NOT ELIGIBLE ❌");

                new AlertDialog.Builder(this)
                        .setTitle("Monthly & Semester Reports")
                        .setMessage(reportMsg)
                        .setPositiveButton("Close", (dialog, which) -> dialog.dismiss())
                        .show();
            });
        }

        if (btnStudentNotifications != null) {
            btnStudentNotifications.setOnClickListener(v -> {
                List<String> notices = AnnouncementStore.getAnnouncements(this);
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < notices.size(); i++) {
                    sb.append((i + 1)).append(". ").append(notices.get(i)).append("\n\n");
                }
                new AlertDialog.Builder(this)
                        .setTitle("Notifications & Absent Alerts")
                        .setMessage(sb.length() > 0 ? sb.toString().trim() : "No new notifications or low attendance warnings.")
                        .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                        .show();
            });
        }

        if (btnStudentDownload != null) {
            btnStudentDownload.setOnClickListener(v -> {
                StringBuilder report = new StringBuilder("=== STUDENT ATTENDANCE REPORT ===\n");
                report.append("Student ID: ").append(studentId).append("\n");
                report.append("Total Records: ").append(fullRecordList.size()).append("\n\n");
                for (AttendanceRecord r : fullRecordList) {
                    report.append("Date: ").append(r.getDate()).append(" | Subject: ").append(r.getSubject()).append(" | Status: ").append(r.getStatus()).append("\n");
                }
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "My Attendance Report (" + studentId + ")");
                shareIntent.putExtra(Intent.EXTRA_TEXT, report.toString());
                startActivity(Intent.createChooser(shareIntent, "Export / Download Report via"));
            });
        }

        if (btnStudentLogout != null) {
            btnStudentLogout.setOnClickListener(v -> {
                try {
                    FirebaseAuth.getInstance().signOut();
                } catch (Exception ignored) {
                }
                getSharedPreferences("AttendancePrefs", MODE_PRIVATE).edit().clear().apply();
                startActivity(new Intent(HomeActivity.this, LoginActivity.class));
                finish();
            });
        }
    }

    private void initTeacherDashboard() {
        recyclerView = findViewById(R.id.recyclerView);
        fabAdd = findViewById(R.id.fabAdd);
        btnCall = findViewById(R.id.btnCall);
        btnShare = findViewById(R.id.btnShare);
        btnWeb = findViewById(R.id.btnWeb);
        btnLogout = findViewById(R.id.btnLogout);
        btnTakeAttendance = findViewById(R.id.btnTakeAttendance);
        btnFilterPresent = findViewById(R.id.btnFilterPresent);
        btnFilterAll = findViewById(R.id.btnFilterAll);
        btnNotice = findViewById(R.id.btnNotice);
        btnRoutine = findViewById(R.id.btnRoutine);
        btnViewSchedule = findViewById(R.id.btnViewSchedule);
        btnAddCourse = findViewById(R.id.btnAddCourse);
        toolbar = findViewById(R.id.toolbar);
        tvWelcomeTeacher = findViewById(R.id.tvWelcomeTeacher);
        tvTotalCourses = findViewById(R.id.tvTotalCourses);
        tvTotalStudents = findViewById(R.id.tvTotalStudents);
        tvTodayPresent = findViewById(R.id.tvTodayPresent);
        tvTodayAbsent = findViewById(R.id.tvTodayAbsent);
        tvTodayDateBanner = findViewById(R.id.tvTodayDateBanner);
        etSearch = findViewById(R.id.etSearch);
        spinnerCourseFilter = findViewById(R.id.spinnerCourseFilter);
        cardTodaySchedule = findViewById(R.id.cardTodaySchedule);

        SharedPreferences prefs = getSharedPreferences("AttendancePrefs", MODE_PRIVATE);
        String teacherName = prefs.getString("teacher_name", "");
        if (teacherName.isEmpty()) {
            teacherName = userEmail.contains("@") ? userEmail.split("@")[0] : userEmail;
            if (!teacherName.isEmpty()) {
                teacherName = teacherName.substring(0, 1).toUpperCase() + teacherName.substring(1);
            }
        }
        final String finalTeacherName = teacherName;

        if (toolbar != null) {
            toolbar.setTitle("Teacher Dashboard (" + finalTeacherName + ")");
        }

        if (cardTodaySchedule != null) cardTodaySchedule.setVisibility(View.VISIBLE);
        if (btnAddCourse != null) btnAddCourse.setVisibility(View.VISIBLE);

        String currentDate = new SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault()).format(new Date());
        if (tvTodayDateBanner != null) tvTodayDateBanner.setText("Date: " + currentDate);

        if (btnViewSchedule != null) {
            btnViewSchedule.setOnClickListener(v -> {
                List<TodayScheduleStore.ScheduledClass> todayClasses = TodayScheduleStore.getTodayClasses();
                CharSequence[] items = new CharSequence[todayClasses.size()];
                for (int i = 0; i < todayClasses.size(); i++) {
                    TodayScheduleStore.ScheduledClass sc = todayClasses.get(i);
                    items[i] = sc.courseName + " (" + sc.courseCode + ")\nTime: " + sc.time + " | Room: " + sc.room;
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Today's Available Classes & Schedule");
                builder.setItems(items, (dialog, which) -> {
                    TodayScheduleStore.ScheduledClass selectedSc = todayClasses.get(which);
                    Intent intent = new Intent(HomeActivity.this, AddEditActivity.class);
                    intent.putExtra("preselected_subject", selectedSc.courseName);
                    startActivity(intent);
                });
                builder.setPositiveButton("Close", (dialog, which) -> dialog.dismiss());
                builder.show();
            });
        }

        if (btnAddCourse != null) {
            btnAddCourse.setOnClickListener(v -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Add New Course");

                LinearLayout layout = new LinearLayout(this);
                layout.setOrientation(LinearLayout.VERTICAL);
                layout.setPadding(32, 16, 32, 16);

                final EditText inputName = new EditText(this);
                inputName.setHint("Course Name (e.g. Machine Learning)");
                layout.addView(inputName);

                final EditText inputCode = new EditText(this);
                inputCode.setHint("Course Code / Section (e.g. CSE 4101)");
                layout.addView(inputCode);

                builder.setView(layout);
                builder.setPositiveButton("Add", (dialog, which) -> {
                    String name = inputName.getText().toString().trim();
                    String code = inputCode.getText().toString().trim();
                    if (!name.isEmpty() && !code.isEmpty()) {
                        String newId = String.valueOf(CourseStore.getCourses(this).size() + 1);
                        CourseStore.addCourse(this, new Course(newId, name, code));
                        Snackbar.make(recyclerView, "Course added successfully!", Snackbar.LENGTH_SHORT).show();
                        recreate();
                    } else {
                        Snackbar.make(recyclerView, "Course name and code cannot be empty", Snackbar.LENGTH_SHORT).show();
                    }
                });
                builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
                builder.show();
            });
        }

        courseList = CourseStore.getCourses(this);
        List<String> courseNames = new ArrayList<>();
        for (Course c : courseList) {
            courseNames.add(c.getCourseName());
        }
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, courseNames);
        spinnerCourseFilter.setAdapter(spinnerAdapter);
        spinnerCourseFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedCourse = courseList.get(position).getCourseName();
                filterByCourse(selectedCourse);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        if (tvWelcomeTeacher != null) {
            tvWelcomeTeacher.setText("Welcome, " + finalTeacherName);
        }

        View profileHeaderLayout = findViewById(R.id.profileHeaderLayout);
        if (profileHeaderLayout != null) {
            profileHeaderLayout.setOnClickListener(v -> {
                startActivity(new Intent(HomeActivity.this, ProfileActivity.class));
            });
        }

        if (btnNotice != null) {
            btnNotice.setText("Send Notice");
            btnNotice.setOnClickListener(v -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Send Announcement to Students");
                final EditText input = new EditText(this);
                input.setHint("Type announcement message here...");
                builder.setView(input);
                builder.setPositiveButton("Send", (dialog, which) -> {
                    String msg = input.getText().toString().trim();
                    if (!msg.isEmpty()) {
                        AnnouncementStore.addAnnouncement(this, finalTeacherName + ": " + msg);
                        Snackbar.make(recyclerView, "Announcement sent successfully!", Snackbar.LENGTH_SHORT).show();
                    }
                });
                builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
                builder.show();
            });
        }

        if (btnRoutine != null) {
            btnRoutine.setOnClickListener(v -> {
                new AlertDialog.Builder(this)
                        .setTitle("Weekly Class Routine")
                        .setMessage(RoutineStore.getWeeklyRoutine())
                        .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                        .show();
            });
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AttendanceAdapter(this, recordList, fullRecordList, new AttendanceAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(AttendanceRecord record) {
                showRecordDialog(record);
            }

            @Override
            public void onEditClick(AttendanceRecord record) {
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
            if (spinnerCourseFilter != null && spinnerCourseFilter.getSelectedItem() != null) {
                filterByCourse(spinnerCourseFilter.getSelectedItem().toString());
            } else {
                loadDataIntoAdapter(fullRecordList);
            }
        });

        btnTakeAttendance.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, AddEditActivity.class));
        });

        fabAdd.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, AddEditActivity.class));
        });

        btnCall.setOnClickListener(v -> {
            Intent callIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:0123456789"));
            startActivity(callIntent);
        });

        btnShare.setOnClickListener(v -> {
            StringBuilder csv = new StringBuilder("ID,Student,Subject,Date,Status\n");
            for (AttendanceRecord r : recordList) {
                csv.append(r.getId()).append(",").append(r.getStudentName()).append(",").append(r.getSubject()).append(",").append(r.getDate()).append(",").append(r.getStatus()).append("\n");
            }
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Attendance CSV Report");
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
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isFirebaseConnected) {
            loadLocalAttendanceData();
        }
        if (sensorManager != null) {
            sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY), SensorManager.SENSOR_DELAY_NORMAL);
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
                // Sensor active
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
        updateCourseSpecificSummary(filtered);
    }

    private void loadDataIntoAdapter(List<AttendanceRecord> list) {
        recordList.clear();
        recordList.addAll(list);
        if (adapter != null) adapter.notifyDataSetChanged();
        updateSummary();
    }

    private void updateCourseSpecificSummary(List<AttendanceRecord> courseRecords) {
        Set<String> uniqueStudents = new HashSet<>();
        int presentCount = 0;
        int absentCount = 0;

        if (courseRecords != null) {
            for (AttendanceRecord r : courseRecords) {
                if (r.getStudentName() != null) uniqueStudents.add(r.getStudentName().trim().toLowerCase());
                String status = r.getStatus() != null ? r.getStatus().toLowerCase() : "";
                if (status.contains("present") || status.contains("p")) {
                    presentCount++;
                } else {
                    absentCount++;
                }
            }
        }

        if (tvTotalStudents != null) tvTotalStudents.setText(String.valueOf(uniqueStudents.size()));
        if (tvTodayPresent != null) tvTodayPresent.setText(String.valueOf(presentCount));
        if (tvTodayAbsent != null) tvTodayAbsent.setText(String.valueOf(absentCount));
    }

    private void updateSummary() {
        Set<String> uniqueStudents = new HashSet<>();
        String todayStr = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        int todayPresent = 0;
        int todayAbsent = 0;

        if (fullRecordList != null) {
            for (AttendanceRecord r : fullRecordList) {
                if (r.getStudentName() != null) uniqueStudents.add(r.getStudentName().trim().toLowerCase());
                String status = r.getStatus() != null ? r.getStatus().toLowerCase() : "";
                boolean isPresent = status.contains("present") || status.contains("p");

                if (r.getDate() != null && r.getDate().equals(todayStr)) {
                    if (isPresent) todayPresent++;
                    else todayAbsent++;
                }
            }
        }

        if (tvTotalCourses != null) tvTotalCourses.setText(String.valueOf(CourseStore.getCourses(this).size()));
        if (tvTotalStudents != null) tvTotalStudents.setText(String.valueOf(uniqueStudents.size()));
        if (tvTodayPresent != null) tvTodayPresent.setText(String.valueOf(todayPresent));
        if (tvTodayAbsent != null) tvTodayAbsent.setText(String.valueOf(todayAbsent));

        if ("Student".equals(userRole)) {
            loadStudentCourseSummaries(fullRecordList);
        }
    }

    private void loadStudentCourseSummaries(List<AttendanceRecord> records) {
        List<CourseSummaryAdapter.CourseSummary> summaries = new ArrayList<>();
        List<Course> courses = CourseStore.getCourses(this);
        for (Course c : courses) {
            List<AttendanceRecord> courseRecords = new ArrayList<>();
            int present = 0;
            int absent = 0;
            for (AttendanceRecord r : records) {
                if (r.getSubject() != null && r.getSubject().equalsIgnoreCase(c.getCourseName())) {
                    courseRecords.add(r);
                    String status = r.getStatus() != null ? r.getStatus().toLowerCase() : "";
                    if (status.contains("present") || status.contains("p")) present++;
                    else absent++;
                }
            }
            int total = courseRecords.size();
            if (total > 0) {
                CourseSummaryAdapter.CourseSummary cs = new CourseSummaryAdapter.CourseSummary();
                cs.courseName = c.getCourseName();
                cs.total = total;
                cs.present = present;
                cs.absent = absent;
                cs.percentage = ((double) present / total) * 100;
                cs.records = courseRecords;
                summaries.add(cs);
            }
        }

        if (recyclerViewStudentCourses != null) {
            CourseSummaryAdapter courseAdapter = new CourseSummaryAdapter(this, summaries, summary -> {
                StringBuilder sb = new StringBuilder();
                for (AttendanceRecord r : summary.records) {
                    sb.append("Date: ").append(r.getDate()).append(" | Status: ").append(r.getStatus()).append("\n");
                }
                new AlertDialog.Builder(this)
                        .setTitle(summary.courseName + " - Attendance History")
                        .setMessage(sb.length() > 0 ? sb.toString().trim() : "No history found.")
                        .setPositiveButton("Close", (dialog, which) -> dialog.dismiss())
                        .show();
            });
            recyclerViewStudentCourses.setAdapter(courseAdapter);
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
        } else if ("Teacher".equals(userRole) && !userEmail.isEmpty()) {
            for (AttendanceRecord r : allRecords) {
                if (r.getTeacherEmail() != null && r.getTeacherEmail().equalsIgnoreCase(userEmail)) {
                    fullRecordList.add(r);
                }
            }
        } else {
            fullRecordList.addAll(allRecords);
        }

        if ("Student".equals(userRole)) {
            loadStudentCourseSummaries(fullRecordList);
        } else {
            if (spinnerCourseFilter != null && spinnerCourseFilter.getSelectedItem() != null) {
                filterByCourse(spinnerCourseFilter.getSelectedItem().toString());
            } else {
                loadDataIntoAdapter(fullRecordList);
            }
        }
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
            Snackbar.make(findViewById(android.R.id.content), "Students are not allowed to delete records", Snackbar.LENGTH_SHORT).show();
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
        Snackbar.make(findViewById(android.R.id.content), "Record deleted successfully", Snackbar.LENGTH_SHORT).show();
    }
}
