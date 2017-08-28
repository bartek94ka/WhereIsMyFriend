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
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MyLocation extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback {

    GoogleMap map;
    LocationManager locationManager;
    GoogleApiClient googleApiClient;

    private GPSTracker gpsTracker;
    private Location mLocation;
    private FirebaseDatabase database;
    private FirebaseAuth _firebaseAuth;
    private FirebaseAuth.AuthStateListener _authStateListener;
    double latitude;
    double longitude;

    private static final String LOG_TAG = "MainActivity";
    public static final GeoLocation CURRENT_LOCATION = new GeoLocation(26.128536, -80.130648);

    //    private DatabaseReference database;
    private GeoFire geofire;
    private Set<GeoQuery> geoQueries = new HashSet<>();

    private List<User> users = new ArrayList<>();
    private ValueEventListener userValueListener;
    private boolean fetchedUserIds;
    private Set<String> userIdsWithListeners = new HashSet<>();

    private RecyclerView.Adapter adapter;
    private int initialListSize;
    private int iterationCount;
    private Location me;
    private Map<String, Location> userIdsToLocations = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_location);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

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
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        database = FirebaseDatabase.getInstance();
        _firebaseAuth = FirebaseAuth.getInstance();

        String currentUserId = _firebaseAuth.getCurrentUser().getUid();
        FirebaseDatabase.getInstance().getReference()
                .child("geofire")
                .child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                double latitude = dataSnapshot.child("Latitude").getValue(Double.class);
                double longitude = dataSnapshot.child("Longitude").getValue(Double.class);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        _authStateListener = new FirebaseAuth.AuthStateListener(){
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(_firebaseAuth.getCurrentUser() == null){
                    Intent loginIntent = new Intent(MyLocation.this, LoginActivity.class);
                    startActivity(loginIntent);
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
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_mylocation) {
            Intent intent = new Intent(MyLocation.this, MyLocation.class);
            startActivity(intent);
            // Handle the camera action
        } else if (id == R.id.home) {
            Intent intent = new Intent(MyLocation.this, Home.class);
            startActivity(intent);
        } else if (id == R.id.nav_logout){
            _firebaseAuth.signOut();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
//        map.
//        Location location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        Log.println(Log.INFO, "onMapReady", "setMyLocationEnabled(true)");
        map.getUiSettings().setScrollGesturesEnabled(false);
//        LatLng myLocation = new LatLng(52.406374, 16.9251681);
        LatLng myLocation = new LatLng(latitude, longitude);
        Marker Poznan = map.addMarker(new MarkerOptions().position(myLocation).title("Moja lokalizacja"));
        Poznan.setTag(0);
        Circle circle = map.addCircle(new CircleOptions().center(myLocation).radius(500).strokeColor(Color.RED));
        circle.setVisible(true);
        int zoom = getZoomLevel(circle);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, zoom - 1));
        //map.setOnMarkerClickListener(this);
        //map.setMyLocationEnabled(true);
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
}
