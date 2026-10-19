package com.katza.myfirstfirebaseapplication;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private EditText etName, etId, etGrade;
    private Button btnInsert, btnSelect, btnUpdate, btnDelete;

    private TextView tvContent;

    FirestoreDAO fsDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        etName = findViewById(R.id.fullname);
        etId = findViewById(R.id.tz);
        etGrade = findViewById(R.id.grade);
        btnInsert = findViewById(R.id.btnInsert);
        btnSelect = findViewById(R.id.btnSelect);
        tvContent = findViewById(R.id.tvcontent);
        btnUpdate = findViewById(R.id.btnUpdate);

        fsDAO = new FirestoreDAO();

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fsDAO.updateDocument(etId.getText().toString(), Integer.parseInt(etGrade.getText().toString()));
            }
        });

        btnInsert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fsDAO.createDocument(etName.getText().toString(),etId.getText().toString(), Integer.parseInt(etGrade.getText().toString()));

            }
        });

        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                   /* fsDAO.readDocumentById(etId.getText().toString(), new IFirestoreCallback() {
                        @Override
                        public void onDataReceived(ArrayList<Student> students) {
                            tvContent.setText(students.get(0).toString());
                        }
                    });
                */
                    tvContent.setText("");
                    fsDAO.readAllDocuments(new IFirestoreCallback() {
                        @Override
                        public void onDataReceived(ArrayList<Student> students) {
                            for(int i=0; i< students.size();i++)
                            {
                                tvContent.setText(tvContent.getText().toString() + students.get(i).toString());
                            }
                        }
                    });


            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        fsDAO.listenToAllDocumentChange();
    }

    @Override
    protected void onStop() {
        super.onStop();
        fsDAO.stopListening();
    }
}