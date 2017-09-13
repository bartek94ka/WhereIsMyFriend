package com.example.bartosz.whereismyfriend;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bartosz.whereismyfriend.Models.User;

import java.util.List;

/**
 * Created by Bartosz on 13.09.2017.
 */

public class UserListRecivedInvitationsAdapter extends BaseAdapter {
    private Context _context;
    private List<User> _users;

    public UserListRecivedInvitationsAdapter(Context context, List<User> users){
        _users = users;
        _context = context;
    }

    public void addListItemToAdapter(List<User> list) {
        //Add list to current array list of data
        _users.addAll(list);
        //Notify UI
        this.notifyDataSetChanged();
    }

    public void deleteListItem(User user){
        _users.remove(user);
        this.notifyDataSetChanged();
    }

    public List<User> GetUserList(){
        return _users;
    }

    public User GetUser(String id){
        for (User user: _users) {
            if(user.Id == id){
                return user;
            }
        }
        return null;
    }

    public void ClearList(){
        _users.clear();
    }

    @Override
    public int getCount() {
        return _users.size();
    }

    @Override
    public Object getItem(int position) {
        return _users.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View v = View.inflate(_context, R.layout.item_user_list_recived, null);
        TextView itemName = (TextView)v.findViewById(R.id.item_name);
        TextView itemSurname = (TextView)v.findViewById(R.id.item_surname);
        TextView itemAge = (TextView)v.findViewById(R.id.item_age);
        final InvitationManager manager = new InvitationManager();
        Button rejectButton = (Button)v.findViewById(R.id.item_reject);
        if(rejectButton != null){
            rejectButton.setTag(_users.get(position).Id);
            rejectButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String userId = (String) v.getTag();
                    User user = GetUser(userId);
                    deleteListItem(user);
                    Toast.makeText(_context, "Invitation has been rejected", Toast.LENGTH_SHORT).show();
                    manager.RejectInvitation(user);
                }
            });
        }
        Button acceptButton = (Button)v.findViewById(R.id.item_accept);
        if(acceptButton != null){
            acceptButton.setTag(_users.get(position).Id);
            acceptButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String userId = (String) v.getTag();
                    User user = GetUser(userId);
                    deleteListItem(user);
                    Toast.makeText(_context, "Invitation has been accepted", Toast.LENGTH_SHORT).show();
                    manager.AcceptInvitation(user);
                }
            });
        }

        //Set text for TextView
        itemName.setText(_users.get(position).Name);
        itemSurname.setText(String.valueOf(_users.get(position).Surname));
        //itemAge.setText(String.valueOf(_users.get(position).Age));

        //Save product id to tag
        v.setTag(_users.get(position));
        return v;
    }
}