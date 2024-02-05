package org.onionrouter.nodes;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.onionrouter.security.RSA;
import org.onionrouter.torserver.TorRouterInfoObject;

import javax.servlet.http.HttpServletResponse;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

public class RouterNode {
    private static final String torNodesServerUrl = "http://localhost:5500";
    private final PrivateKey privateKey;
    private final PublicKey publicKey;
    private RouterStatus routerStatus;
    private int port;

    public RouterNode() {
        KeyPair generatedKeyPair = RSA.generateKeyPair();
        this.privateKey = generatedKeyPair != null ? generatedKeyPair.getPrivate() : null;
        this.publicKey = generatedKeyPair != null ? generatedKeyPair.getPublic() : null;
        sendRouterInfoToServer();
    }

    private void sendRouterInfoToServer() {
        try {
            HttpClient client = HttpClient.newHttpClient();
            TorRouterInfoObject obj = new TorRouterInfoObject(Base64.getEncoder().encodeToString(publicKey.getEncoded()));
            ObjectMapper mapper = new ObjectMapper();
            String jsonString = mapper.writeValueAsString(obj);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(torNodesServerUrl + "/add"))
                    .header("Content-Type", "application/json")
                    .POST(BodyPublishers.ofString(jsonString))
                    .build();

            HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
            if (response.statusCode() != HttpServletResponse.SC_OK)
                throw new Exception("Could not create the router node due to an error");
            // Read the json response and map it to TorRouterInfoObject
            TorRouterInfoObject responseNode = mapper.readValue(response.body(), TorRouterInfoObject.class);
            // Set the port and status from the response of the tor nodes server, after successful addition
            this.port = responseNode.getPort();
            this.routerStatus = responseNode.getStatus();
            System.out.println("Router successfully created with info:");
            System.out.println("port: " + this.port);
            System.out.println("status: " + this.routerStatus);
        } catch (Exception e) {
            System.out.println("Error while creating the router node!");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new RouterNode();
    }
}