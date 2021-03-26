package com.uangel.test;

import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TestFuture {

    class FilePair implements AutoCloseable {
        File file1;
        File file2;

        public FilePair(File file1, File file2) {
            this.file1 = file1;
            this.file2 = file2;
        }

        @Override
        public void close() throws Exception {

        }
    }


    class File implements AutoCloseable {
        @Override
        public void close() {

        }
    }

    CompletableFuture<File> open(String name) {
        return CompletableFuture.supplyAsync(() -> {
            throw new RuntimeException("hello world");
//            try {
//                Thread.sleep(10);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            return new File();
        });
    }


    class SeekResult {
    }

    SeekResult seek(File file) throws IOException {
        return new SeekResult();
    }

    CompletableFuture<Integer> copyStream(FilePair pair) {
        System.out.println("copy stream");
        //CompletableFuture.failedFuture(new Exception());
        return CompletableFuture.completedFuture(100);

    }


    CompletableFuture<FilePair> openSecond(File file1, String name) {
        System.out.println("openSecond");
        var f3 = open(name).thenApply(file2 -> new FilePair(file1, file2));
        return f3;
        //return CompletableFuture.failedFuture(new IOException("no such file"));
    }


    <T extends AutoCloseable, R> CompletableFuture<R> withResource(T a, Function<T, CompletableFuture<R>> mapFunc) {
        var f = mapFunc.apply(a);
        f.whenComplete((r, throwable) -> {
            try {
                a.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        return f;
    }

    @Test
    public void test() throws InterruptedException {
        var t1 = open("file1");
        var t2 = t1.thenCompose(file -> {
            return withResource(file, file1 -> {
                var t3 = openSecond(file1, "file2");
                return t3.thenCompose(filePair -> {
                    return withResource(filePair, this::copyStream);
                });
            });
        });


        var f = open("file1")
                .thenCompose(file -> {
                    try {
                        var sr = seek(file);
                        return CompletableFuture.completedFuture(file);
                    } catch (IOException e) {
                        return CompletableFuture.failedFuture(e);
                    }
                })
                .thenCompose(file1 -> openSecond(file1, "name"))
                .thenCompose(this::copyStream);

        f.thenAccept(n -> {
            System.out.printf("file copied %d bytes\n", n);
        });

        f.whenComplete((integer, throwable) -> {
            if (throwable != null) {
                throwable.printStackTrace();
            }
        });


        System.out.println("before sleep");
        Thread.sleep(100);


    }

    @Test
    public void test2() {
        var optString = Optional.<String>ofNullable("http://hello.com");

        var optUri = optString.map(s -> {
            try {
                return CompletableFuture.completedFuture(new URI(s));
            } catch (URISyntaxException e) {
                return CompletableFuture.<URI>failedFuture(e);
            }
        });

        var futreUri = optUri.orElseGet(() -> {
            return CompletableFuture.failedFuture(new NoSuchElementException("url is null"));
        });

        futreUri.whenComplete((uri, throwable) -> {
            if (throwable != null) {
                throwable.printStackTrace();
            } else {
                System.out.println("send request to " + uri);
            }
        });

    }

    class Response {
        URI uri;

        public Response(URI uri) {
            this.uri = uri;
        }

        @Override
        public String toString() {
            return uri.toString();
        }
    }

    CompletableFuture<Response> sendRequest( URI uri ) {
        return CompletableFuture.completedFuture(new Response(uri));
    }

    static<T> CompletableFuture<List<T>> sequence(List<CompletableFuture<T>> com) {
        return CompletableFuture.allOf(com.toArray(new CompletableFuture<?>[0]))
                .thenApply(v -> com.stream()
                        .map(CompletableFuture::join)
                        .collect(Collectors.toList())
                );
    }

    <T> CompletableFuture<List<T>> filterSuccess(List<CompletableFuture<T>> list) {
        if (list.size() > 0 ) {
            var f = list.get(0);
            var o = f.handle((t, throwable) -> {
                if (throwable != null) {
                    return Optional.<T>empty();
                }
                return Optional.of(t);
            });

            var o4 = o.thenCompose( l1result -> {
                var otherList = filterSuccess( list.subList(1, list.size()) );
                var o3 = otherList.thenApply(l2 -> {
                    if (l1result.isPresent()) {
                        l2.add(l1result.get());
                    }
                    return l2;
                });
                return o3;
            });
            return o4;
        } else {
            return CompletableFuture.completedFuture(new ArrayList<>());
        }

    }

    @Test
    public void test3() {
        List<String> list = new ArrayList<>();
        list.add("http://hello.com");
        list.add(":://");

        var l2 = list.stream().map( s -> {
            System.out.println("hello " + s);
            try {
                return CompletableFuture.completedFuture(new URI(s));
            } catch (URISyntaxException e) {
                return CompletableFuture.<URI>failedFuture(e);
            }
        }).map( uriCompletableFuture -> {
            return uriCompletableFuture.thenCompose(this::sendRequest);
        });

        var l3 = l2.collect(Collectors.toList());

        CompletableFuture<List<Response>> l4 = null;

       // CompletableFuture.allOf(futures.stream().toArray(CompletableFuture[]::new)).join();

        var l5 = filterSuccess(l3);
        l5.whenComplete( (responses, throwable) -> {
            if (throwable != null ) {
                throwable.printStackTrace();
            } else {
                responses.stream().forEach(r -> {
                    System.out.println("r = " + r);
                });
            }
        });
    }
}