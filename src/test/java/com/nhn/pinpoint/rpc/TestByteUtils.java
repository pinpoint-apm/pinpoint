package com.nhn.pinpoint.rpc;

import java.util.Random;

/**
 * @author emeroad
 */
public class TestByteUtils {

    private static final Random RANDOM = new Random();

    public static byte[] createRandomByte(int size) {
        byte[] bytes = new byte[size];
        RANDOM.nextBytes(bytes);
        return bytes;
    }
}
