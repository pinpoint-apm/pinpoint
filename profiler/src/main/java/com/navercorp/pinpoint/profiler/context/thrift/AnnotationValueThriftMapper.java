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

import com.navercorp.pinpoint.common.util.DataType;
import com.navercorp.pinpoint.common.util.IntBooleanIntBooleanValue;
import com.navercorp.pinpoint.common.util.IntStringStringValue;
import com.navercorp.pinpoint.common.util.IntStringValue;
import com.navercorp.pinpoint.common.util.LongIntIntByteByteStringValue;
import com.navercorp.pinpoint.common.util.StringStringValue;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.profiler.context.Annotation;
import com.navercorp.pinpoint.profiler.context.annotation.BooleanAnnotation;
import com.navercorp.pinpoint.profiler.context.annotation.ByteAnnotation;
import com.navercorp.pinpoint.profiler.context.annotation.BytesAnnotation;
import com.navercorp.pinpoint.profiler.context.annotation.DataTypeAnnotation;
import com.navercorp.pinpoint.profiler.context.annotation.DoubleAnnotation;
import com.navercorp.pinpoint.profiler.context.annotation.IntAnnotation;
import com.navercorp.pinpoint.profiler.context.annotation.LongAnnotation;
import com.navercorp.pinpoint.profiler.context.annotation.ShortAnnotation;
import com.navercorp.pinpoint.profiler.context.annotation.StringAnnotation;
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
    public static TAnnotationValue buildTAnnotationValue(Annotation<?> ano) {
        if (ano == null) {
            return null;
        }

        if (ano instanceof IntAnnotation) {
            return TAnnotationValue.intValue(((IntAnnotation) ano).intValue());
        } else if (ano instanceof LongAnnotation) {
            return TAnnotationValue.longValue(((LongAnnotation) ano).longValue());
        } else if (ano instanceof DoubleAnnotation) {
            return TAnnotationValue.doubleValue(((DoubleAnnotation) ano).doubleValue());
        } else if (ano instanceof ShortAnnotation) {
            return TAnnotationValue.shortValue(((ShortAnnotation) ano).shortValue());
        } else if (ano instanceof ByteAnnotation) {
            return TAnnotationValue.byteValue(((ByteAnnotation) ano).byteValue());
        }

        if (ano instanceof StringAnnotation) {
            return TAnnotationValue.stringValue(((StringAnnotation) ano).stringValue());
        }
        if (ano instanceof BooleanAnnotation) {
            return TAnnotationValue.boolValue(((BooleanAnnotation) ano).booleanValue());
        }
        if (ano instanceof BytesAnnotation) {
            return TAnnotationValue.binaryValue(((BytesAnnotation) ano).bytesValue());
        }
        if (ano instanceof DataTypeAnnotation) {
            final DataType dataType = ((DataTypeAnnotation) ano).dataTypeValue();
            if (dataType instanceof IntStringValue) {
                final IntStringValue v = (IntStringValue) dataType;
                final TIntStringValue tIntStringValue = new TIntStringValue(v.getIntValue());
                if (v.getStringValue() != null) {
                    tIntStringValue.setStringValue(v.getStringValue());
                }
                return TAnnotationValue.intStringValue(tIntStringValue);
            } else if (dataType instanceof StringStringValue) {
                final StringStringValue v = (StringStringValue) dataType;
                final TStringStringValue tStringStringValue = new TStringStringValue(v.getStringValue1());
                if (v.getStringValue2() != null) {
                    tStringStringValue.setStringValue2(v.getStringValue2());
                }
                return TAnnotationValue.stringStringValue(tStringStringValue);
            } else if (dataType instanceof IntStringStringValue) {
                final IntStringStringValue v = (IntStringStringValue) dataType;
                final TIntStringStringValue tIntStringStringValue = new TIntStringStringValue(v.getIntValue());
                if (v.getStringValue1() != null) {
                    tIntStringStringValue.setStringValue1(v.getStringValue1());
                }
                if (v.getStringValue2() != null) {
                    tIntStringStringValue.setStringValue2(v.getStringValue2());
                }
                return TAnnotationValue.intStringStringValue(tIntStringStringValue);
            } else if (dataType instanceof LongIntIntByteByteStringValue) {
                final LongIntIntByteByteStringValue v = (LongIntIntByteByteStringValue) dataType;
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
            } else if (dataType instanceof IntBooleanIntBooleanValue) {
                final IntBooleanIntBooleanValue v = (IntBooleanIntBooleanValue) dataType;
                final TIntBooleanIntBooleanValue tvalue = new TIntBooleanIntBooleanValue(v.getIntValue1(), v.isBooleanValue1(), v.getIntValue2(), v.isBooleanValue2());
                return TAnnotationValue.intBooleanIntBooleanValue(tvalue);
            }
        }

        if (ano instanceof TBase) {
            throw new IllegalArgumentException("TBase not supported. Class:" + ano.getClass());
        }
        String str = StringUtils.abbreviate(ano.toString());
        return TAnnotationValue.stringValue(str);
    }
}
