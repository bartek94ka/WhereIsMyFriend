package com.example.bartosz.whereismyfriend.Models;

import java.util.Collection;

/**
 * Created by Bartosz on 01.04.2017.
 */

public class User {
    public User(){}
    public User(String userName, String email, String password, String name, String surname,
                Double range, int age, Location currentLocation){
        UserName = userName;
        Email = email;
        Password = password;
        Name = name;
        Surname = surname;
        FullName = name + " " + surname;
        Range = range;
        Age = age;
        CurrentLocation = currentLocation;
        FriendsId = null;
        RecivedInvitations = null;
        SendInvitations = null;
    }
    public String UserName;
    public String Email;
    public String Password;
    public String Name;
    public String Surname;
    public String FullName;
    public Double Range;
    public int Age;
    public Location CurrentLocation;
    public double latitude;
    public double longitude;
    public String id;
    public Collection<String> FriendsId;
    public Collection<Invitation> RecivedInvitations;
    public Collection<Invitation> SendInvitations;
}