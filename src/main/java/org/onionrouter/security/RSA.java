package org.onionrouter.security;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class RSA {
    private static final String RSA_ALGORITHM = "RSA/ECB/OAEPWITHSHA-256ANDMGF1PADDING";

    // Encryption of AES Secret Key with Public RSA Key
    public static String encrypt(final SecretKey key, final PublicKey publicKey) {
        try {
            Cipher cipherRSA = Cipher.getInstance(RSA_ALGORITHM);
            cipherRSA.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] encrypted = cipherRSA.doFinal(key.getEncoded());
            return Base64.getMimeEncoder().encodeToString(encrypted);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    // Decryption of AES Secret Key with Private RSA Key
    public static SecretKey decrypt(final String encodedMessage, final PrivateKey privateKey) {
        try {
            Cipher cipherRSA = Cipher.getInstance(RSA_ALGORITHM);
            cipherRSA.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] decrypted = cipherRSA.doFinal(Base64.getMimeDecoder().decode(encodedMessage));
            return new SecretKeySpec(decrypted, 0, decrypted.length, "AES");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Generating of Private RSA Key
    public static KeyPair generateKeyPair() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048, new SecureRandom());
            return keyGen.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

    public static void main(String[] args) {

    }
}
