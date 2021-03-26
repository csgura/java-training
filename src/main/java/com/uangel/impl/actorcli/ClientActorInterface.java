package com.uangel.impl.actorcli;

import akka.actor.Actor;
import akka.actor.ActorRef;
import com.uangel.actor.util.ResponseType;
import com.uangel.ctclient.Client;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

class messageClose {

}

class messageSendRequest implements ResponseType<String> {

    String msg;

    public messageSendRequest(String msg) {
        this.msg = msg;
    }
}

public class ClientActorInterface implements Client {

    private ActorRef ref;

    public ClientActorInterface(ActorRef child) {
        this.ref = child;
    }

    @Override
    public void close() {
        ref.tell(new messageClose(), ActorRef.noSender());
    }

    @Override
    public CompletableFuture<String> sendRequest(String msg) {
        return ResponseType.askFor(ref, new messageSendRequest(msg), Duration.ofSeconds(5));
    }
}