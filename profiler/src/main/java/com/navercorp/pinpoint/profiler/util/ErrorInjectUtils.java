package com.nhn.pinpoint.profiler.util;

import java.util.Random;

/**
 *
 */
public class ErrorInjectUtils {

    private static final Random random = new Random();

    public static void randomSleep(int mod) {
        int i = Math.abs(random.nextInt() % mod);
        try {
            Thread.sleep(i);
        } catch (InterruptedException e) {
        }
    }

}
