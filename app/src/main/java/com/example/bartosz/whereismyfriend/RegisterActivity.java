package com.example.bartosz.whereismyfriend;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bartosz.whereismyfriend.Models.User;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseDatabase _database;
    private FirebaseAuth _firebaseAuth;
    private DatabaseReference _databaseRef;
    private FirebaseAuth.AuthStateListener _authStateListener;
    private ProgressDialog _progressBar;

    private EditText etEmail;
    private EditText etPassword;
    private EditText etConfirmPassword;
    private EditText etName;
    private EditText etSurname;
    private EditText etAge;

    private Button buttonRegister;
    private TextView tvLoginLink;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);




        _database = FirebaseDatabase.getInstance();
        _databaseRef = _database.getReference();
        _firebaseAuth = FirebaseAuth.getInstance();

        _progressBar = new ProgressDialog(this);

        etEmail = (EditText) findViewById(R.id.regEmail);
        etPassword = (EditText) findViewById(R.id.regPassword);
        etConfirmPassword = (EditText) findViewById(R.id.regConfirmPassword);
        etName = (EditText) findViewById(R.id.regName);
        etSurname = (EditText) findViewById(R.id.regSurname);
        etAge = (EditText) findViewById(R.id.regAge);

        buttonRegister = (Button) findViewById(R.id.regButton);
        tvLoginLink = (TextView) findViewById(R.id.regLoginHere);

        tvLoginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent registerIntent = new Intent(RegisterActivity.this, LoginActivity.class);
                RegisterActivity.this.startActivity(registerIntent);
            }
        });

        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                RegisterUser();
            }
        });

        _authStateListener = new FirebaseAuth.AuthStateListener(){
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(_firebaseAuth.getCurrentUser() != null){
                    String email = _firebaseAuth.getCurrentUser().getEmail();
//                    DatabaseReference userData = _database.getReference("Users").;
                    Intent registerIntent = new Intent(RegisterActivity.this, LoginActivity.class);
                    RegisterActivity.this.startActivity(registerIntent);
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

    private User CreateUserObject(String email, String name, String surname, String age)
    {
        User newCreatedUser = new User();
        newCreatedUser.Range = 1000.0;
        newCreatedUser.Age = Integer.parseInt(age);
        newCreatedUser.Email = email;
        newCreatedUser.Name = name;
        newCreatedUser.Surname = surname;
        newCreatedUser.FullName = name + ' ' + surname;
//        newCreatedUser.CurrentLocation
        return newCreatedUser;
    }

    private boolean IsRegisterFormValid(String email, String password, String confirmPassword, String name, String surname, String age)
    {
        return false;
    }

    private void AddUserToDb(){

    }
    private boolean IsUserExistsInDatabase(String email)
    {
        Object object = _databaseRef.child("Users").equalTo(email);
        if(object == null)
        {
            return false;
        }
        else{
            return true;
        }
    }
    private void RegisterUser(){

        final String email = etEmail.getText().toString().trim();
        final String password = etPassword.getText().toString().trim();
        String confirmpassword = etConfirmPassword.getText().toString().trim();
        final String name = etName.getText().toString().trim();
        final String surname = etName.getText().toString().trim();
        final String age = etName.getText().toString().trim();

        if(IsUserExistsInDatabase(email) == true)
        {

        }


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

        _progressBar.setMessage("Registering User...");
        _progressBar.show();

        user = CreateUserObject(email, name, surname, age);

        try {
            this._firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Log.d("TAG", "createUserWithEmail:success");
                                Toast.makeText(RegisterActivity.this, "Registered Successfully", Toast.LENGTH_SHORT).show();
                            } else {
                                Log.d("TAG", "createUserWithEmail:fail");
                                Toast.makeText(RegisterActivity.this, "Could not register. Please try again", Toast.LENGTH_SHORT).show();
                            }
                            _progressBar.hide();
                        }
                    });
        }catch (Exception ex){
            System.out.print(ex.getMessage());
        }
    }
}
