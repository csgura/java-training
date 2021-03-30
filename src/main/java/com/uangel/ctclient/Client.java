package com.uangel.ctclient;

import java.util.concurrent.CompletableFuture;


// client interface
public interface Client {
    void close();

    CompletableFuture<String> sendRequest(String msg);
}
