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

package com.navercorp.pinpoint.plugin.rabbitmq.client.interceptor;

import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScope;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.common.util.MapUtils;
import com.navercorp.pinpoint.plugin.rabbitmq.client.RabbitMQClientConstants;
import com.navercorp.pinpoint.plugin.rabbitmq.client.field.setter.HeadersFieldSetter;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Method;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>{@code AMQContentHeader} received as an argument to the constructor is sharable and can be reused, such as
 * {@code MessageProperties.MINIMAL_BASIC}. Any changes made to it (ie injecting pinpoint headers) may have undesirable
 * consequences.
 *
 * <p>Hence, we make a copy via {@code ChannelAspect} and add pinpoint headers to it when propagating trace, and have
 * {@code AMQCommand} use this.
 *
 * @author HyunGil Jeong
 *
 * @see com.navercorp.pinpoint.plugin.rabbitmq.client.aspect.ChannelAspect
 */
public class AMQCommandConstructInterceptor implements AroundInterceptor {

    // AMQP spec
    private static final String AMQP_METHOD_TO_INTERCEPT = "basic.publish";

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final TraceContext traceContext;
    private final InterceptorScope scope;

    public AMQCommandConstructInterceptor(TraceContext traceContext, InterceptorScope scope) {
        this.traceContext = traceContext;
        this.scope = scope;
    }

    @Override
    public void before(Object target, Object[] args) {
        if (!validate(target, args)) {
            return;
        }

        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }

        Trace trace = traceContext.currentRawTraceObject();
        if (trace == null) {
            return;
        }

        final AMQP.BasicProperties properties = (AMQP.BasicProperties) args[1];
        final Map<String, Object> headers = createHeader(properties, trace);
        if (headers != null) {
            ((HeadersFieldSetter) properties)._$PINPOINT$_setHeaders(headers);
        }
    }

    private Map<String, Object> createHeader(AMQP.BasicProperties properties, Trace trace) {
        final Map<String, Object> headers = copyHeader(properties);
        if (trace.canSampled()) {
            TraceId nextId = retrieveNextTraceId();
            if (nextId == null) {
                return null;
            }
            headers.put(RabbitMQClientConstants.META_TRACE_ID, nextId.getTransactionId());
            headers.put(RabbitMQClientConstants.META_SPAN_ID, Long.toString(nextId.getSpanId()));
            headers.put(RabbitMQClientConstants.META_PARENT_SPAN_ID, Long.toString(nextId.getParentSpanId()));
            headers.put(RabbitMQClientConstants.META_PARENT_APPLICATION_TYPE, Short.toString(traceContext.getServerTypeCode()));
            headers.put(RabbitMQClientConstants.META_PARENT_APPLICATION_NAME, traceContext.getApplicationName());
            headers.put(RabbitMQClientConstants.META_FLAGS, Short.toString(nextId.getFlags()));
        } else {
            headers.put(RabbitMQClientConstants.META_SAMPLED, "1");
        }
        return headers;
    }

    private Map<String, Object> copyHeader(AMQP.BasicProperties properties) {
        final Map<String, Object> headers = properties.getHeaders();
        if (MapUtils.isEmpty(headers)) {
            return new HashMap<String, Object>();
        }
        // headers wrapped as unmodifiable map
        return new HashMap<String, Object>(headers);
    }

    private TraceId retrieveNextTraceId() {
        Object attachment = scope.getCurrentInvocation().getAttachment();
        if (attachment == null) {
            if (isDebug) {
                logger.debug("Invalid attachment. Expected {}, but got null", TraceId.class.getName());
            }
            return null;
        }
        if (!(attachment instanceof TraceId)) {
            if (isDebug) {
                logger.debug("Invalid attachment. Expected {}, but got {}", TraceId.class.getName(), attachment.getClass());
            }
            return null;
        }
        return (TraceId) attachment;
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (!validate(target, args)) {
            return;
        }

        if (isDebug) {
            logger.afterInterceptor(target, args);
        }
    }

    private boolean validate(Object target, Object[] args) {
        if (args == null) {
            if (isDebug) {
                logger.debug("Expected arguments, but found none.");
            }
            return false;
        }
        if (args.length != 3) {
            if (isDebug) {
                logger.debug("Expected 3 arguments, but found {}", args.length);
            }
            return false;
        }
        Object method = args[0];
        if (method == null) {
            // valid, but this won't be null producer side
            return false;
        }
        if (!(method instanceof Method)) {
            if (isDebug) {
                logger.debug("Expected args[0] to be {}, but was {}", Method.class.getName(), method.getClass().getName());
            }
            return false;
        }
        if (!AMQP_METHOD_TO_INTERCEPT.equals(((Method) method).protocolMethodName())) {
            return false;
        }
        Object contentHeader = args[1];
        if (!(contentHeader instanceof AMQP.BasicProperties)) {
            // skip header injection for null, or non AMQP.BasicProperties header
            return false;
        }
        if (!(contentHeader instanceof HeadersFieldSetter)) {
            if (isDebug) {
                logger.debug("Invalid args[1]({}) object. Need field setter({})", contentHeader, HeadersFieldSetter.class.getName());
            }
            return false;
        }
        return true;
    }
}
