package com.example.voip;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import javax.sound.sampled.*;

import static com.example.voip.ChatClient.*;

/**
 * Controls the clients user interface containing the global chat and online users
 * Controls all actions performed by the user on the interface
 *
 * @author ROBERT SHONE – 25132687
 * @author KEURAN KISTAN – 23251646
 * @author TASHEEL GOVENDER – 25002112
 */
public class ChatController {

    public static Chatrooms chatrooms;

    @FXML
    public Button btnVoiceNote;

    @FXML
    private TextField typeMessage;

    @FXML
    private VBox VBoxGlobal;

    @FXML
    private ListView<String> onlineUsersListView;

    @FXML
    private Button btnCall;

    private Boolean recording = false;

    private TargetDataLine targetDataLine;

    private ByteArrayOutputStream audioOutputStream;

    private int voiceNoteCounter = 0;

    //private boolean inCall = false;


    /**
     * Initializes the chat application by setting up chatrooms, configuring the online users list,
     * and adding a listener to detect when a username is clicked. Also starts receiving messages from the server.
     *
     * @throws IOException If an I/O error occurs during initialization.
     */
    public void initialize() throws IOException {
        chatrooms = new Chatrooms();
        onlineUsersListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        // Add listener to detect when a username is clicked
        onlineUsersListView.setOnMouseClicked((MouseEvent event) -> {
            String selectedUsername = onlineUsersListView.getSelectionModel().getSelectedItem();
            setUpChatroom(selectedUsername, null, 0, null);
        });

        ChatClient chatClient = new ChatClient();
        chatClient.receiveMessage(VBoxGlobal, onlineUsersListView);
    }

    /**
     * Handles the action of clicking the call button, either signaling a group call or joining/leaving an ongoing call.
     *
     * @param actionEvent The ActionEvent triggered by clicking the call button.
     * @throws IOException If an I/O error occurs while sending a message to the server.
     */
    public void clickCall(ActionEvent actionEvent) throws IOException {

        if (inCall == false) {
            SendMessage("グローバルコール"); // Signals a group call
            btnCall.setText("Leave");
            System.out.println("1");
            inCall = true;
        } else if (btnCall.getText().equals("Join")) {
            System.out.println("You are already in a call");
        } else {
            SendMessage("グローバルコール");
            btnCall.setText("Join");
            System.out.println("2");
            inCall = false;
        }
    }


    /**
     * Sends message when the client clicks send
     *
     * @param actionEvent send button event
     * @throws IOException If message can not be sent
     */
    @FXML
    void clickSend(ActionEvent actionEvent) throws IOException {
        String message = typeMessage.getText();

        if (!message.isEmpty()) {
            HBox hBox = new HBox();
            hBox.setAlignment(Pos.CENTER_RIGHT);
            hBox.setPadding(new Insets(5, 5, 5, 10));

            Text text = new Text(message);
            TextFlow textFlow = new TextFlow(text);

            textFlow.setStyle("-fx-text-fill: rgb(239, 242, 255);" +
                    "-fx-background-color: rgb(15, 125, 242);" +
                    "-fx-background-radius: 20px");


            textFlow.setPadding(new Insets(5, 10, 5, 10));
            text.setFill(Color.color(0.934, 0.945, 0.996));

            hBox.getChildren().add(textFlow);
            VBoxGlobal.getChildren().add(hBox);

            SendMessage(message); // Send message on back-end

            typeMessage.clear();

        }
    }

    /**
     * Receives messages from other clients
     * Checks to see if they are whispers and displays them to the appropriate chat box
     *
     * @param message Incoming message
     * @param VBoxGlobal Global chat VBox
     */
    @FXML
    public static void receiveMessageLabel(String message, VBox VBoxGlobal) {
        if (!message.isEmpty()) {
            HBox hBox = new HBox();
            hBox.setAlignment(Pos.CENTER_LEFT);
            hBox.setPadding(new Insets(5, 5, 5, 10));

            Text text = new Text(message);
            TextFlow textFlow = new TextFlow(text);

            textFlow.setStyle("-fx-text-fill: rgb(239, 242, 255);" +
                    "-fx-background-color: rgb(15, 125, 242);" +
                    "-fx-background-radius: 20px");


            textFlow.setPadding(new Insets(5, 10, 5, 10));
            text.setFill(Color.color(0.934, 0.945, 0.996));

            hBox.getChildren().add(textFlow);

            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    VBoxGlobal.getChildren().add(hBox);
                }
            });
        }
    }

    /**
     * Populates and updates online users list
     *
     * @param clients String array of all clients
     * @param onlineUsersListView ListView of all online users
     */
    public static void onlineUsersLabel(String[] clients, ListView<String> onlineUsersListView) {
        Platform.runLater(() -> {
            // Clear the previous content of the listView
            onlineUsersListView.getItems().clear();

            // Populate the VBox with the list of online users
            for (String client : clients) {
                onlineUsersListView.getItems().add(client);
            }
        });
    }

    /**
     * Closes all open chat rooms
     */
    public static void closeAllChatRooms(){
        chatrooms.closeAllChatrooms();
    }


    /**
     * Opens chat room if needed and displays whisper message
     *
     * @param selectedUsername Receiver of the whisper message
     * @param message The whisper message
     */
    public static void setUpChatroom(String selectedUsername, String message, int num, String text) {
        Platform.runLater(() -> {
            // If chatroom doesn't exist yet
            if (selectedUsername != null && !username.equals(selectedUsername) && chatrooms.getStorage(selectedUsername) == null) {
                try {
                    // Load the FXML file for the chat room GUI
                    FXMLLoader loader = new FXMLLoader(ChatController.class.getResource("DirectMessagesGUI.fxml"));
                    Parent root = loader.load();

                    // Set up the stage for the chat room GUI
                    Stage chatStage = new Stage();

                    // Get the controller instance
                    DirectMessageController controller = loader.getController();
                    // Set the username and stage for the controller
                    controller.setUsername(selectedUsername);
                    controller.setMyUsername(username);
                    controller.setStageForController(chatStage);
                    if (text != null) {
                        controller.setButtonText(text);
                    }

                    // Create storage for controller and stage
                    Storage store = new Storage(chatStage, controller);
                    // Add storage to username
                    chatrooms.addStorage(selectedUsername, store);

                    chatStage.setTitle("Direct Message - " + selectedUsername);
                    chatStage.setScene(new Scene(root, 400, 430));
                    chatStage.show();

                    if (message != null) {
                        if (num == 0) {
                            controller.receiveDirectMessageLabel(message);
                        } else {
                            try {
                                controller.addVoiceNoteButton(message, 1);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }

                    }


                } catch (IOException e) {
                    e.printStackTrace();
                }
                // If it does exist, and it was closed then display it
            } else if (selectedUsername != null && !username.equals(selectedUsername) && chatrooms.getStorage(selectedUsername).isStageVisible() == false) {
                chatrooms.getStorage(selectedUsername).setStageVisible(true);
                if (message != null) {
                    Storage store = chatrooms.getStorage(selectedUsername);
                    DirectMessageController controller = store.getController();
                    if (text != null) {
                        controller.setButtonText(text);
                    }
                    if (num == 0) {
                        controller.receiveDirectMessageLabel(message);
                    } else {
                        try {
                            controller.addVoiceNoteButton(message, 1);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }

                // If it does exist and open then just handle message
            } else if (selectedUsername != null && !username.equals(selectedUsername) && chatrooms.getStorage(selectedUsername).isStageVisible() == true && message != null) {
                Storage store = chatrooms.getStorage(selectedUsername);
                DirectMessageController controller = store.getController();
                if (text != null) {
                    controller.setButtonText(text);
                }
                if (num == 0) {
                    controller.receiveDirectMessageLabel(message);
                } else {
                    try {
                        controller.addVoiceNoteButton(message, 1);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
    }

    /**
     * Record voice note
     * @param actionEvent
     */
    public void onVoiceNote(ActionEvent actionEvent) throws IOException {
        if(!recording) {
            startRecording();
            btnVoiceNote.setText("Stop Recording");
        } else {
            stopRecording();
            addVoiceNoteButton(saveAudioToFile(), VBoxGlobal, 0);
            btnVoiceNote.setText("Voice Note");
        }

    }

    /**
     * Starts recording audio from the microphone and saves it to an audio output stream.
     * Uses a separate thread for continuous recording until stopped.
     */
    private void startRecording() {
        recording = true;

        // Set the audio format
        AudioFormat format = new AudioFormat(44100, 16, 1, true, false);  // Mono, 44.1kHz, 16-bit

        // Get the TargetDataLine for capturing audio
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

        try {
            targetDataLine = (TargetDataLine) AudioSystem.getLine(info);
            targetDataLine.open(format);
            targetDataLine.start();
            audioOutputStream = new ByteArrayOutputStream();

            // Record audio in a separate thread
            new Thread(() -> {
                byte[] buffer = new byte[4096];
                while (recording) {
                    int bytesRead = targetDataLine.read(buffer, 0, buffer.length);
                    audioOutputStream.write(buffer, 0, bytesRead);
                }
            }).start();

        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    /**
     * Stops the audio recording and closes the TargetDataLine if it is open.
     */
    private void stopRecording() {
        recording = false;

        if (targetDataLine != null) {
            targetDataLine.stop();
            targetDataLine.close();
        }
    }

    /**
     * Saves the recorded audio to a WAV file and sends it as a voice note to the server.
     *
     * @return The filename of the saved audio file, or null if saving fails.
     */
    private String saveAudioToFile() {
        if (audioOutputStream != null) {
            try {
                byte[] audioData = audioOutputStream.toByteArray();

                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(audioData);
                AudioFormat format = new AudioFormat(44100, 16, 1, true, false);

                // Create an AudioInputStream from the byte array
                AudioInputStream audioStream = new AudioInputStream(byteArrayInputStream, format, audioData.length / format.getFrameSize());

                // Save the audio to a WAV file
                String filename = username + "__recorded__audio__"+ (voiceNoteCounter++) + ".wav";
                File folder = new File("./VoiceNotes");
                if (!folder.exists()) {
                    folder.mkdirs();
                }
                File outputFile = new File(folder, filename);
                AudioSystem.write(audioStream, AudioFileFormat.Type.WAVE, outputFile);

                System.out.println("Audio saved to " + outputFile.getAbsolutePath()+"\n");

                sendVN(audioData, "グローバルボイスノート " + filename + " " + audioData.length);

                return filename;

            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        return null;
    }


    /**
     * Adds a button to play a voice note file in the GUI.
     *
     * @param filename    The filename of the voice note file.
     * @param VBoxGlobal  The VBox container where the button will be added.
     * @param num         An indicator for button alignment (0 for right-aligned, 1 for left-aligned).
     * @throws IOException If an I/O error occurs while creating the button or playing the audio.
     */
    public static void addVoiceNoteButton(String filename, VBox VBoxGlobal, int num) throws IOException {
        if (filename != null) {


            HBox hBox = new HBox();

            if (num == 0) {
                hBox.setAlignment(Pos.CENTER_RIGHT);
            } else {
                hBox.setAlignment(Pos.CENTER_LEFT);
            }

            hBox.setPadding(new Insets(5, 5, 5, 10));

            Button myButton = new Button(filename);

            myButton.setOnAction(e -> {
                try {
                    File musicPath = new File("./VoiceNotes/"+filename);

                    if (musicPath.exists()) {
                        AudioInputStream audioInput = AudioSystem.getAudioInputStream(musicPath);
                        Clip clip = AudioSystem.getClip();
                        clip.open(audioInput);
                        clip.start();
                    } else {
                        System.out.println(filename + " does not exist");
                    }
                } catch (Exception err) {
                    err.printStackTrace();
                }
            });

            hBox.getChildren().add(myButton);

            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    VBoxGlobal.getChildren().add(hBox);
                }
            });


        } else {
            System.out.println("Audio file is null");
        }
    }
}



