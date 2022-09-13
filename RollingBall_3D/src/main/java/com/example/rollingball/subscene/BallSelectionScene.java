package com.example.rollingball.subscene;

import com.example.rollingball.arena.Ball;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.transform.Translate;

public class BallSelectionScene extends PickScene {
    private static final double MAXIMUM_BALL_ACC1 = 300;
    private static final double MAXIMUM_BALL_ACC2 = 400;
    private static final double MAXIMUM_BALL_ACC3 = 600;

    private final Ball cannon1;
    private final Ball cannon2;
    private final Ball cannon3;

    public BallSelectionScene(double width) {
        super();
        this.cannon1 = new Ball(width, new PhongMaterial(Color.RED), new Translate(150, 200 + 2 * width));
        this.cannon2 = new Ball(width, new PhongMaterial(Color.GREEN), new Translate(400, 200 + 2 * width));
        this.cannon3 = new Ball(width, new PhongMaterial(Color.BLUE), new Translate(650, 200 + 2 * width));
        super.root.getChildren().addAll(cannon1, cannon2, cannon3);
    }

    public double getSelectedMaximumSpeed() {
        double res;
        switch (super.active) {
            case 0:
                res = MAXIMUM_BALL_ACC1;
                break;
            case 1:
                res = MAXIMUM_BALL_ACC2;
                break;
            case 2:
                res = MAXIMUM_BALL_ACC3;
                break;
            default:
                res = MAXIMUM_BALL_ACC2;
        }
        return res;
    }

    public PhongMaterial getSelectedBall() {
        switch (super.active) {
            case 0:
                return (PhongMaterial) cannon1.getMaterial();
            case 1:
                return (PhongMaterial) cannon2.getMaterial();
            case 2:
                return (PhongMaterial) cannon3.getMaterial();
            default:
                return (PhongMaterial) cannon2.getMaterial();
        }
    }
}
