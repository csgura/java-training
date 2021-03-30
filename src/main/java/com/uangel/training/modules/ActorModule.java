package com.uangel.training.modules;

import akka.actor.ActorSystem;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigRenderOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import java.io.File;

@Configuration
@Lazy
public class ActorModule {


    @Bean
    public Config config() {
        var cfg =  ConfigFactory.parseFile(new File("/Users/gura/git/ulib/test/testActor/reference.conf"));

        if (cfg.hasPath("xx.xx")) {
            cfg.getString("xx.xx");
        }
        System.out.println("cfg = " +  cfg.root().render(ConfigRenderOptions.defaults().setOriginComments(false).setJson(false)));

        return ConfigFactory.load(cfg);
    }

    @Bean(destroyMethod = "terminate")
    public ActorSystem actorSystem(  Config cfg) {
        return ActorSystem.create("mysystem", cfg);
    }
}


/*
@Configuration
class ClientFactoryModule {
    @Bean
    public ClientFactory clientFactory( ActorSystem actorsystem ) {
        return new FactoryActorInterface(actorsystem);
    }

    //binder.BindConstructor(ClientFactory.class, FactoryActorInterface.class);
}
*/