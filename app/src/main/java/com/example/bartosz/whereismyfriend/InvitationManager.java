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

    public void SendInvitation(User targetUser){
        taskMap.clear();
        _targetUser = targetUser;
        if(_targetUser.RecivedInvitations == null){
            _targetUser.RecivedInvitations = new ArrayList<String>();
        }
        _targetUser.RecivedInvitations.add(_currentUserId);
        taskMap.put(_targetUser.Id + "/RecivedInvitations", _targetUser.RecivedInvitations);
        Thread thread = new ThreadUpdateData();
        thread.start();
    }
    
    public void SendInvitation(final String targetUserId){
        taskMap.clear();
        Task task1 = GetUserData(_currentUserId).addOnSuccessListener(new OnSuccessListener<User>() {
            @Override
            public void onSuccess(User user) {
                _myUser = user;
                if(_myUser.SendInvitations == null){
                    _myUser.SendInvitations = new ArrayList<String>();
                }
                _myUser.SendInvitations.add(targetUserId);
                taskMap.put(_currentUserId, _myUser);
                if(taskMap.size() == 2){
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
                    reference.updateChildren(taskMap);
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
                taskMap.put(targetUserId, _targetUser);
                if(taskMap.size() == 2){
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
                    reference.updateChildren(taskMap);
                }
            }
        });
    }

    public void AcceptInvitation(User myUser, User userToAccept){
        taskMap.clear();
        if(myUser.FriendsId == null){
            myUser.FriendsId = new ArrayList<String>();
        }
        if(userToAccept.FriendsId == null){
            userToAccept.FriendsId = new ArrayList<String>();
        }
        myUser.FriendsId.add(userToAccept.Id);
        userToAccept.FriendsId.add(myUser.Id);
        myUser.RecivedInvitations.remove(userToAccept.Id);
        userToAccept.SendInvitations.remove(myUser.Id);
        taskMap.put(userToAccept.Id, userToAccept);
        taskMap.put(myUser.Id, myUser);
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.updateChildren(taskMap);
    }

    public void RejectInvitation(User myUser, User userToReject){
        taskMap.clear();
        myUser.RecivedInvitations.remove(userToReject.Id);
        userToReject.SendInvitations.remove(myUser.Id);
        taskMap.put(userToReject.Id, userToReject);
        taskMap.put(myUser.Id, myUser);
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.updateChildren(taskMap);
    }

    private Task<User> GetUserData(String userId) {
        final TaskCompletionSource<User> taskCompletionSource = new TaskCompletionSource<>();
        final DatabaseReference localReference = _firebaseDatabase.getReference("Users").child(userId);
        localReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        User user = dataSnapshot.getValue(User.class);
                        taskCompletionSource.setResult(user);
                        localReference.removeEventListener(this);
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
