package com.example.bartosz.whereismyfriend;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class LoginActivity extends AppCompatActivity {

    private FirebaseDatabase _database;
    private DatabaseReference _databaseRef;
    private FirebaseAuth _firebaseAuth;
    private FirebaseAuth.AuthStateListener _authStateListener;

    private EditText etEmail;
    private EditText etPassword;
    private Button buttonLogin;
    private TextView tvRegisterLink;
    private ProgressDialog progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail = (EditText) findViewById(R.id.loginEmail);
        etPassword = (EditText) findViewById(R.id.loginPassword);

        buttonLogin = (Button) findViewById(R.id.loginButton);
        tvRegisterLink = (TextView) findViewById(R.id.loginRegisterHere);


        _database = FirebaseDatabase.getInstance();
        _databaseRef = _database.getReference();
        _firebaseAuth = FirebaseAuth.getInstance();

        tvRegisterLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);
                LoginActivity.this.startActivity(registerIntent);
            }
        });

        buttonLogin.setOnClickListener((new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginUser();
                Intent registerIntent = new Intent(LoginActivity.this, Home.class);
                LoginActivity.this.startActivity(registerIntent);
            }
        }));

        _authStateListener = new FirebaseAuth.AuthStateListener(){
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(_firebaseAuth.getCurrentUser() != null){
                    String email = _firebaseAuth.getCurrentUser().getEmail();
                    if(email != null)
                    {
                        Intent loginIntent = new Intent(LoginActivity.this, Home.class);
                        startActivity(loginIntent);
                    }
                }
            }
        };
    }

    @Override
    public void onStart(){
        super.onStart();
        _firebaseAuth.addAuthStateListener(_authStateListener);
    }

    @Override
    public void onStop(){
        super.onStop();
        _firebaseAuth.removeAuthStateListener(_authStateListener);
    }

    private void LoginUser(){

        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if(TextUtils.isEmpty(email))
        {
            Toast.makeText(this, "Please enter email", Toast.LENGTH_SHORT).show();
            return;
        }
        if(TextUtils.isEmpty(password))
        {
            Toast.makeText(this, "Please enter password", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setMessage("Loging user...");
        progressBar.show();

        _firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, "Login Successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(LoginActivity.this, "Login Failed. Please try again", Toast.LENGTH_SHORT).show();
                        }
                        progressBar.hide();
                    }
                });
    }
}
