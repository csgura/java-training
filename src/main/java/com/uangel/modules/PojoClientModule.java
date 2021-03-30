package com.uangel.modules;

import com.uangel.impl.pojocli.FactoryImpl;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(FactoryImpl.class)
public class PojoClientModule {
}
