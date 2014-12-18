package com.navercorp.pinpoint.profiler.util;

import java.util.Random;

/**
 *
 */
public final class ErrorInjectUtils {

    private static final Random random = new Random();

    private ErrorInjectUtils() {
    }

    public static void randomSleep(int mod) {
        int i = Math.abs(random.nextInt() % mod);
        try {
            Thread.sleep(i);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();;
        }
    }

}
