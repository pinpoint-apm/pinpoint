package com.navercorp.pinpoint.common.server.util.pair;

/**
 * ImmutablePair
 */
public record LongPair(long first, long second) {

    @Override
    public String toString() {
        return "LongPair{" + first + "=" + second + "}";
    }
}
