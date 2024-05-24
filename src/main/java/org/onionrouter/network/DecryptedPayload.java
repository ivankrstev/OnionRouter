package org.onionrouter.network;

public class DecryptedPayload {
    private String message;
    private String destination;

    public DecryptedPayload() {
    }

    public DecryptedPayload(String message, String destination) {
        this.message = message;
        this.destination = destination;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }
}
