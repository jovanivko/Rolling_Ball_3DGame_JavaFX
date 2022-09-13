package com.example.rollingball.arena;

import com.example.rollingball.Utilities;
import javafx.geometry.Bounds;
import javafx.geometry.Point3D;
import javafx.scene.paint.Material;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Translate;

public class Ball extends Sphere {
    private Translate position;
    private Point3D speed;


    public Ball(double radius, Material material, Translate position) {
        super(radius);
        super.setMaterial(material);

        this.position = position;

        super.getTransforms().add(this.position);

        this.speed = new Point3D(0, 0, 0);
    }

    public boolean update(
            double deltaSeconds,
            double top,
            double bottom,
            double left,
            double right,
            double xAngle,
            double zAngle,
            double maxAngleOffset,
            double maxAcceleration,
            double damp
    ) {
        double newPositionX = this.position.getX() + this.speed.getX() * deltaSeconds;
        double newPositionZ = this.position.getZ() + this.speed.getZ() * deltaSeconds;

        this.position.setX(newPositionX);
        this.position.setZ(newPositionZ);

        double accelerationX = maxAcceleration * zAngle / maxAngleOffset;
        double accelerationZ = -maxAcceleration * xAngle / maxAngleOffset;

        double newSpeedX = (this.speed.getX() + accelerationX * deltaSeconds) * damp;
        double newSpeedZ = (this.speed.getZ() + accelerationZ * deltaSeconds) * damp;

        this.speed = new Point3D(newSpeedX, 0, newSpeedZ);

        boolean xOutOfBounds = (newPositionX > right) || (newPositionX < left);
        boolean zOutOfBounds = (newPositionZ > top) || (newPositionZ < bottom);

        return xOutOfBounds || zOutOfBounds;
    }

    public void reset() {
        this.speed = new Point3D(0, 0, 0);
    }

    public void handleCollision(Box fence) {
        Bounds ballBounds = getBoundsInParent();

        double ballCenterX = ballBounds.getCenterX();
        double ballCenterZ = ballBounds.getCenterZ();
        double ballRadius = getRadius();

        Bounds fenceBounds = fence.getBoundsInParent();

        double minX = fenceBounds.getMinX();
        double maxX = fenceBounds.getMaxX();
        double minZ = fenceBounds.getMinZ();
        double maxZ = fenceBounds.getMaxZ();

        double nX = Utilities.clamp(ballCenterX, minX, maxX);
        double nY = Utilities.clamp(ballCenterZ, minZ, maxZ);

        double dx = nX - ballCenterX;
        double dz = nY - ballCenterZ;

        boolean collisionDetected = (Math.sqrt(dx * dx + dz * dz) < ballRadius);

        if (collisionDetected)
            if (nX == maxX || nX == minX) {
                this.speed = new Point3D(-this.speed.getX(), 0, this.speed.getZ());
            } else if (nY == maxZ || nY == minZ) {
                this.speed = new Point3D(this.speed.getX(), 0, -this.speed.getZ());
            }
    }

    public boolean handleCollision(Coin coin) {
        Bounds ballBounds = getBoundsInParent();
        Bounds coinBounds = coin.getBoundsInParent();
        return ballBounds.intersects(coinBounds);
    }

    public void handleCollision(Cylinder obstacle) {
        Bounds ballBounds = getBoundsInParent();

        double ballX = ballBounds.getCenterX();
        double ballZ = ballBounds.getCenterZ();
        double ballRadius = getRadius();

        Bounds obstacleBounds = obstacle.getBoundsInParent();

        double obstacleX = obstacleBounds.getCenterX();
        double obstacleZ = obstacleBounds.getCenterZ();
        double obstacleRadius = obstacle.getRadius();

        double dx = ballX - obstacleX;
        double dz = ballZ - obstacleZ;

        boolean collided = (Math.sqrt(dx * dx + dz * dz) < ballRadius + obstacleRadius);

        if (collided) {
            //radijalna normala
            Point3D normal = (new Point3D(ballX - obstacleX, 0, ballZ - obstacleZ)).normalize();

            double intensity = Math.cos(this.speed.angle(normal) * Math.PI * 2 / 360) * this.speed.magnitude();
            //intenzitet vektora sudara
            this.speed = this.speed.subtract(normal.multiply(2 * intensity));
        }
    }

    public void handleCollisionSpecial(Cylinder cyl) {
        Bounds ballBounds = getBoundsInParent();
        Bounds cylBounds = cyl.getBoundsInParent();
        if (ballBounds.intersects(cylBounds)) {
            this.speed = this.speed.multiply(-2);
        }
    }

}
