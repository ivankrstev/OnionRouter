package org.onionrouter.network;

import java.io.IOException;
import java.net.URI;
import java.net.HttpURLConnection;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class HttpMessageDispatcher {
    private final HttpClient httpClient;

    public HttpMessageDispatcher() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(java.time.Duration.ofSeconds(10))
                .build();
    }

    public String sendPostRequest(String destination, String message) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(destination))
                    .timeout(java.time.Duration.ofSeconds(10))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(message))
                    .build();

            HttpResponse<String> response = this.httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return handleResponse(response);
        } catch (IOException | InterruptedException e) {
            System.out.println("Error sending POST request: " + e.getMessage());
            return null;
        }
    }


    public String sendGetRequest(String destination) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(destination))
                    .timeout(java.time.Duration.ofSeconds(10))
                    .GET()
                    .build();

            HttpResponse<String> response = this.httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return handleResponse(response);
        } catch (IOException | InterruptedException e) {
            System.out.println("Error sending GET request: " + e.getMessage());
            return null;
        }
    }

    private String handleResponse(HttpResponse<String> response) {
        if (response.statusCode() == HttpURLConnection.HTTP_OK)
            return response.body();
        else return null;
    }
}
