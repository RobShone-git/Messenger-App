package com.example.voip;

import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages chat whisper chat rooms
 *
 * @author ROBERT SHONE – 25132687
 * @author KEURAN KISTAN – 23251646
 * @author TASHEEL GOVENDER – 25002112
 */
public class Chatrooms {

    private HashMap<String, Storage> chatrooms;

    public Chatrooms() {
        chatrooms = new HashMap<>();
    }

    public void addStorage(String username, Storage storage) {
        chatrooms.put(username, storage);
    }

    public Storage getStorage(String username) {
        return chatrooms.get(username);
    }

    /**
     * Close and exit all chatroom's
     */
    public void closeAllChatrooms() {
        for (Map.Entry<String, Storage> entry : chatrooms.entrySet()) {
            Storage storage = entry.getValue();
            storage.closeStage();
        }
    }
}

/**
 * Manages chat whisper chat rooms
 *
 * @author ROBERT SHONE – 25132687
 * @author KEURAN KISTAN – 23251646
 * @author TASHEEL GOVENDER – 25002112
 */
class Storage {

    private static Stage stage;
    private DirectMessageController controller;

    /**
     * Class controller
     *
     * @param stage stage value
     * @param controller controller value
     */
    public Storage(Stage stage, DirectMessageController controller) {
        this.stage = stage;
        this.controller = controller;
    }

    /**
     * Returns controller
     *
     * @return DirectMessageController
     */
    public DirectMessageController getController() {
        return controller;
    }

    /**
     *  Shows if the chat room is open
     *
     * @return True if it is showing, false otherwise
     */
    public boolean isStageVisible() {
        return stage.isShowing();
    }

    /**
     * Sets the visibility of the chat room
     *
     * @param visible If the chat room should dbe visible ort not
     */
    public static void setStageVisible(boolean visible) {
        if (stage != null) {
            if (visible) {
                stage.show();
            } else {
                stage.hide();
            }
        }
    }

    /**
     * Close and exit the stage
     */
    public void closeStage() {
        if (stage != null) {
            stage.close();
        }
    }
}



