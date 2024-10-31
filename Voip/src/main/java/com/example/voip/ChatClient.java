package com.example.voip;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.util.Random;

import static com.example.voip.ChatController.*;
import static com.example.voip.ChatController.setUpChatroom;
import static com.example.voip.LoginController.usernameTaken;

/**
 * 
 * Main client class that handles socket connections, message sending, and GUI launching.
 * Provides methods for setting up the server, checking username uniqueness, sending messages,
 * managing voice communication, launching the login screen, and launching the chat interface.
 * Also includes methods for shutting down the client and receiving messages from other users.
 *
 * @author ROBERT SHONE – 25132687
 * @author KEURAN KISTAN – 23251646
 * @author TASHEEL GOVENDER – 25002112
 */
public class ChatClient {

        /**
     * Indicates whether the client is running.
     */
    private static volatile boolean isRunning = true;

    /**
     * BufferedReader for reading input from the server.
     */
    public static BufferedReader in;

    /**
     * BufferedWriter for writing output to the server.
     */
    public static BufferedWriter out;

    /**
     * DataOutputStream for sending data over the socket.
     */
    public static DataOutputStream dataOutputStream;

    /**
     * DataInputStream for receiving data over the socket.
     */
    public static DataInputStream dataInputStream;

    /**
     * The username of the client.
     */
    public static String username;

    /**
     * The socket for client-server communication.
     */
    public static Socket socket;

    /**
     * The DatagramSocket for UDP communication.
     */
    public static DatagramSocket udpSocket;

    /**
     * Indicates whether the client is in a voice call.
     */
    public static boolean inCall = false;

    /**
     * String representing group call members' ports.
     */
    public static String groupCallMembersPorts;

    /**
     * String representing group call members' IP addresses.
     */
    public static String groupCallMembersIPs;

    /**
     * The port number used by the client.
     */
    public static int myPort;

    public static void main(String[] args) throws IOException {
        launchLogin(); // Open Login GUI
    }

    /**
     * Entry point of the client application. Launches the login GUI.
     *
     * @param args Command-line arguments (not used).
     * @throws IOException If an I/O error occurs.
     */
    public static void setServer(String host, String ip) throws IOException {
        //create socket and store input and output streams
        try {
            socket = new Socket(host, 5000);
        } catch (IOException e) {
            System.out.println("Server is not online, try again later x");
            System.exit(0);
        }

        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

        out.write(ip);
        out.newLine();
        out.flush();

        dataOutputStream = new DataOutputStream(socket.getOutputStream());
        dataInputStream = new DataInputStream(socket.getInputStream());

    }

    /**
     * Validates the users inputted username to ensure it is unique
     *
     * @param message message containing username
     * @param lblUsernameTaken label that will be displayed if username is not unique
     * @throws IOException If user interface can not be launched
     */
    public static void checkUsername(String message, Label lblUsernameTaken) throws IOException {

        int port;

        while (true) {
            Random rand = new Random();
            // Generate a random 4-digit number
            port = rand.nextInt(9000) + 1000;
            // Send port to server to see if there's duplicates
            out.write(port+"");
            out.newLine();
            out.flush();

            // Get back signal if there duplicates
            String cur = in.readLine();
            if (cur.equals("true")) {
                break;
            }
        }
        System.out.println("This is my port: " + port);

        udpSocket = new DatagramSocket(port);
        try {
            receiveVoice(udpSocket);
        } catch (LineUnavailableException e) {
            throw new RuntimeException(e);
        }
        myPort = port;

        username = message;
        out.write(message);
        out.newLine();
        out.flush();

        String result = in.readLine();

        if (result == null) {
            System.out.println("Server is down");
            System.exit(0);
        }

        //check is false then username is taken
        if (result.equals("False")) {
            usernameTaken(lblUsernameTaken);
        } else {
            Platform.runLater(() -> {
                try {
                    launchChat();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }

    }


    /**
     * Sends message to other users
     *
     * @param message the message that will be sent
     * @throws IOException If the writer does not work
     */
    public static void SendMessage(String message) throws IOException {
        if (isRunning) {
            out.write(message);
            out.newLine();
            out.flush();

            //If there's a signal for group voice call
            if (message.equals("グローバルコール")) {
                if (inCall == true) {
                    System.out.println("ending call");
                    // End sending packets and recording audio and close receiving packets
                    inCall = false;
                } else {
                    System.out.println("joining call");
                    inCall = true;

                    // Send list of who is on call
                    sendVoice(udpSocket);

                }
            }

        }
    }

    /**
     * Sends voice data for a whisper call to the specified IP and port via UDP.
     *
     * @param datagramSocket The DatagramSocket used for sending the voice data.
     * @param portWhisper    The port number for the whisper call.
     * @param ipWhisper      The IP address for the whisper call.
     */
    public static void sendWhisperVoice(DatagramSocket datagramSocket, int portWhisper, String ipWhisper) {
        // Create a thread for sending voice data
        Thread senderThread = new Thread(() -> {
            try {
                // Open the microphone for capturing audio
                AudioFormat format = new AudioFormat(44100, 16, 1, true, false);
                DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
                TargetDataLine line = (TargetDataLine) AudioSystem.getLine(info);
                line.open(format);
                line.start();

                // Create a buffer for storing audio data
                byte[] buffer = new byte[1024];

                while (inCall == true) {
                    // Read audio data from the microphone
                    int bytesRead = line.read(buffer, 0, buffer.length);

                    // Send the audio data as a message to each port wanting to call
                    DatagramPacket packet = new DatagramPacket(buffer, bytesRead, InetAddress.getByName(ipWhisper), portWhisper);
                    try {
                        datagramSocket.send(packet);
                    } catch (SocketException e) {
                        // Socket is closed, stop receiving
                        break;
                    }

                }
                line.stop();
                line.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        senderThread.start(); // Start the sender thread
    }

    /**
     * Sends voice data to all members in a group call via UDP.
     *
     * @param datagramSocket The DatagramSocket used for sending the voice data.
     */
    public static void sendVoice(DatagramSocket datagramSocket) {
        // Create a thread for sending voice data
        Thread senderThread = new Thread(() -> {
            try {
                // Open the microphone for capturing audio
                AudioFormat format = new AudioFormat(44100, 16, 1, true, false);
                DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
                TargetDataLine line = (TargetDataLine) AudioSystem.getLine(info);
                line.open(format);
                line.start();

                // Create a buffer for storing audio data
                byte[] buffer = new byte[1024];

                while (inCall == true) {
                    // Read audio data from the microphone

                    int bytesRead = line.read(buffer, 0, buffer.length);

                    if (groupCallMembersPorts == null) {

                    } else {
                        if (!groupCallMembersPorts.equals("")) {
                            String[] tempArr = groupCallMembersPorts.split(" ");
                            String[] ip_list = groupCallMembersIPs.split(" ");

                            int[] portArr = new int[tempArr.length];
                            for (int i = 0; i < tempArr.length; i++) {
                                portArr[i] = Integer.parseInt(tempArr[i]);
                            }

                            // Send the audio data as a message to each port wanting to call
                            for (int x = 0; x < portArr.length; x ++) {
                                if (portArr[x] != myPort) {
                                    DatagramPacket packet = new DatagramPacket(buffer, bytesRead, InetAddress.getByName(ip_list[x]), portArr[x]);
                                    try {
                                        datagramSocket.send(packet);
                                    } catch (SocketException e) {
                                        // Socket is closed, stop receiving
                                        break;
                                    }
                                }

                            }
                        }

                    }



                }
                line.stop();
                line.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        senderThread.start(); // Start the sender thread
    }

    /**
     * Receives voice data via UDP and plays it back using the SourceDataLine.
     *
     * @param datagramSocket The DatagramSocket used for receiving the voice data.
     * @throws LineUnavailableException If the audio line is unavailable or cannot be opened.
     */
    public static void receiveVoice(DatagramSocket datagramSocket) throws LineUnavailableException {
        // Create a thread for receiving voice data
        Thread receiveThread = new Thread(() -> {
            // Open a SourceDataLine for playing back the audio
            AudioFormat format = new AudioFormat(44100, 16, 1, true, false);
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
            SourceDataLine line = null;
            try {
                line = (SourceDataLine) AudioSystem.getLine(info);
                line.open(format);
                line.start();

                // Receive and play voice data
                while (true) {
                    byte[] buffer = new byte[1024];
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    try {
                        datagramSocket.receive(packet);
                    } catch (SocketException e) {
                        // Socket is closed, stop receiving
                        break;
                    }

                    // Write received audio data to the SourceDataLine for playback
                    line.write(buffer, 0, buffer.length);
                }
            } catch (LineUnavailableException | IOException e) {
                throw new RuntimeException(e);
            }

        });
        receiveThread.start();

    }

    /**
     * Sends voice note data along with a message to the server.
     *
     * @param audioData The byte array containing the voice note data.
     * @param message   The message to be sent along with the voice note.
     * @throws IOException          If an I/O error occurs during the write operation.
     * @throws InterruptedException If the thread is interrupted while sleeping.
     */
    public static void sendVN(byte[] audioData, String message) throws IOException, InterruptedException {
        if (isRunning) {
            out.write(message);
            out.newLine();
            out.flush();

            Thread.sleep(1000);

            dataOutputStream.write(audioData);
            dataOutputStream.flush();

        }

    }


    /**
     * Launches login screen
     */
    private static void launchLogin() throws IOException {
        // Start the login GUI
        Application.launch(LoginApplication.class);


    }

    /**
     * Launches the user interface containing the global chat and online users list
     *
     * @throws IOException If it can not launch the user interface
     */
    private static void launchChat() throws IOException {
        // Close the login GUI
        closeLogin();

        // Start the chat GUI directly
        ChatGuiApplication chatApp = new ChatGuiApplication(username);
        Stage chatStage = new Stage();
        chatApp.start(chatStage);

        // Add event handler to detect when the chat GUI is closed
        chatStage.setOnCloseRequest(event -> {
            try {
                isRunning = false;
                inCall = false;
                udpSocket.close();
                socket.close();
                in.close();
                out.close();
                closeAllChatRooms();

            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        });

    }

    public static void shutdownClient() {
        System.out.println("asdasdasd");
        udpSocket.close();
        System.exit(0);
    }

    /**
     * Receives messages from other users
     *
     * @param VBoxGlobal VBox of the global chat
     * @param onlineUsersListView ListView component that displays the online users
     */
    public void receiveMessage(VBox VBoxGlobal, ListView onlineUsersListView) {
        // Create a separate thread for receiving messages
        new Thread(() -> {
            while (isRunning) {
                String message = null;
                try {
                    synchronized (in) {
                        // Synchronize the readLine operation
                        message = in.readLine();

                        if (message == null) {
                            System.out.println("Server Disconnected :(");
                            System.exit(0);
                        }
                    }

                    //Check if message starts with special characters and if so handle username list
                    //Don't translate........ or do
                    if (message != null && message.startsWith("満点をお願いします。")) {
                        String[] temp = message.split(" ");
                        String[] usernames = Arrays.copyOfRange(temp, 1, temp.length);
                        onlineUsersLabel(usernames, onlineUsersListView);

                        // This is for group member on the call
                    } else if (message != null && message.startsWith("グループ通話メンバー ")) {

                        // Find the index of the first space
                        int firstSpaceIndex = message.indexOf(' ');

                        String groupMembers = message.substring(firstSpaceIndex + 1);
                        groupCallMembersPorts = groupMembers;

                        // Now get the string of IPs
                        String ip_list = in.readLine();
                        groupCallMembersIPs = ip_list;


                    // Whisper call
                    } else if (message != null && message.startsWith("ささやき声通話 ")) {

                        String[] temp = message.split(" ");

                        if (temp[1].equals("unavailable")) {
                            inCall = false;
                            System.out.println("User is on a call");
                        } else {
                            inCall = true;
                            System.out.println("8");
                            setUpChatroom(temp[3], "incoming call...", 0, "End");
                            sendWhisperVoice(udpSocket, Integer.parseInt(temp[1]), temp[2]);
                        }


                    // This is to end whisper call
                    } else if (message != null && message.startsWith("音声通話を終了する")) {
                        System.out.println("9");
                        inCall = false;
                        String out = message.split(" ")[1];
                        setUpChatroom(out, "Ending call...", 0, "Call");


                    // This is for a DM
                    } else if (message != null && message.startsWith("これはダイレクトメッセージです")) {
                        // Find the index of the first space
                        int firstSpaceIndex = message.indexOf(' ');
                        // Find the index of the next space after the first one
                        int secondSpaceIndex = message.indexOf(' ', firstSpaceIndex + 1);
                        // Extract the username from the substring between the first and second spaces
                        String username = message.substring(firstSpaceIndex + 1, secondSpaceIndex);
                        // Extract the whisper message from the substring after the second space
                        String whisperMessage = message.substring(secondSpaceIndex + 1);
                        setUpChatroom(username, whisperMessage, 0, null);

                        // This is for Global voice note
                    } else if (message != null && message.startsWith("グローバルボイスノート")) {
                        String[] cur = message.split(" ");
                        String fileName = cur[2];
                        int audioSize = Integer.parseInt(cur[1]);

                        byte[] audioData = new byte[audioSize];
                        dataInputStream.readFully(audioData, 0, audioSize);

                        saveAudioFile(audioData, fileName);
                        addVoiceNoteButton(fileName, VBoxGlobal, 1);

                        // This is for DM voice note
                    } else if (message != null && message.startsWith("プライベートボイスメモ")) {
                        String[] cur = message.split(" ");
                        String fileName = cur[2];
                        String clientUsername = cur[3];
                        int audioSize = Integer.parseInt(cur[1]);

                        byte[] audioData = new byte[audioSize];
                        dataInputStream.readFully(audioData, 0, audioSize);

                        saveAudioFile(audioData, fileName);
                        //addVoiceNoteButton(fileName, VBoxGlobal, 1);
                        setUpChatroom(clientUsername, fileName, 1, null);

                    } else if (message != null) {
                        //playRingtone();
                        receiveMessageLabel(message, VBoxGlobal);
                    }

                } catch (SocketException e) {
                    // Socket closed, exit the loop
                    break;
                } catch (IOException e) {

                }
            }
        }).start();
    }

    private void saveAudioFile(byte[] audioData, String fileName) throws IOException {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(audioData);
        AudioFormat format = new AudioFormat(44100, 16, 1, true, false);

        // Create an AudioInputStream from the byte array
        AudioInputStream audioStream = new AudioInputStream(byteArrayInputStream, format, audioData.length / format.getFrameSize());

        // Save the audio to a WAV file
        File folder = new File("./VoiceNotes");
        if (!folder.exists()) {
            folder.mkdirs();
        }
        File outputFile = new File(folder, fileName);
        AudioSystem.write(audioStream, AudioFileFormat.Type.WAVE, outputFile);

        System.out.println("Audio saved to " + outputFile.getAbsolutePath() + "\n");
    }


    /**
     * Closes the login screen
     */
    private static void closeLogin() {
        // Get the stage of the login GUI
        Stage loginStage = LoginApplication.getStage();

        // Close the login GUI
        loginStage.close();
    }

    private static void playRingtone() {
        try {
            File musicPath = new File("./ringtone/linging.wav");

            if (musicPath.exists()) {
                AudioInputStream audioInput = AudioSystem.getAudioInputStream(musicPath);
                Clip clip = AudioSystem.getClip();
                clip.open(audioInput);
                clip.start();

                Timer timer = new Timer(5000, new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        // Stop the playback
                        clip.stop();
                        // Close the clip to release system resources
                        clip.close();
                    }
                });
                timer.setRepeats(false); // Ensure the task only runs once
                timer.start();
            } else {
                System.out.println("linging.wav does not exist");
            }
        } catch (UnsupportedAudioFileException e) {
            System.out.println("Unsupported audio file format: " + e.getMessage());
        } catch (Exception err) {
            err.printStackTrace();
        }

    }

}
