package com.vlad.Controllers;

import com.vlad.Model.GeneratorContext;
import com.vlad.Model.IterationState;
import com.vlad.Model.PCG32;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.paint.Color;

import javafx.event.ActionEvent;
import java.io.IOException;

public class mainSceneController {

    @FXML
    private Canvas mainCanvas;
    @FXML
    private Button startButton;
    @FXML
    private Button clearButton;
    @FXML
    private Button stopButton;
    @FXML
    private Button continueButton;
    @FXML
    private Slider speedSlider;
    @FXML
    private TextArea countArea  ;
    @FXML
    private  TextArea seedArea;
    @FXML
    private Label countPaintedDots;
    @FXML
    private Label errorUnderZeroLabel;
    @FXML
    private Label errorNotNumberLabel;

    private long paintedDots;

    private PCG32 rng;

    private GraphicsContext gc;

    private volatile boolean isRunning = false;

    @FXML
    void initialize(){
        gc = mainCanvas.getGraphicsContext2D();

        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, mainCanvas.getWidth(), mainCanvas.getHeight());
        gc.setFill(Color.WHITE);

        paintedDots = 0;
    }

    private void showError(String massage){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Помилка введення");
        alert.setHeaderText(null);
        alert.setContentText(massage);
        alert.showAndWait();
    }

    private long checkSamples() {
        long samples = 0;
        try {
            String input = countArea.getText().trim();
            samples = Long.parseLong(input);

            if (samples <= 0) {
                throw new IllegalArgumentException();
            }

        // Якщо нічого не ввели або ввели не число
        } catch (NumberFormatException e) {
            showError("Кількість точок повинна бути числом!");
            isRunning = false;
        // Якщо ввели від'ємне число
        } catch (IllegalArgumentException e) {
            showError("Кількість точок повинна бути більше за нуль!");
            isRunning = false;
        }

        return samples;
    }

    private long checkSeed(){
        long seed = 0;
        try {
            String input = seedArea.getText().trim();
            seed = Long.parseLong(input);

        // Якщо нічого не ввели або ввели не число
        } catch (NumberFormatException e) {
            showError("Зерно повинно бути цілим числом!");
            isRunning = false;
        }

        return seed;
    }

    @FXML
    public void startDrawing(ActionEvent event) throws IOException {
        isRunning = true;

        long samples = checkSamples();

        long seed = checkSeed();

        rng = new PCG32(seed, 54L);
        GeneratorContext generatorContext = new GeneratorContext(rng);

        // Створюємо новий потік, щоб інтерфейс не зависав
        Thread paintThread = new Thread(() -> {
            for (long i = paintedDots; i < samples; i++) {
                if(!isRunning){
                    seedArea.setEditable(true);
                    break;
                }

                seedArea.setEditable(false);

                generatorContext.performSingleStep();
                IterationState currentStep = generatorContext.getHistory().getLast();

                // Малюємо через Platform.runLater
                Platform.runLater(() -> {
                    gc.fillRect(currentStep.getXValue() * mainCanvas.getWidth(), currentStep.getYValue() * mainCanvas.getHeight(), 1, 1);
                    paintedDots += 1;
                    countPaintedDots.setText(String.valueOf(paintedDots));
                });

                try {
                    double sliderValue = speedSlider.getValue();
                    long delay = (long) (101 - sliderValue);

                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    seedArea.setEditable(true);
                    break; // Вихід, якщо потік перервано
                }
            }
        });
        paintThread.setDaemon(true);
        paintThread.start();
    }

    @FXML
    public void clearCanvas(){
        isRunning = false;

        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, mainCanvas.getWidth(), mainCanvas.getHeight());
        gc.setFill(Color.WHITE);

        countPaintedDots.setText("0");
        paintedDots = 0;
    }

    @FXML
    public void stopDrawing(){
        isRunning = false;
    }

}
