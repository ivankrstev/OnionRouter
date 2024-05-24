package org.onionrouter.network;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.onionrouter.network.relay.RelayNodeMessage;
import org.onionrouter.network.relay.RelaySocketClient;
import org.onionrouter.security.AES;
import org.onionrouter.security.RSA;
import org.onionrouter.torserver.TorNodeInfo;

import javax.crypto.SecretKey;
import java.util.List;

public class TorClient {
    private final String torNodesServerAddress;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public TorClient(String torServerAddress) {
        this.torNodesServerAddress = torServerAddress;
    }

    public void sendMessage(String message, String destination) throws Exception {
        TorNodeInfo[] torNodes = getTorNodesFromServer();
        if (torNodes == null || torNodes.length == 0)
            return;
        for (int i = torNodes.length - 1; i >= 0; i--) {
            SecretKey AESKey = AES.generateKey();
            DecryptedPayload decryptedPayloadObject = new DecryptedPayload(message, destination);
            String encryptedMessageJson = objectMapper.writeValueAsString(decryptedPayloadObject);
            encryptedMessageJson = AES.encrypt(encryptedMessageJson, AESKey);
            RelayNodeMessage relayNodeMessage = new RelayNodeMessage(encryptedMessageJson, RSA.encrypt(AESKey, torNodes[i].parseAndGetPublicKey()));
            message = objectMapper.writeValueAsString(relayNodeMessage);
            destination = torNodes[i].getAddress();
        }
        new RelaySocketClient(destination).forwardMessage(message);
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
