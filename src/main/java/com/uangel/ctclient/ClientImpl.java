package com.uangel.ctclient;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class ClientImpl implements Client, ClientStatusListener {
    EventLoopGroup workerGroup = new NioEventLoopGroup();

    volatile List<CompletableFuture<ClientConnection>> connections = new ArrayList<>();

    private String addr;
    private int port;

    public ClientImpl(String addr, int port) {
        this.addr = addr;
        this.port = port;

        checkConns();

    }


    static public CompletableFuture<ClientImpl> New(String addr , int port , int numConnection) {

        var client = new ClientImpl(addr , port);

        for(int i=0;i<numConnection;i++) {
            client.connections.add(ClientConnection.newConnection( client.workerGroup,  client , addr,port));
        }

        return CompletableFuture.completedFuture(client);
    }




    @Override
    public void close() {


        connections.stream().forEach(f -> f.thenAccept(c -> c.close()));


        workerGroup.shutdownGracefully();

    }


    public void checkConns() {

        connections = connections.stream().map(f -> {
            if ( f.isDone() && (f.isCompletedExceptionally() || f.isCancelled()) ){
                System.out.println("try reconnect");
                return ClientConnection.newConnection(this.workerGroup, this, this.addr, this.port);
            }
            return f;
        }).collect(Collectors.toList());

        CompletableFuture.delayedExecutor(5 , TimeUnit.SECONDS).execute(() -> {
            if (!workerGroup.isShutdown()) {
                checkConns();
            }
        });

    }

    @Override
    public void connected(ClientConnection conn) {

    }

    @Override
    public void disconnected(ClientConnection conn) {
        connections = connections.stream().map(f -> {
            if ( f.isDone() && !f.isCompletedExceptionally() && !f.isCancelled()) {
                try {
                    var c = f.get();
                    if ( c == conn) {
                        var newf = new CompletableFuture<ClientConnection>();
                        CompletableFuture.delayedExecutor(1 , TimeUnit.SECONDS).execute(() -> {
                            var cf = ClientConnection.newConnection(this.workerGroup, this, this.addr, this.port);
                            cf.whenComplete((clientConnection, throwable) -> {
                                if(throwable != null) {
                                    newf.completeExceptionally(throwable);
                                } else {
                                    newf.complete(clientConnection);
                                }
                            });
                        });
                        return newf;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
            return f;
        }).collect(Collectors.toList());


//        lock.lock();
//        try {
//            connections.remove(channel);
//        } finally {
//            lock.unlock();
//        }
//        rmConnection();
    }

    private CompletableFuture<String> sendRequestTo( int idx, String msg ) {
       var ret =  connections.get(idx).thenCompose(c -> c.sendRequest(msg));
       return ret.handle((s, throwable) -> {
            if (throwable != null ) {
                if (connections.size() > idx +1 ) {
                    return sendRequestTo(idx+1, msg);
                } else {
                    return CompletableFuture.<String>failedFuture(throwable);
                }
            }
            return CompletableFuture.completedFuture(s);
       }).thenCompose(x -> x);
    }


    @Override
    public CompletableFuture<String> sendRequest(String msg) {

        if(connections.size() > 0) {
            return sendRequestTo(0, msg);
        }
        return CompletableFuture.failedFuture(new NoSuchElementException("no connection"));
    }
}
