package com.navercorp.pinpoint.common.server.util.pair;

/**
 * MutablePair
 */
public class IntegerValuePair {
    private int first;
    private int second;

    public IntegerValuePair(int first, int second) {
        this.first = first;
        this.second = second;
    }

    public int getFirst() {
        return first;
    }

    public int getSecond() {
        return second;
    }

    public void incrementFirst() {
        addFirst(1);
    }

    public void addFirst(int delta) {
        first += delta;
    }


    public void incrementSecond() {
        addSecond(1);
    }

    public void addSecond(int delta) {
        second += delta;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IntegerValuePair that = (IntegerValuePair) o;

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
        return "IntegerValuePair{" + first + "=" + second + "}";
    }

}
