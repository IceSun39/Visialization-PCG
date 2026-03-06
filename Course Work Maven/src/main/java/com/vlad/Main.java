package com.vlad;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException {

            Parent root = FXMLLoader.load(getClass().getResource("/com/vlad/View/mainScene.fxml"));
            stage.setTitle("Генератор PCG");
            stage.setScene(new Scene(root, 900, 500));

            stage.setOnCloseRequest(event -> {
                event.consume();
            });

            stage.show();

        stage.setOnCloseRequest(t -> {
            Platform.exit();
            System.exit(0);
        });
    }

    public static void main(String[] args) {
        launch();
    }

}