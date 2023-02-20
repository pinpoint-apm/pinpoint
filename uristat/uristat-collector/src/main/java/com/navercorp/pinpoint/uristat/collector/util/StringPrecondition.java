package com.navercorp.pinpoint.uristat.collector.util;

import org.springframework.util.StringUtils;

public final class StringPrecondition {
    private StringPrecondition() {
    }

    public static String requireHasLength(String str, String variableName) {
        if (StringUtils.hasLength(str)) {
            return str;
        }
        throw new IllegalArgumentException(variableName + " must not be empty");
    }
}
