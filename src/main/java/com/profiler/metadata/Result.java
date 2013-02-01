package com.profiler.metadata;

/**
 *
 */
public class Result {

    private boolean newValue;
    private int id;

    public Result(boolean newValue, int id) {
        this.newValue = newValue;
        this.id = id;
    }

    public boolean isNewValue() {
        return newValue;
    }

    public int getId() {
        return id;
    }

}
