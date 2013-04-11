package com.profiler.common.io;


import com.profiler.common.dto2.Header;

public class HeaderUtils {
    public static boolean validateSignature(byte signature) {
        return Header.SIGNATURE == signature;
    }
}
