package com.example.voip;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import static com.example.voip.ChatClient.*;

/**
 * Controls whisper user interface
 *
 * @author ROBERT SHONE – 25132687
 * @author KEURAN KISTAN – 23251646
 * @author TASHEEL GOVENDER – 25002112
 */
public class DirectMessageController {

    @FXML
    private TextField txtMessage;

    @FXML
    private VBox privateVbox;

    @FXML
    private AnchorPane rootAnchorPane;

    @FXML
    private Button btnVoiceNote;

    @FXML
    private Button btnCall;

    private String username;
    private String myUsername;
    private Boolean recording = false;
    private Stage stage;
    private TargetDataLine targetDataLine;
    private ByteArrayOutputStream audioOutputStream;
    private int voiceNoteCounter = 0;

    @FXML
    private void initialize() {
        if (stage != null) {
            // Get the window associated with the scene
            Window window = stage.getScene().getWindow();

            // Add an event handler to the onCloseRequest property of the window
            window.setOnCloseRequest(this::handleCloseRequest);
        }

    }

    @FXML
    public void setButtonText(String text) {
        btnCall.setText(text);
    }

    public void clickCall(ActionEvent actionEvent) throws IOException {

        if (inCall == false) {
            SendMessage("ささやき声通話 " + username);
            btnCall.setText("End");
            System.out.println("3");
            inCall = true;
        } else if (btnCall.getText().equals("End")) {
            SendMessage("音声通話を終了する " + username);
            btnCall.setText("Call");
            System.out.println("4");
            inCall = false;
        } else {
            System.out.println("You are already on a call");
        }
    }

    /**
     * Sets the username for the controller
     *
     * @param username Username value
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Sets the my username for the controller
     *
     * @param username Username value
     */
    public void setMyUsername(String username) {
        this.myUsername = username;
    }

    /**
     * Sets the stage for the controller
     *
     * @param stage stage value
     */
    public void setStageForController(Stage stage) {
        this.stage = stage;
    }

    /**
     * Send a whisper
     *
     * @param event Clicking the send button
     * @throws IOException
     */
    @FXML
    void sendPM(ActionEvent event) throws IOException {
            String message = txtMessage.getText();

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
                privateVbox.getChildren().add(hBox);

                SendMessage("これはダイレクトメッセージです " + username +" "+ message); // Send message on back-end

                txtMessage.clear();

            }
    }

    /**
     * Handles a close request
     *
     * @param event Clicking on the close window button
     */
    private void handleCloseRequest(WindowEvent event) {
        // Handle the close request here, for example, set the stage to not visible
        stage.hide(); // Hide the stage
    }

    /**
     * Receives messages and displays it to the user
     *
     * @param message Received message
     */
    @FXML
    public void receiveDirectMessageLabel(String message) {
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
                    privateVbox.getChildren().add(hBox);
                }
            });
        }

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
            addVoiceNoteButton(saveAudioToFile(), 0);
            btnVoiceNote.setText("Voice Note");
        }

    }

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

    private void stopRecording() {
        recording = false;

        if (targetDataLine != null) {
            targetDataLine.stop();
            targetDataLine.close();
        }
    }

    private String saveAudioToFile() {
        if (audioOutputStream != null) {
            try {
                byte[] audioData = audioOutputStream.toByteArray();

                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(audioData);
                AudioFormat format = new AudioFormat(44100, 16, 1, true, false);

                // Create an AudioInputStream from the byte array
                AudioInputStream audioStream = new AudioInputStream(byteArrayInputStream, format, audioData.length / format.getFrameSize());

                // Save the audio to a WAV file
                String filename = myUsername + "__to__" + username + "__recorded__audio__"+ (voiceNoteCounter++) + ".wav";
                File folder = new File("./VoiceNotes");
                if (!folder.exists()) {
                    folder.mkdirs();
                }
                File outputFile = new File(folder, filename);
                AudioSystem.write(audioStream, AudioFileFormat.Type.WAVE, outputFile);

                System.out.println("Audio saved to " + outputFile.getAbsolutePath()+"\n");

                sendVN(audioData, "プライベートボイスメモ " + filename + " " + audioData.length + " " + username);

                return filename;

            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        return null;
    }

    @FXML
    public void addVoiceNoteButton(String filename, int num) throws IOException {
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
                    privateVbox.getChildren().add(hBox);
                }
            });


        } else {
            System.out.println("Audio file is null");
        }
    }
}
