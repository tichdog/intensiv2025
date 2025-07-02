package com.example.intensiv;

public class PointArrayItem {
    private int id;
    private String title;
    private double lat;
    private String imagePath; // путь к изображению

    private double lng;
    private String description;
    private String shrt;
    private String show;
    private Boolean complete;

    public PointArrayItem(int id, String title, double lat, double lng) {
        this.id = id;
        this.title = title;
        this.lat = lat;
        this.lng = lng;
        this.complete = false;
    }

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

    public String getDescription() {
        return description;
    }

    public Boolean getComplete() {
        return complete;
    }

    public void setComplete(boolean f) {
        complete = f;
    }
    public void setId(int id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setLat(double lat) { this.lat = lat; }
    public void setLng(double lng) { this.lng = lng; }
    public void setShow(String show) { this.show = show; }
    public void setShrt(String shrt) { this.shrt = shrt; }
    public void setDescription(String description) { this.description = description; }

    // добавьте геттер и сеттер
    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

}