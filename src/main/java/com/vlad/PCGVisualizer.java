package com.vlad;

import com.vlad.Model.PCG32;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import com.vlad.Model.PCG32;

public class PCGVisualizer extends Application {

    private static final int WIDTH = 800;
    private static final int HEIGHT = 800;
    private static final int SAMPLES = 300_000;

    @Override
    public void start(Stage stage) {

        Canvas canvas = new Canvas(WIDTH, HEIGHT);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, WIDTH, HEIGHT);

        gc.setFill(Color.WHITE);

        PCG32 rng = new PCG32(42L, 54L);

        for (int i = 0; i < SAMPLES; i++) {
            double x = rng.nextDouble();
            double y = rng.nextDouble();

            double px = x * WIDTH;
            double py = y * HEIGHT;

            gc.fillRect(px, py, 1, 1);
        }

        StackPane root = new StackPane(canvas);
        Scene scene = new Scene(root);

        stage.setTitle("PCG Scatter Plot");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}