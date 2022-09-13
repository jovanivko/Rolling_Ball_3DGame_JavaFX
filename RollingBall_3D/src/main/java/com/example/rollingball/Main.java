package com.example.rollingball;

import com.example.rollingball.arena.Arena;
import com.example.rollingball.arena.Ball;
import com.example.rollingball.arena.Coin;
import com.example.rollingball.arena.Hole;
import com.example.rollingball.shapes.Heart;
import com.example.rollingball.subscene.BallSelectionScene;
import com.example.rollingball.subscene.VectorIndicator;
import com.example.rollingball.timer.Timer;

import java.util.Arrays;
import java.util.Objects;

import javafx.animation.*;
import javafx.application.Application;
import javafx.scene.Camera;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.PointLight;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class Main extends Application {
    private static final double WINDOW_SIZE = 800;

    private static final double PODIUM_SIZE = 2000;

    private static final double PODIUM_HEIGHT = 10;

    private static final double CAMERA_FAR_CLIP = 100000;

    private static final double CAMERA_Z = -5000;

    private static final double CAMERA_X_ANGLE = -45;

    private static final double BALL_RADIUS = 50;

    private static final double BALL_DAMP = 0.999;

    private static final double MAX_ANGLE_OFFSET = 30;
    private static final double HOLE_RADIUS = 100;

    private static final double HOLE_HEIGHT = 10;

    private static final double ARENA_DAMP = 0.995;

    private static final double BIRD_VIEW_CAMERA_Y = -2500;
    private static final double FENCE_WIDTH = 10;
    private static final double FENCE_HEIGHT = 100;
    private static final double FENCE_LENGTH = 1000;
    private static final double VECTOR_INDICATOR_SIZE = 150;
    private static final double COIN_RADIUS = 50;
    private static final int NUMBER_OF_HOLES = 2;
    private static final double COIN_HEIGHT = 10;
    private static final int COIN_POINTS = 5;
    private static final double REFLECTOR_LIGHT_SIZE = 100;

    private static final double REFLECTOR_POSITION = 1000;
    private static final double OBSTACLE_RADIUS = 50;

    private static final double OBSTACLE_HEIGHT = 200;

    private static final double MAX_TIME = 60;

    private Group root;

    private Ball ball;

    private Arena arena;

    private Hole[] holes;

    private SubScene scene;

    private MovingCamera defaultCamera;

    private Camera birdViewCamera;

    private Box[] fences;

    private Group overlay;

    private Heart[] lives;
    private int remaining_lives;
    private Translate ballPosition;

    private int points;
    private double remaining_time;
    private Text pointsText;
    private Text timeText;
    private Coin[] coins;

    private PhongMaterial reflectorMaterial;

    private Reflector reflector;
    private PointLight pointLight;
    private boolean reflectorOn;
    private VectorIndicator vectorIndicator;
    private Cylinder[] obstacles;
    private boolean gameEnd;
    private PhongMaterial ballMaterial;
    private double ballAcceleration;
    private Cylinder[] reflective;

    //Pravljenje Overlay scene
    private SubScene createOverlayScene() {
        this.overlay = new Group();
        SubScene subScene = new SubScene(this.overlay, WINDOW_SIZE, WINDOW_SIZE);

        //pravljenje srca
        this.lives = new Heart[5];
        this.remaining_lives = 5;
        double heart_width = new Heart().getBoundsInParent().getWidth();
        for (int i = 0; i < this.lives.length; i++) {
            this.lives[i] = new Heart();
            this.lives[i].getTransforms().add(new Translate(heart_width + (2 * i) * heart_width, heart_width));
        }
        this.overlay.getChildren().addAll(this.lives);

        this.pointsText = new Text(Integer.toString(this.points));
        this.pointsText.setFont(new Font(30));
        this.pointsText.setFill(Color.YELLOW);
        this.pointsText.setTextAlignment(TextAlignment.LEFT);

        this.pointsText.getTransforms().add(new Translate(WINDOW_SIZE - 2 * this.pointsText.getLayoutBounds().getWidth(), this.pointsText.getLayoutBounds().getHeight()));
        this.overlay.getChildren().add(this.pointsText);

		/*Label timeLabel = new Label("Remaining time: ");
		timeLabel.setFont(new Font(30));
		timeLabel.setTextFill(Color.RED);
		timeLabel.getTransforms().add(
				new Translate(WINDOW_WIDTH/2 - timeLabel.getLayoutBounds().getWidth(), timeLabel.getLayoutBounds().getHeight())
		);
		this.overlay.getChildren().add(timeLabel);*/
        this.remaining_time = MAX_TIME;
        this.timeText = new Text("Remaining time: " + String.format("%.2f", this.remaining_time));
        this.timeText.setFont(new Font(30));
        this.timeText.setFill(Color.RED);
        this.timeText.getTransforms().add(new Translate(WINDOW_SIZE / 2 - timeText.getLayoutBounds().getWidth() / 2, timeText.getLayoutBounds().getHeight()));
        this.overlay.getChildren().add(this.timeText);

        this.vectorIndicator = new VectorIndicator(VECTOR_INDICATOR_SIZE, VECTOR_INDICATOR_SIZE);
        this.vectorIndicator.getTransforms().addAll(new Translate(0, WINDOW_SIZE - VECTOR_INDICATOR_SIZE), new Translate(VECTOR_INDICATOR_SIZE / 2, VECTOR_INDICATOR_SIZE / 2));
        this.overlay.getChildren().add(this.vectorIndicator);
        return subScene;
    }

    private boolean endAttempt() {
        if (this.remaining_lives != 0 && this.remaining_time > 0) {
            this.ballPosition.setX(-900);
            this.ballPosition.setY(-55);
            this.ballPosition.setZ(900);
            this.lives[--this.remaining_lives].setVisible(false);

            this.ball.reset();
            this.arena.reset();
        }
        if (this.remaining_lives == 0 || this.remaining_time == 0) {
            Text text = new Text("Kraj igre");
            text.setFont(new Font(30));
            text.setFill(Color.RED);
            text.getTransforms().add(new Translate((WINDOW_SIZE - text

                    .getLayoutBounds().getWidth()) / 2, (WINDOW_SIZE - text.getLayoutBounds().getHeight()) / 2));
            this.overlay.getChildren().add(text);
            this.gameEnd = true;
        }
        return (this.remaining_lives == 0 || this.remaining_time == 0);
    }

    private void addPoints(int numberOfPoints) {
        this.points += numberOfPoints;
        this.pointsText.setText(Integer.toString(this.points));
    }

    private void addCoins() {
        PhongMaterial material = new PhongMaterial(Color.GOLD);
        this.coins = new Coin[4];
        for (int i = 0; i < this.coins.length; i++) {

            this.coins[i] = new Coin(COIN_RADIUS, COIN_HEIGHT, material, COIN_POINTS);

            Rotate rotateY = new Rotate(0, Rotate.Y_AXIS);
            this.coins[i].getTransforms().addAll(new Translate(0, -COIN_HEIGHT, 0), new Rotate(90 * i, Rotate.Y_AXIS), new Translate(PODIUM_SIZE / 4, 0, 0), new Translate(0, -COIN_RADIUS - 5, 0), rotateY, new Rotate(90, Rotate.Z_AXIS));
			/*RotateTransition rotateTransition = new RotateTransition(Duration.seconds(5), this.coins[i]);
			rotateTransition.setAxis(Rotate.Y_AXIS);
			rotateTransition.setFromAngle(0);
			rotateTransition.setToAngle(360);
			rotateTransition.setInterpolator(Interpolator.LINEAR);*/

            TranslateTransition animation = new TranslateTransition(Duration.seconds(5), this.coins[i]);
            animation.setFromY(-COIN_HEIGHT);
            animation.setToY(-10 * COIN_HEIGHT);
            animation.setInterpolator(Interpolator.LINEAR);

            /*ParallelTransition animation = new ParallelTransition(rotateTransition, translateTransition);*/
            animation.setAutoReverse(true);
            animation.setCycleCount(Animation.INDEFINITE);
            animation.play();

            Timeline timeline = new Timeline(new KeyFrame(Duration.ZERO, new KeyValue(rotateY.angleProperty(), 0, Interpolator.LINEAR)), new KeyFrame(Duration.seconds(6), new KeyValue(rotateY.angleProperty(), 360, Interpolator.LINEAR)));
            timeline.setCycleCount(Animation.INDEFINITE);
            timeline.play();
        }
        this.arena.getChildren().addAll(this.coins);
    }

    private void addFences() {
        this.fences = new Box[4];
        PhongMaterial fenceMaterial = new PhongMaterial(Color.BROWN);
        for (int i = 0; i < this.fences.length; i++) {
            this.fences[i] = new Box(FENCE_WIDTH, FENCE_HEIGHT, FENCE_LENGTH);
            this.fences[i].setMaterial(fenceMaterial);
            this.fences[i].getTransforms().addAll(new Rotate(i * 90, Rotate.Y_AXIS), new Translate(PODIUM_SIZE / 2 - FENCE_WIDTH, -FENCE_HEIGHT / 2 - 5, 0));
        }
        this.arena.getChildren().addAll(this.fences);
    }

    private void addReflector() {
        this.reflectorMaterial = new PhongMaterial(Color.DARKGRAY);
        this.reflector = new Reflector(REFLECTOR_LIGHT_SIZE, reflectorMaterial);
        this.reflector.getTransforms().add(new Translate(0, -REFLECTOR_POSITION, 0));
        //this.reflector.turnOn();
        this.reflectorOn = true;

        this.root.getChildren().add(this.reflector);
    }

    private void addObstacles() {
        this.obstacles = new Cylinder[4];

        Image obstacleImage = new Image(Objects.requireNonNull(Main.class.getClassLoader().getResourceAsStream("obstacle.jpg")));
        PhongMaterial obstacleMaterial = new PhongMaterial();
        obstacleMaterial.setDiffuseMap(obstacleImage);

        for (int i = 0; i < this.obstacles.length; i++) {
            this.obstacles[i] = new Cylinder(OBSTACLE_RADIUS, OBSTACLE_HEIGHT);
            this.obstacles[i].setMaterial(obstacleMaterial);
            this.obstacles[i].getTransforms().addAll(new Rotate(90 * i, Rotate.Y_AXIS), new Translate(PODIUM_SIZE / 4, -OBSTACLE_RADIUS * 2 - 5, PODIUM_SIZE / 4));
        }
        this.arena.getChildren().addAll(this.obstacles);
    }

    private void addHoles() {

        this.holes = new Hole[NUMBER_OF_HOLES];

        PhongMaterial phongMaterial2 = new PhongMaterial(Color.YELLOW);
        this.holes[0] = new Hole(HOLE_RADIUS, HOLE_HEIGHT, phongMaterial2, new Translate(PODIUM_SIZE / 2 - 2 * HOLE_RADIUS, -30, -PODIUM_SIZE / 2 + 2 * HOLE_RADIUS), 5);
        this.holes[1] = new Hole(HOLE_RADIUS, HOLE_HEIGHT, phongMaterial2, new Translate(0, -30, 0), -5);

        this.arena.getChildren().addAll(this.holes);
    }

    private void addSpecial() {
        PhongMaterial mat = new PhongMaterial(Color.GREEN);
        this.reflective = new Cylinder[2];
        for (int i = 0; i < this.reflective.length; i++) {
            this.reflective[i] = new Cylinder(OBSTACLE_RADIUS, OBSTACLE_HEIGHT / 2);
            this.reflective[i].setMaterial(mat);
            this.reflective[i].getTransforms().addAll(new Translate(-PODIUM_SIZE / 4 - (i == 0 ? 2 * OBSTACLE_RADIUS : -2 * OBSTACLE_RADIUS) + i * (PODIUM_SIZE / 2), -OBSTACLE_RADIUS - 5, 0));
        }
        this.arena.getChildren().addAll(this.reflective);
    }

    public void start(Stage stage) {
        this.root = new Group();
        this.gameEnd = false;

        Stage selectionStage = new Stage(StageStyle.UNDECORATED);
        selectionStage.initModality(Modality.APPLICATION_MODAL);
        selectionStage.initOwner(stage);
        BallSelectionScene ballSelection = new BallSelectionScene(BALL_RADIUS);
        selectionStage.setScene(ballSelection);
        selectionStage.showAndWait();
        this.ballAcceleration = ballSelection.getSelectedMaximumSpeed();
        this.ballMaterial = ballSelection.getSelectedBall();


        this.scene = new SubScene(this.root, WINDOW_SIZE, WINDOW_SIZE, true, SceneAntialiasing.BALANCED);

        Box podium = new Box(PODIUM_SIZE, PODIUM_HEIGHT, PODIUM_SIZE);
        podium.setMaterial(new PhongMaterial(Color.BLUE));

        Rotate defaultCameraRotateX = new Rotate(CAMERA_X_ANGLE, Rotate.X_AXIS);
        Translate defaultCameraPosition = new Translate(0, 0, CAMERA_Z);
        this.defaultCamera = new MovingCamera(true, defaultCameraPosition, defaultCameraRotateX);
        this.defaultCamera.setFarClip(CAMERA_FAR_CLIP);

        this.root.getChildren().add(this.defaultCamera);

        this.scene.setCamera(this.defaultCamera);

        this.ballPosition = new Translate(-PODIUM_SIZE / 2 + 2 * BALL_RADIUS, -BALL_RADIUS - 5, PODIUM_SIZE / 2 - 2 * BALL_RADIUS);

        this.ball = new Ball(BALL_RADIUS, this.ballMaterial, this.ballPosition);


        this.birdViewCamera = new PerspectiveCamera(true);
        this.birdViewCamera.setFarClip(CAMERA_FAR_CLIP);

        Translate birdViewCameraPosition = new Translate(0, BIRD_VIEW_CAMERA_Y, 0);
        this.birdViewCamera.getTransforms().addAll(birdViewCameraPosition, this.ballPosition, new Rotate(-90, Rotate.X_AXIS));

        this.arena = new Arena();
        this.arena.getChildren().add(podium);
        this.arena.getChildren().add(this.ball);
        this.arena.getChildren().add(this.birdViewCamera);

        addHoles();
        addFences();
        addCoins();
        addReflector();
        addObstacles();
        addSpecial();

        this.root.getChildren().add(this.arena);


        Timer timer = new Timer(deltaSeconds -> {
            this.arena.update(ARENA_DAMP);
            this.vectorIndicator.update(this.arena.getXAngle(), this.arena.getZAngle(), 30);
            this.remaining_time = Utilities.clamp(this.remaining_time - deltaSeconds, 0, MAX_TIME);
            if (!this.gameEnd) this.timeText.setText("Remaining time: " + String.format("%.2f", this.remaining_time));
            if (this.remaining_time == 0) {
                endAttempt();
            }
            if (this.ball != null) {
                Arrays.stream(this.fences).forEach(fence -> ball.handleCollision(fence));
                for (int i = 0; i < this.coins.length; i++) {
                    if (this.coins[i] != null && this.ball.handleCollision(this.coins[i])) {
                        addPoints(this.coins[i].getPoints());
                        this.arena.getChildren().remove(this.coins[i]);
                        this.coins[i] = null;
                    }
                }
                Arrays.stream(this.obstacles).forEach(obstacle -> ball.handleCollision(obstacle));
                boolean outOfArena = this.ball.update(deltaSeconds, PODIUM_SIZE / 2, -PODIUM_SIZE / 2, -PODIUM_SIZE / 2, PODIUM_SIZE / 2, this.arena.getXAngle(), this.arena.getZAngle(), MAX_ANGLE_OFFSET, this.ballAcceleration, BALL_DAMP);
                boolean isInHole = false;
                for (Hole h : holes) {
                    if (h.handleCollision(this.ball) && !this.gameEnd) {
                        isInHole = true;
                        addPoints(h.getPoints());
                    }
                }
                Arrays.stream(this.reflective).forEach(r -> {
                    this.ball.handleCollisionSpecial(r);
                });
                if ((outOfArena || isInHole) && endAttempt()) {
                    this.arena.getChildren().remove(this.ball);
                    this.ball = null;
                }
            }
        });
        timer.start();

        //Zasto ne radi obicno dodavanje subscene sceni
        Scene wrapper = new Scene(new Group(this.scene, createOverlayScene()), WINDOW_SIZE, WINDOW_SIZE, true, SceneAntialiasing.BALANCED);

        wrapper.addEventHandler(KeyEvent.ANY, event -> this.arena.handleKeyEvent(event, 30));

        wrapper.addEventHandler(KeyEvent.ANY, this::handleKeyEvent);

        wrapper.addEventHandler(MouseEvent.ANY, event -> this.defaultCamera.handleMouseEvent(event));

        wrapper.addEventHandler(ScrollEvent.ANY, event -> this.defaultCamera.handleScrollEvent(event));

        Image background = new Image(Objects.requireNonNull(Main.class.getClassLoader().getResourceAsStream("background.jpg")));
        wrapper.setFill(new ImagePattern(background));

        stage.setTitle("Rolling Ball");
        stage.setResizable(false);
        stage.setScene(wrapper);
        stage.show();
    }

    private void handleKeyEvent(KeyEvent event) {
        if (event.getEventType().equals(KeyEvent.KEY_PRESSED))
            if (event.getCode().equals(KeyCode.DIGIT1) || event.getCode().equals(KeyCode.NUMPAD1)) {
                this.scene.setCamera(this.defaultCamera);
            } else if (event.getCode().equals(KeyCode.DIGIT2) || event.getCode().equals(KeyCode.NUMPAD2)) {
                this.scene.setCamera(this.birdViewCamera);
            } else if (event.getCode().equals(KeyCode.DIGIT0) || event.getCode().equals(KeyCode.NUMPAD0)) {
                if (this.reflectorOn) {
                    this.reflector.turnOff();
                } else {
                    this.reflector.turnOn();
                }
                this.reflectorOn = !this.reflectorOn;
            }
    }

    public static void main(String[] args) {
        launch();
    }
}
