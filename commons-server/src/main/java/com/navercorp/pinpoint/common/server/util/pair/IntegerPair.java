package com.navercorp.pinpoint.common.server.util.pair;

/**
 * ImmutablePair
 */
public record IntegerPair(int first, int second) {

    @Override
    public String toString() {
        return "IntegerPair{" + first + "=" + second + "}";
    }
}
