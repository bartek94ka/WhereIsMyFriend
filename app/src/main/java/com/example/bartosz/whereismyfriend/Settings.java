package com.example.bartosz.whereismyfriend;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Toast;

import com.example.bartosz.whereismyfriend.Models.User;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class Settings extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    private FirebaseAuth _firebaseAuth;
    private FirebaseDatabase _database;
    private FirebaseUser _currentUser;
    private User _user;

    private EditText _settingsName;
    private EditText _settingsSurname;
    private EditText _settingsAge;
    private SeekBar _settingsSetRange;

    private EditText _settingsOldPassword;
    private EditText _settingsNewPassword;
    private EditText _settingsConfirmPassword;
    private Button _settingsChangePasswordButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        drawer.requestDisallowInterceptTouchEvent(true);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        _firebaseAuth = FirebaseAuth.getInstance();
        _database = FirebaseDatabase.getInstance();
        _currentUser = _firebaseAuth.getCurrentUser();

        _settingsName = (EditText) findViewById(R.id.settingsName);
        _settingsSurname = (EditText) findViewById(R.id.settingsSurname);
        _settingsAge = (EditText) findViewById(R.id.settingsAge);
        _settingsSetRange = (SeekBar) findViewById(R.id.settingsSetRange);
        _settingsOldPassword = (EditText) findViewById(R.id.settingsOldPassword);
        _settingsNewPassword = (EditText) findViewById(R.id.settingsNewPassword);
        _settingsConfirmPassword = (EditText) findViewById(R.id.settingsConfirmNewPassword);
        _settingsChangePasswordButton = (Button) findViewById(R.id.settingsChangePasswordButton);

        SetChangePasswordButtonAction();
        FillBasicUserData();
        SetBasicUserDataButtonAction();
    }

    private void FillBasicUserData(){
        getUserData(_currentUser.getUid()).addOnCompleteListener(new OnCompleteListener<User>() {
            @Override
            public void onComplete(@NonNull Task<User> task) {
                _user = task.getResult();
                if(_user != null){
                    _settingsName.setText(_user.Name);
                    _settingsSurname.setText(_user.Surname);
                    String age = String.valueOf(_user.Age);
                    _settingsAge.setText(age);
                    int progressValue = _user.Range.intValue();
                    _settingsSetRange.setProgress(progressValue);
                }
            }
        });
    }

    private Task<User> getUserData(String userId)
    {
        final TaskCompletionSource<User> taskCompletionSource = new TaskCompletionSource<>();
        _database.getReference("Users").child(userId).
                addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        User user = dataSnapshot.getValue(User.class);
                        taskCompletionSource.setResult(user);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
        return taskCompletionSource.getTask();
    }

    private void SetBasicUserDataButtonAction(){

    }

    private void SetChangePasswordButtonAction(){

        _settingsChangePasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String oldPassword = _settingsOldPassword.getText().toString().trim();
                final String newPassword = _settingsNewPassword.getText().toString().trim();
                final String confirmPassword = _settingsConfirmPassword.getText().toString().trim();
                IsOldPasswordCorrect(oldPassword).
                        addOnCompleteListener(new OnCompleteListener<Boolean>() {
                    @Override
                    public void onComplete(@NonNull Task<Boolean> task)
                    {
                        Boolean result = task.getResult();
                        if(result == true){
                            if(newPassword.equals(confirmPassword)){
                                ChangeUserPassword(newPassword);
                            }
                            else{
                                Toast.makeText(Settings.this, "Passwords are not equal", Toast.LENGTH_SHORT).show();
                            }
                        }
                        else{
                            Toast.makeText(Settings.this, "Old password is incorrect", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }

    private void ChangeUserPassword(String newPassword){
        if(_currentUser == null){
            return;
        }
        AuthCredential credential = EmailAuthProvider
                .getCredential(_currentUser.getEmail(), newPassword);
        final String password = newPassword;
        _currentUser.updatePassword(password).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.d("PasswordUpdate", "Password updated");
                    Toast.makeText(Settings.this, "Password changed successfully", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(Settings.this, Home.class);
                    startActivity(intent);
                    Settings.this.finish();
                } else {
                    Log.d("PasswordUpadte", "Error password not updated");
                }
            }
        });
    }

    private Task<Boolean> IsOldPasswordCorrect(String oldPassword){
        final TaskCompletionSource<Boolean> taskCompletionSource = new TaskCompletionSource<>();
        _firebaseAuth.signInWithEmailAndPassword(_currentUser.getEmail(), oldPassword).
                addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Boolean isPasswordCorrect = false;
                        if (task.isSuccessful()) {
                            isPasswordCorrect = true;
                        }
                        taskCompletionSource.setResult(isPasswordCorrect);
                    }
                });
        return taskCompletionSource.getTask();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_mylocation) {
            Intent intent = new Intent(Settings.this, MyLocation.class);
            startActivity(intent);
            Settings.this.finish();
        } else if (id == R.id.home) {
            Intent intent = new Intent(Settings.this, Home.class);
            startActivity(intent);
            Settings.this.finish();
        } else if(id == R.id.nav_myfriendlocation){
            Intent intent = new Intent(Settings.this, MyFriendLocation.class);
            startActivity(intent);
            Settings.this.finish();
        } else if(id == R.id.search_all_users) {
            Intent intent = new Intent(Settings.this, SearchAllUsers.class);
            startActivity(intent);
            Settings.this.finish();
        } else if (id == R.id.settings){
            Intent intent = new Intent(Settings.this, Settings.class);
            startActivity(intent);
            Settings.this.finish();
        }else if (id == R.id.nav_logout){
            _firebaseAuth.signOut();
            Settings.this.finish();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
