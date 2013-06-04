package com.nhn.pinpoint.profiler.io;


import com.nhn.pinpoint.common.dto.Header;

public class HeaderUtils {
    public static boolean validateSignature(byte signature) {
        return Header.SIGNATURE == signature;
    }
}
