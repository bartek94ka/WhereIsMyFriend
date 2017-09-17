package com.example.bartosz.whereismyfriend;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.bartosz.whereismyfriend.Models.User;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.maps.model.LatLng;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Bartosz on 12.09.2017.
 */

public class UserManager {

    private FirebaseDatabase _database;
    private FirebaseAuth _firebaseAuth;
    private DatabaseReference _geofireReference;
    private GeoFire _geoFire;

    public UserManager(){
        _database = FirebaseDatabase.getInstance();
        _firebaseAuth = FirebaseAuth.getInstance();
        _geofireReference = _database.getReference("geofire");
        _geoFire = new GeoFire(_geofireReference);
    }

    public Task<User> getUserData(String userId) {
        final TaskCompletionSource<User> taskCompletionSource = new TaskCompletionSource<>();
        final DatabaseReference localReference = _database.getReference("Users").child(userId);
        localReference.
                addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        User user = dataSnapshot.getValue(User.class);
                        taskCompletionSource.setResult(user);
                        localReference.removeEventListener(this);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
        return taskCompletionSource.getTask();
    }

    public void UpdateFirebaseUserData(String userId, User user){
        Map<String,Object> taskMap = new HashMap<String,Object>();
        taskMap.put(userId, user);
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.updateChildren(taskMap);
    }

    public void ChangeUserPassword(final Context context, FirebaseUser currentUser, String newPassword){
        if(currentUser == null){
            return;
        }
        AuthCredential credential = EmailAuthProvider
                .getCredential(currentUser.getEmail(), newPassword);
        final String password = newPassword;
        currentUser.updatePassword(password).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.d("PasswordUpdate", "Password updated");
                    Toast.makeText(context, "Password changed successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Log.d("PasswordUpadte", "Error password not updated");
                }
            }
        });
    }

    public  void RegisterUser(final Context context, String email, String password){

        final ProgressDialog progressDialog = new ProgressDialog(context);
        if(TextUtils.isEmpty(email))
        {
            Toast.makeText(context, "Please enter email", Toast.LENGTH_SHORT).show();
            return;
        }
        if(TextUtils.isEmpty(password))
        {
            Toast.makeText(context, "Please enter password", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.setMessage("Registering User...");
        progressDialog.show();


        try {
            this._firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener((Activity) context, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Log.d("TAG", "createUserWithEmail:success");
                                Toast.makeText(context, "Registered Successfully", Toast.LENGTH_SHORT).show();
                            } else {
                                Log.d("TAG", "createUserWithEmail:fail");
                                Toast.makeText(context, "Could not register. Please try again", Toast.LENGTH_SHORT).show();
                            }
                            progressDialog.hide();
                        }
                    });
        }catch (Exception ex){
            System.out.print(ex.getMessage());
        }
    }

    public Boolean IsUserRegisterCompleted(final Context context, String name, String surname, String age){

        final ProgressDialog progressDialog = new ProgressDialog(context);

        if(TextUtils.isEmpty(name))
        {
            Toast.makeText(context, "Please enter name", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(TextUtils.isEmpty(surname))
        {
            Toast.makeText(context, "Please enter surname", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(TextUtils.isEmpty(age))
        {
            Toast.makeText(context, "Please enter age", Toast.LENGTH_SHORT).show();
            return false;
        }

        progressDialog.setMessage("Completing registering of the User...");
        progressDialog.show();

        String userId = _firebaseAuth.getCurrentUser().getUid();
        String email = _firebaseAuth.getCurrentUser().getEmail();
        User newUser = CreateUserObject(email, name, surname, age);
        try {
            GPSTracker gpsTracker = new GPSTracker(context);
            Location mLocation = gpsTracker.getLocation();
            double latitude = mLocation.getLatitude();
            double longitude = mLocation.getLongitude();
            final com.example.bartosz.whereismyfriend.Models.Location location =
                    new com.example.bartosz.whereismyfriend.Models.Location();
            location.Latitude = latitude;
            location.Longitude = longitude;

            _database.getReference().child("Users").child(userId).setValue(newUser)
                    .addOnCompleteListener((Activity) context, new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful())
                            {
                                Toast.makeText(context, "Registered Successfully", Toast.LENGTH_SHORT).show();
                            }else
                            {
                                Toast.makeText(context, "Could not register. Please try again", Toast.LENGTH_SHORT).show();
                            }
                            progressDialog.hide();
                        }
                    });
            _database.getReference().child("geofire").child(userId).setValue(location);
        }catch (Exception ex){
            System.out.print(ex.getMessage());
        }
        return true;
    }

    public User CreateUserObject(String email, String name, String surname, String age) {
        User newCreatedUser = new User(email, name, surname, 1000.0, Integer.valueOf(age));
        return newCreatedUser;
    }

    public void LoginUser(final Context context, String email, String password){

        if(TextUtils.isEmpty(email))
        {
            Toast.makeText(context, "Please enter email", Toast.LENGTH_SHORT).show();
            return;
        }
        if(TextUtils.isEmpty(password))
        {
            Toast.makeText(context, "Please enter password", Toast.LENGTH_SHORT).show();
            return;
        }

        final ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Loging user...");
        progressDialog.show();

        _firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener((Activity) context, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(context, "Login Successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            try{
                                throw task.getException();
                            }
                            catch (FirebaseAuthInvalidUserException invalidEmail){
                                Toast.makeText(context, "Invalid email", Toast.LENGTH_SHORT).show();
                            }
                            catch (FirebaseAuthInvalidCredentialsException wrongPassword){
                                Toast.makeText(context, "Invalid password", Toast.LENGTH_SHORT).show();
                            }
                            catch (Exception ex){
                                Toast.makeText(context, "Login Failed. Please try again", Toast.LENGTH_SHORT).show();
                            }
                        }
                        progressDialog.hide();
                    }
                });
    }

    public void LogoutUser(){
        if(_firebaseAuth.getCurrentUser() != null){
            _firebaseAuth.signOut();
        }
    }

    public void UpdateCurrentUserLocation(Context context){
        try{
            if(_firebaseAuth == null && _firebaseAuth.getCurrentUser() == null){
                return;
            }
            String currentUserId = _firebaseAuth.getCurrentUser().getUid();
            GPSTracker gpsTracker = new GPSTracker(context);
            Location mLocation = gpsTracker.getLocation();
            double latitude;
            double longitude;
            if(mLocation != null)
            {
                latitude = mLocation.getLatitude();
                longitude = mLocation.getLongitude();
                _geoFire.setLocation(currentUserId, new GeoLocation(latitude, longitude));
            }
        }catch(Exception ex){

        }
    }

    public static boolean isEmailValid(String email) {
        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }
}
