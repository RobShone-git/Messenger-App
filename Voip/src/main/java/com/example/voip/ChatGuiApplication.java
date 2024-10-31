package com.example.voip;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Opens the client user interface
 *
 * @author ROBERT SHONE – 25132687
 * @author KEURAN KISTAN – 23251646
 * @author TASHEEL GOVENDER – 25002112
 */
public class ChatGuiApplication extends Application {
    private String username;

    /**
     * Constructor for the class
     *
     * @param user The clients username
     */
    public ChatGuiApplication(String user) {
        this.username = user;
    }

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ChatGuiApplication.class.getResource("ChatGUI.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 627, 458);
        stage.setTitle(username);
        stage.setScene(scene);

        stage.show();


    }

    public static void main(String[] args) {
        launch();
    }

}
