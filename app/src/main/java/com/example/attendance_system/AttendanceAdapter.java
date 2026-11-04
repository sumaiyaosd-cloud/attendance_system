package com.example.attendance_system;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AttendanceAdapter extends RecyclerView.Adapter<AttendanceAdapter.AttendanceViewHolder> {

    private Context context;
    private List<AttendanceRecord> recordList;
    private List<AttendanceRecord> allRecords;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(AttendanceRecord record);
        void onEditClick(AttendanceRecord record);
        void onDeleteClick(AttendanceRecord record);
    }

    public AttendanceAdapter(Context context, List<AttendanceRecord> recordList, List<AttendanceRecord> allRecords, OnItemClickListener listener) {
        this.context = context;
        this.recordList = recordList;
        this.allRecords = allRecords;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AttendanceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_attendance, parent, false);
        return new AttendanceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AttendanceViewHolder holder, int position) {
        AttendanceRecord record = recordList.get(position);
        holder.tvName.setText(record.getStudentName());
        holder.tvSubject.setText("Course: " + record.getSubject());
        holder.tvDate.setText("Date: " + record.getDate());
        holder.tvStatus.setText(record.getStatus());

        if (record.getStatus() != null && (record.getStatus().equalsIgnoreCase("Present") || record.getStatus().equalsIgnoreCase("P"))) {
            holder.tvStatus.setBackgroundColor(context.getResources().getColor(android.R.color.holo_green_dark));
            holder.tvStatus.setTextColor(context.getResources().getColor(android.R.color.white));
        } else {
            holder.tvStatus.setBackgroundColor(context.getResources().getColor(android.R.color.holo_red_dark));
            holder.tvStatus.setTextColor(context.getResources().getColor(android.R.color.white));
        }

        // Real-life cumulative calculation for this student in this specific course
        int totalClasses = 0;
        int presentClasses = 0;
        if (allRecords != null) {
            for (AttendanceRecord r : allRecords) {
                if (r.getStudentName() != null && r.getStudentName().equalsIgnoreCase(record.getStudentName()) &&
                    r.getSubject() != null && r.getSubject().equalsIgnoreCase(record.getSubject())) {
                    totalClasses++;
                    if (r.getStatus() != null && (r.getStatus().equalsIgnoreCase("Present") || r.getStatus().equalsIgnoreCase("P"))) {
                        presentClasses++;
                    }
                }
            }
        }
        double studentCoursePct = totalClasses > 0 ? ((double) presentClasses / totalClasses) * 100 : 0.0;
        holder.tvPercentage.setText(String.format("Attendance: %.1f%% (%d/%d)", studentCoursePct, presentClasses, totalClasses));
        if (studentCoursePct < 75.0) {
            holder.tvPercentage.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
        } else {
            holder.tvPercentage.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
        }

        if (record.getPhotoUri() != null && !record.getPhotoUri().isEmpty()) {
            try {
                holder.ivPhoto.setImageURI(Uri.parse(record.getPhotoUri()));
            } catch (Exception e) {
                holder.ivPhoto.setImageResource(android.R.drawable.ic_menu_camera);
            }
        } else {
            holder.ivPhoto.setImageResource(android.R.drawable.ic_menu_camera);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(record);
        });
    }

    @Override
    public int getItemCount() {
        return recordList.size();
    }

    public static class AttendanceViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPhoto;
        TextView tvName, tvSubject, tvDate, tvStatus, tvPercentage;

        public AttendanceViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPhoto = itemView.findViewById(R.id.ivItemPhoto);
            tvName = itemView.findViewById(R.id.tvItemName);
            tvSubject = itemView.findViewById(R.id.tvItemSubject);
            tvDate = itemView.findViewById(R.id.tvItemDate);
            tvStatus = itemView.findViewById(R.id.tvItemStatus);
            tvPercentage = itemView.findViewById(R.id.tvItemPercentage);
        }
    }
}
