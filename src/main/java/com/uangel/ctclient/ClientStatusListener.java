package com.uangel.ctclient;

public interface ClientStatusListener {
    void connected(ClientConnection conn);

    void disconnected(ClientConnection conn);
}
