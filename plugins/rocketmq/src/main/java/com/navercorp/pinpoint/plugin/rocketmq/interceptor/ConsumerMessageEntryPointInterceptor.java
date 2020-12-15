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

package com.navercorp.pinpoint.plugin.rocketmq.interceptor;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;

import com.navercorp.pinpoint.bootstrap.context.Header;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.SpanRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.bootstrap.interceptor.SpanRecursiveAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.sampler.SamplingFlagUtils;
import com.navercorp.pinpoint.bootstrap.util.NumberUtils;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.plugin.rocketmq.RocketMQConstants;
import com.navercorp.pinpoint.plugin.rocketmq.description.EntryPointMethodDescriptor;
import com.navercorp.pinpoint.plugin.rocketmq.field.accessor.EndPointFieldAccessor;
import com.navercorp.pinpoint.plugin.rocketmq.field.accessor.RemoteAddressFieldAccessor;

/**
 * @author messi-gao
 */
public class ConsumerMessageEntryPointInterceptor extends SpanRecursiveAroundInterceptor {

    protected static final String SCOPE_NAME = "##ROCKETMQ_ENTRY_POINT_START_TRACE";

    protected static final EntryPointMethodDescriptor ENTRY_POINT_METHOD_DESCRIPTOR =
            new EntryPointMethodDescriptor();

    private final AtomicReference<TraceFactoryProvider.TraceFactory> tracyFactoryReference =
            new AtomicReference<TraceFactoryProvider.TraceFactory>();

    public ConsumerMessageEntryPointInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor) {
        super(traceContext, methodDescriptor, SCOPE_NAME);
        traceContext.cacheApi(ENTRY_POINT_METHOD_DESCRIPTOR);
    }

    @Override
    protected void doInBeforeTrace(SpanEventRecorder recorder, Object target, Object[] args) {
        recorder.recordServiceType(RocketMQConstants.ROCKETMQ_CLIENT_INTERNAL);
    }

    @Override
    protected void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result,
                                  Throwable throwable) {
        recorder.recordApi(methodDescriptor);
        recorder.recordException(throwable);

    }

    @Override
    protected Trace createTrace(Object target, Object[] args) {
        List<MessageExt> msgs = (List<MessageExt>) args[0];
        if (msgs.isEmpty()) {
            return null;
        }
        return createTrace(msgs);
    }

    private Trace createTrace(List<MessageExt> msgs) {
        TraceFactoryProvider.TraceFactory traceFactory = tracyFactoryReference.get();
        if (traceFactory == null) {
            traceFactory = TraceFactoryProvider.get();
            tracyFactoryReference.compareAndSet(null, traceFactory);
        }
        return traceFactory.createTrace(traceContext, msgs);
    }

    private static class TraceFactoryProvider {

        private static TraceFactory get() {
            return new SupportContinueTraceFactory();
        }

        private interface TraceFactory {

            Trace createTrace(TraceContext traceContext, List<MessageExt> msgs);

        }

        private static class DefaultTraceFactory implements TraceFactory {

            final PLogger logger = PLoggerFactory.getLogger(this.getClass());
            final boolean isDebug = logger.isDebugEnabled();

            @Override
            public Trace createTrace(TraceContext traceContext, List<MessageExt> msgs) {
                return createTrace0(traceContext, msgs);
            }

            Trace createTrace0(TraceContext traceContext, List<MessageExt> msgs) {
                final Trace trace = traceContext.newTraceObject();
                if (trace.canSampled()) {
                    final SpanRecorder recorder = trace.getSpanRecorder();
                    recordRootSpan(recorder, msgs);
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

            void recordRootSpan(SpanRecorder recorder, List<MessageExt> msgs) {
                recordRootSpan(recorder, msgs, null, null);
            }

            void recordRootSpan(SpanRecorder recorder, List<MessageExt> msgs, String parentApplicationName,
                                String parentApplicationType) {
                recorder.recordServiceType(RocketMQConstants.ROCKETMQ_CLIENT);
                recorder.recordApi(ENTRY_POINT_METHOD_DESCRIPTOR);

                MessageExt consumerRecord = msgs.get(0);

                String endPointAddress = getEndPointAddress(consumerRecord);
                String remoteAddress = getRemoteAddress(consumerRecord);
                if (StringUtils.isEmpty(endPointAddress)) {
                    endPointAddress = remoteAddress;
                }

                recorder.recordEndPoint(endPointAddress);
                recorder.recordRemoteAddress(remoteAddress);

                String topic = consumerRecord.getTopic();
                recorder.recordRpcName(createRpcName(topic, msgs.size()));
                recorder.recordAcceptorHost(remoteAddress);
                recorder.recordAttribute(RocketMQConstants.ROCKETMQ_TOPIC_ANNOTATION_KEY, topic);
                recorder.recordAttribute(RocketMQConstants.ROCKETMQ_BATCH_ANNOTATION_KEY, msgs.size());

                if (StringUtils.hasText(parentApplicationName) && StringUtils.hasText(parentApplicationType)) {
                    recorder.recordParentApplication(parentApplicationName, NumberUtils
                            .parseShort(parentApplicationType, ServiceType.UNDEFINED.getCode()));
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
                    remoteAddress = ((RemoteAddressFieldAccessor) remoteAddressFieldAccessor)
                            ._$PINPOINT$_getRemoteAddress();
                }

                if (StringUtils.isEmpty(remoteAddress)) {
                    return RocketMQConstants.UNKNOWN;
                } else {
                    return remoteAddress;
                }
            }

            private String createRpcName(String topic, int size) {
                StringBuilder rpcName = new StringBuilder("rocketmq://");
                rpcName.append("topic=").append(topic);
                rpcName.append("?batch=").append(size);

                return rpcName.toString();
            }

        }

        private static class SupportContinueTraceFactory extends DefaultTraceFactory {

            @Override
            public Trace createTrace(TraceContext traceContext, List<MessageExt> msgs) {
                MessageExt consumerRecord = msgs.get(0);
                if (!SamplingFlagUtils.isSamplingFlag(
                        consumerRecord.getUserProperty(Header.HTTP_FLAGS.name()))) {
                    // Even if this transaction is not a sampling target, we have to create Trace object to mark 'not sampling'.
                    // For example, if this transaction invokes rpc call, we can add parameter to tell remote node 'don't sample this transaction'
                    final Trace trace = traceContext.disableSampling();
                    if (isDebug) {
                        logger.debug("remotecall sampling flag found. skip trace");
                    }
                    return trace;
                }

                TraceId traceId = populateTraceIdFromHeaders(traceContext, consumerRecord);
                if (traceId != null) {
                    return createContinueTrace(traceContext, msgs, traceId);
                } else {
                    return createTrace0(traceContext, msgs);
                }
            }

            private TraceId populateTraceIdFromHeaders(TraceContext traceContext,
                                                       MessageExt messageExt) {
                String transactionId = messageExt.getUserProperty(Header.HTTP_TRACE_ID.toString());
                String spanID = messageExt.getUserProperty(Header.HTTP_SPAN_ID.toString());
                String parentSpanID = messageExt.getUserProperty(Header.HTTP_PARENT_SPAN_ID.toString());
                String flags = messageExt.getUserProperty(Header.HTTP_FLAGS.toString());

                if (transactionId == null || spanID == null || parentSpanID == null || flags == null) {
                    return null;
                }

                return traceContext.createTraceId(transactionId, Long.parseLong(parentSpanID),
                                                  Long.parseLong(spanID), Short.parseShort(flags));
            }

            private Trace createContinueTrace(TraceContext traceContext, List<MessageExt> msgs,
                                              TraceId traceId) {
                if (isDebug) {
                    logger.debug("TraceID exist. continue trace. traceId:{}", traceId);
                }

                Trace trace = traceContext.continueTraceObject(traceId);

                Message consumerRecord = msgs.get(0);
                String parentApplicationName = consumerRecord.getUserProperty(
                        Header.HTTP_PARENT_APPLICATION_NAME.toString());
                String parentApplicationType = consumerRecord.getUserProperty(
                        Header.HTTP_PARENT_APPLICATION_TYPE.toString());

                if (trace.canSampled()) {
                    final SpanRecorder recorder = trace.getSpanRecorder();
                    recordRootSpan(recorder, msgs, parentApplicationName, parentApplicationType);
                }
                return trace;
            }

        }

    }

}
