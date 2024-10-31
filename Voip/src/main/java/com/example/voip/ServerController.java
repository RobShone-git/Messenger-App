package com.example.voip;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

import java.io.IOException;

/**
 * Controls Server screen
 *
 * @author ROBERT SHONE – 25132687
 * @author KEURAN KISTAN – 23251646
 * @author TASHEEL GOVENDER – 25002112
 */
public class ServerController {

    @FXML
    private ListView<String> onlineUsersListView;
    @FXML
    private VBox vbxActivity;

    public void initialize() throws IOException {
        ChatServer server = new ChatServer();
        server.receiveListView(onlineUsersListView, vbxActivity);
        setActivity("Server is running", vbxActivity);
    }

    /**
     * Populates and updates online users list
     *
     * @param clients String array of all clients
     * @param onlineUsersListView ListView of all online users
     */
    public static void setOnlineUsers(String[] clients,  ListView<String> onlineUsersListView) {
        Platform.runLater(() -> {
            // Clear the previous content of the listView
            onlineUsersListView.getItems().clear();

            // Populate the VBox with the list of online users
            for (String client : clients) {
                onlineUsersListView.getItems().add(client);
            }
        });
    }

    public static void setActivity(String activity,  VBox vbxActivity) {
        Platform.runLater(() -> {
            Text text = new Text(activity);


            text.setFill(Color.color(0, 0, 0));

            vbxActivity.getChildren().add(text);

        });
    }


}
