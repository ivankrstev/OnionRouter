package org.onionrouter.security;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

public class AES {
    private static final String AES_ALGORITHM = "AES/GCM/NoPadding";

    public static String encrypt(final String messageToEncode, final SecretKey secret) {
        try {
            byte[] iv = new byte[12];
            new SecureRandom().nextBytes(iv);
            Cipher cipherAES = Cipher.getInstance(AES_ALGORITHM);
            cipherAES.init(Cipher.ENCRYPT_MODE, secret, new GCMParameterSpec(128, iv));
            byte[] cipherText = cipherAES.doFinal(messageToEncode.getBytes(StandardCharsets.UTF_8));
            byte[] encrypted = new byte[iv.length + cipherText.length];
            System.arraycopy(iv, 0, encrypted, 0, iv.length);
            System.arraycopy(cipherText, 0, encrypted, iv.length, cipherText.length);
            return Base64.getMimeEncoder().encodeToString(encrypted);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static String decrypt(String encodedMessage, final SecretKey secret) {
        try {
            byte[] encrypted = Base64.getMimeDecoder().decode(encodedMessage.getBytes(StandardCharsets.UTF_8));
            byte[] iv = new byte[12];
            byte[] cipherText = new byte[encrypted.length - 12];
            System.arraycopy(encrypted, 0, iv, 0, iv.length);
            System.arraycopy(encrypted, iv.length, cipherText, 0, cipherText.length);
            Cipher cipherAES = Cipher.getInstance(AES_ALGORITHM);
            cipherAES.init(Cipher.DECRYPT_MODE, secret, new GCMParameterSpec(128, iv));
            byte[] decrypted = cipherAES.doFinal(cipherText);
            return new String(decrypted);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
