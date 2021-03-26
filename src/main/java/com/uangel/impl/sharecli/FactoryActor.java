package com.uangel.impl.sharecli;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;

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
                .match(messageSendRequest.class, this::onSendRequest)
                .build();
    }

    void onSendRequest( messageSendRequest r ) {
        getChild(r.spec).forward(r, context());
    }

    ActorRef getChild(messageNewClient spec) {
        var childName = String.format("%s-%d", spec.addr, spec.port);

        var childOpt = this.getContext().findChild(childName);

        return childOpt.orElseGet(() -> {
            return this.context().actorOf(childActor.props(spec, workerGroup), childName);
        });
    }

    private void onNewClient(messageNewClient req) {

        getChild(req);

        req.sendResponse(sender(), new ClientActorInterface(self(), req), self());

    }


}
