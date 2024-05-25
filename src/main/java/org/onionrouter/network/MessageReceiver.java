package org.onionrouter.network;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class MessageReceiver {
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
            // Sending response back to the client
            resp.setContentType("text/plain");
            resp.getWriter().println("Got something new xD!");
        }

        @Override
        protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
            System.out.println("MessageReceiver: PUT Request received!");
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
            // Sending response back to the client
            resp.setContentType("text/plain");
            resp.getWriter().println("Deleted something new xD!");
        }

        private String getRequestBody(HttpServletRequest req) throws IOException {
            StringBuilder buffer = new StringBuilder();
            req.getReader().lines().forEach(buffer::append);
            String requestBody = buffer.toString();
            byte[] decodedBytes = Base64.getDecoder().decode(requestBody);
            return new String(decodedBytes, StandardCharsets.UTF_8);
        }
    }
}