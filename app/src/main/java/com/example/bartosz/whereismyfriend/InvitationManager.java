package com.example.bartosz.whereismyfriend;

import android.support.annotation.NonNull;

import com.example.bartosz.whereismyfriend.Models.Invitation;
import com.example.bartosz.whereismyfriend.Models.User;
import com.google.android.gms.tasks.Continuation;
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
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Bartosz on 11.09.2017.
 */

public class InvitationManager {

    private FirebaseAuth _firebaseAuth;
    private FirebaseDatabase _firebaseDatabase;
    private String _currentUserId;
    private User _myUser;
    private User _targetUser;
    private boolean HasDataBeenLoaded = false;
    private Map<String,Object> taskMap = new HashMap<String,Object>();


    public InvitationManager(){
        _firebaseAuth = FirebaseAuth.getInstance();
        _firebaseDatabase = FirebaseDatabase.getInstance();
        _currentUserId = _firebaseAuth.getCurrentUser().getUid();
    }

    public void SendInvitation(final String targetUserId){
        Task task1 = GetUserData(_currentUserId).addOnSuccessListener(new OnSuccessListener<User>() {
            @Override
            public void onSuccess(User user) {
                _myUser = user;
                if(_myUser.SendInvitations == null){
                    _myUser.SendInvitations = new ArrayList<String>();
                }
                _myUser.SendInvitations.add(targetUserId);
                taskMap.put(_currentUserId + "/SendInvitations", _myUser.SendInvitations);
                if(taskMap.size() == 2){
                    HasDataBeenLoaded = true;
                    Thread thread = new ThreadUpdateData();
                    thread.start();
                }
            }
        });

         Task task2 = GetUserData(targetUserId).addOnSuccessListener(new OnSuccessListener<User>() {
            @Override
            public void onSuccess(User user) {
                _targetUser = user;
                if(_targetUser.RecivedInvitations == null){
                    _targetUser.RecivedInvitations = new ArrayList<String>();
                }
                _targetUser.RecivedInvitations.add(_currentUserId);
                taskMap.put(targetUserId + "/RecivedInvitations", _targetUser.RecivedInvitations);
                if(taskMap.size() == 2){
                    HasDataBeenLoaded = true;
                    Thread thread = new ThreadUpdateData();
                    thread.start();
                }
            }
        });
    }

    private Task<User> GetUserData(String userId) {
        final TaskCompletionSource<User> taskCompletionSource = new TaskCompletionSource<>();
        _firebaseDatabase.getReference("Users").child(userId).
                addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        User user = dataSnapshot.getValue(User.class);
                        if(HasDataBeenLoaded == false){
                            taskCompletionSource.setResult(user);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
        return taskCompletionSource.getTask();
    }

    public class ThreadUpdateData extends Thread {
        @Override
        public void run() {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
            reference.updateChildren(taskMap);
        }
    }
}
