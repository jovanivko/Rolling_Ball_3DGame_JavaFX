package com.example.rollingball.arena;

import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;

public class Coin extends Cylinder {
    private double radius;
    private double height;
    private PhongMaterial mat;
    private int points;

    public Coin(double radius, double height, PhongMaterial mat, int points) {
        super(radius, height);
        this.radius = radius;
        this.height = height;
        this.points = points;
        this.mat = mat;
        super.setMaterial(mat);
    }

    public int getPoints() {
        return points;
    }
}
