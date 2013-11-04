package com.nhn.pinpoint.rpc.client;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author emeroad
 */
public class State {

    // 0 핸드쉐이크 안함.. 1은 동작중, 2는 closed
    public static final int INIT_RECONNECT = -1;
    public static final int INIT = 0;
    public static final int RUN = 1;
    public static final int CLOSED = 2;
    public static final int RECONNECT = 3;
//    이 상태가 있어야 되나?


    private final AtomicInteger state = new AtomicInteger(INIT);

    public int getState() {
        return this.state.get();
    }

    public boolean isRun() {
        return state.get() == RUN;
    }

    public boolean isClosed() {
        return state.get() == CLOSED;
    }

    public boolean changeRun() {
        final int current = state.get();
        if (current == INIT) {
            return this.state.compareAndSet(INIT, RUN);
        } else if(current == INIT_RECONNECT) {
            return this.state.compareAndSet(INIT_RECONNECT, RUN);
        }
        throw new IllegalStateException("InvalidState current:"  + getString(current) + " change:" + getString(RUN));
    }

    public boolean changeClosed(int before) {
        return this.state.compareAndSet(before, CLOSED);
    }

    public boolean changeClosed() {
        return this.state.compareAndSet(RUN, CLOSED);
    }

    public void setClosed() {
        this.state.set(CLOSED);
    }

    public void setState(int state) {
        this.state.set(state);
    }

    public String getString(int stateCode) {
        switch (stateCode) {
            case INIT:
                return "INIT";
            case RUN:
                return "RUN";
            case CLOSED:
                return "CLOSED";
            case RECONNECT:
                return "RECONNECT";
            case INIT_RECONNECT:
                return "INIT_RECONNECT";
        }
        return "UNKNOWN";
    }

    public String getString() {
        return getString(state.get());
    }
}
