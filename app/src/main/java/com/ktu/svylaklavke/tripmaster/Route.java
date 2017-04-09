package com.ktu.svylaklavke.tripmaster;

import android.graphics.Color;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

//Route class that saves info about the route user will travel along
public class Route {
    public Distance distance;
    public Instructions instructions;
    public Duration duration;
    public String endAddress;
    public LatLng endLocation;
    public String startAddress;
    public LatLng startLocation;
    public String description;
    public List<LatLng> points;
    public ArrayList<InterestingPoint> points_of_interest = new ArrayList<>();
    public boolean mainRoute = false;
    private int basicPolylineWidth = 10;
    String[] colorCode = {"#39add1" , "#b7c0c7"}; //0 - melyna, 1 - pilka
    public int getRouteColor(){
        if (mainRoute)
            return (Color.parseColor(colorCode[0]));
        return (Color.parseColor(colorCode[1]));
    }

    public int getWidth(){
        if (mainRoute)
            return (basicPolylineWidth * 3);
        return  (basicPolylineWidth * 2);
    }
}