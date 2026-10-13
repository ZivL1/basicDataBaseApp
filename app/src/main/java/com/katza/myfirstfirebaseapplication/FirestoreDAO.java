package com.katza.myfirstfirebaseapplication;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FirestoreDAO { //data access object
    private static final String COLLECTION_NAME = "students";

    public void createDocument(String name, String id, int grade)
    {
        Map<String, Object> studentRecord = new HashMap<>();
        studentRecord.put("fullName", name);
        studentRecord.put("id",id);
        studentRecord.put("grade",grade);

        DocumentReference docRef = FirebaseFirestore.getInstance().collection(COLLECTION_NAME).document(id);
        docRef.set(studentRecord).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.d("DAR_FIREBASE","SUCCESSFUL_WRITE");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("DAR_FIREBASE",e.getMessage().toString());
            }
        });

    }

    public void readDocumentById(String docID, IFirestoreCallback IFirestoreCallback)
    {
        DocumentReference docRef = FirebaseFirestore.getInstance().collection(COLLECTION_NAME).document(docID);
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Log.d("DAR_FIREBASE","SUCCESSFUL_READ");
                Student student = documentSnapshot.toObject(Student.class);
                Log.d("DAR_FIREBASE", ""+student);

                ArrayList<Student> students = new ArrayList<>();
                students.add(student);
                IFirestoreCallback.onDataReceived(students);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("DAR_FIREBASE",e.getMessage().toString());

            }
        });

    }

    public void readAllDocuments(IFirestoreCallback IFirestoreCallback)
    {
        CollectionReference colRef = FirebaseFirestore.getInstance().collection(COLLECTION_NAME);
        colRef.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                ArrayList<Student> students = new ArrayList<>();
                for(DocumentSnapshot docSS : queryDocumentSnapshots.getDocuments()){
                    Student student = docSS.toObject(Student.class);
                    students.add(student);
                }
                IFirestoreCallback.onDataReceived(students);
            }

        });
    }
    public void updateDocument(String docID, int newGrade)
    {
        DocumentReference docRef = FirebaseFirestore.getInstance().collection(COLLECTION_NAME).document(docID);
        docRef.update("grade",newGrade).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.d("DAR_FIREBASE","new grade: " + newGrade);
            }
        });
    }
}
