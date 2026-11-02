package com.katza.myfirstfirebaseapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {

    TextView tvGoToSignIn;
    EditText etRegisterMail, etRegisterPass, etRegisterName;
    Button btnRegister;
    String USER_COLLECTION_NAME = "users";
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        tvGoToSignIn = findViewById(R.id.tvGoToSignIn);
        etRegisterMail = findViewById(R.id.etRegisterMail);
        etRegisterPass = findViewById(R.id.etRegisterPass);
        etRegisterName = findViewById(R.id.etRegisterName);
        btnRegister = findViewById(R.id.btnRegister);

        tvGoToSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RegisterActivity.this,SignInActivity.class);
                startActivity(intent);
                finish();
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerUser();
            }
        });
    }

    private void registerUser() {
        String email = etRegisterMail.getText().toString().trim();
        String password = etRegisterPass.getText().toString().trim();
        String name = etRegisterName.getText().toString().trim();

        if(name.isEmpty()||password.isEmpty()||email.isEmpty()){
            Toast.makeText(this, "אחד או יותר מהשדות ריקים", Toast.LENGTH_SHORT).show();
            return;
        }
        if(password.length()<6){
            Toast.makeText(this, "הסיסמה חייבת לכלול לפחות 6 תווים", Toast.LENGTH_SHORT).show();
            return;
        }
        mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    FirebaseUser user = mAuth.getCurrentUser();
                    if(user!=null){
                        saveUserToFirestroe(user.getUid(), name, email);
                    }else{
                        Toast.makeText(RegisterActivity.this, "Authentcation error", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(RegisterActivity.this, "שגיאה ביצירת חשבון", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveUserToFirestroe(String uid, String name, String email) {
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("name",name);
        userMap.put("email",email);

        db.collection(USER_COLLECTION_NAME).document(uid).set(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toast.makeText(RegisterActivity.this, "החשבון נוצר", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                FirebaseUser currentUser = mAuth.getCurrentUser();
                if (currentUser != null) {
                    currentUser.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> deleteTask) {
                            if (deleteTask.isSuccessful()) {
                                Toast.makeText(RegisterActivity.this, "שגיאה בחיבור. אננסה שוב", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(RegisterActivity.this, "שגיאה במחיקת חשבון Auth: " + deleteTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } else {
                    Toast.makeText(RegisterActivity.this, "שגיאה בשמירת נתוני המשתמש", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}