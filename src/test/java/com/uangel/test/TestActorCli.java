package com.uangel.test;

import com.uangel.ctclient.ClientFactory;
import com.uangel.ctserver.Server;
import com.uangel.ctserver.ServerFactory;
import com.uangel.modules.ActorClientModule;
import com.uangel.modules.ActorModule;
import com.uangel.modules.DefaultModule;
import com.uangel.modules.ServerModule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import javax.swing.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {ActorClientModule.class, ActorModule.class, ServerModule.class})
@DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
public class TestActorCli {

    @Autowired
    ClientFactory cf;

    @Autowired
    @Qualifier("server-8080")
    CompletableFuture<Server> server;

    @Test
    public void test() throws ExecutionException, InterruptedException {


        var client = server.thenCompose(s -> {
            return cf.New("127.0.0.1", 8080, 2);
        });


        var response = client.thenCompose(client1 -> {
            return client1.sendRequest("world");
        });

        try {
            assertEquals("hello world", response.get());
        } finally {
            //client.thenAccept(x -> x.close());
            server.thenAccept(x -> x.close());
        }


    }

    @Autowired
    ServerFactory sf;

    @Test(expected = TimeoutException.class)
    public void testTimeout() throws Throwable {


        var server = sf.newServer(8081, (con,request) -> {
            //request.getServer().sendResponse(request, "hello " + request.getMessage().getMsg());
        });

        var client = server.thenCompose(s -> {
            return cf.New("127.0.0.1", 8081, 2);
        });


        var response = client.thenCompose(client1 -> {
            return client1.sendRequest("world");
        });

        try {
            assertEquals("hello world", response.get());
        } catch (ExecutionException e) {
            throw e.getCause();
        } finally {
            client.thenAccept(x -> x.close());
            server.thenAccept(x -> x.close());
        }

    }
}
