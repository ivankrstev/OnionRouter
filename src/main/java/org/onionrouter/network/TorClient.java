package org.onionrouter.network;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.onionrouter.network.relay.RelayNodeMessage;
import org.onionrouter.security.AES;
import org.onionrouter.security.RSA;
import org.onionrouter.torserver.TorNodeInfo;

import javax.crypto.SecretKey;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TorClient {
    private final String torNodesServerAddress;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public TorClient(String torServerAddress) {
        this.torNodesServerAddress = torServerAddress;
    }

    public void sendRequest(HttpPayload httpPayload) throws Exception {
        TorNodeInfo[] torNodes = getTorNodesFromServer();
        if (torNodes == null || torNodes.length == 0) return;
        String destination = torNodes[0].getAddress(); // First (entry) relay node address
        for (int i = torNodes.length - 1; i >= 0; i--) {
            if (i != torNodes.length - 1) {
                // Set a different payload for each relay node(except the exit node, which forwards the request to the final destination)
                httpPayload.setDestination(torNodes[i + 1].getAddress());
                httpPayload.setMethod("POST");
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                httpPayload.setHeaders(headers);
            }
            String httpPayloadSerialized = objectMapper.writeValueAsString(httpPayload);
            SecretKey AESKey = AES.generateKey();
            String encryptedPayloadJSONString = AES.encrypt(httpPayloadSerialized, AESKey);
            RelayNodeMessage relayNodeMessage = new RelayNodeMessage(encryptedPayloadJSONString, RSA.encrypt(AESKey, torNodes[i].parseAndGetPublicKey()));
            httpPayload.setBody(objectMapper.writeValueAsString(relayNodeMessage));
        }
        System.out.println("First relay: " + destination);
        System.out.println("Sending message: " + new String(httpPayload.getBody()));
        System.out.println("Response from tor client: " + new HttpMessageDispatcher().sendPostRequest(destination, new String(httpPayload.getBody())));
    }

    private TorNodeInfo[] getTorNodesFromServer() {
        try {
            String jsonResponse = new HttpMessageDispatcher().sendGetRequest(torNodesServerAddress + "/get-nodes");
            // Deserialize the json response to an array of TorRouterInfoObject
            List<TorNodeInfo> nodes = objectMapper.readValue(jsonResponse, new TypeReference<>() {
            });
            return nodes.toArray(new TorNodeInfo[0]);
        } catch (Exception e) {
            return null;
        }
    }
}
