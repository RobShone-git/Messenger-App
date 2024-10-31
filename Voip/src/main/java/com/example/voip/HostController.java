package com.example.voip;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

import static com.example.voip.LoginApplication.startLoginGUI;

/**
 * Controls the host screen user interface
 */
public class HostController {

    @FXML
    private TextField txtHost;

    @FXML
    private TextField txtIP;

    private static Stage stage;


    public void initialize() throws IOException {
        stage = LoginApplication.getHostStage();
    }

    /**
     * Sends the IP address through to the socket
     *
     * @param actionEvent When the button is clicked
     * @throws IOException If there is an error
     */
    @FXML
    void sendHost(ActionEvent actionEvent) throws IOException {
        String host = txtHost.getText();
        String ip = txtIP.getText();
            // Close the stage
            if (stage != null) {
                startLoginGUI(new Stage(), host, ip);
                stage.close();
            }
    }

}
