package com.example.intensiv;

import java.util.ArrayList;
import java.util.List;

public class RootData {
    private List<PointsData> points;

    public RootData() {
        this.points = new ArrayList<>();
    }
    public List<PointsData> getPoints() { return points; }
    public void addRoute(PointsData route) { points.add(route); }
}