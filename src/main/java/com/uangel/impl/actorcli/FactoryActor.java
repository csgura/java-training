package com.uangel.impl.actorcli;

import akka.actor.AbstractActor;
import akka.actor.Props;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import scala.concurrent.ExecutionContextExecutor;

import java.util.concurrent.Executor;

class FactoryActor extends AbstractActor  {

    EventLoopGroup workerGroup = new NioEventLoopGroup();

    @Override
    public void postStop()  {
        workerGroup.shutdownGracefully();
    }

    public static Props props() {
        return Props.create(FactoryActor.class, () -> new FactoryActor());
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(messageNewClient.class, this::onNewClient)
                .build();
    }

    private void onNewClient(messageNewClient req) {
//        var childName = String.format("%s-%d", req.addr, req.port);
//
//        var childOpt = this.getContext().child(childName);
//        var child = childOpt.getOrElse(() -> {
//           return this.context().actorOf(childActor.props(req, workerGroup), childName);
//        });

        var child = this.getContext().actorOf( childActor.props(req, workerGroup));

        req.sendResponse(sender(), new ClientActorInterface(child), self());

    }


}
