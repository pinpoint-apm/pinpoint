package com.navercorp.pinpoint.grpc.stream;

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

}
