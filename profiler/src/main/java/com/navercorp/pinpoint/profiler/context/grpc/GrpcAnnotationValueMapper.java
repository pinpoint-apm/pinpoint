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

import com.google.protobuf.StringValue;
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

/**
 * WARNING Not thread safe
 * @author Woonduk Kang(emeroad)
 */
public class GrpcAnnotationValueMapper {

    private final PAnnotationValue.Builder annotationBuilder = PAnnotationValue.newBuilder();

    private final StringValue.Builder stringValueBuilder = StringValue.newBuilder();

    private final PIntBooleanIntBooleanValue.Builder intBoolBoolBuilder = PIntBooleanIntBooleanValue.newBuilder();

    private final PLongIntIntByteByteStringValue.Builder longIntIntByteByteStringBuilder = PLongIntIntByteByteStringValue.newBuilder();

    private final PIntStringStringValue.Builder intStringStringBuilder = PIntStringStringValue.newBuilder();

    private final PIntStringValue.Builder intStringBuilder = PIntStringValue.newBuilder();

    private final PStringStringValue.Builder stringStringBuilder = PStringStringValue.newBuilder();

    public PAnnotationValue buildPAnnotationValue(Annotation<?> annotation) {
        if (annotation == null) {
            throw new NullPointerException("annotation");
        }

        if (annotation instanceof GrpcAnnotationSerializable) {
            GrpcAnnotationSerializable serializable = (GrpcAnnotationSerializable) annotation;
            return serializable.apply(this);
        }
        throw new UnsupportedOperationException("unsupported annotation:" + annotation);
    }

    public PIntBooleanIntBooleanValue newIntBooleanIntBooleanValue(IntBooleanIntBooleanValue v) {
        final PIntBooleanIntBooleanValue.Builder builder = this.intBoolBoolBuilder;

        builder.setIntValue1(v.getIntValue1());
        builder.setBoolValue1(v.isBooleanValue1());
        builder.setIntValue2(v.getIntValue2());
        builder.setBoolValue2(v.isBooleanValue2());

        PIntBooleanIntBooleanValue value = builder.build();
        builder.clear();
        return value;
    }

    public PLongIntIntByteByteStringValue newLongIntIntByteByteStringValue(LongIntIntByteByteStringValue v) {
        final PLongIntIntByteByteStringValue.Builder builder = this.longIntIntByteByteStringBuilder;

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

        PLongIntIntByteByteStringValue value = builder.build();
        builder.clear();
        return value;
    }


    public PIntStringStringValue newIntStringStringValue(IntStringStringValue v) {
        final PIntStringStringValue.Builder builder = this.intStringStringBuilder;

        builder.setIntValue(v.getIntValue());
        if (v.getStringValue1() != null) {
            StringValue stringValue1 = newStringValue(v.getStringValue1());
            builder.setStringValue1(stringValue1);
        }
        if (v.getStringValue2() != null) {
            StringValue stringValue2 = newStringValue(v.getStringValue2());
            builder.setStringValue2(stringValue2);
        }
        PIntStringStringValue value = builder.build();
        builder.clear();
        return value;
    }

    public PIntStringValue newIntStringValue(IntStringValue v) {
        final PIntStringValue.Builder builder = this.intStringBuilder;

        builder.setIntValue(v.getIntValue());
        if (v.getStringValue() != null) {
            StringValue stringValue = newStringValue(v.getStringValue());
            builder.setStringValue(stringValue);
        }
        PIntStringValue value = builder.build();
        builder.clear();
        return value;
    }

    public PStringStringValue newStringStringValue(StringStringValue v) {
        final PStringStringValue.Builder builder = this.stringStringBuilder;

        if (v.getStringValue1() != null) {
            StringValue stringValue1 = newStringValue(v.getStringValue1());
            builder.setStringValue1(stringValue1);
        }

        if (v.getStringValue2() != null) {
            StringValue stringValue2 = newStringValue(v.getStringValue2());
            builder.setStringValue2(stringValue2);
        }
        PStringStringValue value = builder.build();
        builder.clear();
        return value;
    }

    public StringValue newStringValue(String stringValue) {
        final StringValue.Builder builder = this.stringValueBuilder;

        builder.setValue(stringValue);
        StringValue value = builder.build();
        builder.clear();
        return value;
    }

    public PAnnotationValue.Builder getAnnotationBuilder() {
        final PAnnotationValue.Builder builder = this.annotationBuilder;
        builder.clear();
        return builder;
    }
}
