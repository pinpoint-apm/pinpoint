/*
 * Copyright 2025 NAVER Corp.
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
package com.navercorp.pinpoint.plugin.pulsar.interceptor;

import com.navercorp.pinpoint.bootstrap.context.Header;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.SpanRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.bootstrap.interceptor.SpanRecursiveAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogManager;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogger;
import com.navercorp.pinpoint.bootstrap.sampler.SamplingFlagUtils;
import com.navercorp.pinpoint.bootstrap.util.NumberUtils;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.plugin.pulsar.PulsarConstants;
import com.navercorp.pinpoint.plugin.pulsar.description.PulsarEntryMethodDescriptor;
import com.navercorp.pinpoint.plugin.pulsar.field.accessor.TopicInfoAccessor;
import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.MessageId;
import org.apache.pulsar.client.impl.ConsumerImpl;
import org.apache.pulsar.common.naming.TopicName;

import java.util.Objects;

/**
 * @author zhouzixin@apache.org
 */
public class ConsumerImplEntryPointInterceptor extends SpanRecursiveAroundInterceptor {

    private static final String SCOPE_NAME = "##PULSAR_ENTRY_POINT_START_TRACE";
    private static final PulsarEntryMethodDescriptor ENTRY_POINT_METHOD_DESCRIPTOR =
            new PulsarEntryMethodDescriptor();

    protected ConsumerImplEntryPointInterceptor(final TraceContext traceContext,
                                                final MethodDescriptor methodDescriptor) {
        super(traceContext, methodDescriptor, SCOPE_NAME);
        traceContext.cacheApi(ENTRY_POINT_METHOD_DESCRIPTOR);
    }

    @Override
    protected void doInBeforeTrace(final SpanEventRecorder recorder, final Object target, final Object[] args) {
        recorder.recordServiceType(PulsarConstants.PULSAR_CLIENT_INTERNAL);
    }

    @Override
    protected void doInAfterTrace(final SpanEventRecorder recorder,
                                  final Object target,
                                  final Object[] args,
                                  final Object result,
                                  final Throwable throwable) {
        recorder.recordApi(methodDescriptor);
        recorder.recordException(throwable);
    }

    @Override
    protected Trace createTrace(final Object target, final Object[] args) {
        final Message<?> msgs = (Message<?>) args[0];
        if (Objects.isNull(msgs)) {
            return null;
        }
        return createTrace(target, msgs);
    }

    private Trace createTrace(Object target, Message<?> message) {
        TraceFactoryProvider.TraceFactory traceFactory = TraceFactoryProvider.get(message);
        return traceFactory.createTrace(target, traceContext, message);
    }

    private static class TraceFactoryProvider {
        public static TraceFactory get(Message<?> message) {
            if (message.getProperties().isEmpty()) {
                return new DefaultTraceFactory();
            } else {
                return new SupportContinueTraceFactory();
            }
        }

        private interface TraceFactory {
            Trace createTrace(Object target, TraceContext traceContext, Message<?> msgs);
        }

        private static class DefaultTraceFactory implements TraceFactory {

            protected final PluginLogger logger = PluginLogManager.getLogger(this.getClass());
            protected final boolean isDebug = logger.isDebugEnabled();

            @Override
            public Trace createTrace(final Object target, final TraceContext traceContext, final Message<?> message) {
                return createTrace0(target, traceContext, message);
            }

            protected Trace createTrace0(final Object target, final TraceContext traceContext, final Message<?> message) {
                final Trace trace = traceContext.newTraceObject();
                if (trace.canSampled()) {
                    final SpanRecorder recorder = trace.getSpanRecorder();
                    recordRootSpan(target, recorder, message);
                    if (isDebug) {
                        logger.debug("TraceID not exist. start new trace.");
                    }
                } else {
                    if (isDebug) {
                        logger.debug("TraceID not exist. camSampled is false. skip trace.");
                    }
                }
                return trace;
            }

            private void recordRootSpan(final Object target, final SpanRecorder recorder, final Message<?> message) {
                recordRootSpan(target, recorder, message, null, null);
            }

            protected void recordRootSpan(final Object target,
                                          final SpanRecorder recorder,
                                          final Message<?> message,
                                          final String parentApplicationName,
                                          final String parentApplicationType) {
                recorder.recordServiceType(PulsarConstants.PULSAR_CLIENT);
                recorder.recordApi(ENTRY_POINT_METHOD_DESCRIPTOR);

                final ConsumerImpl<?> consumer = (ConsumerImpl<?>) target;
                String serviceUrl = consumer.getClient().getLookup().getServiceUrl();
                recorder.recordEndPoint(serviceUrl);
                recorder.recordRemoteAddress(serviceUrl);

                final String topic = ((TopicInfoAccessor) target)._$PINPOINT$_getTopicInfo();
                recorder.recordRpcName(createRpcName(recorder, topic));
                recorder.recordAttribute(PulsarConstants.PULSAR_TOPIC_ANNOTATION_KEY, topic);
                MessageId messageId = message.getMessageId();
                if (messageId != null) {
                    recorder.recordAttribute(PulsarConstants.PULSAR_MESSAGE_ID_ANNOTATION_KEY, messageId.toString());
                    recorder.recordAttribute(PulsarConstants.PULSAR_MESSAGE_SIZE_ANNOTATION_KEY, message.size());
                }

                if (StringUtils.hasText(parentApplicationName) && StringUtils.hasText(parentApplicationType)) {
                    recorder.recordParentApplication(
                            parentApplicationName, NumberUtils.parseShort(
                                    parentApplicationType,
                                    ServiceType.UNDEFINED.getCode()
                            )
                    );
                }
            }

            private String createRpcName(final SpanRecorder recorder, String topic) {
                final StringBuilder rpcName = new StringBuilder("pulsar://");
                rpcName.append("topic=").append(topic);
                int partitionIndex = TopicName.getPartitionIndex(topic);
                recorder.recordAttribute(PulsarConstants.PULSAR_PARTITION_ANNOTATION_KEY, partitionIndex);
                rpcName.append("&partition=").append(partitionIndex);
                return rpcName.toString();
            }
        }

        private static class SupportContinueTraceFactory extends DefaultTraceFactory {
            @Override
            public Trace createTrace(Object target, TraceContext traceContext, Message<?> message) {
                String flag = message.getProperties().get(Header.HTTP_FLAGS.name());
                if (!SamplingFlagUtils.isSamplingFlag(flag)) {
                    final Trace trace = traceContext.disableSampling();
                    if (isDebug) {
                        logger.debug("remotecall sampling flag found. skip trace");
                    }
                    return trace;
                }

                final TraceId traceId = populateTraceIdFromProperties(traceContext, message);
                if (traceId != null) {
                    return createContinueTrace(target, traceContext, message, traceId);
                } else {
                    return createTrace0(target, traceContext, message);
                }
            }

            private Trace createContinueTrace(Object target, TraceContext traceContext,
                                              Message<?> message, TraceId traceId) {
                if (isDebug) {
                    logger.debug("TraceID exist. continue trace. traceId:{}", traceId);
                }
                final boolean isAsyncSend = Boolean.parseBoolean(
                        message.getProperty(PulsarConstants.IS_ASYNC_SEND));
                final String parentApplicationName = message.getProperty(
                        Header.HTTP_PARENT_APPLICATION_NAME.toString());
                final String parentApplicationType = message.getProperty(
                        Header.HTTP_PARENT_APPLICATION_TYPE.toString());

                final Trace trace;
                if (isAsyncSend) {
                    trace = traceContext.continueAsyncTraceObject(traceId);
                } else {
                    trace = traceContext.continueTraceObject(traceId);
                }

                if (trace.canSampled()) {
                    final SpanRecorder recorder = trace.getSpanRecorder();
                    recordRootSpan(target, recorder, message, parentApplicationName, parentApplicationType);
                }
                return trace;
            }

            private TraceId populateTraceIdFromProperties(TraceContext traceContext, Message<?> message) {
                final String transactionId = message.getProperty(Header.HTTP_TRACE_ID.toString());
                final String spanID = message.getProperty(Header.HTTP_SPAN_ID.toString());
                final String parentSpanID = message.getProperty(Header.HTTP_PARENT_SPAN_ID.toString());
                final String flags = message.getProperty(Header.HTTP_FLAGS.toString());

                if (transactionId == null || spanID == null || parentSpanID == null || flags == null) {
                    return null;
                }

                return traceContext.createTraceId(transactionId, Long.parseLong(parentSpanID),
                        Long.parseLong(spanID), Short.parseShort(flags));
            }
        }
    }
}
