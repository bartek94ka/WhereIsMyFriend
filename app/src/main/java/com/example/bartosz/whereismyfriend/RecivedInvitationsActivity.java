package com.example.bartosz.whereismyfriend;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bartosz.whereismyfriend.Models.User;
import com.example.bartosz.whereismyfriend.Services.UpdateUserLocationService;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class RecivedInvitationsActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private TextView _informText;
    private FirebaseAuth _firebaseAuth;

    private UserManager _userManager;
    private UsersCollectionProvider _usersCollectionProvider;
    private User _currentUser;
    private List<User> userList;

    private ListView _listViewUser;
    private UserListRecivedInvitationsAdapter _userListAdapter;
    public Handler _handler;
    public View _footerView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recived_invitations);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        _informText = (TextView) findViewById(R.id.recivedUserInvitationsList_empty);

        _listViewUser = (ListView) findViewById(R.id.listview_users);
        LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        _footerView = inflater.inflate(R.layout.footer_view, null);
        _handler = new MyHandler();
        _firebaseAuth = FirebaseAuth.getInstance();
        _usersCollectionProvider = new UsersCollectionProvider();
        _userManager = new UserManager();
        _userManager.getUserData(_firebaseAuth.getCurrentUser().getUid()).addOnSuccessListener(new OnSuccessListener<User>() {
            @Override
            public void onSuccess(User user) {
                _currentUser = user;
            }
        });

        userList = new ArrayList<>();
        _userListAdapter = new UserListRecivedInvitationsAdapter(getApplicationContext(), userList);
        _listViewUser.setAdapter(_userListAdapter);
        _listViewUser.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Do something
                //Ex: display msg with product id get from view.getTag
                Toast.makeText(getApplicationContext(), "Clicked product id =" + view.getTag(), Toast.LENGTH_SHORT).show();
            }
        });

        _userListAdapter.ClearList();
        Thread thread = new ThreadGetMoreData();
        thread.start();
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
        int id = item.getItemId();

        if (id == R.id.nav_mylocation) {
            Intent intent = new Intent(RecivedInvitationsActivity.this, MyLocation.class);
            startActivity(intent);
            RecivedInvitationsActivity.this.finish();
        } else if (id == R.id.home) {
            Intent intent = new Intent(RecivedInvitationsActivity.this, Home.class);
            startActivity(intent);
            RecivedInvitationsActivity.this.finish();
        } else if(id == R.id.nav_friendsInNearby){
            Intent intent = new Intent(RecivedInvitationsActivity.this, FriendsInNearbyActivity.class);
            startActivity(intent);
            RecivedInvitationsActivity.this.finish();
        } else if (id == R.id.nav_logout){
            Intent intent = new Intent(RecivedInvitationsActivity.this, LoginActivity.class);
            stopService(new Intent(this, UpdateUserLocationService.class));
            _firebaseAuth.signOut();
            startActivity(intent);
            RecivedInvitationsActivity.this.finish();
        } else if (id == R.id.settings){
            Intent intent = new Intent(RecivedInvitationsActivity.this, Settings.class);
            startActivity(intent);
            RecivedInvitationsActivity.this.finish();
        } else if(id == R.id.search_all_users) {
            Intent intent = new Intent(RecivedInvitationsActivity.this, SearchAllUsers.class);
            startActivity(intent);
            RecivedInvitationsActivity.this.finish();
        } else if (id == R.id.nav_recived_invitations){
            Intent intent = new Intent(RecivedInvitationsActivity.this, RecivedInvitationsActivity.class);
            startActivity(intent);
            RecivedInvitationsActivity.this.finish();
        } else if (id == R.id.nav_sended_invitations){
            Intent intent = new Intent(RecivedInvitationsActivity.this, SendedInvitationsActivity.class);
            startActivity(intent);
            RecivedInvitationsActivity.this.finish();
        } else if (id == R.id.nav_myfreindsList){
            Intent intent = new Intent(RecivedInvitationsActivity.this, MyFriendsListActivity.class);
            startActivity(intent);
            RecivedInvitationsActivity.this.finish();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    //Add loading view during search processing
                    _listViewUser.addFooterView(_footerView);
                    break;
                case 1:
                    //Update data adapter and UI
                    _userListAdapter.addListItemToAdapter((ArrayList<User>)msg.obj);
                    int count = ((ArrayList<User>) msg.obj).size();
                    if(count == 0){
                        _informText.setVisibility(View.VISIBLE);
                    }
                    _listViewUser.removeFooterView(_footerView);
                    break;
                default:
                    break;
            }
        }
    }

    public class ThreadGetMoreData extends Thread {
        @Override
        public void run() {
            _usersCollectionProvider.getUserRecivedInvitations().addOnCompleteListener(new OnCompleteListener<ArrayList<User>>() {
                @Override
                public void onComplete(@NonNull Task<ArrayList<User>> task) {
                    _handler.sendEmptyMessage(0);
                    ArrayList<User> lstResult = task.getResult();
                    Message msg = _handler.obtainMessage(1, lstResult);
                    _handler.sendMessage(msg);
                }
            });
        }
    }
}
