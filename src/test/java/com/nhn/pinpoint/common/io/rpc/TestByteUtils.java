package com.nhn.pinpoint.common.io.rpc;

import java.util.Random;

/**
 *
 */
public class TestByteUtils {

    private static final Random RANDOM = new Random();

    public static byte[] createRandomByte(int size) {
        byte[] bytes = new byte[size];
        RANDOM.nextBytes(bytes);
        return bytes;
    }
}
