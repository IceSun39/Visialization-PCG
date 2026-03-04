package com.vlad.Controllers;

import com.vlad.Model.PCG32;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.paint.Color;

import javafx.event.ActionEvent;
import java.io.IOException;

public class mainSceneController {

    @FXML
    private Canvas canvas;
    @FXML
    private Button startButton;
    @FXML
    private Button clearButton;
    @FXML
    private Button stopButton;
    @FXML
    private Slider speedSlider;
    @FXML
    private TextArea countDots;
    @FXML
    private Label countPaintedDots;
    @FXML
    private Label errorUnderZeroLabel;
    @FXML
    private Label errorNotNumberLabel;

    private long samples;

    private PCG32 rng;

    private GraphicsContext gc;

    @FXML
    void initialize(){
        gc = canvas.getGraphicsContext2D();

        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        gc.setFill(Color.WHITE);

        rng = new PCG32(42L, 54L);
    }

    @FXML
    public void startDrawing(ActionEvent event) throws IOException {
        String input = countDots.getText();
        try {
            samples = Long.parseLong(input);

            if(samples < 0){
                throw new IllegalArgumentException();
            }

            for (int i = 0; i < samples; i++) {
                double x = rng.nextDouble();
                double y = rng.nextDouble();

                double px = x * canvas.getWidth();
                double py = y * canvas.getHeight();


                gc.fillRect(px, py, 1, 1);
            }
        }catch (NumberFormatException e){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Помилка введення");
            alert.setHeaderText(null);
            alert.setContentText("Кількість точок повинна бути числом!");
            alert.showAndWait();
        }catch (IllegalArgumentException e){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Помилка введення");
            alert.setHeaderText(null);
            alert.setContentText("Кількість точок повинна бути додатним числом!");
            alert.showAndWait();
        }

    }

}
