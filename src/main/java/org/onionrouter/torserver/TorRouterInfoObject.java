package org.onionrouter.torserver;

import org.onionrouter.nodes.RouterStatus;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class TorRouterInfoObject {
    private String publicKey;
    private String address;
    private RouterStatus routerStatus;

    public TorRouterInfoObject() {
    }

    public TorRouterInfoObject(String publicKey, String address, RouterStatus routerStatus) {
        this.publicKey = publicKey;
        this.address = address;
        this.routerStatus = routerStatus;
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

    public RouterStatus getStatus() {
        return this.routerStatus;
    }

    public void setStatus(RouterStatus routerStatus) {
        this.routerStatus = routerStatus;
    }
}
