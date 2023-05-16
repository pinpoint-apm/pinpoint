/*
 * Copyright 2021 NAVER Corp.
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

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;
import java.util.Map;

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
import com.navercorp.pinpoint.common.plugin.util.HostAndPort;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.plugin.rocketmq.RocketMQConstants;
import com.navercorp.pinpoint.plugin.rocketmq.description.EntryPointMethodDescriptor;
import com.navercorp.pinpoint.plugin.rocketmq.field.accessor.ChannelFutureGetter;
import com.navercorp.pinpoint.plugin.rocketmq.field.accessor.ChannelTablesAccessor;

/**
 * @author messi-gao
 */
public class ConsumerMessageEntryPointInterceptor extends SpanRecursiveAroundInterceptor {

    protected static final String SCOPE_NAME = "##ROCKETMQ_ENTRY_POINT_START_TRACE";

    protected static final EntryPointMethodDescriptor ENTRY_POINT_METHOD_DESCRIPTOR =
            new EntryPointMethodDescriptor();

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
        final List<MessageExt> msgs = (List<MessageExt>) args[0];
        if (msgs.isEmpty()) {
            return null;
        }
        return createTrace(target, msgs);
    }

    private Trace createTrace(Object target, List<MessageExt> msgs) {
        TraceFactoryProvider.TraceFactory traceFactory = TraceFactoryProvider.get(msgs);
        return traceFactory.createTrace(target, traceContext, msgs);
    }

    private static class TraceFactoryProvider {

        private static TraceFactory get(List<MessageExt> msgs) {
            if (msgs.size() == 1) {
                return new SupportContinueTraceFactory();
            } else {
                return new DefaultTraceFactory();
            }
        }

        private interface TraceFactory {

            Trace createTrace(Object target, TraceContext traceContext, List<MessageExt> msgs);

        }

        private static class DefaultTraceFactory implements TraceFactory {

            final PLogger logger = PLoggerFactory.getLogger(this.getClass());
            final boolean isDebug = logger.isDebugEnabled();

            @Override
            public Trace createTrace(Object target, TraceContext traceContext, List<MessageExt> msgs) {
                return createTrace0(target, traceContext, msgs);
            }

            Trace createTrace0(Object target, TraceContext traceContext, List<MessageExt> msgs) {
                final Trace trace = traceContext.newTraceObject();
                if (trace.canSampled()) {
                    final SpanRecorder recorder = trace.getSpanRecorder();
                    recordRootSpan(target, recorder, msgs);
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

            void recordRootSpan(Object target, SpanRecorder recorder, List<MessageExt> msgs) {
                recordRootSpan(target, recorder, msgs, null, null);
            }

            void recordRootSpan(Object target, SpanRecorder recorder, List<MessageExt> msgs,
                                String parentApplicationName,
                                String parentApplicationType) {
                recorder.recordServiceType(RocketMQConstants.ROCKETMQ_CLIENT);
                recorder.recordApi(ENTRY_POINT_METHOD_DESCRIPTOR);

                final MessageExt messageExt = msgs.get(0);

                String acceptorHost = null;
                if (msgs.size() == 1) {
                    acceptorHost = messageExt.getUserProperty(RocketMQConstants.ACCEPTOR_HOST);
                }
                acceptorHost = StringUtils.defaultIfEmpty(acceptorHost, RocketMQConstants.UNKNOWN);

                recorder.recordRemoteAddress(acceptorHost);
                recorder.recordAcceptorHost(acceptorHost);
                recordEndPoint(target, recorder, messageExt);

                final String topic = messageExt.getTopic();
                recorder.recordRpcName(createRpcName(recorder, topic, msgs));
                recorder.recordAttribute(RocketMQConstants.ROCKETMQ_TOPIC_ANNOTATION_KEY, topic);

                if (StringUtils.hasText(parentApplicationName) && StringUtils.hasText(parentApplicationType)) {
                    recorder.recordParentApplication(parentApplicationName, NumberUtils
                            .parseShort(parentApplicationType, ServiceType.UNDEFINED.getCode()));
                }
            }

            private void recordEndPoint(Object target, SpanRecorder recorder, MessageExt messageExt) {
                String endPointAddress = RocketMQConstants.UNKNOWN;
                ChannelTablesAccessor channelTablesAccessor = (ChannelTablesAccessor) target;
                Map<String, Object> channelTables = channelTablesAccessor._$PINPOINT$_getChannelTables();
                SocketAddress socketAddress = messageExt.getStoreHost();
                String brokenAddr = getEndPoint(socketAddress);
                ChannelFutureGetter channelFutureGetter = (ChannelFutureGetter) channelTables.get(brokenAddr);
                if (channelFutureGetter != null) {
                    SocketAddress consumerAddress =
                            channelFutureGetter._$PINPOINT$_getChannelFuture().channel().localAddress();
                    endPointAddress = getEndPoint(consumerAddress);
                }
                recorder.recordEndPoint(endPointAddress);
                recorder.recordAttribute(RocketMQConstants.ROCKETMQ_BROKER_SERVER_STATUS_ANNOTATION_KEY,
                                         brokenAddr);
            }

            private String getEndPoint(SocketAddress socketAddress) {
                if (socketAddress instanceof InetSocketAddress) {
                    final InetSocketAddress inetSocketAddress = (InetSocketAddress) socketAddress;
                    final InetAddress remoteAddress = inetSocketAddress.getAddress();
                    if (remoteAddress != null) {
                        return HostAndPort.toHostAndPortString(remoteAddress.getHostAddress(),
                                                               inetSocketAddress.getPort());
                    }
                    // Warning : InetSocketAddressAvoid unnecessary DNS lookup  (warning:InetSocketAddress.getHostName())
                    final String hostName = inetSocketAddress.getHostName();
                    if (hostName != null) {
                        return HostAndPort.toHostAndPortString(hostName, inetSocketAddress.getPort());
                    }
                }
                return null;
            }

            private String createRpcName(SpanRecorder recorder, String topic, List<MessageExt> msgs) {
                final StringBuilder rpcName = new StringBuilder("rocketmq://");
                rpcName.append("topic=").append(topic);
                if (msgs.size() == 1) {
                    MessageExt messageExt = msgs.get(0);
                    int queueId = messageExt.getQueueId();
                    long commitLogOffset = messageExt.getCommitLogOffset();
                    rpcName.append("?partition=").append(queueId);
                    rpcName.append("&offset=").append(commitLogOffset);
                    recorder.recordAttribute(RocketMQConstants.ROCKETMQ_PARTITION_ANNOTATION_KEY, queueId);
                    recorder.recordAttribute(RocketMQConstants.ROCKETMQ_OFFSET_ANNOTATION_KEY, commitLogOffset);
                } else {
                    rpcName.append("?batch=").append(msgs.size());
                    recorder.recordAttribute(RocketMQConstants.ROCKETMQ_BATCH_ANNOTATION_KEY, msgs.size());
                }
                return rpcName.toString();
            }
        }

        private static class SupportContinueTraceFactory extends DefaultTraceFactory {

            @Override
            public Trace createTrace(Object target, TraceContext traceContext, List<MessageExt> msgs) {
                final MessageExt messageExt = msgs.get(0);
                if (!SamplingFlagUtils.isSamplingFlag(
                        messageExt.getUserProperty(Header.HTTP_FLAGS.name()))) {
                    final Trace trace = traceContext.disableSampling();
                    if (isDebug) {
                        logger.debug("remotecall sampling flag found. skip trace");
                    }
                    return trace;
                }

                final TraceId traceId = populateTraceIdFromHeaders(traceContext, messageExt);
                if (traceId != null) {
                    return createContinueTrace(target, traceContext, msgs, traceId);
                } else {
                    return createTrace0(target, traceContext, msgs);
                }
            }

            private TraceId populateTraceIdFromHeaders(TraceContext traceContext, MessageExt messageExt) {
                final String transactionId = messageExt.getUserProperty(Header.HTTP_TRACE_ID.toString());
                final String spanID = messageExt.getUserProperty(Header.HTTP_SPAN_ID.toString());
                final String parentSpanID = messageExt.getUserProperty(Header.HTTP_PARENT_SPAN_ID.toString());
                final String flags = messageExt.getUserProperty(Header.HTTP_FLAGS.toString());

                if (transactionId == null || spanID == null || parentSpanID == null || flags == null) {
                    return null;
                }

                return traceContext.createTraceId(transactionId, Long.parseLong(parentSpanID),
                                                  Long.parseLong(spanID), Short.parseShort(flags));
            }

            private Trace createContinueTrace(Object target, TraceContext traceContext, List<MessageExt> msgs,
                                              TraceId traceId) {
                if (isDebug) {
                    logger.debug("TraceID exist. continue trace. traceId:{}", traceId);
                }
                final Message consumerRecord = msgs.get(0);
                final boolean isAsyncSend = Boolean.parseBoolean(
                        consumerRecord.getUserProperty(RocketMQConstants.IS_ASYNC_SEND));
                final String parentApplicationName = consumerRecord.getUserProperty(
                        Header.HTTP_PARENT_APPLICATION_NAME.toString());
                final String parentApplicationType = consumerRecord.getUserProperty(
                        Header.HTTP_PARENT_APPLICATION_TYPE.toString());

                final Trace trace;
                if (isAsyncSend) {
                    trace = traceContext.continueAsyncTraceObject(traceId);
                } else {
                    trace = traceContext.continueTraceObject(traceId);
                }

                if (trace.canSampled()) {
                    final SpanRecorder recorder = trace.getSpanRecorder();
                    recordRootSpan(target, recorder, msgs, parentApplicationName, parentApplicationType);
                }
                return trace;
            }

        }

    }

}
