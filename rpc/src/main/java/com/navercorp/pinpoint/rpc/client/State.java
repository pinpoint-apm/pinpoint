/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.rpc.client;

import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author emeroad
 * @author koo.taejin
 */
public class State {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

    // operations on profiler should be light as much possible
	
    // 0 : no handshake, 1: running
    public static final int INIT_RECONNECT = -1;
    public static final int INIT = 0;
    public static final int RUN = 1;
    public static final int RUN_DUPLEX_COMMUNICATION = 2;
    public static final int RUN_SIMPLEX_COMMUNICATION = 3;
    public static final int CLOSED = 4;
    // need this state?
    public static final int RECONNECT = 5;


    private final AtomicInteger state = new AtomicInteger(INIT);

    public int getState() {
        return this.state.get();
    }

    public boolean isRun() {
    	int code = state.get();
    	return isRun(code);
    }
    
    public boolean isRun(int code) {
        return code == RUN || code == RUN_DUPLEX_COMMUNICATION || code == RUN_SIMPLEX_COMMUNICATION;
    }

    public boolean isClosed() {
        return state.get() == CLOSED;
    }

    public boolean changeRun() {
    	logger.debug("State Will Be Changed {}.", getString(RUN));
        final int current = state.get();
        if (current == INIT) {
            return this.state.compareAndSet(INIT, RUN);
        } else if(current == INIT_RECONNECT) {
            return this.state.compareAndSet(INIT_RECONNECT, RUN);
        }
        throw new IllegalStateException("InvalidState current:"  + getString(current) + " change:" + getString(RUN));
    }

    public boolean changeRunDuplexCommunication() {
    	logger.debug("State Will Be Changed {}.", getString(RUN_DUPLEX_COMMUNICATION));
        final int current = state.get();
        if (current == INIT) {
            return this.state.compareAndSet(INIT, RUN_DUPLEX_COMMUNICATION);
        } else if(current == INIT_RECONNECT) {
            return this.state.compareAndSet(INIT_RECONNECT, RUN_DUPLEX_COMMUNICATION);
        } else if (current == RUN) {
        	return this.state.compareAndSet(RUN, RUN_DUPLEX_COMMUNICATION);
        } else if (current == RUN_DUPLEX_COMMUNICATION) {
        	return true;
        }
        throw new IllegalStateException("InvalidState current:"  + getString(current) + " change:" + getString(RUN_DUPLEX_COMMUNICATION));
    }
    
    public boolean changeRunSimplexCommunication() {
        logger.debug("State Will Be Changed {}.", getString(RUN_SIMPLEX_COMMUNICATION));
        final int current = state.get();
        if (current == INIT) {
            return this.state.compareAndSet(INIT, RUN_SIMPLEX_COMMUNICATION);
        } else if(current == INIT_RECONNECT) {
            return this.state.compareAndSet(INIT_RECONNECT, RUN_SIMPLEX_COMMUNICATION);
        } else if (current == RUN) {
            return this.state.compareAndSet(RUN, RUN_SIMPLEX_COMMUNICATION);
        } else if (current == RUN_SIMPLEX_COMMUNICATION) {
            return true;
        }
        throw new IllegalStateException("InvalidState current:"  + getString(current) + " change:" + getString(RUN_SIMPLEX_COMMUNICATION));
        
    }

    public boolean changeClosed(int before) {
    	logger.debug("State Will Be Changed {} -> {}.", getString(before), getString(CLOSED));
        return this.state.compareAndSet(before, CLOSED);
    }

    public boolean changeClosed() {
    	logger.debug("State Will Be Changed {}.", getString(CLOSED));
    	return this.state.compareAndSet(RUN, CLOSED);
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
            case RUN:
                return "RUN";
            case RUN_DUPLEX_COMMUNICATION:
                return "RUN_DUPLEX_COMMUNICATION";
            case RUN_SIMPLEX_COMMUNICATION:
                return "RUN_SIMPLEX_COMMUNICATION";
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
