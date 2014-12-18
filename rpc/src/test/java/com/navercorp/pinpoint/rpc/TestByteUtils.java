package com.navercorp.pinpoint.rpc;

import java.util.Random;

/**
 * @author emeroad
 */
public final class TestByteUtils {

    private static final Random RANDOM = new Random();

    private TestByteUtils() {
    }

    public static byte[] createRandomByte(int size) {
        byte[] bytes = new byte[size];
        RANDOM.nextBytes(bytes);
        return bytes;
    }
}
