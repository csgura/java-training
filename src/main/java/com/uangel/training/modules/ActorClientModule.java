package com.uangel.training.modules;

import com.uangel.training.impl.actorcli.FactoryActorInterface;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(FactoryActorInterface.class)
public class ActorClientModule {
}
