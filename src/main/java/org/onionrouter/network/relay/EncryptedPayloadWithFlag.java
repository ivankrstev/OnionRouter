package org.onionrouter.network.relay;

public class EncryptedPayloadWithFlag {
    private String encryptedPayloadString;
    private String flag;
    private String destination;

    public EncryptedPayloadWithFlag() {
    }

    public EncryptedPayloadWithFlag(String encryptedPayloadString, String flag, String destination) {
        this.encryptedPayloadString = encryptedPayloadString;
        this.flag = flag;
        this.destination = destination;
    }

    public String getEncryptedPayload() {
        return encryptedPayloadString;
    }

    public void setEncryptedPayload(String encryptedPayloadString) {
        this.encryptedPayloadString = encryptedPayloadString;
    }

    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }
}
