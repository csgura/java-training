package com.uangel.actor.util;

import akka.actor.ActorRef;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

import static akka.pattern.Patterns.ask;


public interface ResponseType<T> {
    default void sendResponse(ActorRef pid , T response, ActorRef sender) {
        pid.tell(CompletableFuture.completedFuture(response), sender);
    }

    default void sendResponse(ActorRef pid , Throwable err, ActorRef sender) {
        pid.tell(CompletableFuture.failedFuture(err), sender);
    }

    default void sendFutureResponse(ActorRef pid , CompletableFuture<T> future, ActorRef sender) {
        pid.tell(future, sender);
    }


    static <T>CompletableFuture<T> askFor(ActorRef pid , ResponseType<T> request, Duration timeout) {
        return ask(pid , request, timeout).toCompletableFuture().thenCompose(x -> {
            return (CompletableFuture<T>)x;
        });
    }
}
