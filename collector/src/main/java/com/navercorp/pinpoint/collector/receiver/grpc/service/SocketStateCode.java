package com.navercorp.pinpoint.collector.receiver.grpc.service;

import java.util.Objects;

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
    private final SocketStateCode[] beforeStateSet;

    private static final SocketStateCode[] ALL_STATE_CODES = SocketStateCode.values();

    SocketStateCode(byte id, SocketStateCode... beforeStateSet) {
        this.id = id;
        this.beforeStateSet = Objects.requireNonNull(beforeStateSet, "beforeStateSet");
    }

    public boolean canChangeState(SocketStateCode nextState) {
        if (isError(this)) {
            return false;
        }

        for (SocketStateCode socketStateCode : nextState.beforeStateSet) {
            if (socketStateCode == this) {
                return true;
            }
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

}