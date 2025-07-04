package com.navercorp.pinpoint.common.hbase.wd;

import java.util.Arrays;

public class WdUtils {

    public static class OneByte {

        private static final byte[][] PREFIXES;

        static {
            PREFIXES = new byte[256][];
            for (int i = 0; i < 256; i++) {
                PREFIXES[i] = new byte[]{(byte) i};
            }
        }

        public static byte[][] prefixes() {
            return PREFIXES;
        }

        public static byte[][] prefixes(int from, int to) {
            return Arrays.copyOfRange(PREFIXES, from, to);
        }
    }


    public static int hashBytes(byte[] bytes) {
        return hashBytes(bytes, 0, bytes.length);
    }

    public static int hashBytes(byte[] bytes, int offset, int length) {
        int hash = 1;
        for (int i = offset; i < length; i++) {
            hash = (31 * hash) + (int) bytes[i];
        }
        return hash;
    }
}
