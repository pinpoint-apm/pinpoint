package com.profiler.common.util;


import com.profiler.common.dto.Header;

public class HeaderUtil {
    public static boolean validateSignature(byte signature){
        return Header.SIGNATURE == signature;
    }
}
