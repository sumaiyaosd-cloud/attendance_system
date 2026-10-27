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
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(AttendanceRecord record);
        void onEditClick(AttendanceRecord record);
        void onDeleteClick(AttendanceRecord record);
    }

    public AttendanceAdapter(Context context, List<AttendanceRecord> recordList, OnItemClickListener listener) {
        this.context = context;
        this.recordList = recordList;
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
        holder.tvSubject.setText("Subject: " + record.getSubject());
        holder.tvDate.setText("Date: " + record.getDate());
        holder.tvStatus.setText(record.getStatus());

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
        TextView tvName, tvSubject, tvDate, tvStatus;

        public AttendanceViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPhoto = itemView.findViewById(R.id.ivItemPhoto);
            tvName = itemView.findViewById(R.id.tvItemName);
            tvSubject = itemView.findViewById(R.id.tvItemSubject);
            tvDate = itemView.findViewById(R.id.tvItemDate);
            tvStatus = itemView.findViewById(R.id.tvItemStatus);
        }
    }
}
