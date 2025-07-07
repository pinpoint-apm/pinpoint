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

}
