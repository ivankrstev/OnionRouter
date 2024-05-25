package org.onionrouter.network.relay;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.onionrouter.network.HttpPayload;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.PrivateKey;
import java.util.Objects;

public class RelayHandler extends AbstractHandler {
    private final PrivateKey privateKey;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public RelayHandler(PrivateKey privateKey) {
        this.privateKey = privateKey;
    }

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        baseRequest.setHandled(true); // Mark the request as handled
        try {
            System.out.println("Received request from: " + request.getRemoteAddr() + ":" + request.getRemotePort());

            RelayNodeMessage relayNodeMessage = objectMapper.readValue(request.getInputStream(), RelayNodeMessage.class); // Deserialize the request body to RelayNodeMessage
            EncryptedPayloadWithFlag decryptedPayloadWithFlag = relayNodeMessage.decryptAndDeserializeEncryptedMessage(privateKey); // Decrypt the message
            if (Objects.equals(decryptedPayloadWithFlag.getFlag(), "DESTINATION")) {
                // Forward to final destination
                System.out.println("Payload: " + decryptedPayloadWithFlag.getEncryptedPayload());
                HttpPayload httpPayload = objectMapper.readValue(decryptedPayloadWithFlag.getEncryptedPayload(), HttpPayload.class); // Deserialize the payload
                byte[] forwardResponse = forwardRequestToFinalDestination(decryptedPayloadWithFlag.getDestination(), httpPayload); // Forward the message to the final destination
                System.out.println("Received response from the final destination: " + new String(forwardResponse));
                response.setStatus(HttpServletResponse.SC_OK); // Set the status of the response
                response.setContentType("application/json"); // Set the content type of the response
                response.getOutputStream().write(forwardResponse); // Write the response body
                return;
            }
            // Forward the message to next relay node
            byte[] forwardResponse = forwardRequestToNextRelay(decryptedPayloadWithFlag.getDestination(), decryptedPayloadWithFlag.getEncryptedPayload()); // Forward the message to the next relay node
            System.out.println("Received response from the next relay: " + new String(forwardResponse));
            response.setStatus(HttpServletResponse.SC_OK); // Set the status of the response
            response.setContentType("application/json"); // Set the content type of the response
            response.getOutputStream().write(forwardResponse); // Write the response body
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println("Internal Server Error");
        }
    }

    private byte[] forwardRequestToFinalDestination(String destination, HttpPayload payload) throws IOException, InterruptedException {
        // Forward the message to the final destination and get the response
        HttpClient client = HttpClient.newHttpClient(); // Create a new client
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(destination)); // Create a new request for the final destination
        // Choose the method dynamically
        switch (payload.getMethod().toUpperCase()) {
            case "GET":
                builder.GET();
                break;
            case "POST":
                builder.POST(HttpRequest.BodyPublishers.ofByteArray(payload.getBody()));
                break;
            case "PUT":
                builder.PUT(HttpRequest.BodyPublishers.ofByteArray(payload.getBody()));
                break;
            case "DELETE":
                builder.DELETE();
                break;
            default:
                throw new UnsupportedOperationException("Method not supported");
        }
        payload.getHeaders().forEach(builder::header);  // Set headers from the payload
        HttpRequest request = builder.build(); // Build the request
        HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray()); // Send the request and get the response
        return response.body(); // Return the response body
    }

    private byte[] forwardRequestToNextRelay(String destination, String message) throws IOException, InterruptedException {
        // Forward the message to the next relay node and get the response
        HttpClient client = HttpClient.newHttpClient(); // Create a new client
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(destination)); // Create a new request for the next relay node address
        builder.POST(HttpRequest.BodyPublishers.ofString(message)); // Set the message as the body of the request
        HttpRequest request = builder.build(); // Build the request
        HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray()); // Send the request and get the response
        return response.body(); // Return the response body
    }
}
