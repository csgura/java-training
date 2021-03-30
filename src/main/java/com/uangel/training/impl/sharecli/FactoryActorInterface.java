package com.uangel.training.impl.sharecli;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import com.uangel.training.actor.util.ResponseType;
import com.uangel.training.ctclient.Client;
import com.uangel.training.ctclient.ClientFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

class messageNewClient implements ResponseType<Client> {

    String addr;
    int port;
    int numConnection;

    public messageNewClient(String addr, int port, int numConnection) {
        this.addr = addr;
        this.port = port;
        this.numConnection = numConnection;
    }
}

@Lazy
public class FactoryActorInterface implements ClientFactory, AutoCloseable {

    ActorRef mainActorRef;
    private ActorSystem actorSystem;

    @Autowired
    public FactoryActorInterface(ActorSystem actorSystem) {

        System.out.println("new factory actor interface");
        mainActorRef = actorSystem.actorOf(FactoryActor.props(), "client-factory");

        this.actorSystem = actorSystem;
    }

    @Override
    public CompletableFuture<Client> New(String addr, int port, int numConnection) {
        return ResponseType.askFor(mainActorRef, new messageNewClient(addr , port , numConnection), Duration.ofSeconds(5));
    }

    @Override
    public void close() throws Exception {
        actorSystem.stop(mainActorRef);
    }
}
