# Attendance Management System

An advanced Android application built in **Java and XML** designed for university students and teachers to manage, track, and analyze student attendance in real-time.

---

## Features & Requirements Compliance

### Part 1: Modern Material Design UI/UX
- **Color Palette**: Professional Navy Blue (`#1A237E`) and Accent Teal (`#00897B`).
- **Responsive Layouts**: Fully scrollable views with `ScrollView` and `HorizontalScrollView` to prevent screen clipping.
- **Dashboards**: Separate, tailored dashboards for Teachers and Students with clean Material CardViews.

### Part 2 & 3: Implemented Features & Security
- **Firebase Authentication**: Secure Login, Registration, Logout, and Forgot Password features.
- **Remember Me**: Persistent session saving via `SharedPreferences`.
- **Role-Based Access Control**: Teachers have full CRUD privileges (Add, Edit, Delete, CSV Export, Analytics), while Students have secure view-only access.
- **Attendance Analytics & 75% Rule**: Automatic percentage calculation and 75% eligibility checking with low-attendance warnings.
- **Search & Filter**: Real-time student search by name/subject and status filtering (Present / Absent).
- **CSV Export**: Export attendance records to CSV format and share via Implicit Intents.
- **Camera & Gallery**: Photo capture and gallery image selection.
- **Sensors**: Proximity sensor integration (`SensorManager`).
- **Feedback**: Snackbar and Toast feedback for all user actions.

### Part 4: 12 Mandatory Lab Requirements
1. **5+ Activities**: `SplashActivity`, `LoginActivity`, `RegisterActivity`, `HomeActivity`, `AddEditActivity`.
2. **Layouts & Basic Views**: ConstraintLayout, LinearLayout, CardView, TextView, ImageView, Button, TextInputEditText.
3. **User Input Validation**: `.setError()` on empty/invalid inputs.
4. **Model Class**: `AttendanceRecord.java` with getters and setters.
5. **RecyclerView**: `AttendanceAdapter` and `ViewHolder`.
6. **CRUD Operations**: Complete Add, View, Edit, and Delete (`AlertDialog`, `FAB`).
7. **Firebase Auth**: Login, register, logout, password reset.
8. **Firebase Realtime Database**: Live updates via `ValueEventListener`.
9. **SharedPreferences**: Persistent settings and Remember Me.
10. **Camera & Gallery**: Image intents.
11. **Sensors**: Proximity sensor (`SensorEventListener`).
12. **Implicit Intents**: ACTION_DIAL, ACTION_SEND, ACTION_VIEW.

---

## Firebase Configuration & Setup Instructions

1. **Create Firebase Project**: Go to the [Firebase Console](https://console.firebase.google.com/).
2. **Add Android App**: Register your app with package name `com.example.attendance_system`.
3. **Download google-services.json**: Place the downloaded file into the `app/` folder of the project:
   `app/google-services.json`
4. **Enable Auth & Database**: Enable Email/Password authentication and Firebase Realtime Database in test mode.
5. **Sync & Run**: Open the project in Android Studio, sync Gradle, and run on an emulator or physical device.
