package com.example.bartosz.whereismyfriend;

import android.location.Location;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.bartosz.whereismyfriend.Models.User;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.firebase.geofire.LocationCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

/**
 * Created by Bartosz on 13.09.2017.
 */

public class UserLocationManager {

    private FirebaseAuth _firebaseAuth;
    private FirebaseDatabase _database;
    private GeoFire _geoFire;
    private UserManager _userManager;
    public UserLocationManager(){
        _database = FirebaseDatabase.getInstance();
        DatabaseReference ref = _database.getReference("geofire");
        _geoFire = new GeoFire(ref);
        _firebaseAuth = FirebaseAuth.getInstance();
        _userManager = new UserManager();
    }

    public Task<MarkerOptions> getUserMarker(String id){
        final TaskCompletionSource<MarkerOptions> taskCompletionSource = new TaskCompletionSource<>();
        _geoFire.getLocation(id, new LocationCallback() {
            @Override
            public void onLocationResult(String key, GeoLocation location) {
                final LatLng userLocation = new LatLng(location.latitude, location.longitude);
                _userManager.getUserData(key).addOnCompleteListener(new OnCompleteListener<User>() {
                    @Override
                    public void onComplete(@NonNull Task<User> task) {
                        User user = task.getResult();
                        if(user != null){
                            MarkerOptions userMarker = null;
                            if(user.FullName != null && user.Email != null){
                                userMarker = new MarkerOptions().position(userLocation).
                                        title(user.FullName).
                                        snippet("Email: " + user.Email + ", Age: " + user.Age);
                            } else if(user.FullName != null){
                                userMarker = new MarkerOptions().position(userLocation).
                                        title(user.FullName).
                                        snippet("Age: " + user.Age);
                            }
                            taskCompletionSource.setResult(userMarker);
                        }
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        return taskCompletionSource.getTask();
    }

    public void setCurrentUserLocation(String currentUserId, double latitude, double longitude) {
        _geoFire.setLocation(currentUserId, new GeoLocation(latitude, longitude),
                new GeoFire.CompletionListener() {

                    @Override
                    public void onComplete(String key, DatabaseError error) {
                        if (error != null) {
                            System.err.println("There was an error saving the location to GeoFire: " + error);
                        } else {
                            System.out.println("Location saved on server successfully!");
                        }
                    }
                });
    }
}
