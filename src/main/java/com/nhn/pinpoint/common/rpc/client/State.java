package com.nhn.pinpoint.common.rpc.client;

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
    private Object string;

    public int getState() {
        return this.state.get();
    }

    public boolean changeRun() {
        return this.state.compareAndSet(INIT, RUN);
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

    public String getString() {
        switch (state.get()) {
            case 0:
                return "INIT";
            case 1:
                return "RUN";
            case 2:
                return "CLOSED";
            case 3:
                return "RECONNECT";
        }
        return "UNKNOWN";
    }
}
