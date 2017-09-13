package com.example.bartosz.whereismyfriend;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import com.example.bartosz.whereismyfriend.Models.User;
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

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;

import static android.R.id.list;

public class SearchAllUsers extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private FirebaseAuth _firebaseAuth;
    private FirebaseDatabase database;
    private ListView _listViewUser;
    private UserListAdapter _userListAdapter;
    private List<User> userList;
    private User _currentUser;
    private UserManager _userManager;
    public Handler _handler;
    public View _view;
    public boolean isLoading = false;
    public int currentId=11;

    private Button _searchButton;
    private EditText _searchFullName;

    private Button _inviteUserButton;
    private Button _userDetailsButton;

    private String fullName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_all_users);
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
        database = FirebaseDatabase.getInstance();

        _listViewUser = (ListView) findViewById(R.id.listview_users);

        LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        _view = inflater.inflate(R.layout.footer_view, null);
        _handler = new MyHandler();
        userList = new ArrayList<>();

        //pobranie danych
        _searchButton = (Button) findViewById(R.id.searchButton);
        _searchFullName = (EditText) findViewById(R.id.searchFullName);


        SetSearchButtonAction();

        _userManager = new UserManager();
        _userManager.getUserData(_firebaseAuth.getCurrentUser().getUid()).addOnSuccessListener(new OnSuccessListener<User>() {
            @Override
            public void onSuccess(User user) {
                _currentUser = user;
            }
        });

        _userListAdapter = new UserListAdapter(getApplicationContext(), userList);
        _listViewUser.setAdapter(_userListAdapter);

        _listViewUser.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Do something
                //Ex: display msg with product id get from view.getTag
                Toast.makeText(getApplicationContext(), "Clicked product id =" + view.getTag(), Toast.LENGTH_SHORT).show();
            }
        });
        SetListViewSetOnScrollListener();

    }

    private void SetSearchButtonAction(){
        _searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fullName = _searchFullName.getText().toString().trim();
                boolean isValid = ValidateForm();
                if(isValid){
                    _userListAdapter.ClearList();
                    Thread thread = new ThreadGetMoreData();
                    thread.start();
                }
            }
        });
    }

    private void SetListViewSetOnScrollListener(){
        _listViewUser.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

                int a = totalItemCount;
                //Check when scroll to last item in listview, in this tut, init data in listview = 10 item
/*
                if(view.getLastVisiblePosition() == totalItemCount-1 && _listViewUser.getCount() >=10 && isLoading == false)
*/
/*

                if(view.getLastVisiblePosition() == totalItemCount-1 && _listViewUser.getCount() >= 3 && isLoading == false){
                    isLoading = true;
                    Thread thread = new ThreadGetMoreData();
                    //Start thread
                    thread.start();
                }
*/

            }
        });
    }

    private boolean ValidateForm(){
        if(TextUtils.isEmpty(fullName))
        {
            Toast.makeText(this, "Field can not be empty", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private Task<ArrayList<User>> getUserData()
    {
        final TaskCompletionSource<ArrayList<User>> taskCompletionSource = new TaskCompletionSource<>();
        final ArrayList<User> list = new ArrayList<>();
        final DatabaseReference reference = database.getReference().child("Users");
        reference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot child : dataSnapshot.getChildren()){
                            User user = child.getValue(User.class);
                            boolean isContatin = user.FullName.contains(fullName);
                            boolean isMyUser = child.getKey().equals(_firebaseAuth.getCurrentUser().getUid());
                            boolean isInvitationSend = false;
                            boolean isInvitationRecived = false;
                            boolean isFriend = false;
                            if(_currentUser != null){
                                if(_currentUser.SendInvitations != null){
                                    isInvitationSend = _currentUser.SendInvitations.contains(child.getKey());
                                }
                                if(_currentUser.RecivedInvitations != null){
                                    isInvitationRecived = _currentUser.RecivedInvitations.contains(child.getKey());
                                }
                                if(_currentUser.FriendsId != null){
                                    isFriend = _currentUser.FriendsId.contains(child.getKey());
                                }
                            }
                            if(isContatin == true && isMyUser == false && isInvitationSend == false &&
                                    isInvitationRecived == false && isFriend == false){
                                user.Id = child.getKey();
                                list.add(user);
                            }
                        }
                        taskCompletionSource.setResult(list);
                        reference.removeEventListener(this);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

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
            Intent intent = new Intent(SearchAllUsers.this, MyLocation.class);
            startActivity(intent);
            SearchAllUsers.this.finish();
        } else if (id == R.id.home) {
            Intent intent = new Intent(SearchAllUsers.this, Home.class);
            startActivity(intent);
            SearchAllUsers.this.finish();
        } else if(id == R.id.nav_myfriendlocation){
            Intent intent = new Intent(SearchAllUsers.this, MyFriendLocation.class);
            startActivity(intent);
            SearchAllUsers.this.finish();
        } else if(id == R.id.search_all_users) {
            Intent intent = new Intent(SearchAllUsers.this, SearchAllUsers.class);
            startActivity(intent);
            SearchAllUsers.this.finish();
        } else if (id == R.id.settings){
            Intent intent = new Intent(SearchAllUsers.this, Settings.class);
            startActivity(intent);
            SearchAllUsers.this.finish();
        } else if (id == R.id.nav_logout){
            _firebaseAuth.signOut();
            SearchAllUsers.this.finish();
        } else if (id == R.id.nav_recived_invitations){
            Intent intent = new Intent(SearchAllUsers.this, RecivedInvitationsActivity.class);
            startActivity(intent);
            SearchAllUsers.this.finish();
        } else if (id == R.id.nav_sended_invitations){
            Intent intent = new Intent(SearchAllUsers.this, SendedInvitationsActivity.class);
            startActivity(intent);
            SearchAllUsers.this.finish();
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
                    _listViewUser.addFooterView(_view);
                    break;
                case 1:
                    //Update data adapter and UI
                    _userListAdapter.addListItemToAdapter((ArrayList<User>)msg.obj);
                    //Remove loading view after update listview
                    _listViewUser.removeFooterView(_view);
                    isLoading=false;
                    break;
                default:
                    break;
            }
        }
    }

    public class ThreadGetMoreData extends Thread {
        @Override
        public void run() {
            getUserData().addOnCompleteListener(new OnCompleteListener<ArrayList<User>>() {
                @Override
                public void onComplete(@NonNull Task<ArrayList<User>> task) {
                    _handler.sendEmptyMessage(0);
                    ArrayList<User> lstResult = task.getResult();
                    Message msg = _handler.obtainMessage(1, lstResult);
                    /*try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }*/
                    _handler.sendMessage(msg);
                }
            });
        }
    }
}
