package com.nhn.pinpoint.profiler.util;

/**
 * NamedThreadLocal 사용시 thread local leak 발생시 추적이 쉬움
 *
 * @author emeroad
 */
public class NamedThreadLocal<T> extends ThreadLocal<T> {
    private final String name;

    public NamedThreadLocal(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
