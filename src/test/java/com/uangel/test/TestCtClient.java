package com.uangel.test;

import com.uangel.ctclient.ClientFactory;
import com.uangel.ctserver.Server;
import com.uangel.ctserver.ServerFactory;
import com.uangel.modules.*;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.*;



public class TestCtClient {

    @Autowired
    ClientFactory cf;

    @Test
    public void test() throws ExecutionException, InterruptedException {

        var application = new AnnotationConfigApplicationContext(DefaultModule.class);
        try {
            cf = application.getBean(ClientFactory.class);
            var si = new ServerFactory();
            try {
                var server = si.newServer(8080, (con, request) -> {
                    con.sendResponse(request, "hello " + request.getMsg());
                });

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
            } finally {
                si.close();
            }
        } finally {
            application.close();
        }
    }

    @Test
    public void testLazy() throws ExecutionException, InterruptedException {
        try (var application = new AnnotationConfigApplicationContext(ShareClientModule.class, ServerModule.class)) {

        }
    }

    @Test
    public void testSharedConnection() throws ExecutionException, InterruptedException {
        try( var application = new AnnotationConfigApplicationContext(ShareClientModule.class, ServerModule.class)) {
            var cf = application.getBean(ClientFactory.class);


            var server = (CompletableFuture<Server>)application.getBean("server8080");

            var client = server.thenCompose(s -> {
                return cf.New("127.0.0.1", 8080, 2);
            });


            var response = client.thenCompose(client1 -> {
                return client1.sendRequest("world");
            });

            assertEquals("hello world", response.get());

            client = server.thenCompose(s -> {
                return cf.New("127.0.0.1", 8080, 2);
            });

            Thread.sleep(12000);


            response = client.thenCompose(client1 -> {
                return client1.sendRequest("world");
            });

            assertEquals("hello world", response.get());

        }
    }

    @Test(expected = TimeoutException.class)
    public void testTimeout() throws Throwable {
        try( var application = new AnnotationConfigApplicationContext(DefaultModule.class)) {
            cf = application.getBean(ClientFactory.class);

            var si = new ServerFactory();
            try {
                var server = si.newServer(8080, (con,request) -> {
                    //request.getServer().sendResponse(request, "hello " + request.getMessage().getMsg());
                });

                var client = server.thenCompose(s -> {
                    return cf.New("127.0.0.1", 8080, 2);
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
            } finally {
                si.close();
            }
        }
    }


    @Test
    public void testNoServer() throws Throwable {

        try( var application = new AnnotationConfigApplicationContext(ShareClientModule.class)) {

            cf = application.getBean(ClientFactory.class);

            var client = cf.New("127.0.0.1", 8080, 2);


            Thread.sleep(3000);

            var si = new ServerFactory();
            try {
                var server = si.newServer(8080, (con,request) -> {
                    con.sendResponse(request, "hello " + request.getMsg());
                });

                Thread.sleep(3000);

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

            } finally {
                si.close();
            }
        }
    }
}
