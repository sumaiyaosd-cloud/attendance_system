package com.example.attendance_system;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CourseSummaryAdapter extends RecyclerView.Adapter<CourseSummaryAdapter.ViewHolder> {

    private final Context context;
    private final List<CourseSummary> summaryList;
    private final OnCourseClickListener listener;

    public interface OnCourseClickListener {
        void onCourseClick(CourseSummary summary);
    }

    public static class CourseSummary {
        public String courseName;
        public int total;
        public int present;
        public int absent;
        public double percentage;
        public List<AttendanceRecord> records;
    }

    public CourseSummaryAdapter(Context context, List<CourseSummary> summaryList, OnCourseClickListener listener) {
        this.context = context;
        this.summaryList = summaryList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_student_course, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CourseSummary cs = summaryList.get(position);
        holder.tvCourseName.setText(cs.courseName);
        holder.tvCoursePercentage.setText(String.format("%.1f%%", cs.percentage));
        holder.tvCourseStats.setText(String.format("Present: %d | Absent: %d (Total: %d)", cs.present, cs.absent, cs.total));
        holder.progressBarCourse.setProgress((int) cs.percentage);

        if (cs.percentage >= 75.0) {
            holder.tvCourseEligibility.setText("Status: ELIGIBLE ✅");
            holder.tvCourseEligibility.setBackgroundColor(0xFFE8F5E9);
            holder.tvCourseEligibility.setTextColor(0xFF2E7D32);
        } else {
            holder.tvCourseEligibility.setText("Status: NOT ELIGIBLE ❌");
            holder.tvCourseEligibility.setBackgroundColor(0xFFFFEBEE);
            holder.tvCourseEligibility.setTextColor(0xFFC62828);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onCourseClick(cs);
        });
    }

    @Override
    public int getItemCount() {
        return summaryList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCourseName, tvCoursePercentage, tvCourseStats, tvCourseEligibility;
        ProgressBar progressBarCourse;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCourseName = itemView.findViewById(R.id.tvCourseName);
            tvCoursePercentage = itemView.findViewById(R.id.tvCoursePercentage);
            tvCourseStats = itemView.findViewById(R.id.tvCourseStats);
            tvCourseEligibility = itemView.findViewById(R.id.tvCourseEligibility);
            progressBarCourse = itemView.findViewById(R.id.progressBarCourse);
        }
    }
}
