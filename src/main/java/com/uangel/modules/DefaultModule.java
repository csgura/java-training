package com.uangel.modules;

import com.uangel.impl.actorcli.FactoryActorInterface;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({ActorModule.class, FactoryActorInterface.class})
public class DefaultModule {
}
