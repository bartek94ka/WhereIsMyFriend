package com.example.bartosz.whereismyfriend;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bartosz.whereismyfriend.Models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class CompleteRegisterActivity extends AppCompatActivity {


    private FirebaseDatabase _database;
    private FirebaseAuth _firebaseAuth;
    private DatabaseReference _databaseRef;
    private FirebaseAuth.AuthStateListener _authStateListener;
    private ProgressDialog _progressBar;

    private EditText etName;
    private EditText etSurname;
    private EditText etAge;

    private Button buttonRegister;
    private TextView tvLoginLink;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complete_register);
        _database = FirebaseDatabase.getInstance();
        _databaseRef = _database.getReference();
        _firebaseAuth = FirebaseAuth.getInstance();

        _progressBar = new ProgressDialog(this);

        etName = (EditText) findViewById(R.id.CompleteRegName);
        etSurname = (EditText) findViewById(R.id.CompleteRegSurname);
        etAge = (EditText) findViewById(R.id.CompleteRegAge);

        buttonRegister = (Button) findViewById(R.id.CompleteRegButton);

        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(RegisterUser() == true){
                    Intent registerIntent = new Intent(CompleteRegisterActivity.this, WhereIAmActivity.class);
//                    CompleteRegisterActivity.this.startActivity(registerIntent);
                }
            }
        });

        /*_authStateListener = new FirebaseAuth.AuthStateListener(){
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(_firebaseAuth.getCurrentUser() != null){
                    Intent registerIntent = new Intent(CompleteRegisterActivity.this, WhereIAmActivity.class);
                    CompleteRegisterActivity.this.startActivity(registerIntent);
                }
            }
        };*/
    }
/*

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
*/



    private Boolean RegisterUser(){

        final String name = etName.getText().toString().trim();
        final String surname = etName.getText().toString().trim();
        final String age = etName.getText().toString().trim();

        if(TextUtils.isEmpty(name))
        {
            Toast.makeText(this, "Please enter name", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(TextUtils.isEmpty(surname))
        {
            Toast.makeText(this, "Please enter surname", Toast.LENGTH_SHORT).show();
            return false;
        }

        if(TextUtils.isEmpty(age))
        {
            Toast.makeText(this, "Please enter age", Toast.LENGTH_SHORT).show();
            return false;
        }

        _progressBar.setMessage("Completing registering of the User...");
        _progressBar.show();

        String userId = _firebaseAuth.getCurrentUser().getUid();
        String email = _firebaseAuth.getCurrentUser().getEmail();
        User newUser = CreateUserObject(email, name, surname, age);
        try {
            _database.getReference().child("users").child(userId).setValue(newUser)
                    .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful())
                            {
                                Toast.makeText(CompleteRegisterActivity.this, "Registered Successfully", Toast.LENGTH_SHORT).show();
                            }else
                            {
                                Toast.makeText(CompleteRegisterActivity.this, "Could not register. Please try again", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }catch (Exception ex){
            System.out.print(ex.getMessage());
        }
        return true;
    }

    private User CreateUserObject(String email, String name, String surname, String age)
    {
        User newCreatedUser = new User();
        newCreatedUser.Range = 1000.0;
//        newCreatedUser.Age = Integer.parseInt(age);
        newCreatedUser.Email = email;
        newCreatedUser.Name = name;
        newCreatedUser.Surname = surname;
        newCreatedUser.FullName = name + ' ' + surname;
        return newCreatedUser;
    }
}
