package com.uangel.test;

import com.uangel.hello.Hello;
import com.uangel.modules.DefaultModule;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Priority;


@Lazy
@Configuration
class MockupHelloSameName {

    @Bean
    public Hello hello() {
        return new Hello() {
            @Override
            public String say() {
                return "mockup world";
            }
        };
    }
}

@Configuration
@Lazy
class MockupHelloNotSameName {

    @Bean
    @Primary
    public Hello mockuphello() {
        return new Hello() {
            @Override
            public String say() {
                return "mockup not same";
            }
        };
    }
}

@Component
@Lazy
@Priority(10)
class MockupHello implements Hello {
    @Override
    public String say() {
        return "mockup order 10";
    }
}

@Configuration
@Lazy
@Priority(12)
class MockupHelloHasOrder {
    @Bean
    public Hello mockuphello() {
        return new Hello() {
            @Override
            public String say() {
                return "mockup order 12";
            }
        };
    }
}



@RunWith(SpringRunner.class)
@DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
@ContextConfiguration(classes = {DefaultModule.class, MockupHelloSameName.class})
public class TestModuleOverride {

    @Autowired
    Hello h;

    @Test
    public void test() {
        Assert.assertEquals("mockup world", h.say());
    }

    @Test
    public void testNotSameName( ) {
        try(var application = new AnnotationConfigApplicationContext(DefaultModule.class, MockupHelloNotSameName.class)) {
            var hello = application.getBean(Hello.class);
            Assert.assertEquals("mockup not same", hello.say());
        }
    }

    @Test
    public void testHasOrder( ) {
        try(var application = new AnnotationConfigApplicationContext(DefaultModule.class, MockupHello.class)) {
            var hello = application.getBean(Hello.class);
            Assert.assertEquals("mockup order 10", hello.say());
        }
    }

    // not work
    public void testConfigHasOrder( ) {
        try(var application = new AnnotationConfigApplicationContext(DefaultModule.class, MockupHelloHasOrder.class)) {
            var hello = application.getBean(Hello.class);
            Assert.assertEquals("mockup order 10", hello.say());
        }
    }

}
