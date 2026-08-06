/*
 * Copyright 2026 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.spring.webflux.interceptor;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessorUtils;
import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.bootstrap.interceptor.ResultReplaceAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogManager;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogger;
import com.navercorp.pinpoint.bootstrap.plugin.request.ClientRequestAdaptor;
import com.navercorp.pinpoint.bootstrap.plugin.request.ClientRequestRecorder;
import com.navercorp.pinpoint.bootstrap.plugin.request.ClientRequestWrapper;
import com.navercorp.pinpoint.bootstrap.plugin.request.ClientRequestWrapperAdaptor;
import com.navercorp.pinpoint.bootstrap.plugin.request.DefaultRequestTraceWriter;
import com.navercorp.pinpoint.bootstrap.plugin.request.RequestTraceWriter;
import com.navercorp.pinpoint.bootstrap.plugin.request.util.CookieExtractor;
import com.navercorp.pinpoint.bootstrap.plugin.request.util.CookieRecorder;
import com.navercorp.pinpoint.bootstrap.plugin.request.util.CookieRecorderFactory;
import com.navercorp.pinpoint.bootstrap.util.ScopeUtils;
import com.navercorp.pinpoint.common.util.ArrayArgumentUtils;
import com.navercorp.pinpoint.plugin.reactorsupport.SeamPublisherWrapper;
import com.navercorp.pinpoint.plugin.spring.webflux.SpringWebFluxConstants;
import com.navercorp.pinpoint.plugin.spring.webflux.SpringWebFluxPluginConfig;
import org.springframework.http.client.reactive.ClientHttpRequest;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;

/**
 * Wrapping variant of {@link BodyInserterRequestBuilderWriteToInterceptor} (config-gated by
 * {@code profiler.spring.webflux.wrap.publisher}). The returned publisher is replaced with a
 * wrapped one instead of receiving the next AsyncContext through its injected accessor field.
 * The original passes its conditionally-begun TraceBlock from before() to after() through the
 * weaver; a result-replace interceptor has no such channel, so the state travels on a
 * per-thread frame stack (writeTo runs synchronously, so before/after pair up on one thread).
 * <p>
 * The pairing is structural: before() pushes EXACTLY one frame on every invocation — including
 * early returns and failures — and after() polls exactly one, so a frame can never be claimed by
 * the wrong (e.g. an outer recursive) invocation. after() operates on the trace the frame
 * carries, never on the ambient thread-local, so a lost or foreign binding cannot mispair the
 * cleanup either.
 */
public class WrappingBodyInserterRequestBuilderWriteToInterceptor implements ResultReplaceAroundInterceptor {
    private final PluginLogger logger = PluginLogManager.getLogger(getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    // one entry per before() invocation; EMPTY marks "my before() did nothing to undo".
    static final class Frame {
        static final Frame EMPTY = new Frame();
        AsyncContext asyncContext;
        Trace trace;
        boolean begun;
    }

    private static final ThreadLocal<Deque<Frame>> FRAMES = new ThreadLocal<Deque<Frame>>() {
        @Override
        protected Deque<Frame> initialValue() {
            return new ArrayDeque<>();
        }
    };

    private final TraceContext traceContext;
    private final MethodDescriptor methodDescriptor;
    private final ClientRequestRecorder<ClientRequestWrapper> clientRequestRecorder;
    private final CookieRecorder<ClientHttpRequest> cookieRecorder;
    private final RequestTraceWriter<ClientHttpRequest> requestTraceWriter;

    public WrappingBodyInserterRequestBuilderWriteToInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor) {
        this.traceContext = Objects.requireNonNull(traceContext, "traceContext");
        this.methodDescriptor = Objects.requireNonNull(methodDescriptor, "methodDescriptor");

        final SpringWebFluxPluginConfig config = new SpringWebFluxPluginConfig(traceContext.getProfilerConfig());
        final ClientRequestAdaptor<ClientRequestWrapper> clientRequestAdaptor = ClientRequestWrapperAdaptor.INSTANCE;
        this.clientRequestRecorder = new ClientRequestRecorder<>(config.isParam(), clientRequestAdaptor);

        final CookieExtractor<ClientHttpRequest> cookieExtractor = new ClientHttpRequestCookieExtractor();
        this.cookieRecorder = CookieRecorderFactory.newCookieRecorder(config.getHttpDumpConfig(), cookieExtractor);

        final ClientHttpRequestClientHeaderAdaptor clientHeaderAdaptor = new ClientHttpRequestClientHeaderAdaptor();
        this.requestTraceWriter = new DefaultRequestTraceWriter<>(clientHeaderAdaptor, traceContext);
    }

    @Override
    public void before(Object target, Class<?> returnType, Object[] args) {
        Frame frame = Frame.EMPTY;
        try {
            final AsyncContext asyncContext = AsyncContextAccessorUtils.getAsyncContext(target);
            if (asyncContext == null) {
                return;
            }
            final Trace trace = asyncContext.continueAsyncTraceObject(true);
            if (trace == null) {
                return;
            }

            ScopeUtils.entryAsyncTraceScope(trace);
            // the frame records exactly how far this invocation got, so after() unwinds no more
            // and no less than what actually happened.
            frame = new Frame();
            frame.asyncContext = asyncContext;
            frame.trace = trace;

            if (checkBeforeTraceBlockBegin(trace, args)) {
                final SpanEventRecorder recorder = trace.traceBlockBegin();
                frame.begun = true;

                final ClientHttpRequest request = ArrayArgumentUtils.getArgument(args, 0, ClientHttpRequest.class);
                final TraceId nextId = trace.getTraceId().getNextTraceId();
                recorder.recordNextSpanId(nextId.getSpanId());
                final ClientRequestWrapper clientRequestWrapper = new WebClientRequestWrapper(request);
                requestTraceWriter.write(request, nextId, clientRequestWrapper.getDestinationId());

                recorder.recordServiceType(SpringWebFluxConstants.SPRING_WEBFLUX_CLIENT);
            }
        } catch (Throwable th) {
            if (logger.isWarnEnabled()) {
                logger.warn("BEFORE. Caused:{}", th.getMessage(), th);
            }
        } finally {
            // structural pairing: EVERY before() pushes exactly one frame - early returns and
            // failures push EMPTY - so after() can never claim another invocation's frame.
            FRAMES.get().push(frame);
        }
    }

    // same gate as BodyInserterRequestBuilderWriteToInterceptor.checkBeforeTraceBlockBegin
    private boolean checkBeforeTraceBlockBegin(Trace trace, Object[] args) {
        final ClientHttpRequest request = ArrayArgumentUtils.getArgument(args, 0, ClientHttpRequest.class);
        if (request == null) {
            return false;
        }

        if (requestTraceWriter.isNested(request)) {
            return false;
        }

        if (Boolean.FALSE == trace.canSampled()) {
            requestTraceWriter.write(request);
            return false;
        }

        return true;
    }

    @Override
    public Object after(Object target, Class<?> returnType, Object[] args, Object result, Throwable throwable) {
        final Deque<Frame> frames = FRAMES.get();
        final Frame frame = frames.poll();
        if (frames.isEmpty()) {
            // do not leave an empty deque parked on every pooled thread.
            FRAMES.remove();
        }
        if (frame == null || frame.trace == null) {
            // frame == null: no paired before() ran (defensive); EMPTY: before() did nothing.
            return result;
        }
        // unwind exactly what the paired before() recorded - never the ambient thread-local,
        // which may belong to an outer invocation or have been unbound in between.
        final AsyncContext asyncContext = frame.asyncContext;
        final Trace trace = frame.trace;
        final boolean begun = frame.begun;

        if (!ScopeUtils.leaveAsyncTraceScope(trace)) {
            if (logger.isWarnEnabled()) {
                logger.warn("Failed to leave scope of async trace {}.", trace);
            }
            deleteAsyncContext(trace, asyncContext);
            return result;
        }

        Object ret = result;
        try {
            if (begun) {
                final SpanEventRecorder recorder = trace.currentSpanEventRecorder();
                recorder.recordApi(methodDescriptor);
                recorder.recordException(throwable);

                final ClientHttpRequest request = ArrayArgumentUtils.getArgument(args, 0, ClientHttpRequest.class);
                final ClientRequestWrapper clientRequestWrapper = new WebClientRequestWrapper(request);
                this.clientRequestRecorder.record(recorder, clientRequestWrapper, throwable);
                this.cookieRecorder.record(recorder, request, throwable);

                if (throwable == null && SeamPublisherWrapper.isWrappable(result)) {
                    // make asynchronous trace-id
                    final AsyncContext nextAsyncContext = recorder.recordNextAsyncContext();
                    ret = SeamPublisherWrapper.wrap(result, nextAsyncContext);
                    if (isDebug) {
                        logger.debug("Wrapped result publisher. asyncContext={}", nextAsyncContext);
                    }
                }
            }
        } catch (Throwable th) {
            if (logger.isWarnEnabled()) {
                logger.warn("AFTER error. Caused:{}", th.getMessage(), th);
            }
            ret = result;
        } finally {
            if (begun) {
                trace.traceBlockEnd();
            }
            if (ScopeUtils.isAsyncTraceEndScope(trace)) {
                deleteAsyncContext(trace, asyncContext);
            }
        }
        return ret;
    }

    private void deleteAsyncContext(Trace trace, AsyncContext asyncContext) {
        if (isDebug) {
            logger.debug("Delete async trace {}.", trace);
        }
        trace.close();
        asyncContext.close();
    }
}
