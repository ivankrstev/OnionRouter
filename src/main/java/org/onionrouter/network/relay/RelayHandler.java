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
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.util.Base64;

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
            HttpPayload decryptedPayload = relayNodeMessage.decryptAndDeserializeEncryptedMessage(privateKey); // Decrypt the message

            byte[] decodedBytes = Base64.getDecoder().decode(decryptedPayload.getBody()); // Decode the body
            String bla = new String(decodedBytes, StandardCharsets.UTF_8); // Convert the body to a string
            decryptedPayload.setBody(bla); // Set the body of the payload

            byte[] forwardResponse = forwardRequestToFinalDestination(decryptedPayload); // Forward the message to the final destination
            System.out.println("Received response from the final destination: " + new String(forwardResponse));
            response.setStatus(HttpServletResponse.SC_OK); // Set the status of the response
            response.setContentType("application/json"); // Set the content type of the response
            response.getOutputStream().write(forwardResponse); // Write the response body
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println("Internal Server Error");
        }
    }

    private byte[] forwardRequestToFinalDestination(HttpPayload httpPayload) throws IOException, InterruptedException {
        // Forward the message to the final destination and get the response
        HttpClient client = HttpClient.newHttpClient(); // Create a new client
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(httpPayload.getDestination())); // Create a new request for the final destination
        // Choose the method dynamically
        switch (httpPayload.getMethod().toUpperCase()) {
            case "GET":
                builder.GET();
                break;
            case "POST":
                builder.POST(HttpRequest.BodyPublishers.ofByteArray(httpPayload.getBody()));
                break;
            case "PUT":
                builder.PUT(HttpRequest.BodyPublishers.ofByteArray(httpPayload.getBody()));
                break;
            case "DELETE":
                builder.DELETE();
                break;
            default:
                throw new UnsupportedOperationException("Method not supported");
        }
        httpPayload.getHeaders().forEach(builder::header);  // Set headers from the payload
        HttpRequest request = builder.build(); // Build the request
        HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray()); // Send the request and get the response
        return response.body(); // Return the response body
    }
}
