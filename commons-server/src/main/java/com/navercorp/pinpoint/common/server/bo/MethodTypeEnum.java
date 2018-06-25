package com.navercorp.pinpoint.common.server.bo;

import com.navercorp.pinpoint.common.trace.MethodType;
import com.navercorp.pinpoint.common.util.apache.IntHashMap;

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

    private static final IntHashMap<MethodTypeEnum> METHOD_TYPE_MAP = toMethodTypeMap();

    private static IntHashMap<MethodTypeEnum> toMethodTypeMap() {
        IntHashMap<MethodTypeEnum> methodTypeEnumMap = new IntHashMap<MethodTypeEnum>();
        for (MethodTypeEnum methodType : values()) {
            methodTypeEnumMap.put(methodType.getCode(), methodType);
        }
        return methodTypeEnumMap;
    }


    MethodTypeEnum(int code) {
        this.code = code;
    }

    public int getCode() {
        return this.code;
    }

    public static MethodTypeEnum valueOf(int code) {
        final MethodTypeEnum methodTypeEnum = METHOD_TYPE_MAP.get(code);
        if (methodTypeEnum == null) {
            throw new IllegalStateException("unknown MethodType:" + code);
        }
        return methodTypeEnum;
    }
}
