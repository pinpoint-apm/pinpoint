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

import com.navercorp.pinpoint.bootstrap.context.*;
import com.navercorp.pinpoint.bootstrap.interceptor.SpanRecursiveAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.sampler.SamplingFlagUtils;
import com.navercorp.pinpoint.bootstrap.util.NumberUtils;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.util.ArrayUtils;
import com.navercorp.pinpoint.common.util.BytesUtils;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.plugin.kafka.KafkaClientUtils;
import com.navercorp.pinpoint.plugin.kafka.KafkaConfig;
import com.navercorp.pinpoint.plugin.kafka.KafkaConstants;
import com.navercorp.pinpoint.plugin.kafka.descriptor.EntryPointMethodDescriptor;
import com.navercorp.pinpoint.plugin.kafka.field.accessor.EndPointFieldAccessor;
import com.navercorp.pinpoint.plugin.kafka.field.accessor.RemoteAddressFieldAccessor;
import com.navercorp.pinpoint.plugin.kafka.recorder.DefaultHeaderRecorder;
import com.navercorp.pinpoint.plugin.kafka.recorder.HeaderRecorder;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Taejin Koo
 * @author Victor.Zxy
 */
public class ConsumerRecordEntryPointInterceptor extends SpanRecursiveAroundInterceptor {

    protected static final String SCOPE_NAME = "##KAFKA_ENTRY_POINT_START_TRACE";

    protected static final EntryPointMethodDescriptor ENTRY_POINT_METHOD_DESCRIPTOR = new EntryPointMethodDescriptor();

    private final AtomicReference<TraceFactoryProvider.TraceFactory> traceFactoryReference = new AtomicReference<>();

    protected final int parameterIndex;

    private final TraceFactoryProvider traceFactoryProvider;

    public ConsumerRecordEntryPointInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor, int parameterIndex) {
        super(traceContext, methodDescriptor, SCOPE_NAME);
        traceContext.cacheApi(ENTRY_POINT_METHOD_DESCRIPTOR);
        this.parameterIndex = parameterIndex;
        KafkaConfig config = new KafkaConfig(traceContext.getProfilerConfig());
        this.traceFactoryProvider = new TraceFactoryProvider(config.isHeaderRecorded());
    }

    @Override
    protected void doInBeforeTrace(SpanEventRecorder recorder, Object target, Object[] args) {
        recorder.recordServiceType(KafkaConstants.KAFKA_CLIENT_INTERNAL);
    }


    @Override
    protected void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) {
        recorder.recordApi(methodDescriptor);
        recorder.recordException(throwable);

    }

    @Override
    protected Trace createTrace(Object target, Object[] args) {
        ConsumerRecord consumerRecord = getConsumerRecord(args);

        if (consumerRecord == null) {
            return null;
        }
        Trace newTrace = createTrace(consumerRecord);

        return newTrace;
    }

    private ConsumerRecord getConsumerRecord(Object[] args) {
        Object consumerRecord = getTargetParameter(args);
        if (consumerRecord instanceof ConsumerRecord) {
            return (ConsumerRecord) consumerRecord;
        }

        return null;
    }

    protected Object getTargetParameter(Object[] args) {
        int length = ArrayUtils.getLength(args);
        if (length <= parameterIndex) {
            return null;
        }

        return args[parameterIndex];
    }

    private Trace createTrace(ConsumerRecord consumerRecord) {
        TraceFactoryProvider.TraceFactory traceFactory = traceFactoryReference.get();
        if (traceFactory == null) {
            traceFactory = traceFactoryProvider.get(consumerRecord);
            traceFactoryReference.compareAndSet(null, traceFactory);
        }
        return traceFactory.createTrace(traceContext, consumerRecord);
    }

    private static class TraceFactoryProvider {

        private final boolean isHeaderRecorded;

        public TraceFactoryProvider(boolean isHeaderRecorded) {
            this.isHeaderRecorded = isHeaderRecorded;
        }

        private TraceFactory get(Object object) {
            if (KafkaClientUtils.supportHeaders(object.getClass())) {
                return new SupportContinueTraceFactory(isHeaderRecorded);
            } else {
                return new DefaultTraceFactory();
            }
        }

        private interface TraceFactory {

            Trace createTrace(TraceContext traceContext, ConsumerRecord consumerRecord);

        }

        private static class DefaultTraceFactory implements TraceFactory {

            final PLogger logger = PLoggerFactory.getLogger(this.getClass());
            final boolean isDebug = logger.isDebugEnabled();

            @Override
            public Trace createTrace(TraceContext traceContext, ConsumerRecord consumerRecord) {
                return createTrace0(traceContext, consumerRecord);
            }

            Trace createTrace0(TraceContext traceContext, ConsumerRecord consumerRecord) {
                final Trace trace = traceContext.newTraceObject();
                if (trace.canSampled()) {
                    final SpanRecorder recorder = trace.getSpanRecorder();
                    recordRootSpan(recorder, consumerRecord);
                    if (isDebug) {
                        logger.debug("TraceID not exist. start new trace.");
                    }
                    return trace;
                } else {
                    if (isDebug) {
                        logger.debug("TraceID not exist. camSampled is false. skip trace.");
                    }
                    return trace;
                }
            }

            void recordRootSpan(SpanRecorder recorder, ConsumerRecord consumerRecord) {
                recordRootSpan(recorder, consumerRecord, null, null);
            }

            void recordRootSpan(SpanRecorder recorder, ConsumerRecord consumerRecord, String parentApplicationName, String parentApplicationType) {
                recorder.recordServiceType(KafkaConstants.KAFKA_CLIENT);
                recorder.recordApi(ConsumerRecordEntryPointInterceptor.ENTRY_POINT_METHOD_DESCRIPTOR);

                String endPointAddress = getEndPointAddress(consumerRecord);
                String remoteAddress = getRemoteAddress(consumerRecord);
                endPointAddress = StringUtils.defaultIfEmpty(endPointAddress, remoteAddress);

                recorder.recordEndPoint(endPointAddress);
                recorder.recordRemoteAddress(remoteAddress);

                String topic = consumerRecord.topic();
                recorder.recordRpcName(createRpcName(consumerRecord));
                recorder.recordAcceptorHost(remoteAddress);
                recorder.recordAttribute(KafkaConstants.KAFKA_TOPIC_ANNOTATION_KEY, topic);

                recorder.recordAttribute(KafkaConstants.KAFKA_PARTITION_ANNOTATION_KEY, consumerRecord.partition());
                recorder.recordAttribute(KafkaConstants.KAFKA_OFFSET_ANNOTATION_KEY, consumerRecord.offset());

                if (StringUtils.hasText(parentApplicationName) && StringUtils.hasText(parentApplicationType)) {
                    recorder.recordParentApplication(parentApplicationName, NumberUtils.parseShort(parentApplicationType, ServiceType.UNDEFINED.getCode()));
                }
            }

            private String getEndPointAddress(Object endPointFieldAccessor) {
                String endPointAddress = null;
                if (endPointFieldAccessor instanceof EndPointFieldAccessor) {
                    endPointAddress = ((EndPointFieldAccessor) endPointFieldAccessor)._$PINPOINT$_getEndPoint();
                }

                return endPointAddress;
            }

            private String getRemoteAddress(Object remoteAddressFieldAccessor) {
                String remoteAddress = null;
                if (remoteAddressFieldAccessor instanceof RemoteAddressFieldAccessor) {
                    remoteAddress = ((RemoteAddressFieldAccessor) remoteAddressFieldAccessor)._$PINPOINT$_getRemoteAddress();
                }

                return StringUtils.defaultIfEmpty(remoteAddress, KafkaConstants.UNKNOWN);
            }

            private String createRpcName(ConsumerRecord consumerRecord) {
                StringBuilder rpcName = new StringBuilder("kafka://");
                rpcName.append("topic=").append(consumerRecord.topic());
                rpcName.append("?partition=").append(consumerRecord.partition());
                rpcName.append("&offset=").append(consumerRecord.offset());

                return rpcName.toString();
            }

        }

        private static class SupportContinueTraceFactory extends DefaultTraceFactory {

            private final HeaderRecorder headerRecorder;
            private final boolean headerRecorded;

            public SupportContinueTraceFactory(boolean isHeaderRecorded) {
                this.headerRecorded = isHeaderRecorded;
                this.headerRecorder = new DefaultHeaderRecorder();
            }

            @Override
            public Trace createTrace(TraceContext traceContext, ConsumerRecord consumerRecord) {
                org.apache.kafka.common.header.Headers headers = consumerRecord.headers();
                if (headers == null) {
                    return createTrace0(traceContext, consumerRecord);
                }

                if (!isSampled(headers)) {
                    // Even if this transaction is not a sampling target, we have to create Trace object to mark 'not sampling'.
                    // For example, if this transaction invokes rpc call, we can add parameter to tell remote node 'don't sample this transaction'
                    final Trace trace = traceContext.disableSampling();
                    if (isDebug) {
                        logger.debug("remotecall sampling flag found. skip trace");
                    }
                    return trace;
                }

                TraceId traceId = populateTraceIdFromHeaders(traceContext, headers);
                final Trace trace;
                if (traceId != null) {
                    trace = createContinueTrace(traceContext, consumerRecord, traceId);
                } else {
                    trace = createTrace0(traceContext, consumerRecord);
                }
                if (trace.canSampled() && headerRecorded) {
                    headerRecorder.record(trace.getSpanRecorder(), consumerRecord);
                }
                return trace;
            }

            private boolean isSampled(org.apache.kafka.common.header.Headers headers) {
                org.apache.kafka.common.header.Header sampledHeader = headers.lastHeader(Header.HTTP_SAMPLED.toString());
                if (sampledHeader == null) {
                    return true;
                }

                String sampledFlag = BytesUtils.toString(sampledHeader.value());
                return SamplingFlagUtils.isSamplingFlag(sampledFlag);
            }

            private TraceId populateTraceIdFromHeaders(TraceContext traceContext, org.apache.kafka.common.header.Headers headers) {
                String transactionId = null;
                String spanID = null;
                String parentSpanID = null;
                String flags = null;
                for (org.apache.kafka.common.header.Header header : headers.toArray()) {
                    if (header.key().equals(Header.HTTP_TRACE_ID.toString())) {
                        transactionId = BytesUtils.toString(header.value());
                    } else if (header.key().equals(Header.HTTP_PARENT_SPAN_ID.toString())) {
                        parentSpanID = BytesUtils.toString(header.value());
                    } else if (header.key().equals(Header.HTTP_SPAN_ID.toString())) {
                        spanID = BytesUtils.toString(header.value());
                    } else if (header.key().equals(Header.HTTP_FLAGS.toString())) {
                        flags = BytesUtils.toString(header.value());
                    }
                }

                if (transactionId == null || spanID == null || parentSpanID == null || flags == null) {
                    return null;
                }

                TraceId traceId = traceContext.createTraceId(transactionId, Long.parseLong(parentSpanID), Long.parseLong(spanID), Short.parseShort(flags));
                return traceId;
            }

            private Trace createContinueTrace(TraceContext traceContext, ConsumerRecord consumerRecord, TraceId traceId) {
                if (isDebug) {
                    logger.debug("TraceID exist. continue trace. traceId:{}", traceId);
                }

                Trace trace = traceContext.continueTraceObject(traceId);

                String parentApplicationName = null;
                String parentApplicationType = null;

                org.apache.kafka.common.header.Headers headers = consumerRecord.headers();
                for (org.apache.kafka.common.header.Header header : headers.toArray()) {
                    if (header.key().equals(Header.HTTP_PARENT_APPLICATION_NAME.toString())) {
                        parentApplicationName = BytesUtils.toString(header.value());
                    } else if (header.key().equals(Header.HTTP_PARENT_APPLICATION_TYPE.toString())) {
                        parentApplicationType = BytesUtils.toString(header.value());
                    }
                }

                if (trace.canSampled()) {
                    final SpanRecorder recorder = trace.getSpanRecorder();
                    recordRootSpan(recorder, consumerRecord, parentApplicationName, parentApplicationType);
                }
                return trace;
            }

        }

    }

}
