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
import javafx.stage.FileChooser;

import javafx.event.ActionEvent;
import java.io.IOException;
import java.io.File;
import java.io.PrintWriter;

import java.util.List;

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

    private GraphicsContext gc;

    private PCG32 rng;

    private GeneratorContext generatorContext;

    private volatile boolean isRunning = false;

    private boolean isSaved = false;

    @FXML
    void initialize(){
        gc = mainCanvas.getGraphicsContext2D();

        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, mainCanvas.getWidth(), mainCanvas.getHeight());
        gc.setFill(Color.WHITE);

        paintedDots = 0;
    }

    private void showInfo(Alert.AlertType alertType, String title, String massage){
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
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
            showInfo(Alert.AlertType.ERROR, "Помилка введення", "Кількість точок повинна бути числом!");
            isRunning = false;
        // Якщо ввели від'ємне число
        } catch (IllegalArgumentException e) {
            showInfo(Alert.AlertType.ERROR, "Помилка введення", "Кількість точок повинна бути більше за нуль!");
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
            showInfo(Alert.AlertType.ERROR, "Помилка введення", "Зерно повинно бути цілим числом!");
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
        generatorContext = new GeneratorContext(rng);

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
    public void quickGeneration(){

        long samples = checkSamples();
        long seed = checkSeed();

        rng = new PCG32(seed, 54L);
        generatorContext = new GeneratorContext(rng);
        generatorContext.startGeneration(samples);

        StringBuilder massage = new StringBuilder();
        List<IterationState> history = generatorContext.getHistory();
        int size = history.size();
        if(size > 50){
            massage.append("Перші 50 чисел ");
            size = 50;
        }
        else{
            String partOne;
            String partTwo;
            if(size == 1){
                partOne = "Перше ";
                partTwo = " число ";
            }
            else if(size == 2 || size == 3 || size == 4){
                partOne = "Перші ";
                partTwo = " числа ";
            }
            else{
                partOne = "Перші ";
                partTwo = " чисел ";
            }
            massage.append(partOne).append(size).append(partTwo);
        }

        for(int i = 0; i < size; i++){
            massage.append(history.get(i).getXValue()).append(" ");
        }

        showInfo(Alert.AlertType.INFORMATION, "Генерація завершена", massage.toString());
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

    @FXML
    public void saveToFile() {
        if (generatorContext == null || generatorContext.getHistory().isEmpty()) {
            showInfo(Alert.AlertType.ERROR,"Помилка збереження", "Немає даних для збереження!");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Зберегти результати генерації");


        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Текстові файли (*.txt)", "*.txt"),
                new FileChooser.ExtensionFilter("CSV файли (*.csv)", "*.csv")
        );

        // Відкриваємо вікно "Зберегти як"
        File file = fileChooser.showSaveDialog(mainCanvas.getScene().getWindow());

        if (file != null) {
            saveData(file);
        }
    }

    private void saveData(File file) {
        String fileName = file.getName().toLowerCase();
        String delimiter = fileName.endsWith(".csv") ? ";" : " ";

        try (PrintWriter writer = new PrintWriter(file)) {
            if (fileName.endsWith(".csv")) {
                writer.println("Iteration" + delimiter + "X");
            }

            for (IterationState state : generatorContext.getHistory()) {
                writer.printf("%d%s%.10f%n",
                        state.getIterationNumber(),
                        delimiter,
                        state.getXValue()
                );
            }

            // Додаємо вивід зерна в кінець файлу
            writer.println(); // Порожній рядок для розділення
            writer.println("Seed: " + seedArea.getText().trim());

        } catch (IOException e) {
            showInfo(Alert.AlertType.ERROR, "Помилка збереження",   "Помилка при записі у файл: " + e.getMessage());
        }

        isSaved = true;
    }

}
