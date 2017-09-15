package com.example.bartosz.whereismyfriend;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.View;
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
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class UserDetailsActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private FirebaseAuth _firebaseAuth;

    private UserManager _userManager;
    private TextView _nameSurname;
    private TextView _email;
    private TextView _age;
    private TextView _friendsAmount;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_details);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        _firebaseAuth = FirebaseAuth.getInstance();
        _userManager = new UserManager();
        SetTextViewValues();
        GetUserData();
    }

    private void GetUserData(){
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if(!extras.isEmpty()){
            if(extras.containsKey("userId")){
                String userId = extras.getString("userId");
                _userManager.getUserData(userId).addOnCompleteListener(new OnCompleteListener<User>() {
                    @Override
                    public void onComplete(@NonNull Task<User> task) {
                        if(task.isSuccessful()){
                            User user = task.getResult();
                            _nameSurname.setText(user.FullName);
                            _email.setText(user.Email);
                            _age.setText(String.valueOf(user.Age));
                            if(user.FriendsId != null){
                                _friendsAmount.setText(String.valueOf(user.FriendsId.size()));
                            }
                            else{
                                _friendsAmount.setText("0");
                            }
                        }
                    }
                });
            }
        }
    }

    private void SetTextViewValues(){
        _nameSurname = (TextView) findViewById(R.id.details_nameSurname);
        _email = (TextView) findViewById(R.id.details_Email);
        _age = (TextView) findViewById(R.id.details_Age);
        _friendsAmount = (TextView) findViewById(R.id.details_friendsAmount);
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
            Intent intent = new Intent(UserDetailsActivity.this, MyLocation.class);
            startActivity(intent);
            UserDetailsActivity.this.finish();
        } else if (id == R.id.home) {
            Intent intent = new Intent(UserDetailsActivity.this, Home.class);
            startActivity(intent);
            UserDetailsActivity.this.finish();
        } else if(id == R.id.nav_friendsInNearby){
            Intent intent = new Intent(UserDetailsActivity.this, FriendsInNearbyActivity.class);
            startActivity(intent);
            UserDetailsActivity.this.finish();
        } else if (id == R.id.nav_logout){
            Intent intent = new Intent(UserDetailsActivity.this, LoginActivity.class);
            stopService(new Intent(this, UpdateUserLocationService.class));
            _firebaseAuth.signOut();
            startActivity(intent);
            UserDetailsActivity.this.finish();
        } else if (id == R.id.settings){
            Intent intent = new Intent(UserDetailsActivity.this, Settings.class);
            startActivity(intent);
            UserDetailsActivity.this.finish();
        } else if(id == R.id.search_all_users) {
            Intent intent = new Intent(UserDetailsActivity.this, SearchAllUsers.class);
            startActivity(intent);
            UserDetailsActivity.this.finish();
        } else if (id == R.id.nav_recived_invitations){
            Intent intent = new Intent(UserDetailsActivity.this, RecivedInvitationsActivity.class);
            startActivity(intent);
            UserDetailsActivity.this.finish();
        } else if (id == R.id.nav_sended_invitations){
            Intent intent = new Intent(UserDetailsActivity.this, SendedInvitationsActivity.class);
            startActivity(intent);
            UserDetailsActivity.this.finish();
        } else if (id == R.id.nav_myfreindsList){
            Intent intent = new Intent(UserDetailsActivity.this, MyFriendsListActivity.class);
            startActivity(intent);
            UserDetailsActivity.this.finish();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
