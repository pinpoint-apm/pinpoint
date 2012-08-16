package com.profiler.util;


import com.profiler.dto.Header;

public class HeaderUtil {
    public static boolean validateSignature(byte signature){
        return Header.SIGNATURE == signature;
    }
}
