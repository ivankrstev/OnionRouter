package org.onionrouter.network;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class MessageReceiver {
    // Final Destination
    public static void main(String[] args) throws Exception {
        Server server = new Server();
        ServerConnector serverConnector = new ServerConnector(server);
        serverConnector.setHost("0.0.0.0"); // Listen on all network interfaces
        serverConnector.setPort(8080); // Listen on port 8080
        server.addConnector(serverConnector); // Add the connector to the server
        ServletContextHandler handler = new ServletContextHandler(); // Create a new ServletContextHandler
        handler.setContextPath("/");
        handler.addServlet(new ServletHolder(new MessageReceiverServlet()), "/"); // Add the MessageReceiverServlet to the handler
        server.setHandler(handler);
        server.start();
        server.join();
    }

    private static class MessageReceiverServlet extends HttpServlet {
        @Override
        protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
            System.out.println("MessageReceiver: POST Request received!");
            System.out.println("Request headers:");
            printHeaders(req);
            // Read and log the request body
            String requestBody = getRequestBody(req);
            System.out.println("Request body: " + requestBody);
            // Sending response back to the client
            resp.setContentType("text/plain");
            resp.getWriter().println("Created something new xD!");
        }

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
            System.out.println("MessageReceiver: GET Request received!");
            System.out.println("Request headers:");
            printHeaders(req);
            // Sending response back to the client
            resp.setContentType("text/plain");
            resp.getWriter().println("Got something new xD!");
        }

        @Override
        protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
            System.out.println("MessageReceiver: PUT Request received!");
            System.out.println("Request headers:");
            printHeaders(req);
            // Read and log the request body
            String requestBody = getRequestBody(req);
            System.out.println("Request body: " + requestBody);
            // Sending response back to the client
            resp.setContentType("text/plain");
            resp.getWriter().println("Updated something new xD!");
        }

        @Override
        protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
            System.out.println("MessageReceiver: DELETE Request received!");
            System.out.println("Request headers:");
            printHeaders(req);
            // Sending response back to the client
            resp.setContentType("text/plain");
            resp.getWriter().println("Deleted something new xD!");
        }

        private String getRequestBody(HttpServletRequest req) throws IOException {
            ServletInputStream inputStream = req.getInputStream(); // Get the input stream from the request
            StringBuilder requestBody = new StringBuilder(); // Create a StringBuilder to store the read data
            byte[] buffer = new byte[1024]; // Read 1024 bytes at a time
            int bytesRead; // Number of bytes read
            // Read data from the stream
            while ((bytesRead = inputStream.read(buffer)) != -1) // Read until the end of the stream
                requestBody.append(new String(buffer, 0, bytesRead, StandardCharsets.UTF_8)); // Append the read data to the StringBuilder
            inputStream.close(); // Close the input stream
            return requestBody.toString(); // Return the read data
        }

        private void printHeaders(HttpServletRequest req) {
            // Print all headers
            req.getHeaderNames().asIterator().forEachRemaining(headerName -> System.out.println(headerName + ": " + req.getHeader(headerName)));
        }
    }
}