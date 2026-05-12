package com.vlad.Controllers;

import com.vlad.Model.GeneratorContext;
import com.vlad.Model.IterationState;
import com.vlad.Model.PCG32;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;

import javafx.event.ActionEvent;
import java.io.IOException;
import java.io.File;
import java.io.PrintWriter;

import java.util.List;
import java.util.ArrayList;

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
    private TextField countArea;
    @FXML
    private TextField seedArea;
    @FXML
    private Label countPaintedDots;
    @FXML
    private Label errorUnderZeroLabel;
    @FXML
    private Label errorNotNumberLabel;
    @FXML
    private ProgressIndicator loadingIndicator;
    @FXML
    private VBox controlPanel;

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

        // Отримуємо панель діалогу та додаємо наш CSS
        DialogPane dialogPane = alert.getDialogPane();

        try {
            // Завантажуємо style.css тим самим шляхом, що й для головної сцени
            String cssPath = getClass().getResource("/com/vlad/View/style.css").toExternalForm();
            dialogPane.getStylesheets().add(cssPath);

            // Задаємо кастомний клас для Alert, щоб стилі не конфліктували
            dialogPane.getStyleClass().add("custom-alert");
        } catch (NullPointerException e) {
            System.err.println("Не вдалося знайти файл стилів для Alert.");
        }

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
    public void startDrawing(ActionEvent event) {
        long samples = checkSamples();
        long seed = checkSeed();
        if (samples <= 0) return;

        // Починаємо з нуля: створюємо нові об'єкти
        rng = new PCG32(seed, 54L);
        generatorContext = new GeneratorContext(rng);

        // Очищаємо полотно і лічильники перед новим малюванням
        clearCanvas();

        // Запускаємо малювання
        runDrawingThread(samples);
    }

    @FXML
    public void continueDrawing(ActionEvent event) {
        long samples = checkSamples();
        if (samples <= 0) return;

        // Якщо користувач натиснув "Продовжити", але ще нічого не було створено
        if (rng == null || generatorContext == null) {
            startDrawing(event);
            return;
        }

        // Продовжуємо малювання (не створюємо new PCG32 і не очищаємо екран)
        runDrawingThread(samples);
    }

    // Виносимо логіку потоку в окремий метод
    private void runDrawingThread(long samples) {
        isRunning = true;
        seedArea.setEditable(false);

        Thread paintThread = new Thread(() -> {
            for (long i = paintedDots; i < samples; i++) {
                if (!isRunning) {
                    break;
                }

                generatorContext.performSingleStep();
                IterationState currentStep = generatorContext.getHistory().getLast();

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
                    break;
                }
            }

            // Коли цикл завершився (або його зупинили), розблоковуємо поле зерна
            Platform.runLater(() -> seedArea.setEditable(true));
        });

        paintThread.setDaemon(true);
        paintThread.start();
    }

    @FXML
    public void quickGeneration() {
        // Якщо йде генерація, то призупинити
        isRunning = false;

        long samples = checkSamples();
        long seed = checkSeed();
        if (samples <= 0) return;

        // Вмикаємо анімацію
        setLoadingState(true);

        // Фоновий потік
        Thread quickGenThread = new Thread(() -> {
            rng = new PCG32(seed, 54L);
            generatorContext = new GeneratorContext(rng);
            generatorContext.startGeneration(samples);

            StringBuilder massage = new StringBuilder();
            List<IterationState> history = generatorContext.getHistory();
            int size = history.size();

            if(size > 50){
                massage.append("Перші 50 чисел ");
                size = 50;
            } else {
                String partOne;
                String partTwo;
                if(size == 1){
                    partOne = "Перше ";
                    partTwo = " число ";
                } else if(size == 2 || size == 3 || size == 4){
                    partOne = "Перші ";
                    partTwo = " числа ";
                } else {
                    partOne = "Перші ";
                    partTwo = " чисел ";
                }
                massage.append(partOne).append(size).append(partTwo);
            }

            for(int i = 0; i < size; i++){
                massage.append(history.get(i).getXValue()).append(" ");
            }

            // Повертаємось на UI потік
            Platform.runLater(() -> {
                setLoadingState(false);

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Генерація завершена");
                alert.setHeaderText(null);
                alert.setContentText(massage.toString());


                DialogPane dialogPane = alert.getDialogPane();
                try {
                    String cssPath = getClass().getResource("/com/vlad/View/style.css").toExternalForm();
                    dialogPane.getStylesheets().add(cssPath);
                    dialogPane.getStyleClass().add("custom-alert");
                } catch (NullPointerException e) {
                    System.err.println("Не вдалося знайти файл стилів для Alert.");
                }

                // Створюємо кнопки: кастомну для експорту та стандартну ОК
                ButtonType btnExport = new ButtonType("Експортувати");
                ButtonType btnOk = new ButtonType("ОК", ButtonBar.ButtonData.OK_DONE);

                // Додаємо кнопки у вікно
                alert.getButtonTypes().setAll(btnExport, btnOk);

                // Показуємо вікно та обробляємо натискання
                alert.showAndWait().ifPresent(type -> {
                    if (type == btnExport) {
                        saveToFile(); // Викликаємо існуючий метод експорту, якщо натиснули кнопку експорту
                    }
                });
            });
        });

        quickGenThread.setDaemon(true);
        quickGenThread.start();
    }

    @FXML
    public void runExperiment() {
        // Якщо йде генерація, то призупинити
        isRunning = false;

        long samples = checkSamples();
        long seed = checkSeed();
        if (samples <= 0) return;

        // Вмикаємо анімацію та блокуємо UI
        setLoadingState(true);

        // Запускаємо важкі розрахунки у фоні
        Thread experimentThread = new Thread(() -> {
            PCG32 rng = new PCG32(seed, 54L);
            com.vlad.Model.ExperimentAnalyzer analyzer = new com.vlad.Model.ExperimentAnalyzer();

            double timeInNanoseconds = analyzer.runPerformanceExperiment(rng, (int) samples);
            double timeInMilliseconds = timeInNanoseconds / 1_000_000.0;
            String message = String.format("Згенеровано чисел: %d\nВитрачено часу: %.4f мс", samples, timeInMilliseconds);

            // 3. Повертаємося на головний потік, щоб показати результат і вимкнути анімацію
            Platform.runLater(() -> {
                setLoadingState(false);
                showInfo(Alert.AlertType.INFORMATION, "Результати тестування", message);
            });
        });

        experimentThread.setDaemon(true);
        experimentThread.start();
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

    private void setLoadingState(boolean isLoading) {
        loadingIndicator.setVisible(isLoading); // Показуємо/ховаємо крутилку
        controlPanel.setDisable(isLoading);     // Блокуємо/розблоковуємо всі кнопки зліва
    }

    @FXML
    public void saveToFile() {
        if (generatorContext == null || generatorContext.getHistory().isEmpty()) {
            showInfo(Alert.AlertType.ERROR, "Помилка збереження", "Немає даних для збереження!");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Зберегти результати генерації");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Текстові файли (*.txt)", "*.txt"),
                new FileChooser.ExtensionFilter("CSV файли (*.csv)", "*.csv")
        );

        File file = fileChooser.showSaveDialog(mainCanvas.getScene().getWindow());

        if (file != null) {
            // ВАЖЛИВО: Отримуємо дані з UI ДО запуску фонового потоку
            String seedText = seedArea.getText().trim();
            // Створюємо копію історії, щоб уникнути помилок доступу, якщо генерація триває
            List<IterationState> historyCopy = new ArrayList<>(generatorContext.getHistory());

            setLoadingState(true);

            Thread saveThread = new Thread(() -> {
                try {
                    performBackgroundSave(file, historyCopy, seedText);
                    Platform.runLater(() -> {
                        setLoadingState(false);
                        showInfo(Alert.AlertType.INFORMATION, "Успіх", "Дані збережено успішно.");
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        setLoadingState(false);
                        showInfo(Alert.AlertType.ERROR, "Помилка", "Не вдалося зберегти файл: " + e.getMessage());
                    });
                }
            });
            saveThread.setDaemon(true);
            saveThread.start();
        }
    }

    private void performBackgroundSave(File file, List<IterationState> history, String seed) throws IOException {
        String fileName = file.getName().toLowerCase();
        String delimiter = fileName.endsWith(".csv") ? ";" : " ";

        try (PrintWriter writer = new PrintWriter(file)) {
            if (fileName.endsWith(".csv")) {
                writer.println("Iteration" + delimiter + "X");
            }

            for (IterationState state : history) {
                writer.printf("%d%s%.10f%n", state.getIterationNumber(), delimiter, state.getXValue());
            }

            writer.println();
            writer.println("Seed: " + seed);
        }
    }

}
