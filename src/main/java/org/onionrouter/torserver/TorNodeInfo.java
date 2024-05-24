package org.onionrouter.torserver;

import org.onionrouter.network.relay.RelayStatus;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class TorNodeInfo {
    private String publicKey;
    private String address;
    private RelayStatus relayStatus;

    public TorNodeInfo() {
    }

    public TorNodeInfo(String publicKey, String address, RelayStatus relayStatus) {
        this.publicKey = publicKey;
        this.address = address;
        this.relayStatus = relayStatus;
    }

    public PublicKey parseAndGetPublicKey() {
        try {
            byte[] decodedKey = Base64.getDecoder().decode(publicKey);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decodedKey);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePublic(keySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            return null;
        }
    }

    public String getPublicKey() {
        return this.publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getAddress() {
        return this.address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public RelayStatus getStatus() {
        return this.relayStatus;
    }

    public void setStatus(RelayStatus relayStatus) {
        this.relayStatus = relayStatus;
    }
}
