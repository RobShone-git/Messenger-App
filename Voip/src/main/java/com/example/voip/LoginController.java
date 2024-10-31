package com.example.voip;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.io.IOException;

import static com.example.voip.ChatClient.checkUsername;

/**
 * Controls login screen
 *
 * @author ROBERT SHONE – 25132687
 * @author KEURAN KISTAN – 23251646
 * @author TASHEEL GOVENDER – 25002112
 */
public class LoginController {

    @FXML
    private TextField txtUsername;
    @FXML
    private Label lblUsernameTaken;

    @FXML
    private Label lblInvalidUsername;

    /**
     * Sets clients username
     *
     * @param event clicking button to submit username
     * @throws IOException if no username is received
     */
    @FXML
    void setUsername(ActionEvent event) throws IOException {
        String message = txtUsername.getText();
        if (!message.contains(" ")) {
            if (!message.isEmpty()) {
                txtUsername.clear();
                checkUsername(message, lblUsernameTaken); // Send message on back-end
            }
        } else {
            lblInvalidUsername.setVisible(true);
        }



    }

    /**
     * Sets label if username is taken
     *
     * @param lblUsernameTaken label to display error
     */
    @FXML
    public static void usernameTaken(Label lblUsernameTaken) {

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                lblUsernameTaken.setVisible(true);
            }
        });

    }

}
