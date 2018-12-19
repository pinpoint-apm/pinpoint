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
import com.navercorp.pinpoint.plugin.kafka.KafkaConstants;

import java.util.concurrent.atomic.AtomicReference;

/**
 * The type Consumer multi record entry point interceptor.
 *
 * @author Victor.Zxy
 * @author Taejin Koo
 */
public class ConsumerMultiRecordEntryPointInterceptor extends ConsumerRecordEntryPointInterceptor {

    private final AtomicReference<TraceFactoryProvider.TraceFactory> tracyFactoryReference = new AtomicReference<TraceFactoryProvider.TraceFactory>();

    /**
     * Instantiates a new Consumer multi record entry point interceptor.
     *
     * @param traceContext     the trace context
     * @param methodDescriptor the method descriptor
     */
    public ConsumerMultiRecordEntryPointInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor, int parameterIndex) {
        super(traceContext, methodDescriptor, parameterIndex);
    }

    @Override
    protected Trace createTrace(Object target, Object[] args) {
        ConsumerRecordsDesc consumerRecordsDesc = getConsumerRecordsDesc(args);
        if (consumerRecordsDesc == null) {
            return null;
        }

        Trace newTrace = createTrace(consumerRecordsDesc);
        return newTrace;
    }

    private ConsumerRecordsDesc getConsumerRecordsDesc(Object[] args) {
        return ConsumerRecordsDesc.create(getTargetParameter(args));
    }

    private Trace createTrace(ConsumerRecordsDesc consumerRecordsDesc) {
        TraceFactoryProvider.TraceFactory createTrace = tracyFactoryReference.get();
        if (createTrace == null) {
            createTrace = TraceFactoryProvider.get();
            tracyFactoryReference.compareAndSet(null, createTrace);
        }
        return createTrace.createTrace(traceContext, consumerRecordsDesc);
    }

    private static class TraceFactoryProvider {

        private static TraceFactory get() {

            return new DefaultTraceFactory();
        }

        private interface TraceFactory {

            /**
             * Create trace trace.
             *
             * @param traceContext        the trace context
             * @param consumerRecordsDesc the consumer records description
             * @return the trace
             */
            Trace createTrace(TraceContext traceContext, ConsumerRecordsDesc consumerRecordsDesc);
        }

        private static class DefaultTraceFactory implements TraceFactory {

            /**
             * The Logger.
             */
            final PLogger logger = PLoggerFactory.getLogger(this.getClass());

            @Override
            public Trace createTrace(TraceContext traceContext, ConsumerRecordsDesc consumerRecordsDesc) {
                final Trace trace = traceContext.newTraceObject();
                if (trace.canSampled()) {
                    final SpanRecorder recorder = trace.getSpanRecorder();
                    recordRootSpan(recorder, consumerRecordsDesc);
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

            private void recordRootSpan(SpanRecorder recorder, ConsumerRecordsDesc consumerRecordsDesc) {
                recorder.recordServiceType(KafkaConstants.KAFKA_CLIENT);
                recorder.recordApi(ENTRY_POINT_METHOD_DESCRIPTOR);

                int size = consumerRecordsDesc.size();

                String remoteAddress = consumerRecordsDesc.getRemoteAddress();
                recorder.recordEndPoint(remoteAddress);
                recorder.recordRemoteAddress(remoteAddress);
                recorder.recordAcceptorHost(remoteAddress);

                String topic = consumerRecordsDesc.getTopicString();
                recorder.recordRpcName(createRpcName(topic, size));
                recorder.recordAttribute(KafkaConstants.KAFKA_TOPIC_ANNOTATION_KEY, topic);
                recorder.recordAttribute(KafkaConstants.KAFKA_BATCH_ANNOTATION_KEY, size);
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
