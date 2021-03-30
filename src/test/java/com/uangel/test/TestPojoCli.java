package com.uangel.test;


import com.uangel.ctclient.ClientFactory;
import com.uangel.ctserver.Server;
import com.uangel.ctserver.ServerFactory;
import com.uangel.modules.PojoClientModule;
import com.uangel.modules.ServerModule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.BeanFactoryAnnotationUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.ResolvableType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;


public class TestPojoCli {

    @Test
    public void test() throws ExecutionException, InterruptedException {

        var application = new AnnotationConfigApplicationContext(PojoClientModule.class, ServerModule.class);
        try {

            var server = (CompletableFuture<Server>) application.getBean("server8080");
            //var server = BeanFactoryAnnotationUtils.qualifiedBeanOfType(application.getBeanFactory(), ResolvableType.forClassWithGenerics(CompletableFuture.class, Server.class), "server-8080");

            var cf = application.getBean(ClientFactory.class);


            var client = server.thenCompose(s -> {
                return cf.New("127.0.0.1", 8080, 2);
            });


            var response = client.thenCompose(client1 -> {
                return client1.sendRequest("world");
            });

            try {
                assertEquals("hello world", response.get());
            } finally {
                //client.thenAccept(x -> x.close());
                server.thenAccept(x -> x.close());
            }
        } finally {
            application.close();
        }


    }
}
