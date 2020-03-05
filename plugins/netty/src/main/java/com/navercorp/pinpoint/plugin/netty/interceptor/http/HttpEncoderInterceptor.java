/*
 * Copyright 2018 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.plugin.netty.interceptor.http;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessorUtils;
import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.bootstrap.context.scope.TraceScope;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.request.ClientHeaderAdaptor;
import com.navercorp.pinpoint.bootstrap.plugin.request.ClientRequestAdaptor;
import com.navercorp.pinpoint.bootstrap.plugin.request.ClientRequestRecorder;
import com.navercorp.pinpoint.bootstrap.plugin.request.ClientRequestWrapper;
import com.navercorp.pinpoint.bootstrap.plugin.request.ClientRequestWrapperAdaptor;
import com.navercorp.pinpoint.bootstrap.plugin.request.DefaultRequestTraceWriter;
import com.navercorp.pinpoint.bootstrap.plugin.request.RequestTraceWriter;
import com.navercorp.pinpoint.plugin.netty.NettyClientRequestWrapper;
import com.navercorp.pinpoint.plugin.netty.NettyConfig;
import com.navercorp.pinpoint.plugin.netty.NettyConstants;
import com.navercorp.pinpoint.plugin.netty.NettyUtils;
import com.navercorp.pinpoint.plugin.netty.field.accessor.AsyncStartFlagFieldAccessor;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpMessage;

/**
 * @author Taejin Koo
 */
public class HttpEncoderInterceptor implements AroundInterceptor {

    protected final PLogger logger = PLoggerFactory.getLogger(getClass());
    protected final boolean isDebug = logger.isDebugEnabled();
    protected static final String ASYNC_TRACE_SCOPE = AsyncContext.ASYNC_TRACE_SCOPE;

    private final TraceContext traceContext;
    protected final MethodDescriptor methodDescriptor;
    private final ClientRequestRecorder<ClientRequestWrapper> clientRequestRecorder;
    private final RequestTraceWriter<HttpMessage> requestTraceWriter;

    public HttpEncoderInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor) {
        if (traceContext == null) {
            throw new NullPointerException("traceContext");
        }
        if (methodDescriptor == null) {
            throw new NullPointerException("methodDescriptor");
        }

        this.traceContext = traceContext;
        this.methodDescriptor = methodDescriptor;
        final NettyConfig config = new NettyConfig(traceContext.getProfilerConfig());

        ClientRequestAdaptor<ClientRequestWrapper> clientRequestAdaptor = ClientRequestWrapperAdaptor.INSTANCE;
        this.clientRequestRecorder = new ClientRequestRecorder<ClientRequestWrapper>(config.isParam(), clientRequestAdaptor);
        ClientHeaderAdaptor<HttpMessage> clientHeaderAdaptor = new HttpMessageClientHeaderAdaptor();
        this.requestTraceWriter = new DefaultRequestTraceWriter<HttpMessage>(clientHeaderAdaptor, traceContext);
    }

    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }

        if (!validate(args)) {
            return;
        }

        final Trace trace = traceContext.currentRawTraceObject();
        if (trace == null) {
            beforeAsync(target, args);
        } else {
            before0(trace, target, args);
        }
    }

    private void before0(Trace trace, Object target, Object[] args) {
        if (!trace.canSampled()) {
            final HttpMessage httpMessage = (HttpMessage) args[1];
            this.requestTraceWriter.write(httpMessage);
            return;
        }
        final SpanEventRecorder recorder = trace.traceBlockBegin();
        doInBeforeTrace(recorder, trace, target, args);
    }

    private void beforeAsync(Object target, Object[] args) {
        ((AsyncStartFlagFieldAccessor) args[1])._$PINPOINT$_setAsyncStartFlag(true);

        final AsyncContext asyncContext = getAsyncContext(args[1]);
        if (asyncContext == null) {
            logger.debug("AsyncContext not found");
            return;
        }

        final Trace trace = getAsyncTrace(asyncContext);
        if (trace == null) {
            return;
        }

        // entry scope.
        entryAsyncTraceScope(trace);

        try {
            // trace event for default & async.
            final SpanEventRecorder recorder = trace.traceBlockBegin();
            doInBeforeTrace(recorder, trace, target, args);
        } catch (Throwable th) {
            if (logger.isWarnEnabled()) {
                logger.warn("BEFORE. Caused:{}", th.getMessage(), th);
            }
        }
    }

    protected void doInBeforeTrace(SpanEventRecorder recorder, Trace trace, Object target, Object[] args) {
        // generate next trace id.
        final TraceId nextId = trace.getTraceId().getNextTraceId();
        recorder.recordNextSpanId(nextId.getSpanId());
        recorder.recordServiceType(NettyConstants.SERVICE_TYPE_CODEC_HTTP);

        final ChannelHandlerContext channelHandlerContext = (ChannelHandlerContext) args[0];
        final HttpMessage httpMessage = (HttpMessage) args[1];
        final String host = getHost(channelHandlerContext);
        this.requestTraceWriter.write(httpMessage, nextId, host);
    }

    private String getHost(ChannelHandlerContext channelHandlerContext) {
        if (channelHandlerContext != null) {
            final Channel channel = channelHandlerContext.channel();
            if (channel != null) {
                return NettyUtils.getEndPoint(channel.remoteAddress());
            }
        }
        return null;
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args, result, throwable);
        }

        if (!validate(args)) {
            return;
        }

        boolean async = ((AsyncStartFlagFieldAccessor) args[1])._$PINPOINT$_getAsyncStartFlag();
        if (async) {
            afterAsync(target, args, result, throwable);
        } else {
            after0(target, args, result, throwable);
        }
    }

    private void after0(Object target, Object[] args, Object result, Throwable throwable) {
        final Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }
        final SpanEventRecorder recorder = trace.currentSpanEventRecorder();
        try {
            doInAfterTrace(recorder, target, args, result, throwable);
        } finally {
            trace.traceBlockEnd();
        }
    }

    private void afterAsync(Object target, Object[] args, Object result, Throwable throwable) {
        final AsyncContext asyncContext = getAsyncContext(args[1]);
        if (asyncContext == null) {
            logger.debug("AsyncContext not found");
            return;
        }

        final Trace trace = asyncContext.currentAsyncTraceObject();
        if (trace == null) {
            return;
        }

        // leave scope.
        if (!leaveAsyncTraceScope(trace)) {
            if (logger.isWarnEnabled()) {
                logger.warn("Failed to leave scope of async trace {}.", trace);
            }
            // delete unstable trace.
            deleteAsyncContext(trace, asyncContext);
            return;
        }

        try {
            final SpanEventRecorder recorder = trace.currentSpanEventRecorder();
            doInAfterTrace(recorder, target, args, result, throwable);
        } catch (Throwable th) {
            if (logger.isWarnEnabled()) {
                logger.warn("AFTER error. Caused:{}", th.getMessage(), th);
            }
        } finally {
            trace.traceBlockEnd();
            if (isAsyncTraceDestination(trace)) {
                deleteAsyncContext(trace, asyncContext);
            }
        }
    }

    protected void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) {
        recorder.recordApi(methodDescriptor);
        recorder.recordException(throwable);
        final ChannelHandlerContext channelHandlerContext = (ChannelHandlerContext) args[0];
        final HttpMessage httpMessage = (HttpMessage) args[1];
        this.clientRequestRecorder.record(recorder, new NettyClientRequestWrapper(httpMessage, channelHandlerContext), throwable);
    }

    protected AsyncContext getAsyncContext(Object target) {
        return AsyncContextAccessorUtils.getAsyncContext(target);
    }

    private Trace getAsyncTrace(AsyncContext asyncContext) {
        final Trace trace = asyncContext.continueAsyncTraceObject();
        if (trace == null) {
            if (logger.isWarnEnabled()) {
                logger.warn("Failed to continue async trace. 'result is null'");
            }
            return null;
        }
        if (isDebug) {
            logger.debug("getAsyncTrace() trace {}, asyncContext={}", trace, asyncContext);
        }

        return trace;
    }

    private void deleteAsyncContext(final Trace trace, AsyncContext asyncContext) {
        if (isDebug) {
            logger.debug("Delete async trace {}.", trace);
        }

        trace.close();
        asyncContext.close();
    }

    private void entryAsyncTraceScope(final Trace trace) {
        final TraceScope scope = trace.getScope(ASYNC_TRACE_SCOPE);
        if (scope != null) {
            scope.tryEnter();
        }
    }

    private boolean leaveAsyncTraceScope(final Trace trace) {
        final TraceScope scope = trace.getScope(ASYNC_TRACE_SCOPE);
        if (scope != null) {
            if (scope.canLeave()) {
                scope.leave();
            } else {
                return false;
            }
        }
        return true;
    }

    private boolean isAsyncTraceDestination(final Trace trace) {
        if (!trace.isAsync()) {
            return false;
        }

        final TraceScope scope = trace.getScope(ASYNC_TRACE_SCOPE);
        return scope != null && !scope.isActive();
    }

    private boolean validate(Object[] args) {
        if (args.length != 3) {
            return false;
        }

        if (!(args[0] instanceof ChannelHandlerContext)) {
            return false;
        }
        ChannelHandlerContext channelHandlerContext = (ChannelHandlerContext) args[0];
        Channel channel = channelHandlerContext.channel();
        if (channel == null) {
            return false;
        }

        if (!(args[1] instanceof HttpMessage)) {
            return false;
        }
        HttpMessage httpMessage = (HttpMessage) args[1];
        if (httpMessage.headers() == null) {
            return false;
        }
        if (!(args[1] instanceof AsyncContextAccessor)) {
            return false;
        }
        if (!(args[1] instanceof AsyncStartFlagFieldAccessor)) {
            return false;
        }

        return true;
    }
}