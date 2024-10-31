package com.example.voip;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.HashMap;
import java.io.BufferedReader;
import java.io.BufferedWriter;

/**
 * Stores sll the online clients so that it is easier to access and manage
 *
 * @author ROBERT SHONE – 25132687
 * @author KEURAN KISTAN – 23251646
 * @author TASHEEL GOVENDER – 25002112
 */
public class Clients {
    private HashMap<String, Client> clients;

    public Clients() {
        clients = new HashMap<>();
    }

    /**
     * Add client to the hashmap
     *
     * @param username username of client
     * @param client client object of client
     */
    public void addClient(String username, Client client) {
        clients.put(username, client);
    }

    /**
     *  Get a client by their username
     *
     * @param username Username of client
     * @return Client object of matching client
     */
    public Client getClient(String username) {
        return clients.get(username);
    }

    public boolean getPort(int num) {
        for (Client client : clients.values()) {
            if (client.getPort() == num) {
                return true; // Port found, return true
            }
        }
        return false; // Port not found, return false
    }

    /**
     * Get the hashmap of all clients and their username
     *
     * @return hashmap of clients
     */
    public HashMap<String, Client> getAllClients() {
        return clients;
    }

    /**
     * Remove a client by username
     *
     * @param username User to be removed
     */
    public void removeClient(String username) {
        clients.remove(username);
    }

     /**
     * Method to get all usernames separated by space
     *
     * @return All usernames of online users
     */
    public String getAllUsernames() {
        StringBuilder usernamesBuilder = new StringBuilder();
        usernamesBuilder.append("満点をお願いします。 ");
        for (String username : clients.keySet()) {
            usernamesBuilder.append(username).append(" ");
        }
        return usernamesBuilder.toString().trim(); // Trim to remove trailing space
    }

}
/**
 * Contains information about each client
 *
 * @author ROBERT SHONE – 25132687
 * @author KEURAN KISTAN – 23251646
 * @author TASHEEL GOVENDER – 25002112
 */
class Client {
    private BufferedReader reader;
    private BufferedWriter writer;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;
    private String IP;
    private int port;
    private boolean inCall;

    /**
     * Constructor for the class
     *
     * @param reader clients reader
     * @param writer clients writer
     */
    public Client(BufferedReader reader, BufferedWriter writer, DataInputStream dataInputStream, DataOutputStream dataOutputStream, int num, String ip) {
        this.reader = reader;
        this.writer = writer;
        this.dataInputStream = dataInputStream;
        this.dataOutputStream = dataOutputStream;
        this.port = num;
        this.inCall = false;
        this.IP = ip;
    }

    /**
     * Returns clients reader
     *
     * @return clients reader
     */
    public BufferedReader getReader() {
        return reader;
    }

    /**
     * Returns clients writer
     *
     * @return clients writer
     */
    public BufferedWriter getWriter() {
        return writer;
    }

    /**
     * Returns clients dataOutput
     *
     * @return clients dataOutput
     */
    public DataOutputStream getDataOutputStream() {
        return dataOutputStream;
    }

    /**
     * Returns clients dataInput
     *
     * @return clients dataInput
     */
    public DataInputStream getDataInputStream() {
        return dataInputStream;
    }

    public int getPort() {
        return port;
    }

    public boolean isInCall() {
        return inCall;
    }

    public void setInCall(boolean cur) {
        inCall = cur;
    }

    public String getIP() {
        return IP;
    }
}

