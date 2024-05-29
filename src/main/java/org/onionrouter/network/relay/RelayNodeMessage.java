package org.onionrouter.network.relay;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.onionrouter.network.HttpPayload;
import org.onionrouter.security.AES;
import org.onionrouter.security.RSA;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.security.PrivateKey;

public class RelayNodeMessage {
    // Message that the relay servers will receive as JSON object
    // The encryptedMessage will be further serialized as DecryptedPayload(destination, message)
    private String encryptedMessage;
    private String encryptedAESKey;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public RelayNodeMessage() {
    }

    public RelayNodeMessage(String encryptedMessage, String encryptedAESKey) {
        this.encryptedMessage = encryptedMessage;
        this.encryptedAESKey = encryptedAESKey;
    }

    public String getEncryptedMessage() {
        return encryptedMessage;
    }

    public void setEncryptedMessage(String encryptedMessage) {
        this.encryptedMessage = encryptedMessage;
    }

    public String getEncryptedAESKey() {
        return encryptedAESKey;
    }

    public void setEncryptedAESKey(String encryptedAESKey) {
        this.encryptedAESKey = encryptedAESKey;
    }

    public HttpPayload decryptAndDeserializeEncryptedMessage(final PrivateKey privateKey) {
        try {
            SecretKey decryptedAESKey = RSA.decrypt(encryptedAESKey, privateKey);
            String decryptedMessage = AES.decrypt(encryptedMessage, decryptedAESKey);
            return objectMapper.readValue(decryptedMessage, HttpPayload.class);
        } catch (IOException e) {
            throw new RuntimeException("Error while deserializing the decrypted message");
        }
    }
}
