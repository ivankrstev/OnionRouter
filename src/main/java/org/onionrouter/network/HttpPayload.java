package org.onionrouter.network;

import java.util.Map;
import java.util.HashMap;

public class HttpPayload {
    private String method;
    private byte[] body; // Store the body as a byte array to handle any type of data, textual or binary
    private Map<String, String> headers; // Store the headers as a map

    public HttpPayload() {
    }

    public HttpPayload(String method, String bodyString, Map<String, String> headers) {
        this.method = method;
        this.body = bodyString.getBytes();
        this.headers = headers;
    }

    public HttpPayload(String method, String body) {
        this.method = method;
        this.body = body.getBytes();
        this.headers = new HashMap<>();
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(String bodyString) {
        this.body = bodyString.getBytes();
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public void addHeader(String key, String value) {
        this.headers.put(key, value);
    }
}
