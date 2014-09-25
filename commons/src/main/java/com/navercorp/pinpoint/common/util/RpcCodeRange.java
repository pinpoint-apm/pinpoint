package com.nhn.pinpoint.common.util;

/**
 * @author emeroad
 */
public final class RpcCodeRange {

    public static final short RPC_START = 9000;
    public static final short RPC_END = 10000;

    public static boolean isRpcRange(short code) {
        return code >= RPC_START && code < RPC_END;
    }

}
