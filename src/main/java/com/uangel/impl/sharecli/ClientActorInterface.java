package com.uangel.impl.sharecli;

import akka.actor.ActorRef;
import com.uangel.actor.util.ResponseType;
import com.uangel.ctclient.Client;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

class messageClose {

}

class messageSendRequest implements ResponseType<String> {

    String msg;
    messageNewClient spec;

    public messageSendRequest(String msg, messageNewClient spec) {
        this.msg = msg;
        this.spec = spec;
    }
}

public class ClientActorInterface implements Client {

    private ActorRef ref;
    private messageNewClient spec;

    public ClientActorInterface(ActorRef factoryRef , messageNewClient spec) {

        this.ref = factoryRef;
        this.spec = spec;
    }

    @Override
    public void close() {
        //ref.tell(new messageClose(), ActorRef.noSender());
    }

    @Override
    public CompletableFuture<String> sendRequest(String msg) {
        return ResponseType.askFor(ref, new messageSendRequest(msg, spec), Duration.ofSeconds(5));
    }
}
