package com.example.attendance_system;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

public class AnnouncementStore {
    private static final String PREF_NAME = "AnnouncementPrefs";
    private static final String KEY_ANNOUNCEMENTS = "announcements_json";

    public static List<String> getAnnouncements(Context context) {
        List<String> list = new ArrayList<>();
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String jsonStr = prefs.getString(KEY_ANNOUNCEMENTS, "[]");
        try {
            JSONArray arr = new JSONArray(jsonStr);
            for (int i = 0; i < arr.length(); i++) {
                list.add(arr.getString(i));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (list.isEmpty()) {
            list.add("Welcome to Attendance Manager! Please check your course schedules regularly.");
        }
        return list;
    }

    public static void addAnnouncement(Context context, String message) {
        List<String> list = getAnnouncements(context);
        list.add(0, message);
        try {
            JSONArray arr = new JSONArray();
            for (String s : list) {
                arr.put(s);
            }
            SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            prefs.edit().putString(KEY_ANNOUNCEMENTS, arr.toString()).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
