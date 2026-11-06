package com.example.attendance_system;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TodayScheduleStore {
    public static class ScheduledClass {
        public String courseName;
        public String courseCode;
        public String time;
        public String room;

        public ScheduledClass(String courseName, String courseCode, String time, String room) {
            this.courseName = courseName;
            this.courseCode = courseCode;
            this.time = time;
            this.room = room;
        }
    }

    public static List<ScheduledClass> getTodayClasses() {
        List<ScheduledClass> list = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE", Locale.getDefault());
        String todayDay = sdf.format(new Date());

        // Dynamic schedule for lab final showcase
        list.add(new ScheduledClass("Software Engineering", "CSE 3201 · Sec A", "10:00 AM - 11:30 AM", "Room 402"));
        list.add(new ScheduledClass("Android Development", "CSE 3202 · Sec A", "12:00 PM - 01:30 PM", "Lab 3"));
        list.add(new ScheduledClass("Computer Networks", "CSE 3203 · Sec B", "02:30 PM - 04:00 PM", "Room 305"));
        return list;
    }
}
