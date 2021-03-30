package com.uangel.modules;

import com.uangel.hello.Hello;
import com.uangel.impl.actorcli.FactoryActorInterface;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.annotation.Order;

@Configuration
@Import({ActorModule.class, ActorClientModule.class})
public class DefaultModule {
    @Bean
    @Order(0)
    public Hello hello() {
        return new Hello() {
            @Override
            public String say() {
                return "hello world";
            }
        };
    }
}
