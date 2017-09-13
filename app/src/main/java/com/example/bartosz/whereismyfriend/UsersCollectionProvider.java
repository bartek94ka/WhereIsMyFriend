package com.example.bartosz.whereismyfriend;

import com.example.bartosz.whereismyfriend.Models.User;
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

/**
 * Created by Bartosz on 12.09.2017.
 */

public class UsersCollectionProvider {
    private FirebaseAuth _firebaseAuth;
    private FirebaseDatabase _firebaseDatabase;
    private String _currentUserId;

    public UsersCollectionProvider(){
        _firebaseAuth = FirebaseAuth.getInstance();
        _firebaseDatabase = FirebaseDatabase.getInstance();
        _currentUserId = _firebaseAuth.getCurrentUser().getUid();
    }

    public Task<ArrayList<User>> getUserRecivedInvitations() {
        UserManager userManager = new UserManager();
        final TaskCompletionSource<ArrayList<User>> taskCompletionSource = new TaskCompletionSource<>();
        final ArrayList<User> list = new ArrayList<>();
        final DatabaseReference reference = _firebaseDatabase.getReference().child("Users");
        userManager.getUserData(_currentUserId).addOnSuccessListener(new OnSuccessListener<User>() {
            @Override
            public void onSuccess(User user) {
                final User _currentUser = user;
                reference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot child : dataSnapshot.getChildren()){
                            User user = child.getValue(User.class);
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
                            if(isMyUser == false && isInvitationSend == false &&
                                    isInvitationRecived == true && isFriend == false){
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
            }
        });
        return taskCompletionSource.getTask();
    }

    public Task<ArrayList<User>> getUserSendedInvitations() {
        UserManager userManager = new UserManager();
        final TaskCompletionSource<ArrayList<User>> taskCompletionSource = new TaskCompletionSource<>();
        final ArrayList<User> list = new ArrayList<>();
        final DatabaseReference reference = _firebaseDatabase.getReference().child("Users");
        userManager.getUserData(_currentUserId).addOnSuccessListener(new OnSuccessListener<User>() {
            @Override
            public void onSuccess(User user) {
                final User _currentUser = user;
                reference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot child : dataSnapshot.getChildren()){
                            User user = child.getValue(User.class);
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
                            if(isMyUser == false && isInvitationSend == true &&
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
            }
        });
        return taskCompletionSource.getTask();
    }

    public Task<ArrayList<User>> getUserFriends() {
        UserManager userManager = new UserManager();
        final TaskCompletionSource<ArrayList<User>> taskCompletionSource = new TaskCompletionSource<>();
        final ArrayList<User> list = new ArrayList<>();
        final DatabaseReference reference = _firebaseDatabase.getReference().child("Users");
        userManager.getUserData(_currentUserId).addOnSuccessListener(new OnSuccessListener<User>() {
            @Override
            public void onSuccess(User user) {
                final User _currentUser = user;
                reference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot child : dataSnapshot.getChildren()){
                            User user = child.getValue(User.class);
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
                            if(isMyUser == false && isInvitationSend == false &&
                                    isInvitationRecived == false && isFriend == true){
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
            }
        });
        return taskCompletionSource.getTask();
    }
}
