package com.example.attendance_system;

public class AttendanceRecord {
    private String id;
    private String studentName;
    private String subject;
    private String date;
    private String status; // Present / Absent / Late
    private String photoUri;

    public AttendanceRecord() {
        // Required for Firebase Realtime Database
    }

    public AttendanceRecord(String id, String studentName, String subject, String date, String status, String photoUri) {
        this.id = id;
        this.studentName = studentName;
        this.subject = subject;
        this.date = date;
        this.status = status;
        this.photoUri = photoUri;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPhotoUri() {
        return photoUri;
    }

    public void setPhotoUri(String photoUri) {
        this.photoUri = photoUri;
    }
}
