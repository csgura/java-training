package com.uangel.ctserver;

import com.uangel.ctmessage.CtDecoder;
import com.uangel.ctmessage.CtEncoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class ServerInit implements AutoCloseable {

    EventLoopGroup bossGroup = new NioEventLoopGroup(1);
    EventLoopGroup workerGroup = new NioEventLoopGroup();


    public CompletableFuture<Server> newServer(int port, Consumer<Request> consumer) {

        var server = new Server();
        CompletableFuture<Server> ret = new CompletableFuture<>();
        ServerBootstrap b = new ServerBootstrap();
        b.group(bossGroup, workerGroup);
        b.option(ChannelOption.SO_BACKLOG, 1024);
        b.channel(NioServerSocketChannel.class);

        b.childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel socketChannel) throws Exception {

                socketChannel.pipeline().addLast(new CtDecoder() );
                socketChannel.pipeline().addLast(new CtEncoder());
                socketChannel.pipeline().addLast(new ServerHandler(server, consumer));

            }
        });

        var cf = b.bind(port);
        cf.addListener(( ChannelFuture future) -> {

            if (future.isSuccess()) {
                System.out.println("bind success");
                server.Bind(future.channel());
                ret.complete(server);
            } else {
                System.out.println("bind failed");
                future.cause().printStackTrace();
                ret.completeExceptionally(future.cause());
            }
        });


        return ret;
    }

    public void close() {
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }

    public static void main(String[] args) throws InterruptedException {
        var si = new ServerInit();
        var server = si.newServer(8080, request -> {
            System.out.println("on request");

            request.server.sendResponse(request, request.message +  " world");
        });

        Thread.sleep(30000);

        System.out.println("close server");
        server.thenAccept(s -> s.close());
        Thread.sleep(5000);

        si.close();
    }
}
