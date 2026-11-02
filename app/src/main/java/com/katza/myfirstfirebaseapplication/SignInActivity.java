package com.katza.myfirstfirebaseapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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

public class SignInActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    private String mCustomToken;
    private EditText etMail, etPass;
    private Button btnSignIn;

    private TextView tvToRegister, tvSignInStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_in);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        mAuth = FirebaseAuth.getInstance();

        etMail = findViewById(R.id.etMail);
        etPass = findViewById(R.id.etPass);
        btnSignIn = findViewById(R.id.btnSignIn);
        tvToRegister = findViewById(R.id.tvToRegister);
        tvSignInStatus = findViewById(R.id.tvSignInStatus);

        tvToRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignInActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startSignIn();
            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser!=null) {
            updateUI(currentUser);
        }
    }

    private void startSignIn(){
        String email = etMail.getText().toString().trim();
        String password = etPass.getText().toString().trim();
        if(email.isEmpty()||password.isEmpty()){
                status("אימייל או סיסמה ריקים");
        }else {
            mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        Log.d("SIGN_IN", "signInWithEmailAndPassword:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        updateUI(user);

                    } else {
                        Log.w("SIGN_IN", "signInWithEmailAndPassword:failure", task.getException());
                        status("אימייל או סיסמה שגויים או שגיאת רשת");
                        updateUI(null);
                    }
                }
            });
        }
    }

    private void status(String s) {
        tvSignInStatus.setText(s);
    }

    private void updateUI(FirebaseUser user){
        if(user!=null) {
            Intent intent = new Intent(SignInActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

}