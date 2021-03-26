package com.uangel.test;

import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

public class TestRailway {

    Optional<URI> parseURI( String s )   {
        try {
            return Optional.of(new URI(s));
        } catch (URISyntaxException e) {
            return Optional.empty();
        }
    }
    @Test
    public void test() {
        var optURI = Optional.of(":::+-*$&");

        var optParsed = optURI.flatMap(this::parseURI);

        optParsed.ifPresent( (uri) -> {
            System.out.println("send request to  " + uri);
        });


    }
}
