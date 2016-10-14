package com.navercorp.pinpoint.common.server.bo;

import com.navercorp.pinpoint.common.trace.MethodType;

/**
 * readable class of MethodType
 * @author Woonduk Kang(emeroad)
 */
public enum MethodTypeEnum {
    // method
    DEFAULT(MethodType.DEFAULT),

    // exception message
    EXCEPTION(MethodType.EXCEPTION),

    // information
    ANNOTATION(MethodType.ANNOTATION),

    // method parameter
    PARAMETER(MethodType.PARAMETER),

    // tomcat, jetty, bloc ...
    WEB_REQUEST(MethodType.WEB_REQUEST),

    // sync/async
    INVOCATION(MethodType.INVOCATION),

    // database, javascript

    // corrupted when : 1. slow network, 2. too much node ...
    CORRUPTED(MethodType.CORRUPTED);

    private final int code;

    MethodTypeEnum(int code) {
        this.code = code;
    }

    public int getCode() {
        return this.code;
    }

    public static MethodTypeEnum valueOf(int code) {
        for (MethodTypeEnum methodType : values()) {
            if (methodType.getCode() == code) {
                return methodType;
            }
        }
        throw new IllegalStateException("unknown MethodType:" + code);
    }
}
