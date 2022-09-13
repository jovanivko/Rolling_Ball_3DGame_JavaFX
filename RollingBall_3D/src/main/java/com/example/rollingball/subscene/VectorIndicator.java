package com.example.rollingball.subscene;

import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.transform.Translate;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Rotate;

public class VectorIndicator extends Group {
    private final Rectangle vector;
    private final Rotate rotate;
    private final double width;
    private final double height;

    public VectorIndicator(double width, double height) {
        this.width = width;
        this.height = height;

        double vectorLength = height * 0.01;

        Rectangle map = new Rectangle(this.width, this.height, Color.GREEN);

        map.getTransforms().add(new Translate(-this.width / 2, -this.height / 2));
        map.setStroke(Color.RED);
        map.setStrokeWidth(5);
        getChildren().add(map);

        this.vector = new Rectangle(1, vectorLength);
        this.vector.setFill(Color.RED);

        this.rotate = new Rotate(0);

        this.vector.getTransforms().addAll(
                this.rotate,
                new Translate(0, -vectorLength / 2)
        );
        getChildren().add(this.vector);
    }

    public void update(double xAngle, double zAngle, double maxAngleOffset) {
        double xRatio = zAngle / maxAngleOffset;
        double yRatio = xAngle / maxAngleOffset;

        double x = xRatio * this.width / 2;
        double y = yRatio * this.height / 2;

        double length = Math.sqrt(x * x + y * y);
        this.vector.setHeight(length);

        double angle = (new Point2D(x, y)).normalize().angle(new Point2D(1, 0));
        //Pozitivan ugao x negativna vrednost angle
        if (xAngle > 0)
            angle = 360 - angle;

        this.rotate.setAngle(-angle - 90);
    }
}
