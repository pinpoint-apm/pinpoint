/*
 * Copyright 2018 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.plugin.kafka.interceptor;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.plugin.kafka.KafkaConstants;
import com.navercorp.pinpoint.plugin.kafka.field.accessor.RemoteAddressFieldAccessor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicReference;

/**
 * The type Consumer multi record entry point interceptor.
 *
 * @author Victor.Zxy
 */
public class ConsumerMultiRecordEntryPointInterceptor extends ConsumerRecordEntryPointInterceptor {

    private final AtomicReference<TraceFactoryProvider.TraceFactory> tracyFactoryReference = new AtomicReference<TraceFactoryProvider.TraceFactory>();

    /**
     * Instantiates a new Consumer multi record entry point interceptor.
     *
     * @param traceContext     the trace context
     * @param methodDescriptor the method descriptor
     */
    public ConsumerMultiRecordEntryPointInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor) {
        super(traceContext, methodDescriptor);
    }

    @Override
    protected Trace createTrace(Object target, Object[] args) {

        ConsumerRecords consumerRecords = getConsumerRecords(args);

        if (consumerRecords == null || consumerRecords.isEmpty()) {
            return null;
        }
        Trace newTrace = createTrace(consumerRecords);

        return newTrace;
    }

    private ConsumerRecords getConsumerRecords(Object[] args) {
        if (args == null) {
            return null;
        }

        for (Object arg : args) {
            if (arg instanceof ConsumerRecords) {
                return (ConsumerRecords) arg;
            }
        }
        return null;
    }

    private Trace createTrace(ConsumerRecords consumerRecords) {
        TraceFactoryProvider.TraceFactory createTrace = tracyFactoryReference.get();
        if (createTrace == null) {
            createTrace = TraceFactoryProvider.get();
            tracyFactoryReference.compareAndSet(null, createTrace);
        }
        return createTrace.createTrace(traceContext, consumerRecords);
    }

    private static class TraceFactoryProvider {

        private static TraceFactory get() {

            return new DefaultTraceFactory();
        }

        private interface TraceFactory {

            /**
             * Create trace trace.
             *
             * @param traceContext    the trace context
             * @param consumerRecords the consumer records
             * @return the trace
             */
            Trace createTrace(TraceContext traceContext, ConsumerRecords consumerRecords);
        }

        private static class DefaultTraceFactory implements TraceFactory {

            /**
             * The Logger.
             */
            final PLogger logger = PLoggerFactory.getLogger(this.getClass());

            @Override
            public Trace createTrace(TraceContext traceContext, ConsumerRecords consumerRecords) {

                final Trace trace = traceContext.newTraceObject();
                if (trace.canSampled()) {
                    final SpanRecorder recorder = trace.getSpanRecorder();
                    recordRootSpan(recorder, consumerRecords);
                    if (logger.isDebugEnabled()) {
                        logger.debug("TraceID not exist. start new trace.");
                    }
                    return trace;
                } else {
                    if (logger.isDebugEnabled()) {
                        logger.debug("TraceID not exist. camSampled is false. skip trace.");
                    }
                    return null;
                }
            }

            private void recordRootSpan(SpanRecorder recorder, ConsumerRecords consumerRecords) {
                recorder.recordServiceType(KafkaConstants.KAFKA_CLIENT);
                recorder.recordApi(ENTRY_POINT_METHOD_DESCRIPTOR);

                int count = consumerRecords.count();
                Iterator<ConsumerRecord> iterator = consumerRecords.iterator();
                ConsumerRecord consumerRecord = iterator.next();

                String remoteAddress = getRemoteAddress(consumerRecord);
                recorder.recordEndPoint(remoteAddress);
                recorder.recordRemoteAddress(remoteAddress);

                String topic = consumerRecord.topic();
                recorder.recordRpcName(createRpcName(topic, count));
                recorder.recordAcceptorHost("topic:" + topic);
                recorder.recordAttribute(KafkaConstants.KAFKA_TOPIC_ANNOTATION_KEY, topic);
                recorder.recordAttribute(KafkaConstants.KAFKA_BATCH_ANNOTATION_KEY, count);

            }

            private String getRemoteAddress(Object remoteAddressFieldAccessor) {
                String remoteAddress = null;
                if (remoteAddressFieldAccessor instanceof RemoteAddressFieldAccessor) {
                    remoteAddress = ((RemoteAddressFieldAccessor) remoteAddressFieldAccessor)._$PINPOINT$_getRemoteAddress();
                }

                if (StringUtils.isEmpty(remoteAddress)) {
                    return KafkaConstants.UNKNOWN;
                } else {
                    return remoteAddress;
                }
            }

            private String createRpcName(String topic, int count) {
                StringBuilder rpcName = new StringBuilder("kafka://");
                rpcName.append("topic=").append(topic);
                rpcName.append("?batch=").append(count);
                return rpcName.toString();
            }
        }
    }
}
