package com.nhn.pinpoint.common.io;


import com.nhn.pinpoint.common.dto2.Header;

public class HeaderUtils {
    public static boolean validateSignature(byte signature) {
        return Header.SIGNATURE == signature;
    }
}
