package com.example.bartosz.whereismyfriend;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
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

import com.example.bartosz.whereismyfriend.Models.User;
import com.example.bartosz.whereismyfriend.Services.UpdateUserLocationService;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class MyLocation extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback {

    GoogleMap map;
    LocationManager locationManager;

    private GPSTracker gpsTracker;
    private Location mLocation;
    private FirebaseDatabase database;
    private FirebaseAuth _firebaseAuth;
    GeoFire _geoFire;
    double latitude;
    double longitude;
    private User currentUser;
    private MapFragment mapFragment;
    private UserLocationManager userLocationManager;
    private Timer mTimer;
    private Handler mHandler;
    public static final int notify = 5000;
    private String currentUserId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_location);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        gpsTracker = new GPSTracker(getApplicationContext());
        mLocation = gpsTracker.getLocation();
        if(mLocation != null)
        {
            latitude = mLocation.getLatitude();
            longitude = mLocation.getLongitude();
        }

        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        userLocationManager = new UserLocationManager();
        database = FirebaseDatabase.getInstance();

        _firebaseAuth = FirebaseAuth.getInstance();
        currentUserId = _firebaseAuth.getCurrentUser().getUid();
        DatabaseReference ref = database.getReference("geofire");
        _geoFire = new GeoFire(ref);

        setupFirebase();
        SetTimer();
    }

    private void UpdateUserLocationOnMap(final String currentUserId)
    {
        final DatabaseReference reference = database.getReference("Users").child(currentUserId);
        reference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        map.clear();
                        currentUser = dataSnapshot.getValue(User.class);
                        LatLng myLocation = new LatLng(latitude, longitude);
                        Circle circle = map.addCircle(new CircleOptions().center(myLocation).radius(currentUser.Range).strokeColor(Color.RED));
                        circle.setVisible(true);
                        int zoom = getZoomLevel(circle);
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, zoom - 1));
                        userLocationManager.getUserMarker(currentUserId).addOnCompleteListener(new OnCompleteListener<MarkerOptions>() {
                            @Override
                            public void onComplete(@NonNull Task<MarkerOptions> task) {
                                if(task.isSuccessful()){
                                    MarkerOptions markerOptions = task.getResult();
                                    map.addMarker(markerOptions);
                                }
                            }
                        });
                        reference.removeEventListener(this);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    @Override
    public void onStart(){
        super.onStart();
    }

    @Override
    public void onStop(){
        super.onStop();
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
        //getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        mTimer.cancel();
        int id = item.getItemId();

        if (id == R.id.nav_mylocation) {
            Intent intent = new Intent(MyLocation.this, MyLocation.class);
            startActivity(intent);
            MyLocation.this.finish();
        } else if (id == R.id.home) {
            Intent intent = new Intent(MyLocation.this, Home.class);
            startActivity(intent);
            MyLocation.this.finish();
        } else if(id == R.id.nav_friendsInNearby){
            Intent intent = new Intent(MyLocation.this, FriendsInNearbyActivity.class);
            startActivity(intent);
            MyLocation.this.finish();
        } else if (id == R.id.nav_logout){
            Intent intent = new Intent(MyLocation.this, LoginActivity.class);
            stopService(new Intent(this, UpdateUserLocationService.class));
            _firebaseAuth.signOut();
            startActivity(intent);
            MyLocation.this.finish();
        } else if (id == R.id.settings){
            Intent intent = new Intent(MyLocation.this, Settings.class);
            startActivity(intent);
            MyLocation.this.finish();
        } else if(id == R.id.search_all_users) {
            Intent intent = new Intent(MyLocation.this, SearchAllUsers.class);
            startActivity(intent);
            MyLocation.this.finish();
        } else if (id == R.id.nav_recived_invitations){
            Intent intent = new Intent(MyLocation.this, RecivedInvitationsActivity.class);
            startActivity(intent);
            MyLocation.this.finish();
        } else if (id == R.id.nav_sended_invitations){
            Intent intent = new Intent(MyLocation.this, SendedInvitationsActivity.class);
            startActivity(intent);
            MyLocation.this.finish();
        } else if (id == R.id.nav_myfreindsList){
            Intent intent = new Intent(MyLocation.this, MyFriendsListActivity.class);
            startActivity(intent);
            MyLocation.this.finish();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
    }

    private int getZoomLevel(Circle circle){
        int zoomLevel = 1;
        if(circle != null){
            double radius = circle.getRadius();
            double scale = radius / 500;
            zoomLevel = (int) (16 - Math.log(scale) / Math.log(2));
        }
        return zoomLevel;
    }

    private void setupFirebase() {
        DatabaseReference ref = database.getReference("geofire");
        _geoFire = new GeoFire(ref);
    }

    private void SetTimer(){
        mTimer = new Timer();   //recreate new
        mHandler = new Handler();
        mTimer.scheduleAtFixedRate(new TimeDisplay(), 0, notify);
    }

    private class TimeDisplay extends TimerTask {
        @Override
        public void run() {
            // run on another thread
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    UpdateUserLocationOnMap(currentUserId);
                }
            });
        }
    }
}
