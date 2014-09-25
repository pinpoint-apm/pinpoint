package com.nhn.pinpoint.thrift.io;

/**
 * @author emeroad
 */
final class HeaderUtils {
    public static final int OK = Header.SIGNATURE;
    // TODO L4 상수화 시켜 놓았는데. 변경이 가능하도록 해야 될듯 하다.
    public static final int PASS_L4 = 85; // Udp
    public static final int FAIL = 0;

    public static int validateSignature(byte signature) {
        if (Header.SIGNATURE == signature) {
            return OK;
        } else if (PASS_L4 == signature) {
            return PASS_L4;
        }
        return FAIL;
    }
}
