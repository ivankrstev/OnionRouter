package org.onionrouter.network.relay;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.jetty.server.Server;
import org.onionrouter.network.HttpMessageDispatcher;
import org.onionrouter.security.RSA;
import org.onionrouter.torserver.TorNodeInfo;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

public class RelayNode {
    private static final String torNodesServerUrl = "http://172.28.0.100:5500";
    private final PublicKey publicKey;
    private final RelayStatus relayStatus;
    private final int port = 9000;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public RelayNode(RelayStatus relayStatus) throws Exception {
        this.relayStatus = relayStatus;
        KeyPair generatedKeyPair = RSA.generateKeyPair();
        this.publicKey = generatedKeyPair != null ? generatedKeyPair.getPublic() : null;
        PrivateKey privateKey = generatedKeyPair != null ? generatedKeyPair.getPrivate() : null;
        Server server = new Server(new InetSocketAddress("0.0.0.0", this.port));
        server.setHandler(new RelayHandler(privateKey));
        server.start();
        sendRouterInfoToServer();
        server.join();
    }

    private void sendRouterInfoToServer() {
        try {
            InetAddress IP = InetAddress.getLocalHost();
            String socketAddress = "http://" + IP.getHostAddress() + ":" + this.port;
            TorNodeInfo torNodeInfo = new TorNodeInfo(Base64.getEncoder().encodeToString(publicKey.getEncoded()), socketAddress, this.relayStatus);
            String jsonString = objectMapper.writeValueAsString(torNodeInfo);
            String response = new HttpMessageDispatcher().sendPostRequest(torNodesServerUrl + "/add", jsonString);
            if (response == null)
                throw new Exception("Could not create the relay node due to an error");
            System.out.println("Relay successfully created with:");
            System.out.println("Address: " + socketAddress);
            System.out.println("Status: " + this.relayStatus);
        } catch (Exception e) {
            System.out.println("Error while creating the relay node!");
        }
    }

    public static void main(String[] args) throws Exception {
        RelayStatus relayStatus = System.getenv("ROUTER_STATUS") != null ? RelayStatus.valueOf(System.getenv("ROUTER_STATUS")) : RelayStatus.EXIT;
        new RelayNode(relayStatus);
    }
}