package com.example.rollingball;

import javafx.scene.PerspectiveCamera;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.transform.Translate;
import javafx.scene.transform.Rotate;

public class MovingCamera extends PerspectiveCamera {
    private Translate position;

    private Rotate rotateX;

    private Rotate rotateY;

    private double startX;

    private double startY;

    public MovingCamera(boolean fixedEyeAtCameraZero, Translate position, Rotate rotateX) {
        super(fixedEyeAtCameraZero);

        this.position = position;
        this.rotateX = rotateX;
        this.rotateY = new Rotate(0, Rotate.Y_AXIS);

        getTransforms().addAll(
                this.rotateY,
                this.rotateX,
                this.position
        );
    }

    public void handleMouseEvent(MouseEvent event) {
        if (event.getEventType().equals(MouseEvent.MOUSE_PRESSED)) {
            this.startX = event.getSceneX();
            this.startY = event.getSceneY();
        } else if (event.getEventType().equals(MouseEvent.MOUSE_DRAGGED)) {
            double dx = event.getSceneX() - this.startX;
            double dy = event.getSceneY() - this.startY;

            this.startX = event.getSceneX();
            this.startY = event.getSceneY();

            double signX = (dx > 0) ? 1 : -1;
            double signY = (dy > 0) ? 1 : -1;

            double newAngleX = this.rotateX.getAngle() - signY * 0.5;
            double newAngleY = this.rotateY.getAngle() - signX * 0.5;

            this.rotateX.setAngle(Utilities.clamp(newAngleX, -90, 0));
            this.rotateY.setAngle(newAngleY);
        }
    }

    public void handleScrollEvent(ScrollEvent event) {
        if (event.getDeltaY() > 0) {
            this.position.setZ(this.position.getZ() + 50);
        } else {
            this.position.setZ(this.position.getZ() - 50);
        }
    }
}
