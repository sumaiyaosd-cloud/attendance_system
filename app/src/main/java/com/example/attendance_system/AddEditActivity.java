package com.example.attendance_system;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Spinner;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class AddEditActivity extends AppCompatActivity {

    private ImageView ivPhoto;
    private Button btnCamera, btnGallery, btnSave;
    private TextInputEditText etStudentName, etDate;
    private Spinner spinnerCourses;
    private RadioButton rbPresent, rbLate, rbAbsent;

    private DatabaseReference dbRef;
    private String recordId = "";
    private String photoUriStr = "";
    private List<Course> courseList;

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_PICK = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit);

        ivPhoto = findViewById(R.id.ivPhoto);
        btnCamera = findViewById(R.id.btnCamera);
        btnGallery = findViewById(R.id.btnGallery);
        btnSave = findViewById(R.id.btnSave);
        etStudentName = findViewById(R.id.etStudentName);
        etDate = findViewById(R.id.etDate);
        spinnerCourses = findViewById(R.id.spinnerCourses);
        rbPresent = findViewById(R.id.rbPresent);
        rbLate = findViewById(R.id.rbLate);
        rbAbsent = findViewById(R.id.rbAbsent);

        try {
            dbRef = FirebaseDatabase.getInstance().getReference("attendance_records");
        } catch (Exception e) {
            dbRef = null;
        }

        courseList = CourseStore.getCourses(this);
        String[] courseNames = new String[courseList.size()];
        for (int i = 0; i < courseList.size(); i++) {
            courseNames[i] = courseList.get(i).getCourseName() + " (" + courseList.get(i).getCourseCode() + ")";
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, courseNames);
        spinnerCourses.setAdapter(adapter);

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("preselected_subject")) {
            String preSubject = intent.getStringExtra("preselected_subject");
            for (int i = 0; i < courseList.size(); i++) {
                if (courseList.get(i).getCourseName().equalsIgnoreCase(preSubject)) {
                    spinnerCourses.setSelection(i);
                    break;
                }
            }
        }

        final Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        etDate.setText(sdf.format(calendar.getTime()));

        etDate.setOnClickListener(v -> {
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, selectedYear, selectedMonth, selectedDay) -> {
                Calendar selectedCalendar = Calendar.getInstance();
                selectedCalendar.set(selectedYear, selectedMonth, selectedDay);
                if (selectedCalendar.after(Calendar.getInstance())) {
                    Snackbar.make(etDate, "Future date attendance cannot be taken!", Snackbar.LENGTH_LONG).show();
                    return;
                }
                String formattedDate = String.format(Locale.getDefault(), "%d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
                etDate.setText(formattedDate);
            }, year, month, day);

            datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
            datePickerDialog.show();
        });

        if (intent != null && intent.hasExtra("id")) {
            recordId = intent.getStringExtra("id");
            etStudentName.setText(intent.getStringExtra("name"));
            etDate.setText(intent.getStringExtra("date"));
            String subject = intent.getStringExtra("subject");
            for (int i = 0; i < courseList.size(); i++) {
                if (courseList.get(i).getCourseName().equalsIgnoreCase(subject)) {
                    spinnerCourses.setSelection(i);
                    break;
                }
            }
            String status = intent.getStringExtra("status");
            if (status != null && status.equalsIgnoreCase("Absent")) {
                if (rbAbsent != null) rbAbsent.setChecked(true);
            } else if (status != null && status.equalsIgnoreCase("Late")) {
                if (rbLate != null) rbLate.setChecked(true);
            } else {
                rbPresent.setChecked(true);
            }
            photoUriStr = intent.getStringExtra("photoUri");
            if (photoUriStr != null && !photoUriStr.isEmpty()) {
                try {
                    ivPhoto.setImageURI(Uri.parse(photoUriStr));
                } catch (Exception e) {
                    ivPhoto.setImageResource(android.R.drawable.ic_menu_camera);
                }
            }
        }

        btnCamera.setOnClickListener(v -> {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            } else {
                Snackbar.make(btnCamera, "Camera not available", Snackbar.LENGTH_SHORT).show();
            }
        });

        btnGallery.setOnClickListener(v -> {
            Intent pickPhotoIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(pickPhotoIntent, REQUEST_IMAGE_PICK);
        });

        btnSave.setOnClickListener(v -> saveRecord());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                Bundle extras = data.getExtras();
                if (extras != null) {
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    ivPhoto.setImageBitmap(imageBitmap);
                    photoUriStr = getImageUriFromBitmap(imageBitmap);
                }
            } else if (requestCode == REQUEST_IMAGE_PICK) {
                Uri selectedImageUri = data.getData();
                if (selectedImageUri != null) {
                    ivPhoto.setImageURI(selectedImageUri);
                    photoUriStr = selectedImageUri.toString();
                }
            }
        }
    }

    private String getImageUriFromBitmap(Bitmap bitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "AttendancePhoto", null);
        return path != null ? Uri.parse(path).toString() : "";
    }

    private void saveRecord() {
        String name = etStudentName.getText() != null ? etStudentName.getText().toString().trim() : "";
        int selectedCourseIndex = spinnerCourses.getSelectedItemPosition();
        String subject = selectedCourseIndex >= 0 && selectedCourseIndex < courseList.size() ? courseList.get(selectedCourseIndex).getCourseName() : "Software Engineering";
        String date = etDate.getText() != null ? etDate.getText().toString().trim() : "";
        String status = rbPresent.isChecked() ? "Present" : (rbLate.isChecked() ? "Late" : "Absent");

        if (name.isEmpty()) {
            etStudentName.setError("Student name is required");
            etStudentName.requestFocus();
            return;
        }

        if (date.isEmpty()) {
            etDate.setError("Date is required");
            etDate.requestFocus();
            return;
        }

        SharedPreferences prefs = getSharedPreferences("AttendancePrefs", MODE_PRIVATE);
        String teacherEmail = prefs.getString("logged_email", "teacher@university.edu");

        List<AttendanceRecord> existingRecords = AttendanceLocalStore.getRecords(this);
        for (AttendanceRecord r : existingRecords) {
            if (r.getStudentName().equalsIgnoreCase(name) &&
                r.getSubject().equalsIgnoreCase(subject) &&
                r.getDate().equalsIgnoreCase(date)) {
                if (recordId.isEmpty()) {
                    recordId = r.getId();
                }
                break;
            }
        }

        if (recordId == null || recordId.isEmpty()) {
            recordId = UUID.randomUUID().toString();
        }

        AttendanceRecord record = new AttendanceRecord(recordId, name, subject, date, status, photoUriStr, teacherEmail);

        AttendanceLocalStore.saveRecord(this, record);

        if (dbRef != null) {
            try {
                dbRef.child(recordId).setValue(record);
            } catch (Exception ignored) {
            }
        }

        Snackbar.make(btnSave, "Attendance Saved Successfully!", Snackbar.LENGTH_SHORT).show();
        finish();
    }
}
