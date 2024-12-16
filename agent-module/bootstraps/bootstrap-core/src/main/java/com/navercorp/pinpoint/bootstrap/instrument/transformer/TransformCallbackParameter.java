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

import java.util.Objects;

/**
 * @author youngjin.kim2
 */
public class TransformCallbackParameter {

    private final Object value;
    private final Class<?> type;

    public static TransformCallbackParameter of(Boolean value) {
        return new TransformCallbackParameter(value, Boolean.class);
    }

    public static TransformCallbackParameter of(Long value) {
        return new TransformCallbackParameter(value, Long.class);
    }

    public static TransformCallbackParameter of(Double value) {
        return new TransformCallbackParameter(value, Double.class);
    }

    public static TransformCallbackParameter of(String value) {
        return new TransformCallbackParameter(value, String.class);
    }

    public static TransformCallbackParameter of(String[] value) {
        return new TransformCallbackParameter(value, String[].class);
    }

    public static TransformCallbackParameter of(String[][] value) {
        return new TransformCallbackParameter(value, String[][].class);
    }

    public static TransformCallbackParameter of(ServiceType value) {
        return new TransformCallbackParameter(value, ServiceType.class);
    }

    public static TransformCallbackParameter of(Object value) {
        Objects.requireNonNull(value, "value");
        return new TransformCallbackParameter(value, value.getClass());
    }

    TransformCallbackParameter(Object value, Class<?> type) {
        this.value = value;
        this.type = Objects.requireNonNull(type, "type");
    }

    Object getValue() {
        return value;
    }

    Class<?> getType() {
        return type;
    }

}
