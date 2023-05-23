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

/**
 * @author emeroad
 */
public class DataTypeAnnotation implements Annotation<DataType>,
        GrpcAnnotationSerializable {

    private final int key;
    private final DataType value;


    DataTypeAnnotation(int key, DataType value) {
        this.key = key;
        this.value = value;
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
    public String toString() {
        return "DataTypeAnnotation{" +
                key + "=" + value +
                '}';
    }
}
