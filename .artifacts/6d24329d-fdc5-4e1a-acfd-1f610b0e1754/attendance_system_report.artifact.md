# Attendance Management System - Final Lab Project Report

## Overview
The **Attendance Management System** is a professional Android application built in **Java and XML** with **Firebase Authentication**, **Firebase Realtime Database**, and **Local Dual Storage persistence**. It fully satisfies all university final lab project instructions and mandatory requirements.

---

## Part 1: UI/UX Improvements & Material Design
- **Color Palette**: Professional Navy Blue (`#1A237E`), Dark Navy (`#0D47A1`), and Accent Teal (`#00897B`).
- **Cards & Layouts**: Rounded Material CardViews (`16dp` radius), clean input fields (`TextInputEditText`), responsive layouts with `ScrollView` and `HorizontalScrollView` to prevent screen clipping on any device.
- **Dashboards**: Dedicated **Teacher Dashboard** (with FAB for adding attendance, edit, delete, search, and filter) and **Student Dashboard** (with real-time attendance summary, percentage calculation, 75% eligibility checking, and low attendance warnings).

---

## Part 2 & 3: Implemented Features & Role-Based Access Control

### Mandatory 12 Lab Requirements:
1. **5+ Activities**: `SplashActivity`, `LoginActivity`, `RegisterActivity`, `HomeActivity`, `AddEditActivity` with explicit intents (`putExtra()`).
2. **Layouts & Views**: ConstraintLayout, LinearLayout, CardView, TextView, ImageView, Button, TextInputEditText, RecyclerView, FAB.
3. **User Input Validation**: `.setError()` validation on empty or invalid inputs across login, register, and add/edit forms.
4. **Model Class**: `AttendanceRecord.java` with getters, setters, and default constructor.
5. **Scrolling List**: `RecyclerView` with custom `AttendanceAdapter` and `ViewHolder`.
6. **CRUD Operations**: Complete Add, View, Edit, and Delete using `AlertDialog` and FloatingActionButton.
7. **Firebase Authentication**: Register, login, logout, and persistent session ("Remember Me" via `SharedPreferences`).
8. **Firebase Realtime Database**: Real-time synchronization using `ValueEventListener` + dual storage fallback (`AttendanceLocalStore`).
9. **Phone Settings**: `SharedPreferences` storing user credentials, role, and preferences.
10. **Camera & Gallery**: Photo capture (`MediaStore.ACTION_IMAGE_CAPTURE`) and gallery picking (`Intent.ACTION_PICK`).
11. **Sensors**: Proximity sensor (`SensorManager` + `SensorEventListener`) with real-time feedback.
12. **Implicit Intents**: `ACTION_DIAL` (Call Help), `ACTION_SEND` (Share attendance summary), and `ACTION_VIEW` (Web policy).

### Advanced Features:
- **Search & Filter**: Search students instantly by name and filter by Present/Absent status.
- **Attendance Percentage & 75% Eligibility**: Automatic calculation of attendance percentage (`(present / total) * 100`) with a low-attendance warning banner when attendance falls below 75%.
- **Role-Based Security**: Teachers can manage records (Add/Edit/Delete); Students have secure view-only access to their summary and records.

---

## Firebase Configuration & Database Structure

### 1. `google-services.json`
Place your `google-services.json` file (downloaded from the [Firebase Console](https://console.firebase.google.com/)) inside the `app/` folder:
`app/google-services.json`

### 2. Firebase Database Structure
```json
{
  "attendance_records": {
    "record_id_1": {
      "id": "record_id_1",
      "studentName": "John Doe",
      "subject": "Android Development",
      "date": "2025-05-15",
      "status": "Present",
      "photoUri": "content://..."
    }
  },
  "users": {
    "user_uid_1": {
      "role": "Teacher"
    }
  }
}
```

---

## README.md for GitHub

```markdown
# Attendance Management System

An Android application built in **Java and XML** featuring Firebase Authentication, Realtime Database, Material Design UI, Camera/Gallery integration, sensors, and implicit intents.

## Features
- Teacher & Student Dashboards with Role-Based Access Control
- 5 Activities with Intent Navigation
- Real-time Attendance Tracking & CRUD (Add, View, Edit, Delete)
- Attendance Percentage & 75% Eligibility Checker
- Student Name Search & Status Filtering
- SharedPreferences "Remember Me"
- Camera & Gallery Image Capture
- Proximity Sensor Support
- Implicit Intents (Call, Share, Web)

## How to Run
1. Open the project in Android Studio.
2. Add your `google-services.json` to the `app/` directory.
3. Sync Gradle and run the app on an emulator or physical device.
```
