package com.example.bartosz.whereismyfriend;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class LoginActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {

    private FirebaseDatabase _database;
    private DatabaseReference _databaseRef;
    private FirebaseAuth _firebaseAuth;
    private FirebaseAuth.AuthStateListener _authStateListener;
    private UserManager _userManager;

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
        _userManager = new UserManager();

        GetUserPermision();

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
                    if(email != null && _firebaseAuth.getCurrentUser() != null)
                    {
                        Intent loginIntent = new Intent(LoginActivity.this, Home.class);
                        startActivity(loginIntent);
                        LoginActivity.this.finish();
                    }
                }
            }
        };
    }

    private void GetUserPermision(){
    // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.MAPS_RECEIVE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(LoginActivity.this,
                    Manifest.permission.MAPS_RECEIVE)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(LoginActivity.this,
                        new String[]{Manifest.permission.MAPS_RECEIVE},
                        0x1);
                /*
                static final Integer LOCATION = 0x1;
                static final Integer CALL = 0x2;
                static final Integer WRITE_EXST = 0x3;
                static final Integer READ_EXST = 0x4;
                static final Integer CAMERA = 0x5;
                static final Integer ACCOUNTS = 0x6;
                static final Integer GPS_SETTINGS = 0x7;*/
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 0x1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    Toast.makeText(getApplicationContext(), "To run application correctly, you must give permission", Toast.LENGTH_SHORT).show();
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
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
        if(!_userManager.isEmailValid(email)){
            Toast.makeText(this, "Wrong email foramt. Please enter your email", Toast.LENGTH_SHORT).show();
            return;
        }
        if(TextUtils.isEmpty(password))
        {
            Toast.makeText(this, "Please enter password", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar = new ProgressDialog(this);
        progressBar.setMessage("Loging user...");
        progressBar.show();

        _firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, "Login Successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            try{
                                throw task.getException();
                            }
                            catch (FirebaseAuthInvalidUserException invalidEmail){
                                Toast.makeText(LoginActivity.this, "Invalid email", Toast.LENGTH_SHORT).show();
                            }
                            catch (FirebaseAuthInvalidCredentialsException wrongPassword){
                                Toast.makeText(LoginActivity.this, "Invalid password", Toast.LENGTH_SHORT).show();
                            }
                            catch (Exception ex){
                                Toast.makeText(LoginActivity.this, "Login Failed. Please try again", Toast.LENGTH_SHORT).show();
                            }
                        }
                        progressBar.hide();
                    }
                });
    }
}
