package com.uangel.ctserver;

import com.uangel.ctmessage.CtxMessage;
import io.netty.channel.*;

import java.util.function.Consumer;

public class ServerHandler extends ChannelDuplexHandler {

    Server server;
    private Consumer<Request> consumer;

    public ServerHandler(Server server, Consumer<Request> consumer) {
        this.server = server;
        this.consumer = consumer;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("connected");
        server.channelConnected(ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("disconnected");
        server.channelDisconnected(ctx.channel());

    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof CtxMessage) {
            var request = (CtxMessage)msg;
            System.out.println("message read = " + request.getMsg());
            consumer.accept(new Request(server , ctx.channel(),  request));
        }
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (msg instanceof CtxMessage) {
            var response = (CtxMessage)msg;
            System.out.println("message = " + response.getMsg());
            ctx.write(msg, promise);
        }
        //super.write(ctx, msg, promise);
    }
}
