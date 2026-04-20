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

package com.navercorp.pinpoint.common.server.bo;

import com.google.protobuf.ByteString;
import com.google.protobuf.StringValue;
import com.navercorp.pinpoint.grpc.trace.PAcceptEvent;
import com.navercorp.pinpoint.grpc.trace.PAnnotation;
import com.navercorp.pinpoint.grpc.trace.PAnnotationValue;
import com.navercorp.pinpoint.grpc.trace.PAttribute;
import com.navercorp.pinpoint.grpc.trace.PAttributeArrayValue;
import com.navercorp.pinpoint.grpc.trace.PAttributeKeyValueList;
import com.navercorp.pinpoint.grpc.trace.PAttributeValue;
import com.navercorp.pinpoint.grpc.trace.PIntStringValue;
import com.navercorp.pinpoint.grpc.trace.PMessageEvent;
import com.navercorp.pinpoint.grpc.trace.PNextEvent;
import com.navercorp.pinpoint.grpc.trace.PParentInfo;
import com.navercorp.pinpoint.grpc.trace.PSpan;
import com.navercorp.pinpoint.grpc.trace.PSpanChunk;
import com.navercorp.pinpoint.grpc.trace.PSpanEvent;
import com.navercorp.pinpoint.grpc.trace.PTransactionId;
import com.navercorp.pinpoint.io.SpanVersion;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author Woonduk Kang(emeroad)
 */
public class RandomTSpan {

    private final Random random = new Random();

    public PSpan.Builder randomPSpan() {
        PSpan.Builder span = PSpan.newBuilder();
        PTransactionId transactionId = getPTransactionId();
        span.setTransactionId(transactionId);
        span.setVersion(SpanVersion.TRACE_V2);
        span.setSpanId(random.nextLong());
        span.setParentSpanId(random.nextInt(0, 100000));
        span.setStartTime(System.currentTimeMillis() + random.nextInt(0, 1000));
        span.setElapsed(random.nextInt(0, 2000));

        span.setServiceType(randomServerServiceType());
        PAcceptEvent.Builder acceptEvent = PAcceptEvent.newBuilder();
        acceptEvent.setRpc(randomAlphanumeric(10));
        acceptEvent.setEndPoint(randomAlphanumeric(20));
        acceptEvent.setRemoteAddr(randomAlphanumeric(20));

        PParentInfo.Builder parentInfo = PParentInfo.newBuilder();
        parentInfo.setParentApplicationName("parentApp");
        parentInfo.setParentApplicationType(randomServerServiceType());
        parentInfo.setAcceptorHost("acceptHost");
        acceptEvent.setParentInfo(parentInfo);

        span.setAcceptEvent(acceptEvent);


        List<PAnnotation> tAnnotationList = randomTAnnotationList();
        if (CollectionUtils.isNotEmpty(tAnnotationList)) {
            tAnnotationList.addAll(span.getAnnotationList());
        }
        List<PAttribute> pAttributeList = randomPAttributeList();
        if (CollectionUtils.isNotEmpty(pAttributeList)) {
            span.addAllAttribute(pAttributeList);
        }
        span.setFlag((short) random.nextInt(0, 4));
        span.setErr((short) random.nextInt(0, 2));
//        tSpan.setSpanEventList()


        span.setApiId(random.nextInt(0, 5000));
        if (random.nextBoolean()) {
            span.setApplicationServiceType(randomServerServiceType());
        } else {
            span.setApplicationServiceType(span.getServiceType());
        }
        if (random.nextBoolean()) {
            PIntStringValue.Builder exceptionInfo = PIntStringValue.newBuilder();
            exceptionInfo.setIntValue(random.nextInt(0, 5000));
            exceptionInfo.setStringValue(StringValue.of(randomAlphanumeric(100)));
            span.setExceptionInfo(exceptionInfo);
        }
        span.setLoggingTransactionInfo((byte) random.nextInt(0, 256));
        return span;
    }

    private PTransactionId getPTransactionId() {
        return PTransactionId.newBuilder()
                .setAgentId("agent")
                .setAgentStartTime(System.currentTimeMillis())
                .setSequence(random.nextLong(0, Long.MAX_VALUE))
                .build();
    }

    private short randomServerServiceType() {
        //        Server (1000 ~ 1899)
        return (short) random.nextInt(1000, 1899);
    }

    public List<PAnnotation> randomTAnnotationList() {
        int annotationSize = random.nextInt(0, 3);
        List<PAnnotation> result = new ArrayList<>();
        for (int i = 0; i < annotationSize; i++) {
            result.add(randomTAnnotation(i));
        }
        return result;
    }

    public PAnnotation randomTAnnotation(int key) {
        PAnnotation.Builder annotation = PAnnotation.newBuilder();
        // sort order
        annotation.setKey(key);
        PAnnotationValue.Builder tAnnotationValue = PAnnotationValue.newBuilder();
        tAnnotationValue.setStringValue(randomAlphanumeric(10));
        annotation.setValue(tAnnotationValue);
        return annotation.build();
    }

    public PSpanEvent randomTSpanEvent(short sequence) {
        PSpanEvent.Builder spanEvent = PSpanEvent.newBuilder();
//        @deprecated
//        spanEvent.setSpanId();
        spanEvent.setSequence(sequence);
        spanEvent.setStartElapsed(random.nextInt(0, 1000));
        spanEvent.setEndElapsed(random.nextInt(0, 1000));
//        spanEvent.setRpc(randomAlphanumeric(10));
//         Database (2000 ~ 2899)
        spanEvent.setServiceType((short) random.nextInt(2000, 2889));

        List<PAnnotation> tAnnotationList = randomTAnnotationList();
        if (CollectionUtils.isNotEmpty(tAnnotationList)) {
            spanEvent.addAllAnnotation(tAnnotationList);
        }
        List<PAttribute> pAttributeList = randomPAttributeList();
        if (CollectionUtils.isNotEmpty(pAttributeList)) {
            spanEvent.addAllAttribute(pAttributeList);
        }
        spanEvent.setDepth(random.nextInt(1, 256));
        PMessageEvent messageEvent = PMessageEvent.newBuilder()
                .setNextSpanId(random.nextLong())
                .setDestinationId(randomAlphanumeric(20))
                .setEndPoint(randomAlphanumeric(20))
                .build();

        spanEvent.setNextEvent(PNextEvent.newBuilder().setMessageEvent(messageEvent).build());

        spanEvent.setApiId(random.nextInt(0, 65535));

        spanEvent.setAsyncEvent(random.nextInt());

        if (random.nextBoolean()) {
            PIntStringValue exception = PIntStringValue.newBuilder()
                    .setIntValue(random.nextInt(0, 5000))
                    .setStringValue(StringValue.of(randomAlphanumeric(100)))
                    .build();
            spanEvent.setExceptionInfo(exception);
        }

        return spanEvent.build();
    }

    private int randomNegative(int value) {
        if (random.nextBoolean()) {
            return -value;
        }
        return value;
    }

    public PSpanChunk.Builder randomTSpanChunk() {
        final PSpanChunk.Builder spanChunk = PSpanChunk.newBuilder();
        spanChunk.setVersion(SpanVersion.TRACE_V2);
        spanChunk.setTransactionId(getPTransactionId());
        spanChunk.setSpanId(random.nextLong());

        spanChunk.setEndPoint(randomAlphanumeric(20));

//        tSpanChunk.setSpanEventList()
        spanChunk.setApplicationServiceType(randomServerServiceType());
        return spanChunk;
    }


    private String randomAlphanumeric(int count) {
        return RandomStringUtils.insecure().nextAlphabetic(count);
    }

    public List<PAttribute> randomPAttributeList() {
        int size = random.nextInt(0, 4);
        List<PAttribute> result = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            result.add(randomPAttribute("attr-" + i, 0));
        }
        return result;
    }

    private PAttribute randomPAttribute(String key, int depth) {
        return PAttribute.newBuilder()
                .setKey(key)
                .setValue(randomPAttributeValue(depth))
                .build();
    }

    private PAttributeValue randomPAttributeValue(int depth) {
        PAttributeValue.Builder builder = PAttributeValue.newBuilder();
        int pick = depth >= 2 ? random.nextInt(0, 5) : random.nextInt(0, 7);
        switch (pick) {
            case 0 -> builder.setStringValue(randomAlphanumeric(10));
            case 1 -> builder.setBoolValue(random.nextBoolean());
            case 2 -> builder.setLongValue(random.nextLong());
            case 3 -> builder.setDoubleValue(random.nextDouble());
            case 4 -> {
                byte[] bytes = new byte[random.nextInt(0, 8)];
                random.nextBytes(bytes);
                builder.setBinaryValue(ByteString.copyFrom(bytes));
            }
            case 5 -> {
                PAttributeArrayValue.Builder arrayBuilder = PAttributeArrayValue.newBuilder();
                int arraySize = random.nextInt(0, 3);
                for (int i = 0; i < arraySize; i++) {
                    arrayBuilder.addValues(randomPAttributeValue(depth + 1));
                }
                builder.setArrayValue(arrayBuilder.build());
            }
            case 6 -> {
                PAttributeKeyValueList.Builder kvBuilder = PAttributeKeyValueList.newBuilder();
                int kvSize = random.nextInt(0, 3);
                for (int i = 0; i < kvSize; i++) {
                    kvBuilder.addValues(randomPAttribute("k-" + i, depth + 1));
                }
                builder.setKvlistValue(kvBuilder.build());
            }
        }
        return builder.build();
    }
}
