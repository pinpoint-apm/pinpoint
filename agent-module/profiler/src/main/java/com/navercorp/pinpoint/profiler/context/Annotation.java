package com.navercorp.pinpoint.profiler.context;

public interface Annotation<T> {
    int getKey();

    T getValue();
}
