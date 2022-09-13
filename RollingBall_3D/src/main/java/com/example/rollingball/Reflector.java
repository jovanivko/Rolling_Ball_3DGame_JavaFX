package com.example.rollingball;

import javafx.scene.Group;
import javafx.scene.PointLight;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.transform.Translate;

import java.util.Objects;

public class Reflector extends Group {
    private PhongMaterial mat;
    private Box reflector;
    private PointLight light;
    private double position;
    private Image selfIllumination;

    Reflector(double size, PhongMaterial mat) {
        super();
        this.mat = mat;
        this.reflector = new Box(size, size, size);
        this.reflector.setMaterial(this.mat);
        this.selfIllumination = new Image(Objects.requireNonNull(Main.class.getClassLoader().getResourceAsStream("selfIllumination.png")));
        this.mat.setSelfIlluminationMap(this.selfIllumination);
        super.getChildren().add(reflector);

        this.light = new PointLight(Color.WHITE);
        this.light.setLightOn(true);

        super.getChildren().add(this.light);

    }

    public void turnOn() {
        super.getChildren().add(this.light);
        this.mat.setSelfIlluminationMap(selfIllumination);
    }

    public void turnOff() {
        super.getChildren().remove(this.light);
        this.mat.setSelfIlluminationMap(null);
    }
}
