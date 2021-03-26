package com.uangel.impl.actorcli;

import akka.actor.*;
import com.uangel.actor.util.ResponseType;
import com.uangel.ctclient.ClientConnection;
import com.uangel.ctclient.ClientStatusListener;
import io.netty.channel.EventLoopGroup;
import scala.PartialFunction;
import scala.runtime.BoxedUnit;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

class childActor extends AbstractActorWithStash implements ClientStatusListener {

    private messageNewClient spec;
    private EventLoopGroup workerGroup;


    private Cancellable cancelTimer = getContext().getSystem().getScheduler().scheduleWithFixedDelay(Duration.ofSeconds(5), Duration.ofSeconds(5), self(), new messageConnCheck(), getContext().dispatcher(),  ActorRef.noSender());

    @Override
    public void postStop()  {
        System.out.println("child actor stopped");
        cancelTimer.cancel();
        connections.forEach(ClientConnection::close);
        connections = new ArrayList<>();
    }

    public childActor(messageNewClient req, EventLoopGroup workerGroup) {
        this.spec = req;
        this.workerGroup = workerGroup;

        List<CompletableFuture<ClientConnection>> onGoing = new ArrayList<>();

        for(int i=0;i< req.numConnection;i++) {
            onGoing.add(ClientConnection.newConnection(workerGroup,this,req.addr, req.port));
        }
        getContext().become(new initialState(onGoing).createReceive());
    }

    public static Props props(messageNewClient req, EventLoopGroup workerGroup) {
        return Props.create(childActor.class, () -> new childActor(req, workerGroup));
    }

    @Override
    public Receive createReceive() {
        return null;
    }

    class initialState {
        List<CompletableFuture<ClientConnection>> onGoing;

        public initialState(List<CompletableFuture<ClientConnection>> onGoing) {
            this.onGoing = onGoing;

            var inverse = onGoing.stream().map(f -> {
                return f.handle((clientConnection, throwable) -> {
                    if(throwable!=null) {
                        return CompletableFuture.completedFuture(true);
                    } else {
                        return CompletableFuture.<Boolean>failedFuture(new Exception());
                    }
                }).thenCompose(x -> x );
            }).collect(Collectors.toList());



            CompletableFuture.allOf(inverse.toArray(new CompletableFuture[0])).thenAccept(x -> {
                self().tell(new messageAllFailed(), ActorRef.noSender());
            });
        }

        void onConnected( messageConnected r) {
            unstashAll();
            connections.add(r.conn);
            getContext().become(new activeState().createReceive() );
            r.sendResponse(sender(), true , self());
        }

        void onSendRequest( messageSendRequest r) {
            stash();
        }

        void onClose( messageClose r ) {
            getContext().stop(self());
        }

        void onAllFailed( messageAllFailed r) {
            unstashAll();
            getContext().become(new activeState().createReceive());
        }

        public Receive createReceive() {
            return receiveBuilder()
                    .match(messageSendRequest.class, this::onSendRequest)
                    .match(messageConnected.class, this::onConnected)
                    .match(messageClose.class, this::onClose)
                    .match(messageAllFailed.class , this::onAllFailed)
                    .matchAny(r -> System.out.println("unexpected message : " + r))
                    .build();
        }
    }



    private CompletableFuture<String> sendRequestTo(List<ClientConnection> connections, int idx, String msg ) {
        if (connections.size() == 0 ) {
            return CompletableFuture.failedFuture(new NoSuchElementException("no connection"));
        }

        var ret =  connections.get(idx).sendRequest(msg);
        return ret.handle((s, throwable) -> {
            if (throwable != null ) {
                if (connections.size() > idx +1 ) {
                    return sendRequestTo(connections,idx+1, msg);
                } else {
                    return CompletableFuture.<String>failedFuture(throwable);
                }
            }
            return CompletableFuture.completedFuture(s);
        }).thenCompose(x -> x);
    }

    List<ClientConnection> connections = new ArrayList<>();

    class activeState {
        void onSendRequest( messageSendRequest r) {
            System.out.println("onSendRequest");
//                    var returnPath = this.sender();
//                    sendRequestTo(connections, 0 , r.msg).whenComplete((s, throwable) -> {
//                        if (throwable != null) {
//                            r.sendResponse(returnPath, throwable, self());
//                        } else {
//                            r.sendResponse(returnPath, s, self());
//                        }
//                    });

            r.sendFutureResponse(sender(), sendRequestTo(connections, 0 , r.msg) , self());
        }

        void onConnected( messageConnected r) {
            System.out.println("on Connected");

            var newlist = new ArrayList<>(connections);
            newlist.add(r.conn);

            connections = newlist;

            r.sendResponse(sender(), true , self());
        }

        void onDisconnected(messageDisconnected r) {

            connections = connections.stream()
                    .filter(c -> c!=r.conn)
                    .collect(Collectors.toList());

            getContext().getSystem().getScheduler().scheduleOnce(Duration.ofSeconds(1), self(), new messageReconnect(), context().dispatcher(), ActorRef.noSender());
        }

        void onReconnect(messageReconnect r) {
            ClientConnection.newConnection(workerGroup, childActor.this, spec.addr,spec.port);
        }

        void onClose(messageClose r) {
            getContext().stop(self());
        }

        void onConnCheck(messageConnCheck r) {
            if (connections.size() < spec.numConnection) {
                for(int i=0;i< spec.numConnection - connections.size();i++) {
                    self().tell(new messageReconnect(), ActorRef.noSender());
                }
            }
        }

        public Receive createReceive() {
            return receiveBuilder()
                    .match(messageSendRequest.class, this::onSendRequest)
                    .match(messageConnected.class, this::onConnected)
                    .match(messageDisconnected.class, this::onDisconnected)
                    .match(messageReconnect.class, this::onReconnect)
                    .match(messageClose.class, this::onClose)
                    .match(messageConnCheck.class , this::onConnCheck)
                    .matchAny(r -> System.out.println("unexpected message : " + r))
                    .build();
        }


    }



    @Override
    public void connected(ClientConnection conn) {
        ResponseType
                .askFor(self(), new messageConnected(conn), Duration.ofSeconds(3))
                .whenComplete((aBoolean, throwable) -> {
                    if (throwable!=null) {
                        conn.close();
                    }
                });
        //self().tell(new messageConnected(conn), ActorRef.noSender());
    }

    @Override
    public void disconnected(ClientConnection conn) {
        self().tell(new messageDisconnected(conn), ActorRef.noSender() );
    }

    private static class messageConnected implements ResponseType<Boolean> {
        private ClientConnection conn;

        public messageConnected(ClientConnection conn) {
            this.conn = conn;
        }
    }

    private static class messageDisconnected {
        private ClientConnection conn;

        public messageDisconnected(ClientConnection conn) {
            this.conn = conn;
        }
    }

    private static class messageReconnect {
    }

    private static class messageAllFailed {
    }

    private static class messageConnCheck {
    }

}
