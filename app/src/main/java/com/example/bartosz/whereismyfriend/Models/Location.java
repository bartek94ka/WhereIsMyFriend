package com.example.bartosz.whereismyfriend.Models;

/**
 * Created by Bartosz on 01.04.2017.
 */

public class Location {
    public Location(){}
    public Location(double longitude, double latitude){
        Longitude = longitude;
        Latitude = latitude;
    }
    public double Longitude;
    public double Latitude;
}