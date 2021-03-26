package com.uangel.test;

import org.junit.Test;

import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeoutException;

public class TestCatch {

    interface Hello {
        String say();
    }

    class File implements AutoCloseable {
        @Override
        public void close()  {

        }
    }

    File open(String name) throws IOException {
        return new File();
    }

    void copyStream( File file1 , File file2) throws IOException, TimeoutException {

    }

    public void copyFile4(String name1 , String name2) throws IOException, TimeoutException {
      try( var file1 = open(name1)) {
          try ( var file2 = open(name2)) {
            copyStream(file1, file2 );
          }
      }
    }

    public void copyFile3(String name1 , String name2) throws IOException, TimeoutException {
        var file1 = open(name1);
        try {
            var file2 = open(name2);
            try {
                var file3 = open(name2);
                try {
                    copyStream(file1, file2);
                } finally {
                    file3.close();
                }
            } finally {
                file2.close();
            }
        } finally {
            file1.close();
        }
    }


    public void copyFile2(String name1 , String name2) {
        try {
            var file1 = open(name1);
            try {
                var file2 = open(name2);
                try {
                    copyStream(file1, file2);
                } catch(IOException e) {
                    e.printStackTrace();

                } catch (TimeoutException e) {
                    e.printStackTrace();
                } finally {
                    file2.close();
                }

            } catch ( IOException e)  {
                e.printStackTrace();
            } finally {
                file1.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void copyFile(String name1 , String name2) {

        File file1 = null;
        File file2 = null;
        try {
            file1 = open(name1);
            file2 = open(name2);
            copyStream( file1 , file2);

            file1.close();
            file2.close();
            return;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }

        if (file1 != null ) {
            file1.close();
        }

        if (file2 != null ) {
            file2.close();
        }
    }

    class SSS extends RuntimeException {

    }
    class HelloImpl implements Hello {
        @Override
        public String say() {
            throw new SSS();
        }
    }
    public void error() {
        throw new Error("hello");
    }

    public void runtime() throws NoSuchElementException{
        throw new NoSuchElementException("hello");
    }

    @Test(expected = NoSuchElementException.class)
    public void test3() {

            runtime();

    }


    @Test(expected = InterruptedException.class)
    public void test() throws InterruptedException {
        throw new InterruptedException("hello");
    }

    void hello() throws IOException {

    }

    @Test(expected = InterruptedException.class)
    public void test2() throws Exception {
            hello();
            test();
    }
}
