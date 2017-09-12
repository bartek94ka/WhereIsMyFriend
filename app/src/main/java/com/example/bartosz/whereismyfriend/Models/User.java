package com.example.bartosz.whereismyfriend.Models;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Bartosz on 01.04.2017.
 */

public class User{
    public String Id;
    public String Email;
    public String Name;
    public String Surname;
    public String FullName;
    public Double Range;
    public int Age;
    public List<String> FriendsId;
    public List<String> RecivedInvitations;
    public List<String> SendInvitations;

    public User(){}
    public User(String name, String surname,
                Double range, int age){
        Name = name;
        Surname = surname;
        FullName = name + " " + surname;
        Range = range;
        Age = age;
        FriendsId = new ArrayList<>();
        RecivedInvitations = new ArrayList<>();
        SendInvitations = new ArrayList<>();
    }

    public User(String email, String name, String surname,
                Double range, int age){
        Email = email;
        Name = name;
        Surname = surname;
        FullName = name + " " + surname;
        Range = range;
        Age = age;
        FriendsId = new ArrayList<>();
        RecivedInvitations = new ArrayList<>();
        SendInvitations = new ArrayList<>();
    }
}