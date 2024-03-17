package com.navercorp.pinpoint.profiler.util;

public abstract class NamedRunnable implements Runnable {
    private final String name;

    public NamedRunnable(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "name='" + name;
    }
}
