package com.profiler.common.io;


import com.profiler.common.dto.Header;

public class HeaderUtils {
    public static boolean validateSignature(byte signature) {
        return Header.SIGNATURE == signature;
    }
}
