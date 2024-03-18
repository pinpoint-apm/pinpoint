/*
 * Copyright 2024 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.bootstrap.instrument.transformer;

import com.navercorp.pinpoint.common.trace.ServiceType;

/**
 * @author youngjin.kim2
 */
public class TransformCallbackParameter {

    private final Object value;
    private final ParameterType type;

    public TransformCallbackParameter(Boolean value) {
        this.value = value;
        this.type = ParameterType.BOOLEAN;
    }

    public TransformCallbackParameter(Long value) {
        this.value = value;
        this.type = ParameterType.LONG;
    }

    public TransformCallbackParameter(Double value) {
        this.value = value;
        this.type = ParameterType.DOUBLE;
    }

    public TransformCallbackParameter(String value) {
        this.value = value;
        this.type = ParameterType.STRING;
    }

    public TransformCallbackParameter(String[] value) {
        this.value = value;
        this.type = ParameterType.STRING_ARRAY;
    }

    public TransformCallbackParameter(String[][] value) {
        this.value = value;
        this.type = ParameterType.STRING_ARRAY_ARRAY;
    }

    public TransformCallbackParameter(ServiceType value) {
        this.value = value;
        this.type = ParameterType.SERVICE_TYPE;
    }

    Object getValue() {
        return value;
    }

    ParameterType getType() {
        return type;
    }

    public enum ParameterType {
        BOOLEAN,
        LONG,
        DOUBLE,
        STRING,
        STRING_ARRAY,
        STRING_ARRAY_ARRAY,
        SERVICE_TYPE,
        ;

        public Class<?> getJavaClass() {
            switch (this) {
                case BOOLEAN:
                    return Boolean.class;
                case SERVICE_TYPE:
                    return ServiceType.class;
                case LONG:
                    return Long.class;
                case DOUBLE:
                    return Double.class;
                case STRING:
                    return String.class;
                case STRING_ARRAY:
                    return String[].class;
                case STRING_ARRAY_ARRAY:
                    return String[][].class;
                default:
                    throw new IllegalArgumentException("Unknown type: " + this);
            }
        }
    }

}
