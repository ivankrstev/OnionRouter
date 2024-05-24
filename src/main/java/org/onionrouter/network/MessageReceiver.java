package org.onionrouter.network;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class MessageReceiver {
    public static void main(String[] args) throws Exception {
        //    Rename this(InboxServer or InboxProcessor)
        try (ServerSocket inboxSocket = new ServerSocket(8080)) {
            System.out.println("Inbox server started on port 8080");
            while (true) {
                try (Socket clientSocket = inboxSocket.accept();
                     DataInputStream in = new DataInputStream(clientSocket.getInputStream())) {
                    String message = in.readUTF();
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