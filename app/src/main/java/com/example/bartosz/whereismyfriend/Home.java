package com.example.bartosz.whereismyfriend;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.example.bartosz.whereismyfriend.Models.User;
import com.example.bartosz.whereismyfriend.Services.UpdateUserLocationService;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.vision.text.Text;
import com.google.firebase.auth.FirebaseAuth;

public class Home extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private FirebaseAuth _firebaseAuth;
    private UserManager _userManager;

    TextView _email;
    TextView _fullName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        _userManager = new UserManager();
        _firebaseAuth = FirebaseAuth.getInstance();

        _fullName = (TextView) findViewById(R.id.navigation_FullName);
        _email = (TextView) findViewById(R.id.navigation_Email);

        startService(new Intent(this, UpdateUserLocationService.class));
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
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_mylocation) {
            Intent intent = new Intent(Home.this, MyLocation.class);
            startActivity(intent);
            Home.this.finish();
        } else if (id == R.id.home) {
            Intent intent = new Intent(Home.this, Home.class);
            startActivity(intent);
            Home.this.finish();
        } else if(id == R.id.nav_friendsInNearby){
            Intent intent = new Intent(Home.this, FriendsInNearbyActivity.class);
            startActivity(intent);
            Home.this.finish();
        } else if (id == R.id.nav_logout){
            Intent intent = new Intent(Home.this, LoginActivity.class);
            stopService(new Intent(this, UpdateUserLocationService.class));
            _firebaseAuth.signOut();
            startActivity(intent);
            Home.this.finish();
        } else if (id == R.id.settings){
            Intent intent = new Intent(Home.this, Settings.class);
            startActivity(intent);
            Home.this.finish();
        } else if(id == R.id.search_all_users) {
            Intent intent = new Intent(Home.this, SearchAllUsers.class);
            startActivity(intent);
            Home.this.finish();
        } else if (id == R.id.nav_recived_invitations){
            Intent intent = new Intent(Home.this, RecivedInvitationsActivity.class);
            startActivity(intent);
            Home.this.finish();
        } else if (id == R.id.nav_sended_invitations){
            Intent intent = new Intent(Home.this, SendedInvitationsActivity.class);
            startActivity(intent);
            Home.this.finish();
        } else if (id == R.id.nav_myfreindsList){
            Intent intent = new Intent(Home.this, MyFriendsListActivity.class);
            startActivity(intent);
            Home.this.finish();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
