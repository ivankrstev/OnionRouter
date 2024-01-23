package org.onionrouter.network;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.URL;
import java.net.HttpURLConnection;

public class HttpMessageDispatcher {
    public String sendMessage(String destination, String message) {
        try {
            HttpURLConnection server = createConnection(destination);
            sendRequest(server, message);
            return getResponse(server);
        } catch (IOException e) {
            // Log the exception details
            return "Error: " + e.getMessage();
        }
    }

    private HttpURLConnection createConnection(String destination) throws IOException {
        URL url = new URL(destination);
        HttpURLConnection server = (HttpURLConnection) url.openConnection();
        server.setConnectTimeout(10000);
        server.setReadTimeout(10000);
        server.setDoOutput(true);
        server.setRequestMethod("POST");
        return server;
    }

    private void sendRequest(HttpURLConnection server, String message) throws IOException {
        try (OutputStream os = server.getOutputStream();
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os))) {
            writer.write(message);
            writer.newLine();
        }
    }

    private String getResponse(HttpURLConnection server) throws IOException {
        int responseCode = server.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(server.getInputStream()))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                return response.toString();
            }
        } else {
            return "Sending message failed with HTTP code: " + responseCode;
        }
    }
}
