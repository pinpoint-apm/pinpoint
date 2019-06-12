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

import com.navercorp.pinpoint.rpc.common.SocketState;
import com.navercorp.pinpoint.rpc.common.SocketStateChangeResult;
import com.navercorp.pinpoint.rpc.common.SocketStateCode;
import com.navercorp.pinpoint.rpc.server.handler.ServerStateChangeEventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author Taejin Koo
 */
public class DefaultPinpointServerState {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final DefaultPinpointServer pinpointServer;
    private final List<ServerStateChangeEventHandler> stateChangeEventListeners;

    private final SocketState state;

    public DefaultPinpointServerState(DefaultPinpointServer pinpointServer, List<ServerStateChangeEventHandler> stateChangeEventListeners) {
        this.pinpointServer = pinpointServer;
        this.stateChangeEventListeners = stateChangeEventListeners;
        
        this.state = new SocketState();
    }

    SocketStateChangeResult toConnected() {
        SocketStateCode nextState = SocketStateCode.CONNECTED;
        return to(nextState);
    }

    SocketStateChangeResult toRunWithoutHandshake() {
        SocketStateCode nextState = SocketStateCode.RUN_WITHOUT_HANDSHAKE;
        return to(nextState);
    }

    SocketStateChangeResult toRunSimplex() {
        SocketStateCode nextState = SocketStateCode.RUN_SIMPLEX;
        return to(nextState);
    }

    SocketStateChangeResult toRunDuplex() {
        SocketStateCode nextState = SocketStateCode.RUN_DUPLEX;
        return to(nextState);
    }

    SocketStateChangeResult toBeingClose() {
        SocketStateCode nextState = SocketStateCode.BEING_CLOSE_BY_SERVER;
        return to(nextState);
    }

    SocketStateChangeResult toBeingCloseByPeer() {
        SocketStateCode nextState = SocketStateCode.BEING_CLOSE_BY_CLIENT;
        return to(nextState);
    }

    SocketStateChangeResult toClosed() {
        SocketStateCode nextState = SocketStateCode.CLOSED_BY_SERVER;
        return to(nextState);
    }

    SocketStateChangeResult toClosedByPeer() {
        SocketStateCode nextState = SocketStateCode.CLOSED_BY_CLIENT;
        return to(nextState);
    }

    SocketStateChangeResult toUnexpectedClosed() {
        SocketStateCode nextState = SocketStateCode.UNEXPECTED_CLOSE_BY_SERVER;
        return to(nextState);
    }

    SocketStateChangeResult toUnexpectedClosedByPeer() {
        SocketStateCode nextState = SocketStateCode.UNEXPECTED_CLOSE_BY_CLIENT;
        return to(nextState);
    }

    SocketStateChangeResult toErrorUnknown() {
        SocketStateCode nextState = SocketStateCode.ERROR_UNKNOWN;
        return to(nextState);
    }

    SocketStateChangeResult toErrorSyncStateSession() {
        SocketStateCode nextState = SocketStateCode.ERROR_SYNC_STATE_SESSION;
        return to(nextState);
    }

    private SocketStateChangeResult to(SocketStateCode nextState) {
        String objectUniqName = pinpointServer.getObjectUniqName();
        
        logger.debug("{} stateTo() started. to:{}", objectUniqName, nextState);

        SocketStateChangeResult stateChangeResult = state.to(nextState);
        if (stateChangeResult.isChange()) {
            executeChangeEventHandler(pinpointServer, nextState);
        }

        logger.info("{} stateTo() completed. {}", objectUniqName, stateChangeResult);

        return stateChangeResult;
    }

    private void executeChangeEventHandler(DefaultPinpointServer pinpointServer, SocketStateCode nextState) {
        for (ServerStateChangeEventHandler eachListener : this.stateChangeEventListeners) {
            try {
                eachListener.stateUpdated(pinpointServer, nextState);
            } catch (Exception e) {
                logger.warn("Please handling exception in stateUpdated method. message:{}", e.getMessage(), e);
            }
        }
    }

    boolean isEnableCommunication() {
        return SocketStateCode.isRun(getCurrentStateCode());
    }

    boolean isEnableDuplexCommunication() {
        return SocketStateCode.isRunDuplex(getCurrentStateCode());
    }
    
    SocketStateCode getCurrentStateCode() {
        return state.getCurrentState();
    }

}
