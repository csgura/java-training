package com.uangel.test;

import org.junit.Test;

import java.util.Optional;

public class TestOptional {

    class ASuper{}
    class A extends ASuper{}

    class AImpl extends A{}



    @Test
    public void test() {
        var empty = Optional.<String>empty();

        var box = Optional.of("10");

        var box2 = box.map(a -> {
            System.out.println("hello "+ a);
            return Integer.parseInt(a);
        });


        var box3 = empty.map( a -> {
            System.out.println("empty "+ a);
            return Integer.parseInt(a);
        });

        System.out.println("box2 isEmpty = " + box2.isEmpty());
        System.out.println("box3 isEmpty = " + box3.isEmpty());


        var box4 = box2.filter(integer -> integer > 10);
        System.out.println("box4 isEmpty = " + box4.isEmpty());

        box2 = box2.or(() -> {
            System.out.println("box2 or");
            return Optional.of(11);
        });

        var box5 = box4.or(() -> {
            System.out.println("box4 or");
            return Optional.of(11);
        });
        System.out.println("box5 isEmpty = " + box5.isEmpty());

        box3.ifPresent(integer -> {
            System.out.println("box3 value = " + integer);
        });

        box5.ifPresent(integer -> {
            System.out.println("box5 value = " + integer);
        });


        var abox = Optional.of(new A());

        var abox2 = abox.or(() ->
            {
               return Optional.of(new AImpl());
            }
        );

//        var abox3 = abox.or(() -> {
//                return Optional.of(new ASuper());
//            }
//        );

    }

}
