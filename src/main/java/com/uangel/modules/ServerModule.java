package com.uangel.modules;

import com.uangel.ctserver.Server;
import com.uangel.ctserver.ServerInit;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.CompletableFuture;

@Configuration
public class ServerModule {
    @Bean
    public ServerInit serverThreadPool() {
        return new ServerInit();
    }

    @Bean
    @Qualifier("server-8080")
    CompletableFuture<Server> server8080(ServerInit pool) {
        return pool.newServer(8080, request -> {
            request.getServer().sendResponse(request, "hello " + request.getMessage().getMsg());
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
