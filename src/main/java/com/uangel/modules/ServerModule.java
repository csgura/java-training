package com.uangel.modules;

import com.uangel.ctserver.Server;
import com.uangel.ctserver.ServerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.CompletableFuture;

@Configuration
public class ServerModule {
    @Bean
    public ServerFactory serverThreadPool() {
        return new ServerFactory();
    }

    @Bean
    @Qualifier("server-8080")
    CompletableFuture<Server> server8080(ServerFactory pool) {
        return pool.newServer(8080, (con,request) -> {
            con.sendResponse(request, "hello " + request.getMsg());
        });
    }

    @Bean
    public AutoCloseable serverCloser( @Qualifier("server-8080")CompletableFuture<Server> server) {

        return new AutoCloseable() {
            @Override
            public void close() throws Exception {
                server.thenAccept(s -> s.close());
            }
        };

    }
}
