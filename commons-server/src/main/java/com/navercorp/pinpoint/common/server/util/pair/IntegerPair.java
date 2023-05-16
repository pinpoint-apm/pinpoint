package com.navercorp.pinpoint.common.server.util.pair;

/**
 * ImmutablePair
 */
public class IntegerPair {
    private final int first;
    private final int second;

    public IntegerPair(int first, int second) {
        this.first = first;
        this.second = second;
    }

    public int getFirst() {
        return first;
    }

    public int getSecond() {
        return second;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IntegerPair that = (IntegerPair) o;

        if (first != that.first) return false;
        return second == that.second;
    }

    @Override
    public int hashCode() {
        int result = first;
        result = 31 * result + second;
        return result;
    }

    @Override
    public String toString() {
        return "IntegerPair{" + first + "=" + second + "}";
    }
}
