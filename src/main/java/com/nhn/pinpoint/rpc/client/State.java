package com.nhn.pinpoint.rpc.client;

import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 */
public class State {

    // 0 핸드쉐이크 안함.. 1은 동작중, 2는 closed
    public static final int INIT = 0;
    public static final int RUN = 1;
    public static final int CLOSED = 2;
    public static final int RECONNECT = 3;
//    이 상태가 있어야 되나?
//    private static final int STATE_ERROR_CLOSED = 3;

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
        return this.state.compareAndSet(INIT, RUN);
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
        }
        return "UNKNOWN";
    }

    public String getString() {
        return getString(state.get());
    }
}
