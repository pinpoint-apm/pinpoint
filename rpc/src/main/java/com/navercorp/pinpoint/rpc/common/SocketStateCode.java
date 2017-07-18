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

import com.navercorp.pinpoint.common.util.ArrayUtils;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Taejin Koo
 */
public enum SocketStateCode {
    
    NONE((byte) 1),
    BEING_CONNECT((byte)2, NONE),
    CONNECTED((byte) 3, NONE, BEING_CONNECT),

    CONNECT_FAILED((byte)6, BEING_CONNECT),
    IGNORE((byte) 9, CONNECTED),

    RUN_WITHOUT_HANDSHAKE((byte) 10, CONNECTED), 
    RUN_SIMPLEX((byte) 11, RUN_WITHOUT_HANDSHAKE), 
    RUN_DUPLEX((byte) 12, RUN_WITHOUT_HANDSHAKE),

    BEING_CLOSE_BY_CLIENT((byte) 20, RUN_WITHOUT_HANDSHAKE, RUN_SIMPLEX, RUN_DUPLEX), 
    CLOSED_BY_CLIENT((byte) 22, NONE, BEING_CLOSE_BY_CLIENT), 
    UNEXPECTED_CLOSE_BY_CLIENT((byte) 26, NONE, CONNECTED, RUN_WITHOUT_HANDSHAKE, RUN_SIMPLEX, RUN_DUPLEX),

    BEING_CLOSE_BY_SERVER((byte) 30, RUN_WITHOUT_HANDSHAKE, RUN_SIMPLEX, RUN_DUPLEX), 
    CLOSED_BY_SERVER((byte) 32, NONE, BEING_CLOSE_BY_SERVER), 
    UNEXPECTED_CLOSE_BY_SERVER((byte) 36, NONE, CONNECTED, RUN_WITHOUT_HANDSHAKE, RUN_SIMPLEX, RUN_DUPLEX),

    ERROR_UNKNOWN((byte) 40),
    ERROR_ILLEGAL_STATE_CHANGE((byte) 41),
    ERROR_SYNC_STATE_SESSION((byte) 42);

    private final byte id;
    private final Set<SocketStateCode> validBeforeStateSet;

    private static final Set<SocketStateCode> ALL_STATE_CODES = EnumSet.allOf(SocketStateCode.class);

    SocketStateCode(byte id, SocketStateCode... validBeforeStates) {
        this.id = id;
        this.validBeforeStateSet = asSet(validBeforeStates);
    }

    private Set<SocketStateCode> asSet(SocketStateCode[] validBeforeStates) {
        if (ArrayUtils.isEmpty(validBeforeStates)) {
            return Collections.emptySet();
        }

        final Set<SocketStateCode> temp  = new HashSet<SocketStateCode>();
        Collections.addAll(temp, validBeforeStates);
        return temp;
    }

    public boolean canChangeState(SocketStateCode nextState) {
        if (isError(this)) {
            return false;
        }
        
        Set<SocketStateCode> validBeforeStateSet = nextState.getValidBeforeStateSet();
        if (validBeforeStateSet.contains(this)) {
            return true;
        }

        return isError(nextState);
    }

    public boolean isBeforeConnected() {
        return isBeforeConnected(this);
    }
    
    public static boolean isBeforeConnected(SocketStateCode code) {
        switch (code) {
            case NONE:
            case BEING_CONNECT:
                return true;
            default:
                return false;
        }
    }
    
    public boolean isRun() {
        return isRun(this);
    }
    
    public static boolean isRun(SocketStateCode code) {
        switch (code) {
            case RUN_WITHOUT_HANDSHAKE:
            case RUN_SIMPLEX:
            case RUN_DUPLEX:
                return true;
            default:
                return false;
        }
    }
    
    public boolean isRunDuplex() {
        return isRunDuplex(this);
    }

    public static boolean isRunDuplex(SocketStateCode code) {
        switch (code) {
            case RUN_DUPLEX:
                return true;
            default:
                return false;
        }
    }
    
    public boolean onClose() {
        return onClose(this);
    }
    
    public static boolean onClose(SocketStateCode code) {
        switch (code) {
            case BEING_CLOSE_BY_CLIENT:
            case BEING_CLOSE_BY_SERVER:
                return true;
            default:
                return false;
        }
    }
    
    public boolean isClosed() {
        return isClosed(this);
    }

    public static boolean isClosed(SocketStateCode code) {
        switch (code) {
            case CLOSED_BY_CLIENT:
            case UNEXPECTED_CLOSE_BY_CLIENT:
            case CLOSED_BY_SERVER:
            case UNEXPECTED_CLOSE_BY_SERVER:
            case ERROR_UNKNOWN:
            case ERROR_ILLEGAL_STATE_CHANGE:
            case ERROR_SYNC_STATE_SESSION:
                return true;
            default:
                return false;
        }
    }
    
    private boolean isError(SocketStateCode code) {
        switch (code) {
            case ERROR_ILLEGAL_STATE_CHANGE:
            case ERROR_UNKNOWN:
                return true;
            default:
                return false;
        }
    }

    public static SocketStateCode getStateCode(byte id) {

        for (SocketStateCode code : ALL_STATE_CODES) {
            if (code.id == id) {
                return code;
            }
        }

        return null;
    }

    public byte getId() {
        return id;
    }
    
    private Set<SocketStateCode> getValidBeforeStateSet() {
        return validBeforeStateSet;
    }

}