package com.example.voip;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Opens the Server interface
 *
 * @author ROBERT SHONE – 25132687
 * @author KEURAN KISTAN – 23251646
 * @author TASHEEL GOVENDER – 25002112
 */
public class ServerApplication extends Application {


    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ChatGuiApplication.class.getResource("ServerGUI.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 500, 400);
        stage.setTitle("Server");
        stage.setScene(scene);
        stage.show();

        // Add event handler to detect when the GUI window is closed
        stage.setOnCloseRequest(event -> {
            // Call a function in the server class to close the program gracefully
            ChatServer.shutdownServer();
        });

    }

    public static void main(String[] args) {
        launch();
    }

}
