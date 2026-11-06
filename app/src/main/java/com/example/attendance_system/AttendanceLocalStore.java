package com.example.attendance_system;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class AttendanceLocalStore {
    private static final String PREF_NAME = "LocalAttendancePrefs";
    private static final String KEY_RECORDS = "records_json";

    public static List<AttendanceRecord> getRecords(Context context) {
        List<AttendanceRecord> list = new ArrayList<>();
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String jsonStr = prefs.getString(KEY_RECORDS, "[]");
        try {
            JSONArray arr = new JSONArray(jsonStr);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                AttendanceRecord r = new AttendanceRecord(
                        obj.optString("id"),
                        obj.optString("studentName"),
                        obj.optString("subject"),
                        obj.optString("date"),
                        obj.optString("status"),
                        obj.optString("photoUri"),
                        obj.optString("teacherEmail", "")
                );
                list.add(r);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public static void saveRecord(Context context, AttendanceRecord record) {
        List<AttendanceRecord> list = getRecords(context);
        boolean found = false;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getId().equals(record.getId())) {
                list.set(i, record);
                found = true;
                break;
            }
        }
        if (!found) {
            list.add(record);
        }
        saveAll(context, list);
    }

    public static void deleteRecord(Context context, String id) {
        List<AttendanceRecord> list = getRecords(context);
        list.removeIf(r -> r.getId().equals(id));
        saveAll(context, list);
    }

    private static void saveAll(Context context, List<AttendanceRecord> list) {
        try {
            JSONArray arr = new JSONArray();
            for (AttendanceRecord r : list) {
                JSONObject obj = new JSONObject();
                obj.put("id", r.getId());
                obj.put("studentName", r.getStudentName());
                obj.put("subject", r.getSubject());
                obj.put("date", r.getDate());
                obj.put("status", r.getStatus());
                obj.put("photoUri", r.getPhotoUri());
                obj.put("teacherEmail", r.getTeacherEmail() != null ? r.getTeacherEmail() : "");
                arr.put(obj);
            }
            SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            prefs.edit().putString(KEY_RECORDS, arr.toString()).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
