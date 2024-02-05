package org.onionrouter.torserver;

import org.onionrouter.nodes.RouterStatus;

public class TorRouterInfoObject {
    private String publicKey;
    private int port;
    private RouterStatus routerStatus;

    public TorRouterInfoObject() {
    }

    // For setting the middle/exit nodes
    public TorRouterInfoObject(String publicKey) {
        this.publicKey = publicKey;
    }

    // For setting the entry node only
    public TorRouterInfoObject(String publicKey, int port, RouterStatus routerStatus) {
        this.publicKey = publicKey;
        this.port = port;
        this.routerStatus = routerStatus;
    }

    public TorRouterInfoObject(int port, RouterStatus routerStatus) {
        // We use this, so we can read the response from the server that stores the tor nodes info,
        // which contains from the generated port and router status
        this.port = port;
        this.routerStatus = routerStatus;
    }

    public String getPublicKey() {
        return this.publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public int getPort() {
        return this.port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public RouterStatus getStatus() {
        return this.routerStatus;
    }

    public void setStatus(RouterStatus routerStatus) {
        this.routerStatus = routerStatus;
    }
}
