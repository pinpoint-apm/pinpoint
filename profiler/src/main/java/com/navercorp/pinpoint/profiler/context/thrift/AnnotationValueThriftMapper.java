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

package com.navercorp.pinpoint.profiler.context.thrift;

import com.navercorp.pinpoint.common.util.IntBooleanIntBooleanValue;
import com.navercorp.pinpoint.common.util.IntStringStringValue;
import com.navercorp.pinpoint.common.util.IntStringValue;
import com.navercorp.pinpoint.common.util.LongIntIntByteByteStringValue;
import com.navercorp.pinpoint.common.util.StringStringValue;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.thrift.dto.TAnnotationValue;
import com.navercorp.pinpoint.thrift.dto.TIntBooleanIntBooleanValue;
import com.navercorp.pinpoint.thrift.dto.TIntStringStringValue;
import com.navercorp.pinpoint.thrift.dto.TIntStringValue;
import com.navercorp.pinpoint.thrift.dto.TLongIntIntByteByteStringValue;
import com.navercorp.pinpoint.thrift.dto.TStringStringValue;
import org.apache.thrift.TBase;

/**
 * @author Woonduk Kang(emeroad)
 */
public class AnnotationValueThriftMapper {
    public static TAnnotationValue buildTAnnotationValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof String) {
            return TAnnotationValue.stringValue((String) value);
        }
        if (value instanceof Integer) {
            return TAnnotationValue.intValue((Integer) value);
        }
        if (value instanceof Long) {
            return TAnnotationValue.longValue((Long) value);
        }
        if (value instanceof Boolean) {
            return TAnnotationValue.boolValue((Boolean) value);
        }
        if (value instanceof Byte) {
            return TAnnotationValue.byteValue((Byte) value);
        }
        if (value instanceof Float) {
            // thrift does not contain "float" type
            return TAnnotationValue.doubleValue((Float) value);
        }
        if (value instanceof Double) {
            return TAnnotationValue.doubleValue((Double) value);
        }
        if (value instanceof byte[]) {
            return TAnnotationValue.binaryValue((byte[]) value);
        }
        if (value instanceof Short) {
            return TAnnotationValue.shortValue((Short) value);
        }
        if (value instanceof IntStringValue) {
            final IntStringValue v = (IntStringValue) value;
            final TIntStringValue tIntStringValue = new TIntStringValue(v.getIntValue());
            if (v.getStringValue() != null) {
                tIntStringValue.setStringValue(v.getStringValue());
            }
            return TAnnotationValue.intStringValue(tIntStringValue);
        }
        if (value instanceof StringStringValue) {
            final StringStringValue v = (StringStringValue) value;
            final TStringStringValue tStringStringValue = new TStringStringValue(v.getStringValue1());
            if (v.getStringValue2() != null) {
                tStringStringValue.setStringValue2(v.getStringValue2());
            }
            return TAnnotationValue.stringStringValue(tStringStringValue);
        }
        if (value instanceof IntStringStringValue) {
            final IntStringStringValue v = (IntStringStringValue) value;
            final TIntStringStringValue tIntStringStringValue = new TIntStringStringValue(v.getIntValue());
            if (v.getStringValue1() != null) {
                tIntStringStringValue.setStringValue1(v.getStringValue1());
            }
            if (v.getStringValue2() != null) {
                tIntStringStringValue.setStringValue2(v.getStringValue2());
            }
            return TAnnotationValue.intStringStringValue(tIntStringStringValue);
        }
        if (value instanceof LongIntIntByteByteStringValue) {
            final LongIntIntByteByteStringValue v = (LongIntIntByteByteStringValue) value;
            final TLongIntIntByteByteStringValue tvalue = new TLongIntIntByteByteStringValue(v.getLongValue(), v.getIntValue1());
            if (v.getIntValue2() != -1) {
                tvalue.setIntValue2(v.getIntValue2());
            }
            if (v.getByteValue1() != -1) {
                tvalue.setByteValue1(v.getByteValue1());
            }
            if (v.getByteValue2() != -1) {
                tvalue.setByteValue2(v.getByteValue2());
            }
            if (v.getStringValue() != null) {
                tvalue.setStringValue(v.getStringValue());
            }
            return TAnnotationValue.longIntIntByteByteStringValue(tvalue);
        }
        if (value instanceof IntBooleanIntBooleanValue) {
            final IntBooleanIntBooleanValue v = (IntBooleanIntBooleanValue) value;
            final TIntBooleanIntBooleanValue tvalue = new TIntBooleanIntBooleanValue(v.getIntValue1(), v.isBooleanValue1(), v.getIntValue2(), v.isBooleanValue2());
            return TAnnotationValue.intBooleanIntBooleanValue(tvalue);
        }
        if (value instanceof TBase) {
            throw new IllegalArgumentException("TBase not supported. Class:" + value.getClass());
        }
        String str = StringUtils.abbreviate(value.toString());
        return TAnnotationValue.stringValue(str);
    }
}
