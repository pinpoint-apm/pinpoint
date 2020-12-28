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

import static org.apache.rocketmq.common.message.MessageDecoder.NAME_VALUE_SEPARATOR;
import static org.apache.rocketmq.common.message.MessageDecoder.PROPERTY_SEPARATOR;

import java.util.HashMap;
import java.util.Map;

import org.apache.rocketmq.common.protocol.header.SendMessageRequestHeader;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.Header;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.bootstrap.context.scope.TraceScope;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.plugin.rocketmq.RocketMQConstants;
import com.navercorp.pinpoint.plugin.rocketmq.field.accessor.EndPointFieldAccessor;

/**
 * @author messi-gao
 */
public class ProducerSendInterceptor implements AroundInterceptor {
    private final PLogger logger = PLoggerFactory.getLogger(getClass());
    private final boolean isDebug = logger.isDebugEnabled();
    private static final String SCOPE_NAME = "ROCKETMQ_ASYNC_TRACE_SCOPE";
    private final MethodDescriptor methodDescriptor;
    private final TraceContext traceContext;

    public ProducerSendInterceptor(MethodDescriptor methodDescriptor, TraceContext traceContext) {
        this.methodDescriptor = methodDescriptor;
        this.traceContext = traceContext;
    }

    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }
        try {
            AsyncContextAccessor sendCallback = getSendCallback(args);
            Trace trace = traceContext.currentTraceObject();
            if (sendCallback != null) {
                if (isSkipTrace()) {
                    // Skip recursive invoked or duplicated span(entry point)
                    return;
                }
                AsyncContext asyncContext = sendCallback._$PINPOINT$_getAsyncContext();
                ((AsyncContextAccessor) target)._$PINPOINT$_setAsyncContext(asyncContext);
                trace = asyncContext.continueAsyncTraceObject();

                if (isDebug) {
                    logger.debug("Created trace. trace={}", trace);
                }

                if (trace == null) {
                    // Skip
                    return;
                }

                // init entry point scope
                if (!initScope(trace)) {
                    // Defense code
                    deleteTrace(trace);
                    return;
                }

                // entry point scope
                entryScope(trace);

            }
            if (!trace.canSampled()) {
                return;
            }
            // ------------------------------------------------------
            final SpanEventRecorder recorder = trace.traceBlockBegin();
            doInBeforeTrace(recorder, target, args);
        } catch (Throwable th) {
            if (logger.isWarnEnabled()) {
                logger.warn("BEFORE. Caused:{}", th.getMessage(), th);
            }
        }
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args, result, throwable);
        }

        final Trace trace = traceContext.currentRawTraceObject();
        if (trace == null) {
            return;
        }
        if (trace.isAsync()) {
            if (!hasScope(trace)) {
                // Not in scope
                return;
            }

            if (!leaveScope(trace)) {
                // Defense code
                deleteTrace(trace);
                return;
            }

            if (!isEndScope(trace)) {
                // Ignored recursive call.
                return;
            }

            if (!trace.canSampled()) {
                deleteTrace(trace);
                return;
            }
        }

        // ------------------------------------------------------
        try {
            final SpanEventRecorder recorder = trace.currentSpanEventRecorder();
            doInAfterTrace(recorder, target, args, result, throwable);
        } catch (Throwable th) {
            if (logger.isWarnEnabled()) {
                logger.warn("AFTER. Caused:{}", th.getMessage(), th);
            }
        } finally {
            trace.traceBlockEnd();
        }
    }

    protected void doInBeforeTrace(SpanEventRecorder recorder, Object target, Object[] args) {
        recorder.recordServiceType(RocketMQConstants.ROCKETMQ_CLIENT);
        recorder.recordApi(methodDescriptor);

        String endPoint = ((EndPointFieldAccessor) target)._$PINPOINT$_getEndPoint();
        recorder.recordEndPoint(endPoint);
        recorder.recordDestinationId(endPoint);

        final SendMessageRequestHeader sendMessageRequestHeader = (SendMessageRequestHeader) args[3];
        recorder.recordAttribute(RocketMQConstants.ROCKETMQ_TOPIC_ANNOTATION_KEY,
                                 sendMessageRequestHeader.getTopic());
        recorder.recordAttribute(RocketMQConstants.ROCKETMQ_PARTITION_ANNOTATION_KEY,
                                 sendMessageRequestHeader.getQueueId());
        Trace trace = traceContext.currentRawTraceObject();
        TraceId nextId = trace.getTraceId().getNextTraceId();
        recorder.recordNextSpanId(nextId.getSpanId());
        // set header
        final StringBuilder properties = new StringBuilder(sendMessageRequestHeader.getProperties());
        final Map<String, Object> paramMap = new HashMap<>();
        paramMap.put(Header.HTTP_FLAGS.toString(), String.valueOf(nextId.getFlags()));
        paramMap.put(Header.HTTP_PARENT_APPLICATION_NAME.toString(), traceContext.getApplicationName());
        paramMap.put(Header.HTTP_PARENT_APPLICATION_TYPE.toString(),
                     String.valueOf(traceContext.getServerTypeCode()));
        paramMap.put(Header.HTTP_PARENT_SPAN_ID.toString(), String.valueOf(nextId.getParentSpanId()));
        paramMap.put(Header.HTTP_SPAN_ID.toString(), String.valueOf(nextId.getSpanId()));
        paramMap.put(Header.HTTP_TRACE_ID.toString(), nextId.getTransactionId());
        paramMap.put(RocketMQConstants.ENDPOINT, endPoint);
        paramMap.put(RocketMQConstants.IS_ASYNC_SEND, trace.isAsync());

        for (Map.Entry<String, Object> entry : paramMap.entrySet()) {
            properties.append(entry.getKey());
            properties.append(NAME_VALUE_SEPARATOR);
            properties.append(entry.getValue());
            properties.append(PROPERTY_SEPARATOR);
        }
        sendMessageRequestHeader.setProperties(properties.toString());
    }

    protected void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args,
                                  Object result, Throwable throwable) {
        if (throwable != null) {
            recorder.recordException(throwable);
        }
    }

    private AsyncContextAccessor getSendCallback(Object[] args) {
        return (AsyncContextAccessor) args[6];
    }

    private boolean isSkipTrace() {
        final Trace trace = traceContext.currentRawTraceObject();
        if (trace == null) {
            return false;
        }
        if (hasScope(trace)) {
            // Entry Scope
            entryScope(trace);
            if (isDebug) {
                logger.debug("Skip recursive invoked");
            }
        } else {
            if (isDebug) {
                logger.debug("Skip duplicated entry point");
            }
        }
        // Skip recursive invoke or duplicated entry point
        return true;
    }

    private boolean initScope(final Trace trace) {
        final TraceScope oldScope = trace.addScope(SCOPE_NAME);
        if (oldScope != null) {
            // delete corrupted trace.
            if (logger.isInfoEnabled()) {
                logger.info("Duplicated trace scope={}.", oldScope.getName());
            }
            return false;
        }

        return true;
    }

    private void entryScope(final Trace trace) {
        final TraceScope scope = trace.getScope(SCOPE_NAME);
        if (scope != null) {
            scope.tryEnter();
            if (isDebug) {
                logger.debug("Try enter trace scope={}", scope.getName());
            }
        }
    }

    private boolean leaveScope(final Trace trace) {
        final TraceScope scope = trace.getScope(SCOPE_NAME);
        if (scope != null) {
            if (scope.canLeave()) {
                scope.leave();
                if (isDebug) {
                    logger.debug("Leave trace scope={}", scope.getName());
                }
            } else {
                if (logger.isInfoEnabled()) {
                    logger.info("Failed to leave scope. trace={}", trace);
                }
                return false;
            }
        }
        return true;
    }

    private boolean hasScope(final Trace trace) {
        final TraceScope scope = trace.getScope(SCOPE_NAME);
        return scope != null;
    }

    private boolean isEndScope(final Trace trace) {
        final TraceScope scope = trace.getScope(SCOPE_NAME);
        return scope != null && !scope.isActive();
    }

    private void deleteTrace(final Trace trace) {
        traceContext.removeTraceObject();
        trace.close();
        if (isDebug) {
            logger.debug("Delete trace.");
        }
    }
}
