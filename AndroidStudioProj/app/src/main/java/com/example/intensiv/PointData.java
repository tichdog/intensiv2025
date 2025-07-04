package com.example.intensiv;

class PointData {
    private int id;
    private String title;
    private double lat;
    private double lng;
    private String description;
    private String shrt;
    private String show;

    public int getId() {
        return id;
    }
    public String getShort() {
        return shrt;
    }
    public String getShow() {
        return show;
    }
    public String getTitle() {
        return title;
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }
    public String getDescription(){
        return description;
    }
}