package com.example.bartosz.whereismyfriend;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
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
import com.google.android.gms.tasks.OnSuccessListener;
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

public class MyFriendLocation extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback {

    GoogleMap map;
    LocationManager locationManager;

    private GPSTracker gpsTracker;
    private Location mLocation;
    private FirebaseDatabase database;
    private FirebaseAuth _firebaseAuth;
    private UserLocationManager _userLocationManager;
    GeoFire _geoFire;
    double latitude;
    double longitude;
    private User currentUser;
    private MapFragment mapFragment;

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
        database = FirebaseDatabase.getInstance();
        _firebaseAuth = FirebaseAuth.getInstance();
        String currentUserId = _firebaseAuth.getCurrentUser().getUid();
        getCurrentUserData(currentUserId);
        DatabaseReference ref = database.getReference("geofire");
        _geoFire = new GeoFire(ref);

        setupFirebase();
        _userLocationManager = new UserLocationManager();
        _userLocationManager.setCurrentUserLocation(currentUserId, latitude, longitude);
    }

    private void getCurrentUserData(String currentUserId) {
        database.getReference("Users").child(currentUserId).
                addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        map.clear();
                        currentUser = dataSnapshot.getValue(User.class);
                        LatLng myLocation = new LatLng(latitude, longitude);
                        CircleOptions circle = new CircleOptions().center(myLocation).radius(currentUser.Range).strokeColor(Color.RED);
                        final int zoom = getZoomLevel(circle);
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, zoom - 1));
                        Intent intent = getIntent();
                        Bundle extras = intent.getExtras();
                        if(!extras.isEmpty()){
                            if(extras.containsKey("userId")){
                                String userId = extras.getString("userId");
                                _userLocationManager.getUserMarker(userId).addOnSuccessListener(new OnSuccessListener<MarkerOptions>() {
                                    @Override
                                    public void onSuccess(MarkerOptions markerOptions) {
                                        map.addMarker(markerOptions);
                                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(markerOptions.getPosition(), zoom - 1));
                                    }
                                });
                            }
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
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
            Intent intent = new Intent(MyFriendLocation.this, MyLocation.class);
            startActivity(intent);
            MyFriendLocation.this.finish();
        } else if (id == R.id.home) {
            Intent intent = new Intent(MyFriendLocation.this, Home.class);
            startActivity(intent);
            MyFriendLocation.this.finish();
        } else if(id == R.id.nav_friendsInNearby){
            Intent intent = new Intent(MyFriendLocation.this, FriendsInNearbyActivity.class);
            startActivity(intent);
            MyFriendLocation.this.finish();
        } else if (id == R.id.nav_logout){
            Intent intent = new Intent(MyFriendLocation.this, LoginActivity.class);
            stopService(new Intent(this, UpdateUserLocationService.class));
            _firebaseAuth.signOut();
            startActivity(intent);
            MyFriendLocation.this.finish();
        } else if (id == R.id.settings){
            Intent intent = new Intent(MyFriendLocation.this, Settings.class);
            startActivity(intent);
            MyFriendLocation.this.finish();
        } else if(id == R.id.search_all_users) {
            Intent intent = new Intent(MyFriendLocation.this, SearchAllUsers.class);
            startActivity(intent);
            MyFriendLocation.this.finish();
        } else if (id == R.id.nav_recived_invitations){
            Intent intent = new Intent(MyFriendLocation.this, RecivedInvitationsActivity.class);
            startActivity(intent);
            MyFriendLocation.this.finish();
        } else if (id == R.id.nav_sended_invitations){
            Intent intent = new Intent(MyFriendLocation.this, SendedInvitationsActivity.class);
            startActivity(intent);
            MyFriendLocation.this.finish();
        } else if (id == R.id.nav_myfreindsList){
            Intent intent = new Intent(MyFriendLocation.this, MyFriendsListActivity.class);
            startActivity(intent);
            MyFriendLocation.this.finish();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
    }

    private int getZoomLevel(CircleOptions circle){
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
}
