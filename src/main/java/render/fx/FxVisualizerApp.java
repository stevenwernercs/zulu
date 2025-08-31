package render.fx;

import com.trifidearth.zulu.brain.Brain;
import com.trifidearth.zulu.coordinate.Coordinate;
import com.trifidearth.zulu.coordinate.CoordinateBounds;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.application.ConditionalFeature;
import javafx.geometry.Insets;
import javafx.scene.*;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.*;

public class FxVisualizerApp extends Application {

    private Brain brain;
    private CoordinateBounds bounds;
    private final Group pointsGroup = new Group();
    private final Map<Coordinate, Sphere> pointNodes = new HashMap<>();

    // Camera
    private final PerspectiveCamera camera = new PerspectiveCamera(true);
    private double anchorX, anchorY;
    private double angleX = 20, angleY = 30; // degrees
    private double distance = 250;

    @Override
    public void start(Stage stage) {
        if (!Platform.isSupported(ConditionalFeature.SCENE3D)) {
            System.out.println("JavaFX: SCENE3D not supported on this system. Use the Swing visualizer (default).");
            Stage info = new Stage();
            info.setTitle("JavaFX 3D Not Supported");
            info.setScene(new Scene(new BorderPane(new Label("JavaFX 3D not supported. Use Swing renderer.")), 420, 120));
            info.show();
            return;
        }
        // Root UI
        BorderPane root = new BorderPane();
        SubScene sub = new SubScene(buildWorld(), 1024, 768, true, SceneAntialiasing.BALANCED);
        sub.setFill(Color.color(0.07, 0.07, 0.09));
        sub.setCamera(camera);
        root.setCenter(sub);
        Label help = new Label("Right-drag rotate • Scroll zoom • R reset • ESC quit");
        help.setPadding(new Insets(6));
        help.setTextFill(Color.LIGHTGRAY);
        root.setTop(help);

        Scene scene = new Scene(root);
        stage.setTitle("Zulu JavaFX 3D Visualizer");
        stage.setScene(scene);
        stage.show();

        setupCameraControls(scene);
        setupBrain();
        openLogWindow();
        startRenderLoop();
    }

    private Group buildWorld() {
        Group world = new Group();

        // Axes
        Group axes = new Group();
        Box x = new Box(200, 1, 1); x.setMaterial(new PhongMaterial(Color.RED));
        Box y = new Box(1, 200, 1); y.setMaterial(new PhongMaterial(Color.LIMEGREEN));
        Box z = new Box(1, 1, 200); z.setMaterial(new PhongMaterial(Color.DODGERBLUE));
        axes.getChildren().addAll(x, y, z);

        // Bounds cube (wireframe-like)
        int r = 60;
        Box cube = new Box(r*2, r*2, r*2);
        cube.setMaterial(new PhongMaterial(Color.gray(0.6, 0.15)));
        cube.setDrawMode(DrawMode.LINE);

        world.getChildren().addAll(axes, cube, pointsGroup);

        camera.setNearClip(0.1);
        camera.setFarClip(5000);
        camera.setFieldOfView(60);
        updateCamera();
        return world;
    }

    private void updateCamera() {
        // Position camera on a sphere around origin
        double radX = Math.toRadians(angleX);
        double radY = Math.toRadians(angleY);
        double cx = distance * Math.cos(radX) * Math.sin(radY);
        double cy = distance * Math.sin(radX);
        double cz = distance * Math.cos(radX) * Math.cos(radY);
        camera.getTransforms().setAll(
                new Translate(0, 0, 0), // look at origin
                new Rotate(-angleY, Rotate.Y_AXIS),
                new Rotate(-angleX, Rotate.X_AXIS),
                new Translate(0, 0, -distance)
        );
    }

    private void setupCameraControls(Scene scene) {
        scene.setOnScroll(ev -> {
            distance *= Math.pow(0.90, ev.getDeltaY() / 40.0);
            if (distance < 20) distance = 20;
            if (distance > 2000) distance = 2000;
            updateCamera();
        });
        scene.setOnMousePressed(ev -> {
            if (ev.getButton() == MouseButton.SECONDARY) { anchorX = ev.getSceneX(); anchorY = ev.getSceneY(); }
        });
        scene.setOnMouseDragged(ev -> {
            if (ev.getButton() == MouseButton.SECONDARY) {
                double dx = ev.getSceneX() - anchorX;
                double dy = ev.getSceneY() - anchorY;
                anchorX = ev.getSceneX(); anchorY = ev.getSceneY();
                angleY += dx * 0.5; angleX += dy * 0.5;
                angleX = Math.max(-89, Math.min(89, angleX));
                updateCamera();
            }
        });
        scene.setOnKeyReleased(ev -> {
            if (ev.getCode() == KeyCode.ESCAPE) Platform.exit();
            if (ev.getCode() == KeyCode.R) { angleX = 20; angleY = 30; distance = 250; updateCamera(); }
        });
    }

    private void setupBrain() {
        bounds = new CoordinateBounds(new Coordinate(0, 0, 0), 60);
        try {
            brain = new Brain(bounds, 6, 24, 6);
            brain.start();
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    private void startRenderLoop() {
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                redrawPoints();
            }
        };
        timer.start();
    }

    private void redrawPoints() {
        Map<Coordinate, List<String>> map = brain.getNodeLocationMap();
        // Create or reuse spheres for points
        for (Map.Entry<Coordinate, List<String>> e : map.entrySet()) {
            Coordinate c = e.getKey();
            Sphere s = pointNodes.get(c);
            if (s == null) {
                s = new Sphere(1.8);
                pointNodes.put(c, s);
                pointsGroup.getChildren().add(s);
            }
            s.setTranslateX(c.getX());
            s.setTranslateY(-c.getY()); // invert Y for visual preference
            s.setTranslateZ(c.getZ());
            // Color by label type (pick first)
            Color color = Color.LIGHTGRAY;
            for (String label : e.getValue()) {
                if (label.startsWith("T")) { color = Color.DEEPSKYBLUE; break; }
                if (label.endsWith("_N")) { color = Color.WHITE; break; }
                if (label.endsWith("_D")) { color = Color.LIMEGREEN; break; }
                if (label.endsWith("_A")) { color = Color.ORANGERED; break; }
                if (label.endsWith("_S")) { color = Color.MAGENTA; break; }
            }
            s.setMaterial(new PhongMaterial(color));
        }
        // Optionally could remove old spheres; for now, let GC handle via pointNodes map updates.
    }

    private void openLogWindow() {
        // Optional GUI Brain Console when -Dgui.log=true
        if (Boolean.getBoolean("gui.log")) {
            FxLogWindow log = new FxLogWindow();
            log.show();
            PrintStream ps = new PrintStream(log.asOutputStream(), true);
            brain.out = ps;
            ps.println("JavaFX Brain Console started.");
        } else {
            brain.out = System.out;
            System.out.println("JavaFX renderer started (logging to terminal).");
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
