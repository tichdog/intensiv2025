package com.example.intensiv;

import java.util.List;

// PointsData.java - для основного объекта points
public class PointsData {
    private int id;
    private String name;
    private List<PointArrayItem> pointsarray;

    // Геттеры и сеттеры
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<PointArrayItem> getPointsarray() { return pointsarray; }
    public void setPointsarray(List<PointArrayItem> pointsarray) {
        this.pointsarray = pointsarray;
    }
}
