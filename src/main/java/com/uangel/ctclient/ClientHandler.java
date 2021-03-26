package com.uangel.ctclient;

import com.uangel.ctmessage.CtxMessage;
import io.netty.channel.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ClientHandler extends ChannelDuplexHandler {

    Map<Long, Request> transactions = new HashMap<>();

    long nextID = 0;
    private ClientConnection clientConnection;

    public ClientHandler(ClientConnection clientConnection) {
        this.clientConnection = clientConnection;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        clientConnection.connected(ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        clientConnection.disconnected(ctx.channel());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof CtxMessage) {
            var response = (CtxMessage) msg;
            var request = transactions.remove(response.getTrid());
            if (request != null ) {
                request.promise.complete(response.getMsg());
            }
        }
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (msg instanceof Request) {
            var request = (Request)msg;
            var trid = nextID++;

             var wf = ctx.write(new CtxMessage( trid, request.msg), promise);
             transactions.put(trid, request);

             wf.addListener((ChannelFuture future) -> {
                 System.out.println("write future is in event loop? = " + ctx.executor().inEventLoop());

                if (!future.isSuccess()) {
                    transactions.remove(trid);
                    request.promise.completeExceptionally(future.cause());
                }
             });

             ctx.executor().schedule(() -> {
                 transactions.remove(trid);
                 request.promise.completeExceptionally(new TimeoutException("time out"));
             }, 5 , TimeUnit.SECONDS);
        }
    }


}
