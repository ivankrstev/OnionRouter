package org.onionrouter.nodes;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.onionrouter.network.HttpMessageDispatcher;
import org.onionrouter.network.RelayNodeSocketHandler;
import org.onionrouter.security.RSA;
import org.onionrouter.torserver.TorNodeInfo;

import java.net.Socket;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;
import java.util.Objects;

public class RelayNode {
    private static final String torNodesServerUrl = "http://localhost:5500";
    private final PublicKey publicKey;
    private final RelayStatus relayStatus;
    private final int port;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public RelayNode(int port, RelayStatus relayStatus) {
        this.port = port;
        this.relayStatus = relayStatus;
        KeyPair generatedKeyPair = RSA.generateKeyPair();
        this.publicKey = generatedKeyPair != null ? generatedKeyPair.getPublic() : null;
        PrivateKey privateKey = generatedKeyPair != null ? generatedKeyPair.getPrivate() : null;
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            sendRouterInfoToServer();
            // Continuously accept new client connections
            while (true) {
                Socket clientSocket;
                try {
                    // Wait for a connection from a client
                    clientSocket = serverSocket.accept();
                    // Handle the client connection in separate thread
                    new RelayNodeSocketHandler(clientSocket, privateKey).start();
                } catch (IOException ignored) {
                    System.out.println("Error while accepting the client socket!");
                }
            }
        } catch (IOException ignored) {
            System.out.println("Error while creating the server socket!");
        }
    }

    private void sendRouterInfoToServer() {
        try {
            InetAddress IP = InetAddress.getLocalHost();
            String socketAddress = IP.getHostAddress() + ":" + this.port;
            TorNodeInfo torNodeInfo = new TorNodeInfo(Base64.getEncoder().encodeToString(publicKey.getEncoded()), socketAddress, this.relayStatus);
            String jsonString = objectMapper.writeValueAsString(torNodeInfo);
            String response = new HttpMessageDispatcher().sendPostRequest(torNodesServerUrl + "/add", jsonString);
            if (response == null)
                throw new Exception("Could not create the router node due to an error");
            System.out.println("Router successfully created with:");
            System.out.println("Address: " + socketAddress);
            System.out.println("Status: " + this.relayStatus);
        } catch (Exception e) {
            System.out.println("Error while creating the router node!");
        }
    }

    public static void main(String[] args) {
        RelayStatus relayStatus = System.getenv("ROUTER_STATUS") != null ? RelayStatus.valueOf(System.getenv("ROUTER_STATUS")) : RelayStatus.ENTRY;
        int port = Integer.parseInt(Objects.requireNonNullElse(System.getenv("PORT"), "5000"));
        new RelayNode(port, relayStatus);
    }
}