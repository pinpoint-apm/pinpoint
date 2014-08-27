package com.nhn.pinpoint.common.util;

import org.junit.Test;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * test를 위해 hashcode를 찾을때 돌려볼수 있는 코드
 */
public class HashCodeTest {
    //    @Test
    public void test2() throws InterruptedException {
//        "test"
//        "tetU"
//        "uGTt"
//        System.out.println((int)'A');
//        System.out.println((int)'Z');
        System.out.println("test".hashCode());
        System.out.println("tfUU".hashCode());
        String a = "23 123";
        System.out.println(a.hashCode());
        System.out.println("test:" + a.hashCode());
        final int hashCode = a.hashCode();
        final ExecutorService es = Executors.newFixedThreadPool(4);
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                execute(hashCode);
            }
        };
        es.execute(runnable);
        es.execute(runnable);
        es.execute(runnable);
        es.execute(runnable);
        es.awaitTermination(1, TimeUnit.HOURS);
//        execute(hashCode, random);
//        test
//        -1757224452


    }

    private void execute(int hashCode) {
        Random random = new Random();
        while (true) {
            int i = random.nextInt(30);
//            System.out.println(i);
            StringBuilder sb = new StringBuilder();
//            sb.append("12 ");
            for (int j = 0; j < i; j++) {
                char c = get(random);
                sb.append(c);
            }
            sb.append(" 7");
            String s = sb.toString();
//            System.out.println(s.hashCode());
//            System.out.println(s);
            if (hashCode == s.hashCode()) {
//                if(a.equals(s)) {
//                    continue;
//                }
                System.out.println("find!!!  equals:" + s);
                break;
            }
        }
    }

    //    @Test
    public void test() {
//        "test"
//        "tetU"
//        "uGTt"
//        System.out.println((int)'A');
//        System.out.println((int)'Z');
        String a = "test";
        System.out.println("test:" + a.hashCode());
        int hashCode = a.hashCode();
        Random random = new Random();
        while (true) {
            int i = random.nextInt(50);
//            System.out.println(i);
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < i; j++) {
                char c = get(random);
                sb.append(c);
            }
            String s = sb.toString();
//            System.out.println(s);
            if (hashCode == s.hashCode()) {
                if ("test".equals(s)) {
                    continue;
                }
                System.out.println("equals:" + s);
                break;
            }
        }
//        test
//        -1757224452
    }

    char get(Random rand) {
//        65->90  : 25
//        97->122;  25
        int choice = (char) rand.nextInt(2);
        char ch;
        if (choice == 0) {
            ch = (char) ((rand.nextInt(25)) + 97);
        } else {
            ch = (char) ((rand.nextInt(25)) + 65);
        }

        while (true) {
            if (ch >= 'a' && ch <= 'z' || ch >= 'A' && ch <= 'Z') {
                return ch;
            }
        }
    }
}
