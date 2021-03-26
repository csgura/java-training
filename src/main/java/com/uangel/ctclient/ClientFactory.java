package com.uangel.ctclient;

import java.util.concurrent.CompletableFuture;

public interface ClientFactory {
    CompletableFuture<Client> New(String addr , int port , int numConnection);
}
