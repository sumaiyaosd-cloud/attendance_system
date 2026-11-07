# Attendance Management System

An advanced Android application built using Java and XML, designed for university students and teachers to manage, track, and analyze student attendance in real-time.

## Project Information

- **Project Name**: Attendance Management System
- **Developer**: Sumaiya Akter
- **Student ID**: CS-D-85-22-122932
- **Department**: Computer Science and Engineering
- **University**: Dhaka International University
- **Platform**: Android
- **Programming Language**: Java
- **UI Design**: XML
- **IDE**: Android Studio
- **Database**: Firebase Realtime Database

---

## Features & Requirements Compliance

### Part 1: Modern Material Design UI/UX
- **Color Palette**: Professional Navy Blue (`#1A237E`) and Accent Teal (`#00897B`).
- **Responsive Layouts**: ScrollView and HorizontalScrollView to prevent screen clipping.
- **Separate Dashboards**: Dedicated dashboards for Teachers and Students.
- **Material Design**: Clean and modern Material CardView-based interface.

### Part 2 & 3: Implemented Features & Security
1. **Firebase Authentication**
   - Secure Login and Registration.
   - Logout functionality.
   - Forgot Password feature.
2. **Remember Me**
   - Persistent session saving using SharedPreferences.
3. **Role-Based Access Control**
   - Teachers have full CRUD privileges.
   - Students have secure view-only access.
   - Separate permissions for Teachers and Students.
4. **Attendance Analytics & 75% Rule**
   - Automatic attendance percentage calculation.
   - 75% attendance eligibility checking.
   - Low-attendance warnings.
5. **Search & Filter**
   - Real-time student search by name and subject.
   - Filter attendance by Present and Absent status.
6. **CSV Export**
   - Export attendance records into CSV format.
   - Share exported files using Android Implicit Intents.
7. **Camera & Gallery**
   - Capture photos using the device camera.
   - Select images from the device gallery.
8. **Sensor Integration**
   - Proximity Sensor integration using Android SensorManager.
9. **Feedback System**
   - Snackbar and Toast notifications for user actions.
   - User-friendly success and error messages.

---

## Part 4: 12 Mandatory Lab Requirements

| No. | Requirement | Implementation |
| :--- | :--- | :--- |
| 1 | 5+ Activities | SplashActivity, LoginActivity, RegisterActivity, HomeActivity, AddEditActivity, ProfileActivity |
| 2 | Layouts & Basic Views | ConstraintLayout, LinearLayout, CardView, TextView, ImageView, Button, TextInputEditText |
| 3 | Input Validation | setError() for empty or invalid inputs |
| 4 | Model Class | AttendanceRecord.java with getters and setters |
| 5 | RecyclerView | AttendanceAdapter and ViewHolder |
| 6 | CRUD Operations | Add, View, Edit and Delete using AlertDialog and FAB |
| 7 | Firebase Authentication | Login, Registration, Logout and Password Reset |
| 8 | Firebase Realtime Database | Live updates using ValueEventListener |
| 9 | SharedPreferences | Persistent settings and Remember Me |
| 10 | Camera & Gallery | Camera and image selection intents |
| 11 | Sensors | Proximity Sensor using SensorEventListener |
| 12 | Implicit Intents | ACTION_DIAL, ACTION_SEND and ACTION_VIEW |

---

## Technologies Used

- Java
- XML
- Android Studio
- Firebase Authentication
- Firebase Realtime Database
- SharedPreferences
- RecyclerView
- CardView
- Material Design Components
- Android Camera and Gallery API
- SensorManager
- Implicit Intents

---

## Firebase Configuration & Setup Instructions

### Step 1: Create a Firebase Project
1. Visit the Firebase Console: [https://console.firebase.google.com/](https://console.firebase.google.com/)
2. Create a new Firebase project.

### Step 2: Add Android App
1. Register your Android application.
2. Use the package name: `com.example.attendance_system`

### Step 3: Download `google-services.json`
1. Download the `google-services.json` configuration file.
2. Place it inside the `app/` directory.
   - Expected location: `app/google-services.json`

### Step 4: Enable Authentication & Database
1. Enable Email/Password authentication in Firebase Authentication.
2. Create a Firebase Realtime Database.
3. Configure the database security rules appropriately.

### Step 5: Sync & Run
1. Open the project in Android Studio.
2. Sync Gradle files.
3. Connect an Android device or start an emulator.
4. Run the application.

---

## How to Run the Application

1. Clone or download this repository.
2. Open the project in Android Studio.
3. Wait for Gradle Sync to complete.
4. Configure Firebase using your `google-services.json` file.
5. Connect an Android device or emulator.
6. Click the Run button.

---

## GitHub Repository

- **Repository**: [https://github.com/sumaiyaosd-cloud/attendance_system](https://github.com/sumaiyaosd-cloud/attendance_system)

---

## Developer

- **Sumaiya Akter**
- **Student ID**: CS-D-85-22-122932
- **Department of Computer Science and Engineering**
- **Dhaka International University**
- **Project**: Attendance Management System
