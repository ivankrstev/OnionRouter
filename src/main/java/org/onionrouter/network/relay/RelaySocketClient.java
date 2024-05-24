package org.onionrouter.network.relay;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class RelaySocketClient {
    private final String nextRelayHost;
    private final int nextRelayPort;

    public RelaySocketClient(String nextRelayHostAddress) {
        try {
            String[] parts = nextRelayHostAddress.split(":");
            this.nextRelayHost = parts[0];
            this.nextRelayPort = Integer.parseInt(parts[1]);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid next relay host address: " + nextRelayHostAddress);
        }
    }

    public void forwardMessage(String message) {
        try (Socket socket = new Socket(this.nextRelayHost, this.nextRelayPort);
             DataOutputStream out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()))) {
            // Using try-with-resources to ensure socket and output stream are automatically closed
            byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8); // Convert the message to bytes
            out.writeInt(messageBytes.length); // Write the length of the message
            out.write(messageBytes); // Write the message bytes
            System.out.println("The message forwarded to next relay: " + message);
        } catch (IOException e) {
            System.out.println("Error while forwarding the message to the next relay!");
        }
    }
}