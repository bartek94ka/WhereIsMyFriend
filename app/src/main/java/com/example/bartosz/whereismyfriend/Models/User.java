package com.example.bartosz.whereismyfriend.Models;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by Bartosz on 01.04.2017.
 */

public class User{
    public String Email;
    public String Name;
    public String Surname;
    public String FullName;
    public Double Range;
    public int Age;
    public Collection<String> FriendsId;
    public Collection<Invitation> RecivedInvitations;
    public Collection<Invitation> SendInvitations;

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
}