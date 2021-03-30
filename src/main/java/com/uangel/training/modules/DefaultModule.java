package com.uangel.training.modules;

import com.uangel.training.hello.Hello;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
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
