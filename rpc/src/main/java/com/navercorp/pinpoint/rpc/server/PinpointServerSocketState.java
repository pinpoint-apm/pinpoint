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

package com.navercorp.pinpoint.rpc.server;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author koo.taejin
 */
class PinpointServerSocketState {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private PinpointServerSocketStateCode beforeState = PinpointServerSocketStateCode.NONE;
    private PinpointServerSocketStateCode currentState = PinpointServerSocketStateCode.NONE;

    private synchronized PinpointServerSocketStateCode changeState0(PinpointServerSocketStateCode nextState, List<PinpointServerSocketStateCode> skipLogicStateList, boolean throwException) {
        if (skipLogicStateList != null) {
            for (PinpointServerSocketStateCode skipLogicState : skipLogicStateList) {
                if (this.currentState == skipLogicState) {
                    return currentState;
                }
            }
        }
        
        boolean enable = this.currentState.canChangeState(nextState);
        if (enable) {
            this.beforeState = this.currentState;
            this.currentState = nextState;
            return null;
        }
        
        // if state can't be changed, just log.
        // no problem because the state of socket has been already closed.
        PinpointServerSocketStateCode checkBefore = this.beforeState;
        PinpointServerSocketStateCode checkCurrent = this.currentState;

        String errorMessage = cannotChangeMessage(checkBefore, checkCurrent, nextState);

        this.beforeState = this.currentState;
        this.currentState = PinpointServerSocketStateCode.ERROR_ILLEGAL_STATE_CHANGE;

        if (throwException) {
            throw new IllegalStateException(errorMessage);
        } else {
            logger.warn(errorMessage);
            return beforeState;
        }
    }
    
    /**
    * @return <tt>null</tt> if state changed expected value. or
    *         <tt>currentState</tt> if state do not changed.
    */
    public PinpointServerSocketStateCode changeState(PinpointServerSocketStateCode nextState, PinpointServerSocketStateCode... skipLogicStateList) {
        return changeState0(nextState, Arrays.asList(skipLogicStateList), false);
    }

    public PinpointServerSocketStateCode changeStateThrowWhenFailed(PinpointServerSocketStateCode nextState, PinpointServerSocketStateCode... skipLogicStateList) {
        return changeState0(nextState, Arrays.asList(skipLogicStateList), true);
    }

    private String cannotChangeMessage(PinpointServerSocketStateCode checkBefore, PinpointServerSocketStateCode checkCurrent, PinpointServerSocketStateCode nextState) {
        return "Can not change State(current:" + checkCurrent + " before:" + checkBefore + " next:" + nextState + ")";
    }

    public synchronized PinpointServerSocketStateCode getCurrentState() {
        return currentState;
    }

}
