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

import java.util.HashSet;
import java.util.Set;

/**
 * @author koo.taejin
 */
public enum PinpointServerStateCode {

    // NONE : No event
    // RUN_WITHOUT_HANDSHAKE  : can send message only to server without handshake each other.
    // RUN_SIMPLEX  : can send message only to server
    // RUN_DUPLEX_COMMUNICATION : can communicate each other by full-duplex
    // BEING_SHUTDOWN :  received a close packet from a peer first and releasing resources
    // SHUTDOWN : has been closed
    // UNEXPECTED_SHUTDOWN : has not received a close packet from a peer but a peer has been shutdown

    NONE(),
    RUN_WITHOUT_HANDSHAKE(NONE), //Simplex Communication
    RUN_SIMPLEX(NONE, RUN_WITHOUT_HANDSHAKE),
    RUN_DUPLEX(NONE, RUN_WITHOUT_HANDSHAKE),
    BEING_SHUTDOWN(RUN_SIMPLEX, RUN_DUPLEX, RUN_WITHOUT_HANDSHAKE),
    SHUTDOWN(RUN_SIMPLEX, RUN_DUPLEX, RUN_WITHOUT_HANDSHAKE, BEING_SHUTDOWN),
    UNEXPECTED_SHUTDOWN(RUN_SIMPLEX, RUN_DUPLEX, RUN_WITHOUT_HANDSHAKE),


    // need  messages to close a connection from server to agent
    // for example, checked all of needed things followed by HELLO, if a same agent name exists, have to notify that to agent or not?
    ERROR_UNKOWN(RUN_SIMPLEX, RUN_DUPLEX, RUN_WITHOUT_HANDSHAKE),
    ERROR_ILLEGAL_STATE_CHANGE(NONE, RUN_SIMPLEX, RUN_DUPLEX, RUN_WITHOUT_HANDSHAKE, BEING_SHUTDOWN, SHUTDOWN);

    private final Set<PinpointServerStateCode> validBeforeStateSet;

    private PinpointServerStateCode(PinpointServerStateCode... validBeforeStates) {
        this.validBeforeStateSet = new HashSet<PinpointServerStateCode>();

        if (validBeforeStates != null) {
            for (PinpointServerStateCode eachStateCode : validBeforeStates) {
                getValidBeforeStateSet().add(eachStateCode);
            }
        }
    }

    public boolean canChangeState(PinpointServerStateCode nextState) {
        Set<PinpointServerStateCode> validBeforeStateSet = nextState.getValidBeforeStateSet();

        if (validBeforeStateSet.contains(this)) {
            return true;
        }

        return false;
    }

    public Set<PinpointServerStateCode> getValidBeforeStateSet() {
        return validBeforeStateSet;
    }

    public static boolean isRun(PinpointServerStateCode code) {
        if (code == RUN_SIMPLEX || code == RUN_DUPLEX || code == RUN_WITHOUT_HANDSHAKE) {
            return true;
        }

        return false;
    }

    public static boolean isRunDuplexCommunication(PinpointServerStateCode code) {
        if (code == RUN_DUPLEX) {
            return true;
        }

        return false;
    }

    public static boolean isFinished(PinpointServerStateCode code) {
        if (code == SHUTDOWN || code == UNEXPECTED_SHUTDOWN || code == ERROR_UNKOWN || code == ERROR_ILLEGAL_STATE_CHANGE) {
            return true;
        }
        return false;
    }

    public static PinpointServerStateCode getStateCode(String name) {
        PinpointServerStateCode[] allStateCodes = PinpointServerStateCode.values();

        for (PinpointServerStateCode code : allStateCodes) {
            if (code.name().equalsIgnoreCase(name)) {
                return code;
            }
        }

        return null;
    }

}
