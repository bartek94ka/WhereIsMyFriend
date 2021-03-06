package com.example.bartosz.whereismyfriend;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bartosz.whereismyfriend.Models.User;
import com.example.bartosz.whereismyfriend.R;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * Created by Bartosz on 05.09.2017.
 */

public class UserListAdapter extends BaseAdapter {
    private Context _context;
    private List<User> _users;

    public UserListAdapter(Context context, List<User> users){
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
        View v = View.inflate(_context, R.layout.item_user_list, null);
        TextView itemName = (TextView)v.findViewById(R.id.item_name);
        TextView itemSurname = (TextView)v.findViewById(R.id.item_surname);
        TextView itemAge = (TextView)v.findViewById(R.id.item_age);
        final InvitationManager manager = new InvitationManager();
        ImageButton inviteButton = (ImageButton)v.findViewById(R.id.item_invite);
        if(inviteButton != null){
            inviteButton.setTag(_users.get(position).Id);
            inviteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String userId = (String) v.getTag();
                    User user = GetUser(userId);
                    deleteListItem(user);
                    Toast.makeText(_context, "Invitation has been sended", Toast.LENGTH_SHORT).show();
                    manager.SendInvitation(userId);
                }
            });
        }
        ImageButton detailsButton = (ImageButton)v.findViewById(R.id.item_details);
        if(detailsButton != null){
            detailsButton.setTag(_users.get(position).Id);
            detailsButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String userId = (String) v.getTag();
                    User user = GetUser(userId);
                    Intent intent = new Intent(_context, UserDetailsActivity.class);
                    intent.putExtra("userId",userId);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    _context.startActivity(intent);
                }
            });
        }

        //Set text for TextView
        itemName.setText(_users.get(position).Name);
        itemSurname.setText(String.valueOf(_users.get(position).Surname));
        String age = String.valueOf(_users.get(position).Age);
        itemAge.setText("Age: " + age);

        //Save product id to tag
        v.setTag(_users.get(position));
        return v;
    }
}
