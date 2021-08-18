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

package com.navercorp.pinpoint.profiler.context.grpc;

import com.google.protobuf.ByteString;
import com.google.protobuf.StringValue;
import com.navercorp.pinpoint.common.util.DataType;
import com.navercorp.pinpoint.common.util.IntBooleanIntBooleanValue;
import com.navercorp.pinpoint.common.util.IntStringStringValue;
import com.navercorp.pinpoint.common.util.IntStringValue;
import com.navercorp.pinpoint.common.util.LongIntIntByteByteStringValue;
import com.navercorp.pinpoint.common.util.StringStringValue;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.grpc.trace.PAnnotationValue;
import com.navercorp.pinpoint.grpc.trace.PIntBooleanIntBooleanValue;
import com.navercorp.pinpoint.grpc.trace.PIntStringStringValue;
import com.navercorp.pinpoint.grpc.trace.PIntStringValue;
import com.navercorp.pinpoint.grpc.trace.PLongIntIntByteByteStringValue;
import com.navercorp.pinpoint.grpc.trace.PStringStringValue;
import com.navercorp.pinpoint.profiler.context.Annotation;
import com.navercorp.pinpoint.profiler.context.annotation.BooleanAnnotation;
import com.navercorp.pinpoint.profiler.context.annotation.ByteAnnotation;
import com.navercorp.pinpoint.profiler.context.annotation.BytesAnnotation;
import com.navercorp.pinpoint.profiler.context.annotation.DataTypeAnnotation;
import com.navercorp.pinpoint.profiler.context.annotation.DoubleAnnotation;
import com.navercorp.pinpoint.profiler.context.annotation.IntAnnotation;
import com.navercorp.pinpoint.profiler.context.annotation.LongAnnotation;
import com.navercorp.pinpoint.profiler.context.annotation.NullAnnotation;
import com.navercorp.pinpoint.profiler.context.annotation.ShortAnnotation;
import com.navercorp.pinpoint.profiler.context.annotation.StringAnnotation;
import com.navercorp.pinpoint.profiler.context.annotation.ObjectAnnotation;
import org.apache.thrift.TBase;

import java.util.Objects;

/**
 * WARNING Not thread safe
 * @author Woonduk Kang(emeroad)
 */
public class GrpcAnnotationValueMapper {

    private final PAnnotationValue.Builder annotationBuilder = PAnnotationValue.newBuilder();
    private final StringValue.Builder stringValueBuilder = StringValue.newBuilder();

    public PAnnotationValue buildPAnnotationValue(Annotation<?> ano) {
        if (ano == null) {
            throw new NullPointerException("annotation");
        }

        if (ano instanceof NullAnnotation) {
            return null;
        }
        if (ano instanceof StringAnnotation) {
            PAnnotationValue.Builder builder = getAnnotationBuilder();
            builder.setStringValue(((StringAnnotation) ano).stringValue());
            return builder.build();
        }

        if (ano instanceof IntAnnotation) {
            PAnnotationValue.Builder builder = getAnnotationBuilder();
            builder.setIntValue(((IntAnnotation) ano).intValue());
            return builder.build();
        } else if (ano instanceof LongAnnotation) {
            PAnnotationValue.Builder builder = getAnnotationBuilder();
            builder.setLongValue(((LongAnnotation) ano).longValue());
            return builder.build();
        } else if (ano instanceof DoubleAnnotation) {
            PAnnotationValue.Builder builder = getAnnotationBuilder();
            builder.setDoubleValue(((DoubleAnnotation) ano).doubleValue());
            return builder.build();
        } else if (ano instanceof ShortAnnotation) {
            PAnnotationValue.Builder builder = getAnnotationBuilder();
            builder.setShortValue(((ShortAnnotation) ano).shortValue());
            return builder.build();
        } else if (ano instanceof ByteAnnotation) {
            PAnnotationValue.Builder builder = getAnnotationBuilder();
            builder.setByteValue(((ByteAnnotation) ano).byteValue());
            return builder.build();
        }

        if (ano instanceof BooleanAnnotation) {
            PAnnotationValue.Builder builder = getAnnotationBuilder();
            builder.setBoolValue(((BooleanAnnotation) ano).booleanValue());
            return builder.build();
        }
        if (ano instanceof BytesAnnotation) {
            PAnnotationValue.Builder builder = getAnnotationBuilder();
            byte[] bytes = ((BytesAnnotation) ano).bytesValue();
            builder.setBinaryValue(ByteString.copyFrom(bytes));
            return builder.build();
        }

        if (ano instanceof DataTypeAnnotation) {
            DataType dataType = ((DataTypeAnnotation) ano).dataTypeValue();
            if (dataType instanceof IntStringValue) {
                final IntStringValue v = (IntStringValue) dataType;
                PIntStringValue pIntStringValue = newIntStringValue(v);

                PAnnotationValue.Builder builder = getAnnotationBuilder();
                builder.setIntStringValue(pIntStringValue);

                return builder.build();
            } else if (dataType instanceof StringStringValue) {
                final StringStringValue v = (StringStringValue) dataType;
                PStringStringValue pStringStringValue = newStringStringValue(v);

                PAnnotationValue.Builder builder = getAnnotationBuilder();
                builder.setStringStringValue(pStringStringValue);
                return builder.build();
            } else if (dataType instanceof IntStringStringValue) {
                final IntStringStringValue v = (IntStringStringValue) dataType;
                final PIntStringStringValue pIntStringStringValue = newIntStringStringValue(v);
                PAnnotationValue.Builder builder = getAnnotationBuilder();
                builder.setIntStringStringValue(pIntStringStringValue);
                return builder.build();
            } else if (dataType instanceof LongIntIntByteByteStringValue) {
                final LongIntIntByteByteStringValue v = (LongIntIntByteByteStringValue) dataType;
                final PLongIntIntByteByteStringValue pValue = newLongIntIntByteByteStringValue(v);

                PAnnotationValue.Builder builder = getAnnotationBuilder();
                builder.setLongIntIntByteByteStringValue(pValue);
                return builder.build();
            } else if (dataType instanceof IntBooleanIntBooleanValue) {
                final IntBooleanIntBooleanValue v = (IntBooleanIntBooleanValue) dataType;
                final PIntBooleanIntBooleanValue pValue = newIntBooleanIntBooleanValue(v);
                PAnnotationValue.Builder builder = getAnnotationBuilder();
                builder.setIntBooleanIntBooleanValue(pValue);
                return builder.build();
            }
        }
        if (ano instanceof ObjectAnnotation) {
            String str = ((ObjectAnnotation)ano).unknownValue();
            PAnnotationValue.Builder builder = getAnnotationBuilder();
            builder.setStringValue(str);
            return builder.build();
        }

        if (ano instanceof TBase) {
            throw new IllegalArgumentException("TBase not supported. Class:" + ano.getClass());
        }

        String str = StringUtils.abbreviate(Objects.toString(ano.getValue()));
        PAnnotationValue.Builder builder = getAnnotationBuilder();
        builder.setStringValue(str);
        return builder.build();
    }

    private PIntBooleanIntBooleanValue newIntBooleanIntBooleanValue(IntBooleanIntBooleanValue v) {
        PIntBooleanIntBooleanValue.Builder builder = PIntBooleanIntBooleanValue.newBuilder();
        builder.setIntValue1(v.getIntValue1());
        builder.setBoolValue1(v.isBooleanValue1());
        builder.setIntValue2(v.getIntValue2());
        builder.setBoolValue2(v.isBooleanValue2());
        return builder.build();
    }

    private PLongIntIntByteByteStringValue newLongIntIntByteByteStringValue(LongIntIntByteByteStringValue v) {
        final PLongIntIntByteByteStringValue.Builder builder = PLongIntIntByteByteStringValue.newBuilder();
        builder.setLongValue(v.getLongValue());
        builder.setIntValue1(v.getIntValue1());
        if (v.getIntValue2() != -1) {
            builder.setIntValue2(v.getIntValue2());
        }
        if (v.getByteValue1() != -1) {
            builder.setByteValue1(v.getByteValue1());
        }
        if (v.getByteValue2() != -1) {
            builder.setByteValue2(v.getByteValue2());
        }
        if (v.getStringValue() != null) {
            StringValue stringValue = newStringValue(v.getStringValue());
            builder.setStringValue(stringValue);
        }
        return builder.build();
    }


    private PIntStringStringValue newIntStringStringValue(IntStringStringValue v) {
        final PIntStringStringValue.Builder builder = PIntStringStringValue.newBuilder();
        builder.setIntValue(v.getIntValue());
        if (v.getStringValue1() != null) {
            StringValue stringValue1 = newStringValue(v.getStringValue1());
            builder.setStringValue1(stringValue1);
        }
        if (v.getStringValue2() != null) {
            StringValue stringValue2 = newStringValue(v.getStringValue2());
            builder.setStringValue2(stringValue2);
        }
        return builder.build();
    }

    private PIntStringValue newIntStringValue(IntStringValue v) {
        PIntStringValue.Builder valueBuilder = PIntStringValue.newBuilder();
        valueBuilder.setIntValue(v.getIntValue());
        if (v.getStringValue() != null) {
            StringValue stringValue = newStringValue(v.getStringValue());
            valueBuilder.setStringValue(stringValue);
        }
        return valueBuilder.build();
    }

    private PStringStringValue newStringStringValue(StringStringValue v) {
        PStringStringValue.Builder builder = PStringStringValue.newBuilder();
        if (v.getStringValue1() != null) {
            StringValue stringValue1 = newStringValue(v.getStringValue1());
            builder.setStringValue1(stringValue1);
        }

        if (v.getStringValue2() != null) {
            StringValue stringValue2 = newStringValue(v.getStringValue2());
            builder.setStringValue2(stringValue2);
        }
        return builder.build();
    }

    private StringValue newStringValue(String stringValue) {
        final StringValue.Builder builder = this.stringValueBuilder;
        builder.clear();
        builder.setValue(stringValue);
        return builder.build();
    }

    private PAnnotationValue.Builder getAnnotationBuilder() {
        final PAnnotationValue.Builder builder = this.annotationBuilder;
        builder.clear();
        return builder;
    }
}
