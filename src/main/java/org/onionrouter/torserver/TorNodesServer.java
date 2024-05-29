package org.onionrouter.torserver;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class TorNodesServer extends AbstractHandler {
    // We use connectedNodes to store the nodes of the tor network, so we can access their public keys, addresses(ports) and status
    private final CopyOnWriteArrayList<TorNodeInfo> connectedNodes = new CopyOnWriteArrayList<>();
    // Object mapper for writing/reading json values
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handle(String s, Request request, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException {
        httpServletResponse.setContentType("application/json");
        if ("POST".equalsIgnoreCase(request.getMethod()) && "/add".equals(s))
            handleAddTorNode(httpServletRequest, httpServletResponse);
        else if ("GET".equalsIgnoreCase(request.getMethod()) && "/get-nodes".equals(s))
            handleGetTorNodes(httpServletResponse);
        else {
            httpServletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            httpServletResponse.getWriter().println("{\"error\":\"Unsupported operation\"}");
        }
        request.setHandled(true);
    }

    private void handleGetTorNodes(HttpServletResponse response) throws IOException {
        if (connectedNodes.size() < 2) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println("{\"error\":\"Insufficient nodes available\"}");
            return;
        }
        List<TorNodeInfo> shuffledNodes = shuffleNodes();
        if (shuffledNodes.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println("{\"error\":\"Insufficient nodes available\"}");
            return;
        }
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().println(objectMapper.writeValueAsString(shuffledNodes));
    }

    private void handleAddTorNode(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            TorNodeInfo node = objectMapper.readValue(request.getInputStream(), TorNodeInfo.class);
            if (node == null || node.getPublicKey() == null || node.getPublicKey().isEmpty() || node.getAddress() == null || node.getStatus() == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().println("{\"error\":\"Error processing request\"}");
                return;
            }
            connectedNodes.add(node);
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().println(objectMapper.writeValueAsString(node));
            System.out.println("Node joined the network: " + node.getAddress() + " with status: " + node.getStatus());
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println("{\"error\":\"Error processing request\"}");
        }
    }

    private List<TorNodeInfo> shuffleNodes() {
        // Shuffle the nodes to prevent the same order of nodes every time
        // And to make sure the last node is an EXIT node
        List<TorNodeInfo> entryNodes = new ArrayList<>();
        List<TorNodeInfo> middleNodes = new ArrayList<>();
        List<TorNodeInfo> exitNodes = new ArrayList<>();
        Collections.shuffle(connectedNodes);
        for (TorNodeInfo node : connectedNodes)
            switch (node.getStatus()) {
                case ENTRY:
                    entryNodes.add(node);
                    break;
                case MIDDLE:
                    middleNodes.add(node);
                    break;
                case EXIT:
                    exitNodes.add(node);
                    break;
            }
        // If there are no entry or exit nodes, return an empty list (we need at least 1 entry and 1 exit node)
        if (entryNodes.isEmpty() || exitNodes.isEmpty())
            return new ArrayList<>();
        Collections.shuffle(entryNodes);
        TorNodeInfo entryNode = entryNodes.get(0);
        TorNodeInfo lastExitNode = exitNodes.remove(exitNodes.size() - 1);
        middleNodes.addAll(exitNodes);
        Collections.shuffle(middleNodes);
        if (middleNodes.size() > 6)
            middleNodes = middleNodes.subList(0, 6); // We only need 6 middle nodes(for simplicity we don't use all the middle nodes available)
        // Add the nodes in a new list and return it
        List<TorNodeInfo> shuffledNodes = new ArrayList<>();
        shuffledNodes.add(entryNode);
        shuffledNodes.addAll(middleNodes);
        shuffledNodes.add(lastExitNode);
        return shuffledNodes;
    }

    public static void main(String[] args) throws Exception {
        Server server = new Server(5500);
        server.setHandler(new TorNodesServer());
        server.start();
        server.join();
    }
}
