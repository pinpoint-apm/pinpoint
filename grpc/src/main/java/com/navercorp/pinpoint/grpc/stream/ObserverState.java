package com.navercorp.pinpoint.grpc.stream;

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

public enum ObserverState {
    RUN,
    COMPLETED,
    ERROR;

    public boolean isCompleted() {
        return this == COMPLETED;
    }

    public boolean isError() {
        return this == ERROR;
    }

    public boolean isRun() {
        return this == RUN;
    }

    public boolean isClosed() {
        return this != RUN;
    }

    public static <T> boolean changeComplete(AtomicReferenceFieldUpdater<T, ObserverState> updater, T target) {
        return updater.compareAndSet(target, RUN, COMPLETED);
    }

    public static <T> boolean changeError(AtomicReferenceFieldUpdater<T, ObserverState> updater, T target) {
        return updater.compareAndSet(target, RUN, ERROR);
    }

}
