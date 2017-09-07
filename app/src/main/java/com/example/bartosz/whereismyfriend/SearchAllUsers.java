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
import android.text.TextUtils;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import com.example.bartosz.whereismyfriend.Models.User;
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

import static android.R.id.list;

public class SearchAllUsers extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private FirebaseAuth _firebaseAuth;
    private FirebaseDatabase database;
    private ListView _listViewUser;
    private UserListAdapter _userListAdapter;
    private List<User> userList;
    public Handler _handler;
    public View _view;
    public boolean isLoading = false;
    public int currentId=11;

    private Button _searchButton;
    private EditText _searchName;
    private EditText _searchSurname;
    private EditText _searchAge;



    private String name;
    private String surname;
    private String age;

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
        _searchName = (EditText) findViewById(R.id.searchName);
        _searchSurname = (EditText) findViewById(R.id.searchSurname);
        _searchAge = (EditText) findViewById(R.id.searchAge);

        _searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                name = _searchName.getText().toString().trim();
                surname = _searchSurname.getText().toString().trim();
                age = _searchAge.getText().toString().trim();
                boolean isValid = ValidateForm();
                if(isValid){
                    _userListAdapter.ClearList();
                    Thread thread = new ThreadGetMoreData();
                    thread.start();
                }
            }
        });

        _userListAdapter = new UserListAdapter(getApplicationContext(), userList);
        _listViewUser.setAdapter(_userListAdapter);

        _listViewUser.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

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
        if(TextUtils.isEmpty(name) && TextUtils.isEmpty(surname) && TextUtils.isEmpty(age))
        {
            Toast.makeText(this, "Fields can not be empty", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private Task<ArrayList<User>> getUserData()
    {
        final TaskCompletionSource<ArrayList<User>> taskCompletionSource = new TaskCompletionSource<>();
        final ArrayList<User> list = new ArrayList<>();
        DatabaseReference reference = database.getReference();
        reference.child("Users").
                addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot child : dataSnapshot.getChildren()){
                            User user = child.getValue(User.class);

                            boolean equals = user.Name.equals(name);
                            if(equals == true){
                                list.add(user);
                            }
                        }
                        taskCompletionSource.setResult(list);
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
            // Handle the camera action
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
        } else if (id == R.id.nav_logout){
            _firebaseAuth.signOut();
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
            //Add footer view after get data
            //Search more data
            final ArrayList<User> lstResult;
            getUserData().addOnCompleteListener(new OnCompleteListener<ArrayList<User>>() {
                @Override
                public void onComplete(@NonNull Task<ArrayList<User>> task) {
                    _handler.sendEmptyMessage(0);
                    ArrayList<User> lstResult = task.getResult();
                    Message msg = _handler.obtainMessage(1, lstResult);
                    _handler.sendMessage(msg);
                }
            });
            //Delay time to show loading footer when debug, remove it when release
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
