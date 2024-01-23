package org.onionrouter.network;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;

public class MessageReceiver {
    public static void main(String[] args) throws Exception {
        Server server = new Server();

        ServerConnector serverConnector = new ServerConnector(server);
        serverConnector.setHost("onion-router-receiver");
        serverConnector.setPort(8081);
        server.addConnector(serverConnector);

        ServletContextHandler handler = new ServletContextHandler();
        handler.setContextPath("/");
        handler.addServlet(new ServletHolder(new MessageReceiverServlet()), "/");
        server.setHandler(handler);

        server.start();
        server.join();
    }

    private static class MessageReceiverServlet extends HttpServlet {
        @Override
        protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
            System.out.println("MessageReceiver: Request received!");

            // Read the request body
            StringBuilder requestBody = new StringBuilder();
            String line;
            try (BufferedReader reader = req.getReader()) {
                while ((line = reader.readLine()) != null)
                    requestBody.append(line).append('\n');
            }

            // Logging the message
            System.out.println("Received a message: " + requestBody);

            // Sending response to the client
            resp.setContentType("text/plain");
            resp.getWriter().println("Message received successfully!");
        }
    }
}
