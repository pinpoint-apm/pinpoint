package com.navercorp.pinpoint.sdk.v1.concurrent.util;

public class Counter {
    private static long cnt;

    public static void add() {
        cnt++;
    }

    public static long get() {
        return cnt;
    }

    public static void reset() {
        cnt = 0;
    }
}
