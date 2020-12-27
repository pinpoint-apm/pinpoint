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
import com.navercorp.pinpoint.bootstrap.interceptor.SpanRecursiveAroundInterceptor;
import com.navercorp.pinpoint.plugin.rocketmq.RocketMQConstants;
import com.navercorp.pinpoint.plugin.rocketmq.field.accessor.EndPointFieldAccessor;

/**
 * @author messi-gao
 */
public class ProducerSendInterceptor extends SpanRecursiveAroundInterceptor {
    private static final String SCOPE_NAME = "ROCKETMQ_ASYNC_TRACE_SCOPE";
    private final MethodDescriptor methodDescriptor;
    private final TraceContext traceContext;

    public ProducerSendInterceptor(MethodDescriptor methodDescriptor, TraceContext traceContext) {
        super(traceContext, methodDescriptor, SCOPE_NAME);
        this.methodDescriptor = methodDescriptor;
        this.traceContext = traceContext;
    }

    @Override
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
        final Trace next;
        AsyncContextAccessor sendCallback = getSendCallback(args);
        if (sendCallback != null) {
            next = ((AsyncContextAccessor) target)._$PINPOINT$_getAsyncContext().continueAsyncTraceObject();
        } else {
            next = traceContext.currentRawTraceObject();
        }
        TraceId nextId = next.getTraceId().getNextTraceId();
        recorder.recordNextSpanId(nextId.getSpanId());
        // set header
        final StringBuilder properties = new StringBuilder(sendMessageRequestHeader.getProperties());
        final Map<String, String> paramMap = new HashMap<>();
        paramMap.put(Header.HTTP_FLAGS.toString(), String.valueOf(nextId.getFlags()));
        paramMap.put(Header.HTTP_PARENT_APPLICATION_NAME.toString(), traceContext.getApplicationName());
        paramMap.put(Header.HTTP_PARENT_APPLICATION_TYPE.toString(),
                     String.valueOf(traceContext.getServerTypeCode()));
        paramMap.put(Header.HTTP_PARENT_SPAN_ID.toString(), String.valueOf(nextId.getParentSpanId()));
        paramMap.put(Header.HTTP_SPAN_ID.toString(), String.valueOf(nextId.getSpanId()));
        paramMap.put(Header.HTTP_TRACE_ID.toString(), nextId.getTransactionId());
        paramMap.put(RocketMQConstants.ENDPOINT, endPoint);

        for (Map.Entry<String, String> entry : paramMap.entrySet()) {
            properties.append(entry.getKey());
            properties.append(NAME_VALUE_SEPARATOR);
            properties.append(entry.getValue());
            properties.append(PROPERTY_SEPARATOR);
        }
        sendMessageRequestHeader.setProperties(properties.toString());
    }

    @Override
    protected Trace createTrace(Object target, Object[] args) {
        AsyncContextAccessor sendCallback = getSendCallback(args);
        if (sendCallback != null) {
            AsyncContext asyncContext = sendCallback._$PINPOINT$_getAsyncContext();
            ((AsyncContextAccessor) target)._$PINPOINT$_setAsyncContext(asyncContext);
            return asyncContext.continueAsyncTraceObject();
        }
        return traceContext.currentRawTraceObject();
    }

    @Override
    protected void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args,
                                  Object result, Throwable throwable) {
        if (throwable != null) {
            recorder.recordException(throwable);
        }
    }

    private AsyncContextAccessor getSendCallback(Object[] args) {
        return (AsyncContextAccessor) args[6];
    }
}
