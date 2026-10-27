# Implementation Plan - Attendance Management System

A complete Android application built in **Java and XML** that solves the real problem of student/employee attendance tracking, featuring all 12 required features, Firebase authentication and real-time database, sensors, camera/gallery, implicit intents, SharedPreferences, and Material Design.

## User Review Required

> [!IMPORTANT]
> - **Language**: Java for all source logic (`.java`), XML for layouts (`.xml`), and Gradle build configurations.
> - **Firebase**: Uses Firebase Authentication and Firebase Realtime Database. (Will include a clean setup guide in README for Firebase project connection).
> - **Activities**: 5 distinct Activities (`SplashActivity`, `LoginActivity`, `RegisterActivity`, `HomeActivity`, `AddEditActivity`).

## Proposed Changes

### Build Configuration & Manifest
#### [MODIFY] [build.gradle.kts](file:///C:/Users/HP/AndroidStudioProjects/attendance_system/app/build.gradle.kts)
- Add Firebase BOM and Realtime Database / Auth dependencies.
- Enable ViewBinding / view support for XML layouts.

#### [MODIFY] [AndroidManifest.xml](file:///C:/Users/HP/AndroidStudioProjects/attendance_system/app/src/main/AndroidManifest.xml)
- Register all 5 Activities (`SplashActivity`, `LoginActivity`, `RegisterActivity`, `HomeActivity`, `AddEditActivity`).
- Add permissions for Camera, Internet, and Sensor access.
- Set application theme to Material Components.

### Data Models & Adapters
#### [NEW] [AttendanceRecord.java](file:///C:/Users/HP/AndroidStudioProjects/attendance_system/app/src/main/java/com/example/attendance_system/AttendanceRecord.java)
- Java model class with getters, setters, default constructor, and fields: `id`, `studentName`, `subject`, `date`, `status`, `photoUri`.

#### [NEW] [AttendanceAdapter.java](file:///C:/Users/HP/AndroidStudioProjects/attendance_system/app/src/main/java/com/example/attendance_system/AttendanceAdapter.java)
- RecyclerView Adapter and ViewHolder for displaying attendance records in CardViews with click listeners for Edit, Delete, and View.

### Activities & UI (XML Layouts + Java Activities)
#### [NEW] [SplashActivity.java & activity_splash.xml](file:///C:/Users/HP/AndroidStudioProjects/attendance_system/app/src/main/java/com/example/attendance_system/SplashActivity.java)
- Screen 1: Splash screen checking Firebase current user and SharedPreferences settings.

#### [NEW] [LoginActivity.java & activity_login.xml](file:///C:/Users/HP/AndroidStudioProjects/attendance_system/app/src/main/java/com/example/attendance_system/LoginActivity.java)
- Screen 2: Firebase login with email/password input validation (`TextInputEditText` + `setError()`).

#### [NEW] [RegisterActivity.java & activity_register.xml](file:///C:/Users/HP/AndroidStudioProjects/attendance_system/app/src/main/java/com/example/attendance_system/RegisterActivity.java)
- Screen 3: Firebase user registration with validation.

#### [NEW] [HomeActivity.java & activity_home.xml](file:///C:/Users/HP/AndroidStudioProjects/attendance_system/app/src/main/java/com/example/attendance_system/HomeActivity.java)
- Screen 4: Dashboard with RecyclerView of real-time attendance records from Firebase Realtime Database, FloatingActionButton to add record, SharedPreferences settings manager, Proximity Sensor listener, and Implicit Intents (Dial, Share, View).

#### [NEW] [AddEditActivity.java & activity_add_edit.xml](file:///C:/Users/HP/AndroidStudioProjects/attendance_system/app/src/main/java/com/example/attendance_system/AddEditActivity.java)
- Screen 5: Form to Add/Edit attendance record, pick or capture photo with camera/gallery, and save to Firebase Realtime Database.

## Verification Plan

### Automated Tests
- Run Gradle build (`./gradlew assembleDebug`) to verify compilation without errors.

### Manual Verification
- Deploy app to emulator/device.
- Test registration, login, logout, and stay logged in.
- Test adding, editing, viewing, and deleting attendance records (CRUD).
- Test camera photo capture/gallery pick, SharedPreferences settings, Proximity sensor reaction, and implicit intents (dial, share, web link).
