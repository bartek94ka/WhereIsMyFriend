package com.example.bartosz.whereismyfriend;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.util.Log;
import android.widget.Toast;

import com.example.bartosz.whereismyfriend.Models.User;
import com.example.bartosz.whereismyfriend.Models.Location;
import com.example.bartosz.whereismyfriend.Models.Invitation;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class LoginActivity extends AppCompatActivity {

    private FirebaseDatabase _database;
    //private FirebaseAuth _firebaseAuth;
    private DatabaseReference _databaseRef;


    //private ProgressDialog progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //progressBar = new ProgressDialog(this);
        final EditText etEmail = (EditText) findViewById(R.id.loginEmail);
        final EditText etPassword = (EditText) findViewById(R.id.loginPassword);

        final Button buttonLogin = (Button) findViewById(R.id.loginButton);
        final TextView tvRegisterLink = (TextView) findViewById(R.id.loginRegisterHere);


        _database = FirebaseDatabase.getInstance();
        _databaseRef = _database.getReference();
        //_firebaseAuth = FirebaseAuth.getInstance();



        tvRegisterLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);
                LoginActivity.this.startActivity(registerIntent);
            }
        });

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Location location = new Location(52.390758, 16.951566);
                Invitation invitation = null;
                User user = new User();
                user.UserName = "mkaczorowski";
                user.Name = "Miłosz";
                user.Surname = "Kaczorowski";
                user.Age = 34;
                user.Email = "mkaczorowski@whereismyfriend.com";
                user.SendInvitations = null;
                user.RecivedInvitations = null;
                user.Range = 3000.0;

                //DatabaseReference myData = _databaseRef.child("Users").child("mkaczorowski").getRef();//.setValue(user);

                _databaseRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.d("onDataChange", dataSnapshot.toString());
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });
    }

    private void LoginUser(){
        /*String email = etEmail.getText().toString().trim();
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
        progressBar.show();*/
        //_firebaseAuth.
    }
}
