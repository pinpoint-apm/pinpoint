/*
 * Copyright 2019 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.profiler.context.grpc;

import com.google.protobuf.ByteString;
import com.navercorp.pinpoint.common.util.IntBooleanIntBooleanValue;
import com.navercorp.pinpoint.common.util.IntStringStringValue;
import com.navercorp.pinpoint.common.util.IntStringValue;
import com.navercorp.pinpoint.common.util.LongIntIntByteByteStringValue;
import com.navercorp.pinpoint.common.util.StringStringValue;
import com.navercorp.pinpoint.grpc.trace.PAnnotationValue;
import org.junit.Test;

import static org.junit.Assert.*;

public class GrpcAnnotationValueMapperTest {

    @Test
    public void buildPAnnotationValue() throws Exception {
        GrpcAnnotationValueMapper mapper = new GrpcAnnotationValueMapper();

        PAnnotationValue value = mapper.buildPAnnotationValue("foo");
        assertEquals("foo", value.getStringValue());

        value = mapper.buildPAnnotationValue(Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, value.getIntValue());

        value = mapper.buildPAnnotationValue(Long.MAX_VALUE);
        assertEquals(Long.MAX_VALUE, value.getLongValue());

        value = mapper.buildPAnnotationValue(Boolean.TRUE);
        assertEquals(Boolean.TRUE, value.getBoolValue());

        value = mapper.buildPAnnotationValue(Byte.MAX_VALUE);
        assertEquals(Byte.MAX_VALUE, value.getByteValue());

        value = mapper.buildPAnnotationValue(Float.MAX_VALUE);
        assertEquals(Float.MAX_VALUE, value.getDoubleValue(), 0);

        value = mapper.buildPAnnotationValue(Double.MAX_VALUE);
        assertEquals(Double.MAX_VALUE, value.getDoubleValue(), 0);

        value = mapper.buildPAnnotationValue("foo".getBytes());
        assertEquals(ByteString.copyFrom("foo".getBytes()), value.getBinaryValue());

        value = mapper.buildPAnnotationValue(Short.MAX_VALUE);
        assertEquals(Short.MAX_VALUE, value.getShortValue());

        IntStringValue intStringValue = new IntStringValue(1, "string");
        value = mapper.buildPAnnotationValue(intStringValue);
        assertEquals(intStringValue.getIntValue(), value.getIntStringValue().getIntValue());
        assertEquals(intStringValue.getStringValue(), value.getIntStringValue().getStringValue());

        StringStringValue stringStringValue = new StringStringValue("string1", "string2");
        value = mapper.buildPAnnotationValue(stringStringValue);
        assertEquals(stringStringValue.getStringValue1(), value.getStringStringValue().getStringValue1());
        assertEquals(stringStringValue.getStringValue2(), value.getStringStringValue().getStringValue2());

        IntStringStringValue intStringStringValue = new IntStringStringValue(1, "string1", "string2");
        value = mapper.buildPAnnotationValue(intStringStringValue);
        assertEquals(intStringStringValue.getIntValue(), value.getIntStringStringValue().getIntValue());
        assertEquals(intStringStringValue.getStringValue1(), value.getIntStringStringValue().getStringValue1());
        assertEquals(intStringStringValue.getStringValue2(), value.getIntStringStringValue().getStringValue2());

        LongIntIntByteByteStringValue longIntIntByteByteStringValue = new LongIntIntByteByteStringValue(1, 1, 1, (byte)1, (byte)1, "string");
        value = mapper.buildPAnnotationValue(longIntIntByteByteStringValue);
        assertEquals(longIntIntByteByteStringValue.getLongValue(), value.getLongIntIntByteByteStringValue().getLongValue());
        assertEquals(longIntIntByteByteStringValue.getIntValue1(), value.getLongIntIntByteByteStringValue().getIntValue1());
        assertEquals(longIntIntByteByteStringValue.getIntValue2(), value.getLongIntIntByteByteStringValue().getIntValue2());
        assertEquals(longIntIntByteByteStringValue.getByteValue1(), value.getLongIntIntByteByteStringValue().getByteValue1());
        assertEquals(longIntIntByteByteStringValue.getByteValue2(), value.getLongIntIntByteByteStringValue().getByteValue2());
        assertEquals(longIntIntByteByteStringValue.getStringValue(), value.getLongIntIntByteByteStringValue().getStringValue());

        IntBooleanIntBooleanValue intBooleanIntBooleanValue = new IntBooleanIntBooleanValue(1,Boolean.TRUE, 1, Boolean.TRUE);
        value = mapper.buildPAnnotationValue(intBooleanIntBooleanValue);
        assertEquals(intBooleanIntBooleanValue.getIntValue1(), value.getIntBooleanIntBooleanValue().getIntValue1());
        assertEquals(intBooleanIntBooleanValue.isBooleanValue1(), value.getIntBooleanIntBooleanValue().getBoolValue1());
        assertEquals(intBooleanIntBooleanValue.getIntValue2(), value.getIntBooleanIntBooleanValue().getIntValue2());
        assertEquals(intBooleanIntBooleanValue.isBooleanValue2(), value.getIntBooleanIntBooleanValue().getBoolValue2());
    }
}