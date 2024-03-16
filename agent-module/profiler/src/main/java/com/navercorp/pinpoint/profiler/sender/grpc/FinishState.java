package com.navercorp.pinpoint.profiler.sender.grpc;

public class FinishState {

    private volatile State state = State.RUN;

    public enum State {
        RUN,
        COMPLETED,
        ERROR;

        public boolean isRun() {
            return this == RUN;
        }

        public boolean isFinish() {
            return this != RUN;
        }
    }

    public void completed() {
        state = State.COMPLETED;
    }

    public void error() {
        state = State.ERROR;
    }

    public State current() {
        return state;
    }

    public boolean isRun() {
        return state.isRun();
    }

}
