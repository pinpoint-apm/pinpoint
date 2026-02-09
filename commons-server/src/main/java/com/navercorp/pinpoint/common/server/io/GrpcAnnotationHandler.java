/*
 * Copyright 2025 NAVER Corp.
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

package com.navercorp.pinpoint.common.server.io;

import com.google.protobuf.ByteString;
import com.navercorp.pinpoint.common.server.annotation.BinaryAnnotationBo;
import com.navercorp.pinpoint.common.server.annotation.BooleanAnnotationBo;
import com.navercorp.pinpoint.common.server.annotation.ByteAnnotationBo;
import com.navercorp.pinpoint.common.server.annotation.DataTypeAnnotationBo;
import com.navercorp.pinpoint.common.server.annotation.DoubleAnnotationBo;
import com.navercorp.pinpoint.common.server.annotation.IntAnnotationBo;
import com.navercorp.pinpoint.common.server.annotation.LongAnnotationBo;
import com.navercorp.pinpoint.common.server.annotation.NullAnnotationBo;
import com.navercorp.pinpoint.common.server.annotation.ShortAnnotationBo;
import com.navercorp.pinpoint.common.server.annotation.StringAnnotationBo;
import com.navercorp.pinpoint.common.server.bo.AnnotationBo;
import com.navercorp.pinpoint.common.server.bo.AnnotationFactory;
import com.navercorp.pinpoint.common.util.BytesStringStringValue;
import com.navercorp.pinpoint.common.util.IntBooleanIntBooleanValue;
import com.navercorp.pinpoint.common.util.IntStringStringValue;
import com.navercorp.pinpoint.common.util.IntStringValue;
import com.navercorp.pinpoint.common.util.LongIntIntByteByteStringValue;
import com.navercorp.pinpoint.common.util.StringStringValue;
import com.navercorp.pinpoint.grpc.trace.PAnnotation;
import com.navercorp.pinpoint.grpc.trace.PAnnotationValue;
import com.navercorp.pinpoint.grpc.trace.PBytesStringStringValue;
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
        PAnnotationValue.FieldCase fieldCase = value.getFieldCase();
        return switch (fieldCase) {
            case STRINGVALUE -> value.getStringValue();
            case BOOLVALUE -> value.getBoolValue();
            case INTVALUE -> value.getIntValue();
            case LONGVALUE -> value.getLongValue();
            case SHORTVALUE -> value.getShortValue();
            case DOUBLEVALUE -> value.getDoubleValue();
            case BINARYVALUE -> value.getBinaryValue();
            case BYTEVALUE -> value.getByteValue();
            case INTSTRINGVALUE -> value.getIntStringValue();
            case STRINGSTRINGVALUE -> value.getStringStringValue();
            case INTSTRINGSTRINGVALUE -> value.getIntStringStringValue();
            case LONGINTINTBYTEBYTESTRINGVALUE -> value.getLongIntIntByteByteStringValue();
            case INTBOOLEANINTBOOLEANVALUE -> value.getIntBooleanIntBooleanValue();
            case BYTESSTRINGSTRINGVALUE -> value.getBytesStringStringValue();
            case FIELD_NOT_SET -> null;
        };
    }

    @Override
    public AnnotationBo getAnnotation(PAnnotation annotation) {
        if (!annotation.hasValue()) {
            return new NullAnnotationBo(annotation.getKey());
        }
        final int key = annotation.getKey();

        final PAnnotationValue value = annotation.getValue();
        PAnnotationValue.FieldCase fieldCase = value.getFieldCase();
        return switch (fieldCase) {
            case STRINGVALUE -> new StringAnnotationBo(key, value.getStringValue());
            case BOOLVALUE -> new BooleanAnnotationBo(key, value.getBoolValue());
            case INTVALUE -> new IntAnnotationBo(key, value.getIntValue());
            case LONGVALUE -> new LongAnnotationBo(key, value.getLongValue());
            case SHORTVALUE -> new ShortAnnotationBo(key, (short) value.getShortValue());
            case DOUBLEVALUE -> new DoubleAnnotationBo(key, value.getDoubleValue());
            case BINARYVALUE -> new BinaryAnnotationBo(key, value.getBinaryValue().toByteArray());
            case BYTEVALUE -> new ByteAnnotationBo(key, (byte) value.getByteValue());
            // DataType ---------------
            case INTSTRINGVALUE -> new DataTypeAnnotationBo(key, newIntStringValue(value.getIntStringValue()));
            case STRINGSTRINGVALUE -> new DataTypeAnnotationBo(key, newStringStringValue(value.getStringStringValue()));
            case INTSTRINGSTRINGVALUE -> new DataTypeAnnotationBo(key, newIntStringString(value.getIntStringStringValue()));
            case LONGINTINTBYTEBYTESTRINGVALUE -> new DataTypeAnnotationBo(key, newLongIntIntByteByteStringValue(value.getLongIntIntByteByteStringValue()));
            case INTBOOLEANINTBOOLEANVALUE -> new DataTypeAnnotationBo(key, newIntBooleanIntBooleanValue(value.getIntBooleanIntBooleanValue()));
            case BYTESSTRINGSTRINGVALUE -> new DataTypeAnnotationBo(key, newBytesStringString(value.getBytesStringStringValue()));
            // -----------------
            case FIELD_NOT_SET -> new NullAnnotationBo(key);
        };
    }


    @Override
    public Object buildCustomAnnotationValue(Object annotationValue) {
        if (annotationValue instanceof ByteString byteString) {
            return byteString.toByteArray();
        }

        if (annotationValue instanceof PIntStringValue pValue) {
            return newIntStringValue(pValue);
        } else if (annotationValue instanceof PIntStringStringValue pValue) {
            return newIntStringString(pValue);
        } else if (annotationValue instanceof PStringStringValue pValue) {
            return newStringStringValue(pValue);
        } else if (annotationValue instanceof PLongIntIntByteByteStringValue pValue) {
            return newLongIntIntByteByteStringValue(pValue);
        } else if (annotationValue instanceof PIntBooleanIntBooleanValue pValue) {
            return newIntBooleanIntBooleanValue(pValue);
        } else if (annotationValue instanceof PBytesStringStringValue pValue) {
            return newBytesStringString(pValue);
        }
        return null;
    }


    private IntStringValue newIntStringValue(PIntStringValue pValue) {
        String stringValue = null;
        if (pValue.hasStringValue()) {
            stringValue = pValue.getStringValue().getValue();
        }
        return new IntStringValue(pValue.getIntValue(), stringValue);
    }

    private IntStringStringValue newIntStringString(PIntStringStringValue pValue) {
        String stringValue1 = null;
        if (pValue.hasStringValue1()) {
            stringValue1 = pValue.getStringValue1().getValue();
        }
        String stringValue2 = null;
        if (pValue.hasStringValue2()) {
            stringValue2 = pValue.getStringValue2().getValue();
        }
        return new IntStringStringValue(pValue.getIntValue(), stringValue1, stringValue2);
    }

    private BytesStringStringValue newBytesStringString(PBytesStringStringValue pValue) {
        String stringValue1 = null;
        if (pValue.hasStringValue1()) {
            stringValue1 = pValue.getStringValue1().getValue();
        }
        String stringValue2 = null;
        if (pValue.hasStringValue2()) {
            stringValue2 = pValue.getStringValue2().getValue();
        }
        return new BytesStringStringValue(pValue.getBytesValue().toByteArray(), stringValue1, stringValue2);
    }

    private StringStringValue newStringStringValue(PStringStringValue pValue) {
        String stringValue1 = null;
        if (pValue.hasStringValue1()) {
            stringValue1 = pValue.getStringValue1().getValue();
        }

        String stringValue2 = null;
        if (pValue.hasStringValue2()) {
            stringValue2 = pValue.getStringValue2().getValue();
        }
        return new StringStringValue(stringValue1, stringValue2);
    }

    private IntBooleanIntBooleanValue newIntBooleanIntBooleanValue(PIntBooleanIntBooleanValue pValue) {
        return new IntBooleanIntBooleanValue(pValue.getIntValue1(), pValue.getBoolValue1(),
                pValue.getIntValue2(), pValue.getBoolValue2());
    }

    private LongIntIntByteByteStringValue newLongIntIntByteByteStringValue(PLongIntIntByteByteStringValue pValue) {
        String stringValue = null;
        if (pValue.hasStringValue()) {
            stringValue = pValue.getStringValue().getValue();
        }
        return new LongIntIntByteByteStringValue(pValue.getLongValue(), pValue.getIntValue1(), pValue.getIntValue2(),
                (byte)pValue.getByteValue1(), (byte)pValue.getByteValue2(), stringValue);
    }
}
