package com.example.bartosz.whereismyfriend.Models;

/**
 * Created by Bartosz on 01.04.2017.
 */

public class Invitation {
    public Invitation(){}
    public Invitation(String userId){
        UserId = userId;
    }
    public String UserId;
    public int Status;
}