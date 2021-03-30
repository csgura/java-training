package com.uangel.training.modules;

import com.uangel.training.impl.pojocli.FactoryImpl;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(FactoryImpl.class)
public class PojoClientModule {
}
