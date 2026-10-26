package com.katza.myfirstfirebaseapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class StudentAdapter extends ArrayAdapter<Student> {
    public StudentAdapter(@NonNull Context context, @NonNull List<Student> students) {
        super(context, 0, students);
    }

    @NonNull
    @Override

    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_student, parent, false);
        }
        Student student = getItem(position);

        TextView tvName = convertView.findViewById(R.id.tvStudentName);
        TextView tvId = convertView.findViewById(R.id.tvStudentId);
        TextView tvGrade = convertView.findViewById(R.id.tvStudentGrade);
        ImageView ivProfile = convertView.findViewById(R.id.ivStudentImage);

        if (student != null) {
            tvName.setText(student.getFullName());
            tvId.setText("ת.ז: " + student.getId());
            tvGrade.setText("ציון: " + student.getGrade());

            // המרת מחרוזת ה-Base64 ל-Bitmap בעזרת מחלקת ה-ImageUtils שלך
            String base64Image = student.getData(); // יש להתאים ל-getter שבמחלקת Student
            if (base64Image != null && !base64Image.isEmpty()) {
                Bitmap bitmap = ImageUtils.convertBase64ToBitmap(base64Image);
                ivProfile.setImageBitmap(bitmap);
            } else {
                ivProfile.setImageResource(R.drawable.img); // תמונת ברירת מחדל
            }
        }

        return convertView;

    }
}