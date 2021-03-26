package com.uangel.ctclient;

import java.util.concurrent.CompletableFuture;

public interface Client {
    void close();

    CompletableFuture<String> sendRequest(String msg);
}
