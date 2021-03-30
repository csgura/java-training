package com.uangel.impl.pojocli;

import com.uangel.ctclient.Client;
import com.uangel.ctclient.ClientConnection;
import com.uangel.ctclient.ClientFactory;

import java.util.concurrent.CompletableFuture;


// ClientFactory 의 구현
public class FactoryImpl implements ClientFactory, AutoCloseable {

    @Override
    public CompletableFuture<Client> New(String addr, int port, int numConnection) {
        var client = new ClientImpl(addr , port);

        for(int i=0;i<numConnection;i++) {
            client.connections.add(ClientConnection.newConnection( client.workerGroup,  client , addr,port));
        }

        return CompletableFuture.completedFuture(client);
    }

    public void close() {
        System.out.println("factory close");
    }
}
