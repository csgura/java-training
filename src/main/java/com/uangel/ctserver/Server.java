package com.uangel.ctserver;

import com.uangel.ctmessage.CtxMessage;
import io.netty.channel.Channel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class Server implements AutoCloseable {
    Channel serverChannel;


    public void Bind(Channel serverChannel) {
        this.serverChannel = serverChannel;


    }

    public void close() {
        serverChannel.close();

        lock.lock();
        try {
            connections.stream().forEach(channel -> channel.close());
            connections = new ArrayList<>();
        } finally {
            lock.unlock();
        }
    }

    ReentrantLock lock = new ReentrantLock();

    List<Channel> connections = new ArrayList<>();

    public void channelConnected(Channel channel) {
        lock.lock();
        try {
            connections.add(channel);
        } finally {
            lock.unlock();
        }
    }

    public void channelDisconnected(Channel channel) {
        lock.lock();
        try {
            connections.remove(channel);
        } finally {
            lock.unlock();
        }
    }

    public void sendResponse(Request request, String msg) {
        request.channel.writeAndFlush(new CtxMessage(request.message.getTrid(), msg));
    }
}
