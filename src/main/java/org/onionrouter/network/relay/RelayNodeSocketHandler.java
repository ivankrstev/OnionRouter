package org.onionrouter.network.relay;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.onionrouter.network.DecryptedPayload;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;

public class RelayNodeSocketHandler extends Thread {
    private final Socket clientSocket;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final PrivateKey privateKey;

    public RelayNodeSocketHandler(Socket socket, PrivateKey privateKey) {
        this.clientSocket = socket;
        this.privateKey = privateKey;
    }

    public void run() {
        try (DataInputStream in = new DataInputStream(new BufferedInputStream(clientSocket.getInputStream()))) {
            int messageLength = in.readInt(); // Read the length of the message
            byte[] messageBytes = new byte[messageLength]; // Create a byte array to store the message
            in.readFully(messageBytes); // Read the message bytes
            String relayNodeMessageString = new String(messageBytes, StandardCharsets.UTF_8); // Convert the message bytes to string
            System.out.println("Received an encrypted message: " + relayNodeMessageString);

            RelayNodeMessage relayNodeMessage = objectMapper.readValue(relayNodeMessageString, RelayNodeMessage.class);
            DecryptedPayload decryptedMessage = relayNodeMessage.decryptAndDeserializeEncryptedMessage(privateKey);
            System.out.println("Decrypted message: " + decryptedMessage);

            new RelaySocketClient(decryptedMessage.getDestination()).forwardMessage(decryptedMessage.getMessage());
        } catch (IOException e) {
            System.out.println("Error while reading the message from the client!");
        }
    }
}