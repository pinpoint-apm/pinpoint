package com.navercorp.pinpoint.profiler.context;

public interface Annotation<T> {
    @Deprecated
    int getAnnotationKey();

    int getKey();

    T getValue();
}
