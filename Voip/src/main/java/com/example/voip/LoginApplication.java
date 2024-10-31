package com.example.voip;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

import static com.example.voip.ChatClient.setServer;

/**
 * Oversees login user interface
 *
 * @author ROBERT SHONE – 25132687
 * @author KEURAN KISTAN – 23251646
 * @author TASHEEL GOVENDER – 25002112
 */
public class LoginApplication extends Application {
    private static Stage stageLogin; // Store the stage reference

    private static Stage stage; // Store the Stage object

    @Override
    public void start(Stage primaryStage) throws IOException {
        stage = primaryStage; // Store the Stage object
        FXMLLoader fxmlLoader = new FXMLLoader(LoginApplication.class.getResource("HostGUI.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 600, 500);
        stage.setTitle("Welcome");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Starts the login user interface
     *
     * @param primaryStage The stage for the user interface
     * @throws IOException If they can't open the user interface
     */
    public static void startLoginGUI(Stage primaryStage, String host, String ip) throws IOException {
        setServer(host, ip);
        stageLogin = primaryStage;
        FXMLLoader fxmlLoader = new FXMLLoader(LoginApplication.class.getResource("LoginGUI.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 500, 250);
        primaryStage.setTitle("Hello!");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch();
    }

    /**
     * Gets login stage
     *
     * @return Host stage
     */
    public static Stage getHostStage() {
        return stage;
    }

    /**
     * Gets login stage
     *
     * @return Login stage
     */
    public static Stage getStage() {
        return stageLogin;
    }
}
