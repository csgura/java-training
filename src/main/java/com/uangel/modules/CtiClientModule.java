package com.uangel.modules;

import com.typesafe.config.Config;
import com.uangel.ctclient.Client;
import com.uangel.ctclient.ClientFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import java.util.concurrent.CompletableFuture;

@Configuration
@Lazy
public class CtiClientModule {

    @Bean
    @Qualifier("cti-client")
    public CompletableFuture<Client> ctiClient(ClientFactory cf) {
        return cf.New("127.0.0.1", 8080, 2);
    }
}
