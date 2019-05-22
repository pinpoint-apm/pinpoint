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

package com.navercorp.pinpoint.common.server.bo.grpc;

import com.google.protobuf.Descriptors;
import com.navercorp.pinpoint.common.server.bo.AnnotationFactory;
import com.navercorp.pinpoint.common.util.IntBooleanIntBooleanValue;
import com.navercorp.pinpoint.common.util.IntStringStringValue;
import com.navercorp.pinpoint.common.util.IntStringValue;
import com.navercorp.pinpoint.common.util.LongIntIntByteByteStringValue;
import com.navercorp.pinpoint.common.util.StringStringValue;
import com.navercorp.pinpoint.grpc.trace.PAnnotation;
import com.navercorp.pinpoint.grpc.trace.PAnnotationValue;
import com.navercorp.pinpoint.grpc.trace.PIntBooleanIntBooleanValue;
import com.navercorp.pinpoint.grpc.trace.PIntStringStringValue;
import com.navercorp.pinpoint.grpc.trace.PIntStringValue;
import com.navercorp.pinpoint.grpc.trace.PLongIntIntByteByteStringValue;
import com.navercorp.pinpoint.grpc.trace.PStringStringValue;

/**
 * @author Woonduk Kang(emeroad)
 */
public class GrpcAnnotationHandler implements AnnotationFactory.AnnotationTypeHandler<PAnnotation> {

    @Override
    public int getKey(PAnnotation annotation) {
        return annotation.getKey();
    }

    @Override
    public Object getValue(PAnnotation annotation) {
        if (!annotation.hasValue()) {
            return null;
        }
        final PAnnotationValue value = annotation.getValue();

        Descriptors.Descriptor descriptorForType = value.getDescriptorForType();
        int number = value.getFieldCase().getNumber();
        Descriptors.FieldDescriptor fieldByNumber = descriptorForType.findFieldByNumber(number);
        return value.getField(fieldByNumber);
    }


    @Override
    public Object buildCustomAnnotationValue(Object annotationValue) {
        if (annotationValue instanceof PIntStringValue) {
            return newIntStringValue(annotationValue);
        } else if (annotationValue instanceof PIntStringStringValue) {
            return newIntStringString(annotationValue);
        } else if (annotationValue instanceof PStringStringValue) {
            return newStringStringValue(annotationValue);
        } else if (annotationValue instanceof PLongIntIntByteByteStringValue) {
            return newLongIntIntByteByteStringValue(annotationValue);
        } else if (annotationValue instanceof PIntBooleanIntBooleanValue) {
            return newIntBooleanIntBooleanValue(annotationValue);
        }
        return null;
    }


    private IntStringValue newIntStringValue(Object annotationValue) {
        PIntStringValue tValue = (PIntStringValue) annotationValue;
        return new IntStringValue(tValue.getIntValue(), tValue.getStringValue());
    }

    private IntStringStringValue newIntStringString(Object annotationValue) {
        PIntStringStringValue tValue = (PIntStringStringValue) annotationValue;
        return new IntStringStringValue(tValue.getIntValue(), tValue.getStringValue1(), tValue.getStringValue2());
    }

    private StringStringValue newStringStringValue(Object annotationValue) {
        PStringStringValue tValue = (PStringStringValue) annotationValue;
        return new StringStringValue(tValue.getStringValue1(), tValue.getStringValue2());
    }

    private IntBooleanIntBooleanValue newIntBooleanIntBooleanValue(Object annotationValue) {
        PIntBooleanIntBooleanValue tValue = (PIntBooleanIntBooleanValue) annotationValue;
        return new IntBooleanIntBooleanValue(tValue.getIntValue1(), tValue.getBoolValue1(),
                tValue.getIntValue2(), tValue.getBoolValue2());
    }

    private LongIntIntByteByteStringValue newLongIntIntByteByteStringValue(Object annotationValue) {
        PLongIntIntByteByteStringValue tValue = (PLongIntIntByteByteStringValue) annotationValue;
        return new LongIntIntByteByteStringValue(tValue.getLongValue(), tValue.getIntValue1(), tValue.getIntValue2(),
                (byte)tValue.getByteValue1(), (byte)tValue.getByteValue2(), tValue.getStringValue());
    }
}
