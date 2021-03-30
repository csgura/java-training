package com.uangel.test;


import com.uangel.ctclient.ClientFactory;
import com.uangel.ctserver.ServerFactory;
import com.uangel.modules.PojoClientModule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = PojoClientModule.class)
@DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
public class TestPojoCli {
    @Autowired
    ClientFactory cf;

    @Test
    public void test() throws ExecutionException, InterruptedException {
        var si = new ServerFactory();
        try {
            var server = si.newServer(8080, (con, request) -> {
                con.sendResponse(request, "hello " + request.getMsg());
            });

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
            si.close();
        }
    }
}
