package org.onionrouter.network;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class MessageReceiver {
    public static void main(String[] args) throws Exception {
        //    Rename this(InboxServer or InboxProcessor)
        try (ServerSocket inboxSocket = new ServerSocket(8080)) {
            System.out.println("Inbox server started on port 8080");
            while (true) {
                try (Socket clientSocket = inboxSocket.accept();
                     DataInputStream in = new DataInputStream(new BufferedInputStream(clientSocket.getInputStream()))) {
                    int messageLength = in.readInt(); // Read the length of the message
                    byte[] messageBytes = new byte[messageLength]; // Create a byte array to store the message
                    in.readFully(messageBytes); // Read the message bytes
                    String message = new String(messageBytes, StandardCharsets.UTF_8); // Convert the message bytes to string
                    System.out.println("Received message in inbox: " + message);
                } catch (IOException e) {
                    System.out.println("Error while accepting the client socket!");
                }
            }
        } catch (IOException e) {
            System.out.println("Error while creating the inbox socket!");
        }
    }
}