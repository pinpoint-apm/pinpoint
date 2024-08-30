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

import com.navercorp.pinpoint.common.profiler.util.TransactionIdUtils;
import com.navercorp.pinpoint.thrift.dto.TAnnotation;
import com.navercorp.pinpoint.thrift.dto.TAnnotationValue;
import com.navercorp.pinpoint.thrift.dto.TIntStringValue;
import com.navercorp.pinpoint.thrift.dto.TSpan;
import com.navercorp.pinpoint.thrift.dto.TSpanChunk;
import com.navercorp.pinpoint.thrift.dto.TSpanEvent;
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

    public TSpan randomTSpan() {
        final TSpan tSpan = new TSpan();
        tSpan.setAgentId("agentId");
        tSpan.setApplicationName("appName");
        tSpan.setAgentStartTime(System.currentTimeMillis());

        tSpan.setTransactionId(TransactionIdUtils.formatByteBuffer("agent", System.currentTimeMillis(), random.nextLong(0, Long.MAX_VALUE)));
        tSpan.setSpanId(random.nextLong());
        tSpan.setParentSpanId(random.nextInt(0, 100000));
        tSpan.setStartTime(System.currentTimeMillis() + random.nextInt(0, 1000));
        tSpan.setElapsed(random.nextInt(0, 2000));
        tSpan.setRpc(randomAlphanumeric(10));

        tSpan.setServiceType(randomServerServiceType());
        tSpan.setEndPoint(randomAlphanumeric(20));
        tSpan.setRemoteAddr(randomAlphanumeric(20));

        List<TAnnotation> tAnnotationList = randomTAnnotationList();
        if (CollectionUtils.isNotEmpty(tAnnotationList)) {
            tSpan.setAnnotations(tAnnotationList);
        }
        tSpan.setFlag((short) random.nextInt(0, 4));
        tSpan.setErr((short) random.nextInt(0, 2));
//        tSpan.setSpanEventList()
        tSpan.setParentApplicationName("parentApp");
        tSpan.setParentApplicationType(randomServerServiceType());
        tSpan.setAcceptorHost("acceptHost");
        tSpan.setApiId(random.nextInt(0, 5000));
        if (random.nextBoolean()) {
            tSpan.setApplicationServiceType(randomServerServiceType());
        } else {
            tSpan.setApplicationServiceType(tSpan.getServiceType());
        }
        if (random.nextBoolean()) {
            TIntStringValue exceptionInfo = new TIntStringValue();
            exceptionInfo.setIntValue(random.nextInt(0, 5000));
            exceptionInfo.setStringValue(randomAlphanumeric(100));
            tSpan.setExceptionInfo(exceptionInfo);
        }
        tSpan.setLoggingTransactionInfo((byte) random.nextInt(0, 256));
        return tSpan;
    }

    private short randomServerServiceType() {
        //        Server (1000 ~ 1899)
        return (short) random.nextInt(1000, 1899);
    }

    public List<TAnnotation> randomTAnnotationList() {
        int annotationSize = random.nextInt(0, 3);
        List<TAnnotation> result = new ArrayList<>();
        for (int i = 0; i < annotationSize; i++) {
            result.add(randomTAnnotation(i));
        }
        return result;
    }

    public TAnnotation randomTAnnotation(int key) {
        TAnnotation tAnnotation = new TAnnotation();
        // sort order
        tAnnotation.setKey(key);
        TAnnotationValue tAnnotationValue = new TAnnotationValue();
        tAnnotationValue.setStringValue(randomAlphanumeric(10));
        tAnnotation.setValue(tAnnotationValue);
        return tAnnotation;
    }

    public TSpanEvent randomTSpanEvent(short sequence) {
        TSpanEvent tSpanEvent = new TSpanEvent();
//        @deprecated
//        tSpanEvent.setSpanId();
        tSpanEvent.setSequence(sequence);
        tSpanEvent.setStartElapsed(random.nextInt(0, 1000));
        tSpanEvent.setEndElapsed(random.nextInt(0, 1000));
//        tSpanEvent.setRpc(randomAlphanumeric(10));
//         Database (2000 ~ 2899)
        tSpanEvent.setServiceType((short) random.nextInt(2000, 2889));
        tSpanEvent.setEndPoint(randomAlphanumeric(10));

        List<TAnnotation> tAnnotationList = randomTAnnotationList();
        if (CollectionUtils.isNotEmpty(tAnnotationList)) {
            tSpanEvent.setAnnotations(tAnnotationList);
        }
        tSpanEvent.setDepth(random.nextInt(0, 256));
        tSpanEvent.setNextSpanId(random.nextLong());

        tSpanEvent.setDestinationId(randomAlphanumeric(20));
        tSpanEvent.setApiId(random.nextInt(0, 65535));

        tSpanEvent.setNextAsyncId(random.nextInt());

        if (random.nextBoolean()) {
            TIntStringValue exceptionInfo = new TIntStringValue();
            exceptionInfo.setIntValue(random.nextInt(0, 5000));
            exceptionInfo.setStringValue(randomAlphanumeric(100));
            tSpanEvent.setExceptionInfo(exceptionInfo);
        }

        return tSpanEvent;
    }

    private int randomNegative(int value) {
        if (random.nextBoolean()) {
            return -value;
        }
        return value;
    }

    public TSpanChunk randomTSpanChunk() {
        final TSpanChunk tSpanChunk = new TSpanChunk();
        tSpanChunk.setAgentId("agentId");
        tSpanChunk.setApplicationName("appName");
        tSpanChunk.setAgentStartTime(System.currentTimeMillis());

        tSpanChunk.setTransactionId(TransactionIdUtils.formatByteBuffer("agent", System.currentTimeMillis(), random.nextLong(0, Long.MAX_VALUE)));
        tSpanChunk.setSpanId(random.nextLong());

        tSpanChunk.setEndPoint(randomAlphanumeric(20));

//        tSpanChunk.setSpanEventList()
        tSpanChunk.setApplicationServiceType(randomServerServiceType());
        return tSpanChunk;
    }


    private String randomAlphanumeric(int count) {
        return RandomStringUtils.insecure().nextAlphabetic(count);
    }
}
