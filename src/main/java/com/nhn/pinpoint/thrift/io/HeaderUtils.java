package com.nhn.pinpoint.thrift.io;

/**
 * @author emeroad
 */
class HeaderUtils {
    public static boolean validateSignature(byte signature) {
        return Header.SIGNATURE == signature;
    }
}
