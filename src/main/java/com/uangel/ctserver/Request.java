package com.uangel.ctserver;

import com.uangel.ctmessage.CtxMessage;
import io.netty.channel.Channel;

public class Request {
    Server server;
    Channel channel;
    CtxMessage message;

    public Request(Server server, Channel channel, CtxMessage message) {
        this.server = server;
        this.channel = channel;
        this.message = message;
    }

    public Server getServer() {
        return server;
    }

    public Channel getChannel() {
        return channel;
    }

    public CtxMessage getMessage() {
        return message;
    }
}
