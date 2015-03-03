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

package com.navercorp.pinpoint.rpc.common;

/**
 * @author Taejin Koo
 */
public class SocketState {

    private SocketStateCode beforeState = SocketStateCode.NONE;
    private SocketStateCode currentState = SocketStateCode.NONE;

    public synchronized SocketStateChangeResult changeState(SocketStateCode nextState) {
        boolean enable = this.currentState.canChangeState(nextState);
        if (enable) {
            this.beforeState = this.currentState;
            this.currentState = nextState;
            return new SocketStateChangeResult(true, beforeState, currentState, nextState);
        }

        return new SocketStateChangeResult(false, beforeState, currentState, nextState);
    }

    public SocketStateChangeResult stateToBeingConnect() {
        SocketStateCode nextState = SocketStateCode.BEING_CONNECT;
        return changeState(nextState);
    }

    public SocketStateChangeResult stateToConnected() {
        SocketStateCode nextState = SocketStateCode.CONNECTED;
        return changeState(nextState);
    }

    public SocketStateChangeResult stateToConnectFailed() {
        SocketStateCode nextState = SocketStateCode.CONNECT_FAILED;
        return changeState(nextState);
    }

    public SocketStateChangeResult stateToIgnore() {
        SocketStateCode nextState = SocketStateCode.IGNORE;
        return changeState(nextState);
    }

    public SocketStateChangeResult stateToRunWithoutHandshake() {
        SocketStateCode nextState = SocketStateCode.RUN_WITHOUT_HANDSHAKE;
        return changeState(nextState);
    }

    public SocketStateChangeResult stateToRunSimplex() {
        SocketStateCode nextState = SocketStateCode.RUN_SIMPLEX;
        return changeState(nextState);
    }

    public SocketStateChangeResult stateToRunDuplex() {
        SocketStateCode nextState = SocketStateCode.RUN_DUPLEX;
        return changeState(nextState);
    }

    public SocketStateChangeResult stateToBeingCloseByClient() {
        SocketStateCode nextState = SocketStateCode.BEING_CLOSE_BY_CLIENT;
        return changeState(nextState);
    }

    public SocketStateChangeResult stateToClosedByClient() {
        SocketStateCode nextState = SocketStateCode.CLOSED_BY_CLIENT;
        return changeState(nextState);
    }

    public SocketStateChangeResult stateToUnexpectedCloseByClient() {
        SocketStateCode nextState = SocketStateCode.UNEXPECTED_CLOSE_BY_CLIENT;
        return changeState(nextState);
    }

    public SocketStateChangeResult stateToBeingCloseByServer() {
        SocketStateCode nextState = SocketStateCode.BEING_CLOSE_BY_SERVER;
        return changeState(nextState);
    }

    public SocketStateChangeResult stateToClosedByServer() {
        SocketStateCode nextState = SocketStateCode.CLOSED_BY_SERVER;
        return changeState(nextState);
    }

    public SocketStateChangeResult stateToUnexpectedCloseByServer() {
        SocketStateCode nextState = SocketStateCode.UNEXPECTED_CLOSE_BY_SERVER;
        return changeState(nextState);
    }

    public SocketStateChangeResult stateToUnkownError() {
        SocketStateCode nextState = SocketStateCode.ERROR_UNKOWN;
        return changeState(nextState);
    }

    public synchronized SocketStateCode getCurrentState() {
        return currentState;
    }
    
    @Override
    public String toString() {
        SocketStateCode beforeState;
        SocketStateCode currentState;
        
        synchronized (this) {
            beforeState = this.beforeState;
            currentState = this.currentState;
        }
        
        StringBuilder toString = new StringBuilder();

        toString.append(this.getClass().getSimpleName());
        toString.append("(");
        toString.append(beforeState);
        toString.append("->");
        toString.append(currentState);
        toString.append(")");
        
        return toString.toString();
    }

}