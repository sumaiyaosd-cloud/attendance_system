package com.example.attendance_system;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class CourseStore {
    private static final String PREF_NAME = "CoursePrefs";
    private static final String KEY_COURSES = "courses_json";

    public static List<Course> getCourses(Context context) {
        List<Course> list = new ArrayList<>();
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String jsonStr = prefs.getString(KEY_COURSES, "");

        if (jsonStr.isEmpty()) {
            list.add(new Course("1", "Software Engineering", "CSE 3201 · Section A"));
            list.add(new Course("2", "Android Development", "CSE 3202 · Section A"));
            list.add(new Course("3", "Computer Networks", "CSE 3203 · Section B"));
            list.add(new Course("4", "Artificial Intelligence", "CSE 3204 · Section A"));
            saveCourses(context, list);
            return list;
        }

        try {
            JSONArray arr = new JSONArray(jsonStr);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                list.add(new Course(obj.optString("id"), obj.optString("courseName"), obj.optString("courseCode")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public static void saveCourses(Context context, List<Course> list) {
        try {
            JSONArray arr = new JSONArray();
            for (Course c : list) {
                JSONObject obj = new JSONObject();
                obj.put("id", c.getId());
                obj.put("courseName", c.getCourseName());
                obj.put("courseCode", c.getCourseCode());
                arr.put(obj);
            }
            SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            prefs.edit().putString(KEY_COURSES, arr.toString()).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void addCourse(Context context, Course course) {
        List<Course> list = getCourses(context);
        list.add(course);
        saveCourses(context, list);
    }
}
