package com.example.intensiv;

import java.util.ArrayList;
import java.util.List;

// PointsData.java - для основного объекта points
public class PointsData {
    private int id;
    private String name;
    private List<PointArrayItem> pointsarray;
    public PointsData(int id, String name) {
        this.id = id;
        this.name = name;
        this.pointsarray = new ArrayList<>();
    }

    // Геттеры и сеттеры
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public void addPoint(PointArrayItem point) { pointsarray.add(point); }

    public List<PointArrayItem> getPointsarray() { return pointsarray; }
    public void setPointsarray(List<PointArrayItem> pointsarray) {
        this.pointsarray = pointsarray;
    }
}
