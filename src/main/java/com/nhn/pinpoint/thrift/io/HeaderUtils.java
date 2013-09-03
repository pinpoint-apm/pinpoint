package com.nhn.pinpoint.thrift.io;


class HeaderUtils {
    public static boolean validateSignature(byte signature) {
        return Header.SIGNATURE == signature;
    }
}
