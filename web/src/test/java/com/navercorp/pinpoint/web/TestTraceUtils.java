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

package com.navercorp.pinpoint.web;

import com.navercorp.pinpoint.bootstrap.context.SpanId;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.SpanEventBo;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.profiler.util.TransactionId;
import com.navercorp.pinpoint.web.util.ServiceTypeRegistryMockFactory;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static com.navercorp.pinpoint.common.trace.ServiceTypeProperty.INCLUDE_DESTINATION_ID;
import static com.navercorp.pinpoint.common.trace.ServiceTypeProperty.QUEUE;
import static com.navercorp.pinpoint.common.trace.ServiceTypeProperty.RECORD_STATISTICS;
import static com.navercorp.pinpoint.common.trace.ServiceTypeProperty.TERMINAL;

/**
 * @author HyunGil Jeong
 */
public class TestTraceUtils {

    public static final short UNKNOWN_TYPE_CODE = ServiceType.UNKNOWN.getCode();
    public static final String UNKNOWN_TYPE_NAME = ServiceType.UNKNOWN.getName();
    public static final short USER_TYPE_CODE = ServiceType.USER.getCode();
    public static final String USER_TYPE_NAME = ServiceType.USER.getName();
    public static final short TEST_STAND_ALONE_TYPE_CODE = ServiceType.TEST_STAND_ALONE.getCode();
    public static final String TEST_STAND_ALONE_TYPE_NAME = ServiceType.TEST_STAND_ALONE.getName();
    public static final short TOMCAT_TYPE_CODE = 1010;
    public static final String TOMCAT_TYPE_NAME = "TOMCAT";
    public static final short RPC_TYPE_CODE = 9999;
    public static final String RPC_TYPE_NAME = "RPC";
    public static final short BACKEND_TYPE_CODE = 2100;
    public static final String BACKEND_TYPE_NAME = "BACKEND";
    public static final short CACHE_TYPE_CODE = 8100;
    public static final String CACHE_TYPE_NAME = "CACHE";
    public static final short MESSAGE_QUEUE_TYPE_CODE = 8310;
    public static final String MESSAGE_QUEUE_TYPE_NAME = "MESSAGE_QUEUE";

    public static ServiceTypeRegistryService mockServiceTypeRegistryService() {

        ServiceTypeRegistryMockFactory mockFactory = new ServiceTypeRegistryMockFactory();
        mockFactory.addServiceTypeMock(UNKNOWN_TYPE_CODE, UNKNOWN_TYPE_NAME, RECORD_STATISTICS);
        mockFactory.addServiceTypeMock(USER_TYPE_CODE, USER_TYPE_NAME, RECORD_STATISTICS);
        mockFactory.addServiceTypeMock(TEST_STAND_ALONE_TYPE_CODE, TEST_STAND_ALONE_TYPE_NAME, RECORD_STATISTICS);
        mockFactory.addServiceTypeMock(TOMCAT_TYPE_CODE, TOMCAT_TYPE_NAME, RECORD_STATISTICS);
        mockFactory.addServiceTypeMock(RPC_TYPE_CODE, RPC_TYPE_NAME, RECORD_STATISTICS);
        mockFactory.addServiceTypeMock(BACKEND_TYPE_CODE, BACKEND_TYPE_NAME, TERMINAL, INCLUDE_DESTINATION_ID, RECORD_STATISTICS);
        mockFactory.addServiceTypeMock(CACHE_TYPE_CODE, CACHE_TYPE_NAME, TERMINAL, INCLUDE_DESTINATION_ID, RECORD_STATISTICS);
        mockFactory.addServiceTypeMock(MESSAGE_QUEUE_TYPE_CODE, MESSAGE_QUEUE_TYPE_NAME, QUEUE, RECORD_STATISTICS);

        return mockFactory.createMockServiceTypeRegistryService();
    }

    private static class TransactionIdGenerator {

        private final Map<String, AtomicInteger> sequenceMap = new ConcurrentHashMap<>();

        TransactionId generate(String agentId) {
            int nextSequence = getNextSequence(agentId);
            return new TransactionId(agentId, 0L, nextSequence);
        }

        private int getNextSequence(String agentId) {
            AtomicInteger sequence = sequenceMap.get(agentId);
            if (sequence == null) {
                sequence = new AtomicInteger();
                AtomicInteger currentSequence = sequenceMap.putIfAbsent(agentId, sequence);
                if (currentSequence != null) {
                    sequence = currentSequence;
                }
            }
            return sequence.incrementAndGet();
        }
    }

    public static class SpanBuilder {

        private static final Random random = new Random();

        private static final TransactionIdGenerator transactionIdGenerator = new TransactionIdGenerator();

        private final int version;
        private final String applicationName;
        private final String agentId;

        private long spanId = SpanId.NULL;
        private long startTime;
        private long collectorAcceptTime;
        private int elapsed;
        private int errorCode;
        private SpanBo parentSpan;

        public SpanBuilder(String applicationName, String agentId) {
            this(0, applicationName, agentId);
        }

        public SpanBuilder(int version, String applicationName, String agentId) {
            this.version = version;
            this.applicationName = applicationName;
            this.agentId = agentId;
        }

        public SpanBuilder spanId(long spanId) {
            if (spanId == SpanId.NULL) {
                throw new IllegalArgumentException("Invalid spanId : " + spanId);
            }
            this.spanId = spanId;
            return this;
        }

        public SpanBuilder startTime(long startTime) {
            this.startTime = startTime;
            return this;
        }

        public SpanBuilder collectorAcceptTime(long collectorAcceptTime) {
            this.collectorAcceptTime = collectorAcceptTime;
            return this;
        }

        public SpanBuilder elapsed(int elapsed) {
            this.elapsed = elapsed;
            return this;
        }

        public SpanBuilder errorCode(int errorCode) {
            this.errorCode = errorCode;
            return this;
        }

        public SpanBuilder parentSpan(SpanBo parentSpan) {
            this.parentSpan = parentSpan;
            return this;
        }

        public SpanBo build() {
            SpanBo spanBo = new SpanBo();
            spanBo.setVersion(version);
            spanBo.setApplicationId(applicationName);
            spanBo.setAgentId(agentId);
            long spanId = this.spanId;
            if (spanId == SpanId.NULL) {
                spanId = random.nextLong();
            }
            spanBo.setSpanId(spanId);
            spanBo.setServiceType(TEST_STAND_ALONE_TYPE_CODE);
            spanBo.setApplicationServiceType(TEST_STAND_ALONE_TYPE_CODE);
            spanBo.setStartTime(startTime);
            spanBo.setCollectorAcceptTime(collectorAcceptTime);
            spanBo.setElapsed(elapsed);
            spanBo.setErrCode(errorCode);
            if (parentSpan != null) {
                spanBo.setTransactionId(parentSpan.getTransactionId());
                spanBo.setParentSpanId(parentSpan.getSpanId());
            } else {
                spanBo.setTransactionId(transactionIdGenerator.generate(agentId));
                spanBo.setParentSpanId(-1L);
            }
            return spanBo;
        }
    }

    public abstract static class SpanEventBuilder {

        private final short serviceType;
        private final String destinationId;
        private final String endPoint;
        private final int startElapsed;
        private final int endElapsed;

        SpanEventBuilder(short serviceType, String destinationId, String endPoint, int startElapsed, int endElapsed) {
            this.serviceType = serviceType;
            this.destinationId = destinationId;
            this.endPoint = endPoint;
            this.startElapsed = startElapsed;
            this.endElapsed = endElapsed;
        }

        public SpanEventBo build() {
            SpanEventBo spanEventBo = new SpanEventBo();
            spanEventBo.setServiceType(serviceType);
            spanEventBo.setDestinationId(destinationId);
            spanEventBo.setEndPoint(endPoint);
            spanEventBo.setStartElapsed(startElapsed);
            spanEventBo.setEndElapsed(endElapsed);
            return spanEventBo;
        }
    }

    public static class RpcSpanEventBuilder extends SpanEventBuilder {

        private long nextSpanId;

        public RpcSpanEventBuilder(String url, int startElapsed, int endElapsed) {
            super(RPC_TYPE_CODE, url, null, startElapsed, endElapsed);
        }

        public RpcSpanEventBuilder nextSpanId(long nextSpanId) {
            if (nextSpanId == 0) {
                throw new IllegalArgumentException("nextSpanId must not be 0");
            }
            this.nextSpanId = nextSpanId;
            return this;
        }

        @Override
        public SpanEventBo build() {
            SpanEventBo spanEventBo = super.build();
            spanEventBo.setNextSpanId(nextSpanId);
            return spanEventBo;
        }
    }

    public static class CacheSpanEventBuilder extends SpanEventBuilder {
        public CacheSpanEventBuilder(String destinationId, String endPoint, int startElapsed, int endElapsed) {
            super(CACHE_TYPE_CODE, destinationId, endPoint, startElapsed, endElapsed);
        }
    }

    public static class BackEndSpanEventBuilder extends SpanEventBuilder {
        public BackEndSpanEventBuilder(String destinationId, String endPoint, int startElapsed, int endElapsed) {
            super(BACKEND_TYPE_CODE, destinationId, endPoint, startElapsed, endElapsed);
        }
    }
}
