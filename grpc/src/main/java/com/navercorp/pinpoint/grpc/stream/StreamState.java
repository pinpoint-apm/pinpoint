package com.navercorp.pinpoint.grpc.stream;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

public class StreamState {
    private static final AtomicReferenceFieldUpdater<StreamState, ObserverState> STATE
            = AtomicReferenceFieldUpdater.newUpdater(StreamState.class, ObserverState.class, "state");

    private final ClientCallContext.CallType callType;
    private volatile ObserverState state = ObserverState.RUN;

    public StreamState(ClientCallContext.CallType callType) {
        this.callType = Objects.requireNonNull(callType, "callType");
    }

    public ClientCallContext.CallType getCallType() {
        return callType;
    }

    public boolean onCompleteState() {
        return STATE.compareAndSet(this, ObserverState.RUN, ObserverState.COMPLETED);
    }

    public boolean onErrorState() {
        return STATE.compareAndSet(this, ObserverState.RUN, ObserverState.ERROR);
    }

    public boolean isCompleted() {
        return state.isCompleted();
    }

    public boolean isError() {
        return state.isError();
    }

    public boolean isRun() {
        return state.isRun();
    }

    public boolean isClosed() {
        return state.isClosed();
    }

    @Override
    public String toString() {
        return "State{" + callType + '=' + state +'}';
    }
}
