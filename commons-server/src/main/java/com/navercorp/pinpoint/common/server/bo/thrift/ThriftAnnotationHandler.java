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

package com.navercorp.pinpoint.common.server.bo.thrift;

import com.navercorp.pinpoint.common.server.bo.AnnotationFactory;
import com.navercorp.pinpoint.common.util.IntBooleanIntBooleanValue;
import com.navercorp.pinpoint.common.util.IntStringStringValue;
import com.navercorp.pinpoint.common.util.IntStringValue;
import com.navercorp.pinpoint.common.util.LongIntIntByteByteStringValue;
import com.navercorp.pinpoint.common.util.StringStringValue;
import com.navercorp.pinpoint.thrift.dto.TAnnotation;
import com.navercorp.pinpoint.thrift.dto.TAnnotationValue;
import com.navercorp.pinpoint.thrift.dto.TIntBooleanIntBooleanValue;
import com.navercorp.pinpoint.thrift.dto.TIntStringStringValue;
import com.navercorp.pinpoint.thrift.dto.TIntStringValue;
import com.navercorp.pinpoint.thrift.dto.TLongIntIntByteByteStringValue;
import com.navercorp.pinpoint.thrift.dto.TStringStringValue;

/**
 * @author Woonduk Kang(emeroad)
 */
public class ThriftAnnotationHandler implements AnnotationFactory.AnnotationTypeHandler<TAnnotation> {
    @Override
    public int getKey(TAnnotation annotation) {
        return annotation.getKey();
    }

    @Override
    public Object getValue(TAnnotation annotation) {
        TAnnotationValue value = annotation.getValue();
        if (value == null) {
            return null;
        }
        return value.getFieldValue();
    }

    @Override
    public Object buildCustomAnnotationValue(Object annotationValue) {
        if (annotationValue instanceof TIntStringValue) {
            return newIntStringValue(annotationValue);
        } else if (annotationValue instanceof TIntStringStringValue) {
            return newIntStringString(annotationValue);
        } else if (annotationValue instanceof TStringStringValue) {
            return newStringStringValue(annotationValue);
        } else if (annotationValue instanceof TLongIntIntByteByteStringValue) {
            return newLongIntIntByteByteStringValue(annotationValue);
        } else if (annotationValue instanceof TIntBooleanIntBooleanValue) {
            return newIntBooleanIntBooleanValue(annotationValue);
        }
        return null;
    }



    private IntStringValue newIntStringValue(Object annotationValue) {
        TIntStringValue tValue = (TIntStringValue) annotationValue;
        return new IntStringValue(tValue.getIntValue(), tValue.getStringValue());
    }

    private IntStringStringValue newIntStringString(Object annotationValue) {
        TIntStringStringValue tValue = (TIntStringStringValue) annotationValue;
        return new IntStringStringValue(tValue.getIntValue(), tValue.getStringValue1(), tValue.getStringValue2());
    }

    private StringStringValue newStringStringValue(Object annotationValue) {
        TStringStringValue tValue = (TStringStringValue) annotationValue;
        return new StringStringValue(tValue.getStringValue1(), tValue.getStringValue2());
    }


    private IntBooleanIntBooleanValue newIntBooleanIntBooleanValue(Object annotationValue) {
        TIntBooleanIntBooleanValue tValue = (TIntBooleanIntBooleanValue) annotationValue;
        return new IntBooleanIntBooleanValue(tValue.getIntValue1(), tValue.isBoolValue1(),
                tValue.getIntValue2(), tValue.isBoolValue2());
    }


    private LongIntIntByteByteStringValue newLongIntIntByteByteStringValue(Object o) {
        TLongIntIntByteByteStringValue tValue = (TLongIntIntByteByteStringValue) o;
        return new LongIntIntByteByteStringValue(tValue.getLongValue(), tValue.getIntValue1(), tValue.getIntValue2(),
                tValue.getByteValue1(), tValue.getByteValue2(), tValue.getStringValue());
    }
}
