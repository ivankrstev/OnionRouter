package org.onionrouter.network;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.onionrouter.network.relay.EncryptedPayloadWithFlag;
import org.onionrouter.network.relay.RelayNodeMessage;
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

    public void sendRequest(String destination, HttpPayload httpPayload) throws Exception {
        TorNodeInfo[] torNodes = getTorNodesFromServer();
        if (torNodes == null || torNodes.length == 0)
            return;
        String message = objectMapper.writeValueAsString(httpPayload);
        for (int i = torNodes.length - 1; i >= 0; i--) {
            SecretKey AESKey = AES.generateKey();
            String flag = i == torNodes.length - 1 ? "DESTINATION" : "RELAY";
            EncryptedPayloadWithFlag encryptedPayloadWithFlag = new EncryptedPayloadWithFlag(message, flag, destination);
            String encryptedPayloadWithFlagJSONString = objectMapper.writeValueAsString(encryptedPayloadWithFlag);
            encryptedPayloadWithFlagJSONString = AES.encrypt(encryptedPayloadWithFlagJSONString, AESKey);
            RelayNodeMessage relayNodeMessage = new RelayNodeMessage(encryptedPayloadWithFlagJSONString, RSA.encrypt(AESKey, torNodes[i].parseAndGetPublicKey()));
            message = objectMapper.writeValueAsString(relayNodeMessage);
            destination = torNodes[i].getAddress();
        }
        System.out.println("First relay: " + destination);
        System.out.println("Sending message: " + message);
        System.out.println("Response from tor client: " + new HttpMessageDispatcher().sendPostRequest(destination, message));
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
