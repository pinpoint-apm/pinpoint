/*
 * Copyright 2024 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.plugin.ktor.interceptor;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessorUtils;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptorHelper;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.ApiIdAwareAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogManager;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogger;
import com.navercorp.pinpoint.bootstrap.plugin.RequestRecorderFactory;
import com.navercorp.pinpoint.bootstrap.plugin.request.RequestAdaptor;
import com.navercorp.pinpoint.bootstrap.plugin.request.ServerCookieRecorder;
import com.navercorp.pinpoint.bootstrap.plugin.request.ServerHeaderRecorder;
import com.navercorp.pinpoint.bootstrap.plugin.request.ServletRequestListener;
import com.navercorp.pinpoint.bootstrap.plugin.request.ServletRequestListenerBuilder;
import com.navercorp.pinpoint.bootstrap.plugin.request.util.ParameterRecorder;
import com.navercorp.pinpoint.common.util.ArrayArgumentUtils;
import com.navercorp.pinpoint.plugin.ktor.KtorConstants;
import com.navercorp.pinpoint.plugin.ktor.KtorPluginConfig;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpRequest;


public class NettyHttp1HandlerHandleRequestInterceptor implements ApiIdAwareAroundInterceptor {
    private final PluginLogger logger = PluginLogManager.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();
    private final TraceContext traceContext;
    private final ServletRequestListener<HttpRequestAndContext> servletRequestListener;


    public NettyHttp1HandlerHandleRequestInterceptor(TraceContext traceContext, RequestRecorderFactory<HttpRequestAndContext> requestRecorderFactory) {
        this.traceContext = traceContext;
        final KtorPluginConfig config = new KtorPluginConfig(traceContext.getProfilerConfig());
        RequestAdaptor<HttpRequestAndContext> requestAdaptor = new HttpRequestAdaptor();
        ParameterRecorder<HttpRequestAndContext> parameterRecorder = ParameterRecorderFactory.newParameterRecorderFactory(config.getExcludeProfileMethodFilter(), config.isTraceRequestParam());

        ServletRequestListenerBuilder<HttpRequestAndContext> reqBuilder = new ServletRequestListenerBuilder<>(KtorConstants.KTOR, traceContext, requestAdaptor);
        reqBuilder.setExcludeURLFilter(config.getExcludeUrlFilter());
        reqBuilder.setTraceExcludeMethodFilter(config.getTraceExcludeMethodFilter());
        reqBuilder.setParameterRecorder(parameterRecorder);
        reqBuilder.setRequestRecorderFactory(requestRecorderFactory);

        final ProfilerConfig profilerConfig = traceContext.getProfilerConfig();
        reqBuilder.setRealIpSupport(config.getRealIpHeader(), config.getRealIpEmptyValue());
        reqBuilder.setHttpStatusCodeRecorder(profilerConfig.getHttpStatusCodeErrors());
        reqBuilder.setServerHeaderRecorder(profilerConfig.readList(ServerHeaderRecorder.CONFIG_KEY_RECORD_REQ_HEADERS));
        reqBuilder.setServerCookieRecorder(profilerConfig.readList(ServerCookieRecorder.CONFIG_KEY_RECORD_REQ_COOKIES));

        this.servletRequestListener = reqBuilder.build();
    }

    @Override
    public void before(Object target, int apiId, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }
        try {
            final ChannelHandlerContext ctx = ArrayArgumentUtils.getArgument(args, 0, ChannelHandlerContext.class);
            final HttpRequest request = ArrayArgumentUtils.getArgument(args, 1, HttpRequest.class);
            if (ctx == null || request == null) {
                return;
            }

            MethodDescriptor methodDescriptor = MethodDescriptorHelper.apiId(apiId);
            final HttpRequestAndContext httpRequestAndContext = new HttpRequestAndContext(request, ctx);
            this.servletRequestListener.initialized(httpRequestAndContext, KtorConstants.KTOR_INTERNAL, methodDescriptor);

            // Set end-point
            final Trace trace = this.traceContext.currentRawTraceObject();
            if (trace == null) {
                return;
            }

            final SpanEventRecorder recorder = trace.currentSpanEventRecorder();
            if (recorder != null) {
                // make asynchronous trace-id
                final AsyncContext asyncContext = recorder.recordNextAsyncContext();
                // HttpRequest
                AsyncContextAccessorUtils.setAsyncContext(asyncContext, args, 1);
                if (isDebug) {
                    logger.debug("Set asyncContext to args[2]. asyncContext={}", asyncContext);
                }
            }
        } catch (Throwable t) {
            logger.info("Failed to request event handle.", t);
        }
    }

    @Override
    public void after(Object target, int apiId, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args, result, throwable);
        }

        try {
            final ChannelHandlerContext ctx = ArrayArgumentUtils.getArgument(args, 0, ChannelHandlerContext.class);
            final HttpRequest request = ArrayArgumentUtils.getArgument(args, 1, HttpRequest.class);
            if (ctx == null || request == null) {
                return;
            }

            final int statusCode = getStatusCode(request);
            final HttpRequestAndContext httpRequestAndContext = new HttpRequestAndContext(request, ctx);
            this.servletRequestListener.destroyed(httpRequestAndContext, throwable, statusCode);
        } catch (Throwable t) {
            logger.info("Failed to request event handle.", t);
        }
    }

    private int getStatusCode(final HttpRequest response) {
        return 0;
    }
}
