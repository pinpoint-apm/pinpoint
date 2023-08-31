package com.navercorp.pinpoint.profiler.monitor.metric.profilermetric;

import java.util.Objects;


public class Field<T> {
    private final String name;
    private final T value;

    public Field(String name, T value) {
        this.name = Objects.requireNonNull(name, "name");
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public T getValue() {
        return value;
    }

    @Override
    public String toString() {
        return name + ":" + value;
    }
}
