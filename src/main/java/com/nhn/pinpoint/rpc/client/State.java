package com.nhn.pinpoint.rpc.client;

import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author emeroad
 * @author koo.taejin
 */
public class State {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	// 프로파일러에서 동작하는 것들은 최대한 가볍게 함 
	
    // 0 핸드쉐이크 안함.. 1은 동작중, 2는 closed
    public static final int INIT_RECONNECT = -1;
    public static final int INIT = 0;
    public static final int RUN_WITHOUT_REGISTER = 1;
    public static final int RUN = 2;
    public static final int CLOSED = 3;
//    이 상태가 있어야 되나?
    public static final int RECONNECT = 4;


    private final AtomicInteger state = new AtomicInteger(INIT);

    public int getState() {
        return this.state.get();
    }

    public boolean isRun() {
    	int code = state.get();
        return code == RUN_WITHOUT_REGISTER || code == RUN;
    }
    
    public boolean isRun(int code) {
        return code == RUN_WITHOUT_REGISTER || code == RUN;
    }

    public boolean isClosed() {
        return state.get() == CLOSED;
    }

    public boolean changeRunWithoutRegister() {
    	logger.debug("State Will Be Changed {}.", getString(RUN_WITHOUT_REGISTER));
        final int current = state.get();
        if (current == INIT) {
            return this.state.compareAndSet(INIT, RUN_WITHOUT_REGISTER);
        } else if(current == INIT_RECONNECT) {
            return this.state.compareAndSet(INIT_RECONNECT, RUN_WITHOUT_REGISTER);
        }
        throw new IllegalStateException("InvalidState current:"  + getString(current) + " change:" + getString(RUN_WITHOUT_REGISTER));
    }

    public boolean changeRun() {
    	logger.debug("State Will Be Changed {}.", getString(RUN));
        final int current = state.get();
        if (current == INIT) {
            return this.state.compareAndSet(INIT, RUN);
        } else if(current == INIT_RECONNECT) {
            return this.state.compareAndSet(INIT_RECONNECT, RUN);
        } else if (current == RUN_WITHOUT_REGISTER) {
        	return this.state.compareAndSet(RUN_WITHOUT_REGISTER, RUN);
        }
        throw new IllegalStateException("InvalidState current:"  + getString(current) + " change:" + getString(RUN));
    }

    public boolean changeClosed(int before) {
    	logger.debug("State Will Be Changed {} -> {}.", getString(before), getString(CLOSED));
        return this.state.compareAndSet(before, CLOSED);
    }

    public boolean changeClosed() {
    	logger.debug("State Will Be Changed {}.", getString(CLOSED));
    	return this.state.compareAndSet(RUN_WITHOUT_REGISTER, CLOSED);
    }

    public void setClosed() {
        this.state.set(CLOSED);
    }

    public void setState(int state) {
    	logger.debug("State Will Be Changed {}.", getString(state));
        this.state.set(state);
    }

    public String getString(int stateCode) {
        switch (stateCode) {
            case INIT:
                return "INIT";
            case RUN_WITHOUT_REGISTER:
                return "RUN_WITHOUT_REGISTER";
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
