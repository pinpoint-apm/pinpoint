/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.util;


import com.navercorp.pinpoint.common.util.IntBooleanIntBooleanValue;
import com.navercorp.pinpoint.common.util.IntStringStringValue;
import com.navercorp.pinpoint.common.util.IntStringValue;
import com.navercorp.pinpoint.common.util.LongIntIntByteByteStringValue;
import com.navercorp.pinpoint.common.util.StringStringValue;
import com.navercorp.pinpoint.common.util.StringUtils;
import org.apache.thrift.TBase;

/**
 * @author emeroad
 */
public final class AnnotationValueMapper {

    private AnnotationValueMapper() {
    }

    public static Object checkValueType(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof String) {
            return value;
        } else if (value instanceof Integer) {
            return value;
        } else if (value instanceof Long) {
            return value;
        } else if (value instanceof Boolean) {
            return value;
        } else if (value instanceof Byte) {
            return value;
        } else if (value instanceof Float) {
            // thrift does not contain "float" typet
            return value;
        } else if (value instanceof Double) {
            return value;
        } else if (value instanceof byte[]) {
            return value;
        } else if (value instanceof Short) {
            return value;
        } else if (value instanceof IntStringValue) {
            return value;
        } else if (value instanceof IntStringStringValue) {
            return value;
        } else if (value instanceof LongIntIntByteByteStringValue) {
            return value;
        } else if (value instanceof IntBooleanIntBooleanValue) {
            return value;
        } else if (value instanceof StringStringValue) {
            return value;
        } else if (value instanceof TBase) {
            throw new IllegalArgumentException("TBase not supported. Class:" + value.getClass());
        }

        String str = StringUtils.abbreviate(value.toString());
        return str;
    }


}
