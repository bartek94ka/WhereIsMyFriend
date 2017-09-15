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
    GeoFire _geoFire;
    double latitude;
    double longitude;
    private User currentUser;
    private MapFragment mapFragment;
    private boolean HasDataBeenLoaded = false;

    private static final String LOG_TAG = "MainActivity";
    public static final GeoLocation CURRENT_LOCATION = new GeoLocation(26.128536, -80.130648);

    //    private DatabaseReference database;
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
        setCurrentUserLocation(currentUserId);

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

    private void setCurrentUserLocation(String currentUserId)
    {
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

    private void getCurrentUserData(String currentUserId)
    {
        database.getReference("Users").child(currentUserId).
                addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        currentUser = dataSnapshot.getValue(User.class);
                        LatLng myLocation = new LatLng(latitude, longitude);
                        Circle circle = map.addCircle(new CircleOptions().center(myLocation).radius(currentUser.Range).strokeColor(Color.RED));
                        circle.setVisible(true);
                        int zoom = getZoomLevel(circle);
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, zoom - 1));
                        fetchUsers();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private Task<User> getUserData(String userId)
    {
        final TaskCompletionSource<User> taskCompletionSource = new TaskCompletionSource<>();
        database.getReference("Users").child(userId).
                addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        User user = dataSnapshot.getValue(User.class);
                        if(HasDataBeenLoaded == false){
                            HasDataBeenLoaded = true;
                            taskCompletionSource.setResult(user);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
        return taskCompletionSource.getTask();
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
            _firebaseAuth.signOut();
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

    private void fetchUsers(){
        GeoQuery geoQuery = _geoFire.queryAtLocation(new GeoLocation(latitude, longitude), currentUser.Range);
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {

                if(key == _firebaseAuth.getCurrentUser().getUid()){
                    return;
                }
                /*boolean isEnteredUserFriend = currentUser.FriendsId.contains(key);
                if(isEnteredUserFriend == false){
                    return;
                }*/


                //User user = getUserData(key).getResult();

                Location loc = new Location("to");
                loc.setLatitude(location.latitude);
                loc.setLongitude(location.longitude);
                final LatLng userLocation = new LatLng(location.latitude, location.longitude);
                /*map.addMarker(new MarkerOptions().position(userLocation).
                                title(user.FullName).
                                snippet(user.Email + " " + user.Age)
                                );*/
                getUserData(key).addOnCompleteListener(new OnCompleteListener<User>() {
                    @Override
                    public void onComplete(@NonNull Task<User> task) {
                        User user = task.getResult();
                        if(user != null){
                            map.addMarker(new MarkerOptions().position(userLocation).
                                    title(user.FullName).
                                    snippet(user.Email + " " + user.Age)
                            );
                        }
                    }
                });
                if (!fetchedUserIds) {
                    userIdsToLocations.put(key, loc);
                } else {
                    userIdsToLocations.put(key, loc);
                }
            }

            @Override
            public void onKeyExited(String key) {
                Log.d(LOG_TAG, "onKeyExited: ");
                /*if (userIdsWithListeners.contains(key)) {
                    int position = getUserPosition(key);
                    users.remove(position);
                    adapter.notifyItemRemoved(position);
                }*/
            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {
                Log.d(LOG_TAG, "onKeyMoved: ");
            }

            @Override
            public void onGeoQueryReady() {
                Log.d(LOG_TAG, "onGeoQueryReady: ");
                initialListSize = userIdsToLocations.size();
                if (initialListSize == 0) {
                    fetchedUserIds = true;
                }
                iterationCount = 0;
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {
                Log.e(LOG_TAG, "onGeoQueryError: ", error.toException());
            }
        });
        geoQueries.add(geoQuery);
    }

    private void setupFirebase() {
        DatabaseReference ref = database.getReference("geofire");
        _geoFire = new GeoFire(ref);
    }

    /*private int b (User u) {
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).id.equals(u.id)) {
                Log.d(LOG_TAG, "getIndexOfNewUser: " + i);
                return i;
            }
        }
        throw new RuntimeException();
    }

    private int getUserPosition(String id) {
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).id.equals(id)) {
                return i;
            }
        }
        return -1;
    }*/
}
