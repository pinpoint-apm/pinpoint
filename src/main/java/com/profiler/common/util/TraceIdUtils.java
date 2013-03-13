package com.profiler.common.util;

/**
 *
 */
public class TraceIdUtils {

    public static final String formatString(long mostSigBits, long leastSigBits) {
        return (digits(mostSigBits >> 32, 8) + "-" +
                digits(mostSigBits >> 16, 4) + "-" +
                digits(mostSigBits, 4) + "-" +
                digits(leastSigBits >> 48, 4) + "-" +
                digits(leastSigBits, 12));
    }

    private static String digits(long val, int digits) {
        long hi = 1L << (digits * 4);
        return Long.toHexString(hi | (val & (hi - 1))).substring(1);
    }

    public static String[] parseTraceId(String traceId) {
        String[] components = traceId.split("-");
        if (components.length != 5) {
            throw new IllegalArgumentException("Invalid TraceId string: "+ traceId);
        }
        for (int i=0; i<5; i++) {
            components[i] = "0x"+components[i];
        }
        return components;
    }

    public static long parseMostId(String[] parsedTraceId) {
        long mostSigBits = Long.decode(parsedTraceId[0]).longValue();
        mostSigBits <<= 16;
        mostSigBits |= Long.decode(parsedTraceId[1]).longValue();
        mostSigBits <<= 16;
        mostSigBits |= Long.decode(parsedTraceId[2]).longValue();
        return mostSigBits;

    }

    public static long parseLeastId(String[] parsedTraceId) {

        long leastSigBits = Long.decode(parsedTraceId[3]).longValue();
        leastSigBits <<= 48;
        leastSigBits |= Long.decode(parsedTraceId[4]).longValue();
        return leastSigBits;
    }
}
