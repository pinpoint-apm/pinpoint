/*
 * Copyright 2023 NAVER Corp.
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

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessorUtils;
import com.navercorp.pinpoint.bootstrap.context.*;
import com.navercorp.pinpoint.bootstrap.interceptor.SpanRecursiveAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.common.util.ArrayArgumentUtils;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.plugin.kafka.KafkaConstants;
import com.navercorp.pinpoint.plugin.kafka.descriptor.KafkaStreamsMethodDescriptor;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.util.concurrent.atomic.AtomicReference;


public class AddRecordsToTasksInterceptor extends SpanRecursiveAroundInterceptor {
    private final PLogger logger = PLoggerFactory.getLogger(getClass());
    protected static final String SCOPE_NAME = "##KAFKA_STREAMS_START_TRACE";

    protected static final KafkaStreamsMethodDescriptor METHOD_DESCRIPTOR = new KafkaStreamsMethodDescriptor();

    private final AtomicReference<TraceFactoryProvider.TraceFactory> traceFactoryReference = new AtomicReference<>();
    private final TraceFactoryProvider traceFactoryProvider;



    public AddRecordsToTasksInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor) {
        super(traceContext, methodDescriptor, SCOPE_NAME);
        traceContext.cacheApi(METHOD_DESCRIPTOR);
        this.traceFactoryProvider = new TraceFactoryProvider();

    }

    @Override
    protected void doInBeforeTrace(SpanEventRecorder recorder, Object target, Object[] args) {
        recorder.recordServiceType(KafkaConstants.KAFKA_STREAMS);

        AsyncContext asyncContext = recorder.recordNextAsyncContext();
        AsyncContextAccessorUtils.setAsyncContext(asyncContext, target);
    }

    @Override
    protected Trace createTrace(Object target, Object[] args) {
        Iterable<ConsumerRecord> records = ArrayArgumentUtils.getArgument(args, 1, Iterable.class);

        if (records == null) {
            return null;
        }

        ConsumerRecordsDesc consumerRecordsDesc = ConsumerRecordsDesc.create(records);
        if (consumerRecordsDesc == null) {
            return null;
        }

        TraceFactoryProvider.TraceFactory traceFactory = traceFactoryReference.get();
        if (traceFactory == null) {
            traceFactory = traceFactoryProvider.get();
            traceFactoryReference.compareAndSet(null, traceFactory);
        }

        return traceFactory.createTrace(traceContext, consumerRecordsDesc);
    }


    @Override
    protected void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) {
        recorder.recordApi(methodDescriptor);

        if (throwable != null) {
            recorder.recordException(throwable);
        }
    }



    private static class TraceFactoryProvider {

        private static TraceFactoryProvider.TraceFactory get() {

            return new TraceFactoryProvider.DefaultTraceFactory();
        }

        private interface TraceFactory {

            /**
             * Create trace trace.
             *
             * @param traceContext        the trace context
             * @return the trace
             */
            Trace createTrace(TraceContext traceContext, ConsumerRecordsDesc consumerRecords);
        }

        private static class DefaultTraceFactory implements TraceFactoryProvider.TraceFactory {

            /**
             * The Logger.
             */
            final PLogger logger = PLoggerFactory.getLogger(this.getClass());

            @Override
            public Trace createTrace(TraceContext traceContext, ConsumerRecordsDesc consumerRecords) {
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
                    return trace;
                }
            }

            private void recordRootSpan(SpanRecorder recorder, ConsumerRecordsDesc consumerRecords) {
                recorder.recordServiceType(KafkaConstants.KAFKA_STREAMS);
                recorder.recordApi(METHOD_DESCRIPTOR);

                int size = consumerRecords.size();

                String endPointAddress = consumerRecords.getEndPointAddress();
                String remoteAddress = consumerRecords.getRemoteAddress();
                endPointAddress = StringUtils.defaultIfEmpty(endPointAddress, remoteAddress);

                recorder.recordEndPoint(endPointAddress);
                recorder.recordRemoteAddress(remoteAddress);
                recorder.recordAcceptorHost(remoteAddress);

                String topic = consumerRecords.getTopicString();
                recorder.recordRpcName(createRpcName(topic, size));
                recorder.recordAttribute(KafkaConstants.KAFKA_TOPIC_ANNOTATION_KEY, topic);
                recorder.recordAttribute(KafkaConstants.KAFKA_BATCH_ANNOTATION_KEY, size);
            }

            private String createRpcName(String topic, int count) {
                StringBuilder rpcName = new StringBuilder("kafka-streams://");
                rpcName.append("topic=").append(topic);
                rpcName.append("?batch=").append(count);
                return rpcName.toString();
            }
        }

    }
}
