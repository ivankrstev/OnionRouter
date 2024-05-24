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

public class TorNodesServer extends AbstractHandler {
    // We use connectedNodes to store the nodes of the tor network, so we can access their public keys, addresses(ports) and status
    private static final List<TorRouterInfoObject> connectedNodes = Collections.synchronizedList(new ArrayList<>());
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
        List<TorRouterInfoObject> shuffledNodes = shuffleNodes();
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().println(objectMapper.writeValueAsString(shuffledNodes));
    }

    private void handleAddTorNode(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            TorRouterInfoObject node = objectMapper.readValue(request.getInputStream(), TorRouterInfoObject.class);
            node.setStatus(generateRouterStatus());
            node.setPort(generateNonExistingPort());
            if (node.getStatus() == MIDDLE)
                connectedNodes.add(connectedNodes.size() - 1, node); // Make sure no middle node is added as the last node
            else connectedNodes.add(node);
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().println(objectMapper.writeValueAsString(node));
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println("{\"error\":\"Error processing request\"}");
        }
    }

    private List<TorRouterInfoObject> shuffleNodes() {
        // Shuffle the nodes to prevent the same order of nodes every time
        // And to make sure the last node is an EXIT node
        TorRouterInfoObject entryNode = null;
        List<TorRouterInfoObject> middleAndExitNodes = new ArrayList<>();
        TorRouterInfoObject lastExitNode = null;
        Collections.shuffle(connectedNodes);
        synchronized (connectedNodes) {
            for (TorRouterInfoObject node : connectedNodes)
                switch (node.getStatus()) {
                    case ENTRY:
                        if (entryNode == null) entryNode = node;
                        break;
                    case MIDDLE:
                        middleAndExitNodes.add(node);
                        break;
                    case EXIT:
                        middleAndExitNodes.add(node);
                        if (lastExitNode == null) lastExitNode = node;
                        break;
                }
            Collections.shuffle(middleAndExitNodes);
            // Ensure the last element is an EXIT node
            middleAndExitNodes.remove(lastExitNode);
            middleAndExitNodes.add(lastExitNode);
            // Add the nodes in a new list and return it
            List<TorRouterInfoObject> shuffledNodes = new ArrayList<>();
            shuffledNodes.add(entryNode);
            shuffledNodes.addAll(middleAndExitNodes);
            return shuffledNodes;
        }
    }

    public static void main(String[] args) throws Exception {
        Server server = new Server(5500);
        server.setHandler(new TorNodesServer());
        server.start();
        server.join();
    }
}
