package com.uangel.ctclient;

import com.uangel.ctmessage.CtDecoder;
import com.uangel.ctmessage.CtEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.concurrent.CompletableFuture;

public class ClientConnection {

    private Channel channel;
    private ClientStatusListener client;

    public ClientConnection(ClientStatusListener client) {
        this.client = client;
    }


    public CompletableFuture<String> sendRequest( String msg ) {
        CompletableFuture<String> promise = new CompletableFuture<>();
        channel.writeAndFlush(new Request( msg , promise ));

        return promise;
    }

    public static CompletableFuture<ClientConnection> newConnection(EventLoopGroup workerGroup,  ClientStatusListener client, String addr , int port) {
        ClientConnection connection = new ClientConnection(client);
        CompletableFuture<ClientConnection> ret = new CompletableFuture<>();

        Bootstrap b = new Bootstrap();
        b.group(workerGroup);
        b.channel(NioSocketChannel.class);
        b.handler(new ChannelInitializer<SocketChannel>() {
            protected void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline().addLast(new CtDecoder() );
                ch.pipeline().addLast(new CtEncoder());
                ch.pipeline().addLast(new ClientHandler(connection));
            }
        });

        var cf = b.connect(addr, port);
        cf.addListener((ChannelFuture future) -> {
            if (future.isSuccess()) {
                connection.channel = future.channel();
                ret.complete(connection);
            } else {
                System.out.println("connect failed");
                ret.completeExceptionally(future.cause());
            }
        });
        return ret;
    }

    public void connected(Channel channel) {
        client.connected(this);
    }

    public void disconnected(Channel channel) {
        client.disconnected(this);
    }

    public void close() {
        this.channel.close();
    }
}
