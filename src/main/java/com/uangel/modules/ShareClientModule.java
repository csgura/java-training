package com.uangel.modules;

import com.uangel.impl.sharecli.FactoryActorInterface;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({FactoryActorInterface.class})
public class ShareClientModule {
}
