/*
 * Copyright 2021 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.context.annotation;

import com.navercorp.pinpoint.common.util.DataType;
import com.navercorp.pinpoint.common.util.IntBooleanIntBooleanValue;
import com.navercorp.pinpoint.common.util.IntStringStringValue;
import com.navercorp.pinpoint.common.util.IntStringValue;
import com.navercorp.pinpoint.common.util.LongIntIntByteByteStringValue;
import com.navercorp.pinpoint.common.util.StringStringValue;
import com.navercorp.pinpoint.grpc.trace.PAnnotationValue;
import com.navercorp.pinpoint.grpc.trace.PIntBooleanIntBooleanValue;
import com.navercorp.pinpoint.grpc.trace.PIntStringStringValue;
import com.navercorp.pinpoint.grpc.trace.PIntStringValue;
import com.navercorp.pinpoint.grpc.trace.PLongIntIntByteByteStringValue;
import com.navercorp.pinpoint.grpc.trace.PStringStringValue;
import com.navercorp.pinpoint.profiler.context.Annotation;
import com.navercorp.pinpoint.profiler.context.grpc.GrpcAnnotationSerializable;
import com.navercorp.pinpoint.profiler.context.grpc.GrpcAnnotationValueMapper;
import com.navercorp.pinpoint.profiler.context.thrift.AnnotationValueThriftMapper;
import com.navercorp.pinpoint.profiler.context.thrift.ThriftAnnotationSerializable;
import com.navercorp.pinpoint.thrift.dto.TAnnotationValue;
import com.navercorp.pinpoint.thrift.dto.TIntBooleanIntBooleanValue;
import com.navercorp.pinpoint.thrift.dto.TIntStringStringValue;
import com.navercorp.pinpoint.thrift.dto.TIntStringValue;
import com.navercorp.pinpoint.thrift.dto.TLongIntIntByteByteStringValue;
import com.navercorp.pinpoint.thrift.dto.TStringStringValue;

/**
 * @author emeroad
 */
public class DataTypeAnnotation implements Annotation<DataType>,
        GrpcAnnotationSerializable, ThriftAnnotationSerializable {

    private final int key;
    private final DataType value;


    DataTypeAnnotation(int key, DataType value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public int getAnnotationKey() {
        return getKey();
    }

    @Override
    public int getKey() {
        return key;
    }

    @Override
    public DataType getValue() {
        return value;
    }

    @Override
    public PAnnotationValue apply(GrpcAnnotationValueMapper context) {
        PAnnotationValue.Builder builder = context.getAnnotationBuilder();

        final DataType dataType = this.value;
        if (dataType instanceof IntStringValue) {
            final IntStringValue v = (IntStringValue) dataType;
            PIntStringValue pIntStringValue = context.newIntStringValue(v);
            builder.setIntStringValue(pIntStringValue);
            return builder.build();
        } else if (dataType instanceof StringStringValue) {
            final StringStringValue v = (StringStringValue) dataType;
            PStringStringValue pStringStringValue = context.newStringStringValue(v);
            builder.setStringStringValue(pStringStringValue);
            return builder.build();
        } else if (dataType instanceof IntStringStringValue) {
            final IntStringStringValue v = (IntStringStringValue) dataType;
            final PIntStringStringValue pIntStringStringValue = context.newIntStringStringValue(v);
            builder.setIntStringStringValue(pIntStringStringValue);
            return builder.build();
        } else if (dataType instanceof LongIntIntByteByteStringValue) {
            final LongIntIntByteByteStringValue v = (LongIntIntByteByteStringValue) dataType;
            final PLongIntIntByteByteStringValue pValue = context.newLongIntIntByteByteStringValue(v);
            builder.setLongIntIntByteByteStringValue(pValue);
            return builder.build();
        } else if (dataType instanceof IntBooleanIntBooleanValue) {
            final IntBooleanIntBooleanValue v = (IntBooleanIntBooleanValue) dataType;
            final PIntBooleanIntBooleanValue pValue = context.newIntBooleanIntBooleanValue(v);
            builder.setIntBooleanIntBooleanValue(pValue);
            return builder.build();
        }
        throw new UnsupportedOperationException("unsupported type:" + dataType);
    }

    @Override
    public TAnnotationValue apply(AnnotationValueThriftMapper context) {
        final DataType dataType = this.value;
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
        throw new UnsupportedOperationException("unsupported type:" + dataType);
    }

    @Override
    public String toString() {
        return "DataTypeAnnotation{" +
                key + "=" + value +
                '}';
    }
}
