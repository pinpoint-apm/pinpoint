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

    public synchronized SocketStateChangeResult to(SocketStateCode nextState) {
        boolean enable = this.currentState.canChangeState(nextState);
        if (enable) {
            this.beforeState = this.currentState;
            this.currentState = nextState;
            return new SocketStateChangeResult(true, beforeState, currentState, nextState);
        }

        return new SocketStateChangeResult(false, beforeState, currentState, nextState);
    }

    public SocketStateChangeResult toBeingConnect() {
        SocketStateCode nextState = SocketStateCode.BEING_CONNECT;
        return to(nextState);
    }

    public SocketStateChangeResult toConnected() {
        SocketStateCode nextState = SocketStateCode.CONNECTED;
        return to(nextState);
    }

    public SocketStateChangeResult toConnectFailed() {
        SocketStateCode nextState = SocketStateCode.CONNECT_FAILED;
        return to(nextState);
    }

    public SocketStateChangeResult toIgnore() {
        SocketStateCode nextState = SocketStateCode.IGNORE;
        return to(nextState);
    }

    public SocketStateChangeResult toRunWithoutHandshake() {
        SocketStateCode nextState = SocketStateCode.RUN_WITHOUT_HANDSHAKE;
        return to(nextState);
    }

    public SocketStateChangeResult toRunSimplex() {
        SocketStateCode nextState = SocketStateCode.RUN_SIMPLEX;
        return to(nextState);
    }

    public SocketStateChangeResult toRunDuplex() {
        SocketStateCode nextState = SocketStateCode.RUN_DUPLEX;
        return to(nextState);
    }

    public SocketStateChangeResult toBeingCloseByClient() {
        SocketStateCode nextState = SocketStateCode.BEING_CLOSE_BY_CLIENT;
        return to(nextState);
    }

    public SocketStateChangeResult toClosedByClient() {
        SocketStateCode nextState = SocketStateCode.CLOSED_BY_CLIENT;
        return to(nextState);
    }

    public SocketStateChangeResult toUnexpectedCloseByClient() {
        SocketStateCode nextState = SocketStateCode.UNEXPECTED_CLOSE_BY_CLIENT;
        return to(nextState);
    }

    public SocketStateChangeResult toBeingCloseByServer() {
        SocketStateCode nextState = SocketStateCode.BEING_CLOSE_BY_SERVER;
        return to(nextState);
    }

    public SocketStateChangeResult toClosedByServer() {
        SocketStateCode nextState = SocketStateCode.CLOSED_BY_SERVER;
        return to(nextState);
    }

    public SocketStateChangeResult toUnexpectedCloseByServer() {
        SocketStateCode nextState = SocketStateCode.UNEXPECTED_CLOSE_BY_SERVER;
        return to(nextState);
    }

    public SocketStateChangeResult toUnknownError() {
        SocketStateCode nextState = SocketStateCode.ERROR_UNKNOWN;
        return to(nextState);
    }
    
    public SocketStateChangeResult toSyncStateSessionError() {
        SocketStateCode nextState = SocketStateCode.ERROR_SYNC_STATE_SESSION;
        return to(nextState);
    }

    public boolean checkState(SocketStateCode expectedState) {
        return getCurrentState() == expectedState;
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