/*
 * Copyright 2025 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.common.server.bo;

import com.navercorp.pinpoint.common.trace.MethodType;
import org.jspecify.annotations.Nullable;

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
        MethodTypeEnum methodTypeEnum = getMethodTypeEnum(code);
        if (methodTypeEnum == null) {
            throw new IllegalStateException("unknown MethodType:" + code);
        }
        return methodTypeEnum;
    }

    private static @Nullable MethodTypeEnum getMethodTypeEnum(int code) {
        return switch (code) {
            case MethodType.DEFAULT -> DEFAULT;
            case MethodType.EXCEPTION -> EXCEPTION;
            case MethodType.ANNOTATION -> ANNOTATION;
            case MethodType.PARAMETER -> PARAMETER;
            case MethodType.WEB_REQUEST -> WEB_REQUEST;
            case MethodType.INVOCATION -> INVOCATION;
            case MethodType.CORRUPTED -> CORRUPTED;
            default -> null;
        };
    }

    public static MethodTypeEnum defaultValueOf(int code) {
        MethodTypeEnum methodTypeEnum = getMethodTypeEnum(code);
        if (methodTypeEnum == null) {
            return DEFAULT;
        }
        return methodTypeEnum;
    }
}
