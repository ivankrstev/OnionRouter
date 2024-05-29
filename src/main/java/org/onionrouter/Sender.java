package org.onionrouter;

import org.onionrouter.network.HttpPayload;
import org.onionrouter.network.TorClient;

import java.util.HashMap;

public class Sender {
    public static void main(String[] args) throws Exception {
        Thread.sleep(10000);
        String torNodesServerAddress = "http://172.28.0.100:5500";
        TorClient torClient = new TorClient(torNodesServerAddress);
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + java.util.Base64.getEncoder().encodeToString("admin:admin".getBytes()));
        headers.put("Content-Type", "text/plain");
        torClient.sendRequest(new HttpPayload("http://172.28.0.200:8080", "POST", "This is a message to be sent❤️❤️❤️ ⬇️⬇️⬇️⬇️", headers));
    }
}