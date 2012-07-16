package com.profiler.util;

/**
 * @author emeroad
 */
public class NamedThreadLocal<T> extends ThreadLocal<T> {
    private final String name;

    public NamedThreadLocal(String name) {
        this.name = name;
    }
}
