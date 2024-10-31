package com.example.voip;

import javafx.application.Application;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

import static com.example.voip.ServerController.setActivity;
import static com.example.voip.ServerController.setOnlineUsers;

/**
 * Server class that is run to start the server.
 *  Starts the server socket and waits for clients to join
 *
 * @author ROBERT SHONE – 25132687
 * @author KEURAN KISTAN – 23251646
 * @author TASHEEL GOVENDER – 25002112
 */
public class ChatServer {


    public static Clients clients;
    public static ListView onlineUsersListView;
    public static VBox vbxActivity;

    //public static ArrayList<Integer> groupCallMembers = new ArrayList();
    static HashMap<Integer, String> groupCallMembers = new HashMap<>();


    public static void main(String[] args) throws IOException {
        //Create server
        ServerSocket serverSocket = new ServerSocket(5000);
        System.out.println("Server is running...");

        // Start the login GUI
        new Thread(() -> {
            // Launch the JavaFX application
            Application.launch(ServerApplication.class, args);
        }).start();

        //Create Clients which maps usernames to client objects
        clients = new Clients();

        //Create threads so multiple clients can be accpeted at same time
        new Thread(() -> {
            try {
                //Wait for connection to server and create separate thread to handle that client
                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    new Thread(new ClientHandler(clientSocket)).start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

    }

    public static void shutdownServer() {
        System.exit(0);
    }

    public void receiveListView(ListView listView, VBox vbx) {
        onlineUsersListView = listView;
        vbxActivity = vbx;
    }

    /**
     * Handles the connection between the server and the client
     *
     * @author ROBERT SHONE – 25132687
     * @author KEURAN KISTAN – 23251646
     * @author TASHEEL GOVENDER – 25002112
     */
    private static class ClientHandler implements Runnable {
        private Socket clientSocket;
        private BufferedWriter writer;
        private BufferedReader reader;
        private DataOutputStream dataOutputStream;
        private DataInputStream dataInputStream;
        private String username;

        /**
         * Constructor for the class
         *
         * @param socket Client socket
         */
        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        @Override
        public void run() {
            try {
                writer = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
                reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                dataOutputStream = new DataOutputStream(clientSocket.getOutputStream());
                dataInputStream = new DataInputStream(clientSocket.getInputStream());

                String ip = reader.readLine();

                int port;
                // Check port
                while (true) {
                    String temp_port = reader.readLine();
                    // If port not used send back the ok
                    if (clients.getPort(Integer.parseInt(temp_port))) {
                        writer.write("false");
                        writer.newLine();
                        writer.flush();
                    } else {
                        writer.write("true");
                        writer.newLine();
                        writer.flush();
                        port = Integer.parseInt(temp_port);
                        break;
                    }

                }

                // Get a unique username
                boolean isUsernameUnique = false;

                // Keep asking for a username until a unique one is provided
                while (!isUsernameUnique) {
                    username = reader.readLine();

                    if (username == null) {
                        clientSocket.close();
                        writer.close();
                        reader.close();
                        dataOutputStream.close();
                        dataInputStream.close();
                        return;
                    }
                    // Check if the username is unique
                    if (clients.getClient(username) != null) {
                        // Found a duplicate
                        writer.write("False");
                    } else {
                        // Username is unique
                        writer.write("True");
                        System.out.println("User has connected!");
                        isUsernameUnique = true;
                    }
                    writer.newLine();
                    writer.flush();
                }

                // Add new client to hash-map
                Client client = new Client(reader, writer, dataInputStream, dataOutputStream, port, ip);
                clients.addClient(username, client);

                setOnlineUsers(clients.getAllClients().keySet().toArray(new String[0]), onlineUsersListView);
                setActivity(username + " joined the server with port: " + port, vbxActivity);

                // Notify all clients about the new user
                broadcast(username + " has joined the chat.\n", null);
                // Send all online users to all clients
                broadcast(clients.getAllUsernames(), null);

                // Handle client messages
                String message;
                while ((message = reader.readLine()) != null) {
                    if (!message.isEmpty()) {
                        // Whisper
                        if (message.startsWith("これはダイレクトメッセージです")) {
                            handleWhisper(message);
                            String otherUser = message.split(" ")[1];
                            setActivity(username + " is whispering " + otherUser, vbxActivity);

                        // Global call
                        } else if (message.startsWith("グローバルコール")) {

                            // if in call then must remove from list
                            if (client.isInCall() == true) {
                                client.setInCall(false);
                                setActivity(username + " has left global call", vbxActivity);

                                groupCallMembers.remove(port);

                                broadcast("グループ通話メンバー " + portArrayToString(), null);
                                broadcast(IPArrayToString(), null);


                            // if not in call then add to list
                            } else {
                                client.setInCall(true);
                                setActivity(username + " has joined global call", vbxActivity);

                                groupCallMembers.put(port, ip);

                                broadcast("グループ通話メンバー " + portArrayToString(), null);
                                broadcast(IPArrayToString(), null);
                            }


                        // Whisper call
                        } else if (message.startsWith("ささやき声通話")) {

                            String whisperUsername = message.split(" ")[1];

                            // if receiver is not on a call
                            if (clients.getClient(whisperUsername).isInCall() == false) {

                                client.setInCall(true);
                                clients.getClient(whisperUsername).setInCall(true);

                                int whisperPort = clients.getClient(whisperUsername).getPort();
                                String whisperIP = clients.getClient(whisperUsername).getIP();

                                BufferedWriter bf = clients.getClient(whisperUsername).getWriter();
                                bf.write("ささやき声通話 " + client.getPort() + " " + client.getIP() + " " + username);
                                bf.newLine();
                                bf.flush();

                                writer.write("ささやき声通話 " + whisperPort + " " + whisperIP + " " + whisperUsername);
                                writer.newLine();
                                writer.flush();

                            // if receiver is on a call
                            } else {
                                writer.write("ささやき声通話 unavailable");
                                writer.newLine();
                                writer.flush();
                            }



                        // End voice call
                        } else if (message.startsWith("音声通話を終了する")) {

                            String user = message.split(" ")[1];

                            client.setInCall(false);
                            clients.getClient(user).setInCall(false);

                            BufferedWriter bf = clients.getClient(user).getWriter();
                            bf.write("音声通話を終了する " + username);
                            bf.newLine();
                            bf.flush();

                            writer.write("音声通話を終了する " + user);
                            writer.newLine();
                            writer.flush();


                        // Global voice note
                        } else if (message.startsWith("グローバルボイスノート")) {
                            System.out.println(message);
                            String[] cur = message.split(" ");
                            String fileName = cur[1];
                            int audioSize = Integer.parseInt(cur[2]);

                            byte[] audioData = new byte[audioSize];
                            dataInputStream.readFully(audioData, 0, audioSize);

                            broadcast("グローバルボイスノート " + audioSize + " " + fileName, writer);

                            Thread.sleep(1000);

                            broadcastVoiceNote(audioData, dataOutputStream);
                            setActivity(username + " send voice note to global", vbxActivity);

                        // DM voice note
                        } else if (message.startsWith("プライベートボイスメモ")) {
                            System.out.println(message);
                            String[] cur = message.split(" ");
                            String fileName = cur[1];
                            int audioSize = Integer.parseInt(cur[2]);
                            String sendToUsername = cur[3];

                            byte[] audioData = new byte[audioSize];
                            dataInputStream.readFully(audioData, 0, audioSize);

                            // Check if person sending to exists
                            if (clients.getClient(sendToUsername) == null) {
                                try {
                                    writer.write("これはダイレクトメッセージです " + sendToUsername+ " " + "User does not exist/Not online\n");
                                    writer.flush();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                // show sender this message
                                try {

                                    BufferedWriter clientTempWriter = clients.getClient(sendToUsername).getWriter();
                                    clientTempWriter.write("プライベートボイスメモ " + audioSize + " " + fileName + " " + username);
                                    clientTempWriter.newLine();
                                    clientTempWriter.flush();


                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }

                            Thread.sleep(1000);

                            DataOutputStream clientTemp = clients.getClient(sendToUsername).getDataOutputStream();
                            clientTemp.write(audioData);
                            clientTemp.flush();

                            String otherUser = message.split(" ")[3];
                            setActivity(username + " is whispering a voice note to " + otherUser, vbxActivity);


                        } else {
                            System.out.println("GLobal message");
                            // Broadcast the message to all clients
                            broadcast(username + ": " + message + "\n", writer);
                            setActivity(username + " sent message to global", vbxActivity);
                        }
                    } else {
                        handleDisconnection();
                        break;
                    }
                }
                handleDisconnection();
            } catch (IOException e) {
                try {
                    handleDisconnection();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }

            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        private static String portArrayToString() {
            StringBuilder sb = new StringBuilder();
            for (Integer number : groupCallMembers.keySet()) {
                sb.append(number).append(" ");
            }
            // Remove the trailing space
            String result = sb.toString().trim();

            return result;
        }

        private static String IPArrayToString() {
            StringBuilder sb = new StringBuilder();
            for (String ip : groupCallMembers.values()) {
                sb.append(ip).append(" ");
            }
            // Remove the trailing space
            String result = sb.toString().trim();

            return result;
        }


        /**
         * Sends message to global chat
         *
         * @param message Message to be sent
         * @param writer Sender of the message
         */
        private static synchronized void broadcast(String message, BufferedWriter writer) {

            for (Client client : clients.getAllClients().values()) {

                if (client.getWriter() != writer) {
                    BufferedWriter clientWriter = client.getWriter();
                    try {
                        clientWriter.write(message);
                        clientWriter.newLine();
                        clientWriter.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
        }

        /**
         * Sends voice note to global chat
         *
         * @param audioData audio data to be sent
         * @param dataOutputStream Sender of the voice message
         */
        private static synchronized void broadcastVoiceNote(byte[] audioData, DataOutputStream dataOutputStream) {

            for (Client client : clients.getAllClients().values()) {

                if (client.getDataOutputStream() != dataOutputStream) {
                    DataOutputStream clientWriter = client.getDataOutputStream();
                    try {
                        //Do not take this out as for some reason it lets the server receive the audio bytes
                        //for (int x = 0; x < audioData.length; x++) {
                            //System.out.print(audioData[x]);
                        //}
                        Thread.sleep(1000);
                        clientWriter.write(audioData);
                        clientWriter.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }

            }
        }

        /**
         * Handles the whisper between clients
         *
         * @param message Whisper message
         */
        private void handleWhisper(String message) {

            //@Wommy what's up man

            // Find the index of the first space
            int firstSpaceIndex = message.indexOf(' ');

            // Find the index of the next space after the first one
            int secondSpaceIndex = message.indexOf(' ', firstSpaceIndex + 1);

            // Extract the username from the substring between the first and second spaces
            String userToSendMessage = message.substring(firstSpaceIndex + 1, secondSpaceIndex);

            // Extract the whisper message from the substring after the second space
            String whisperMessage = message.substring(secondSpaceIndex + 1);


                // Check if person sending to exists
                if (clients.getClient(userToSendMessage) == null) {
                    try {
                        writer.write("これはダイレクトメッセージです " + userToSendMessage+ " " + "User does not exist/Not online\n");
                        writer.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    // show sender this message
                    try {

                        BufferedWriter client = clients.getClient(userToSendMessage).getWriter();
                        client.write("これはダイレクトメッセージです " + this.username + " " + whisperMessage + "\n");
                        client.flush();


                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
        }


        /**
         * Handles the disconnection of the client
         *
         * @throws IOException If disconnection is unsuccessful
         */
        private void handleDisconnection() throws IOException {
            System.out.println("User has Disconnected!");
            if (username != null) {
                Client deserter = clients.getClient(username);
                clientSocket.close();
                deserter.getWriter().close();
                deserter.getReader().close();
                clients.removeClient(username);
                broadcast(username + " has left the chat.\n", null);
                // Update online client list on clients side
                broadcast(clients.getAllUsernames(), null);
                setOnlineUsers(clients.getAllClients().keySet().toArray(new String[0]), onlineUsersListView);
                setActivity(username + " left the server", vbxActivity);
            }

        }
    }

}
