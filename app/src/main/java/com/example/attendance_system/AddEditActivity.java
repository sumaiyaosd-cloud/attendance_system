package com.example.attendance_system;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayOutputStream;
import java.util.UUID;

public class AddEditActivity extends AppCompatActivity {

    private ImageView ivPhoto;
    private Button btnCamera, btnGallery, btnSave;
    private TextInputEditText etStudentName, etSubject, etDate, etStatus;

    private DatabaseReference dbRef;
    private String recordId = "";
    private String photoUriStr = "";

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
        etSubject = findViewById(R.id.etSubject);
        etDate = findViewById(R.id.etDate);
        etStatus = findViewById(R.id.etStatus);

        dbRef = FirebaseDatabase.getInstance().getReference("attendance_records");

        // Check if editing existing record
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("id")) {
            recordId = intent.getStringExtra("id");
            etStudentName.setText(intent.getStringExtra("name"));
            etSubject.setText(intent.getStringExtra("subject"));
            etDate.setText(intent.getStringExtra("date"));
            etStatus.setText(intent.getStringExtra("status"));
            photoUriStr = intent.getStringExtra("photoUri");
            if (photoUriStr != null && !photoUriStr.isEmpty()) {
                try {
                    ivPhoto.setImageURI(Uri.parse(photoUriStr));
                } catch (Exception e) {
                    ivPhoto.setImageResource(android.R.drawable.ic_menu_camera);
                }
            }
        }

        // Feature 10: Take photo with camera intent
        btnCamera.setOnClickListener(v -> {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            } else {
                Toast.makeText(this, "Camera not available", Toast.LENGTH_SHORT).show();
            }
        });

        // Feature 10: Pick photo from gallery intent
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
                    // Convert bitmap to URI string representation for storage
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
        String subject = etSubject.getText() != null ? etSubject.getText().toString().trim() : "";
        String date = etDate.getText() != null ? etDate.getText().toString().trim() : "";
        String status = etStatus.getText() != null ? etStatus.getText().toString().trim() : "";

        // Feature 3: User input with validation (no crash on empty/wrong input)
        if (name.isEmpty()) {
            etStudentName.setError("Student name is required");
            etStudentName.requestFocus();
            return;
        }

        if (subject.isEmpty()) {
            etSubject.setError("Subject is required");
            etSubject.requestFocus();
            return;
        }

        if (date.isEmpty()) {
            etDate.setError("Date is required");
            etDate.requestFocus();
            return;
        }

        if (status.isEmpty()) {
            status = "Present";
        }

        if (recordId == null || recordId.isEmpty()) {
            recordId = dbRef.push().getKey();
        }

        AttendanceRecord record = new AttendanceRecord(recordId, name, subject, date, status, photoUriStr);

        dbRef.child(recordId).setValue(record).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(AddEditActivity.this, "Attendance Saved Successfully!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(AddEditActivity.this, "Failed to save record", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
