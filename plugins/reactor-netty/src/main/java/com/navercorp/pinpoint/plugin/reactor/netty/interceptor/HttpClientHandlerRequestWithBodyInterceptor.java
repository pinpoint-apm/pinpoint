/*
 * Copyright 2023 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.reactor.netty.interceptor;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AsyncContextSpanEventApiIdAwareAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.plugin.util.SocketAddressUtils;
import com.navercorp.pinpoint.common.plugin.util.HostAndPort;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.util.ArrayUtils;
import com.navercorp.pinpoint.plugin.reactor.netty.ReactorNettyConstants;
import reactor.netty.channel.ChannelOperations;

import java.net.InetSocketAddress;

/**
 * @author jaehong.kim
 */
public class HttpClientHandlerRequestWithBodyInterceptor extends AsyncContextSpanEventApiIdAwareAroundInterceptor {

    public HttpClientHandlerRequestWithBodyInterceptor(TraceContext traceContext) {
        super(traceContext);
    }

    @Override
    public void beforeTrace(AsyncContext asyncContext, Trace trace, SpanEventRecorder recorder, Object target, int apiId, Object[] args) {
        if (ArrayUtils.isEmpty(args)) {
            // Skip
            return;
        }

        // Set HttpClientOptions
        if (args[0] instanceof AsyncContextAccessor) {
            ((AsyncContextAccessor) args[0])._$PINPOINT$_setAsyncContext(asyncContext);
            if (isDebug) {
                logger.debug("Set asyncContext to args[0]. asyncContext={}", asyncContext);
            }
        }

        if (trace.canSampled()) {
            // Set hostname
            if (args[0] instanceof ChannelOperations) {
                try {
                    final ChannelOperations channelOperations = (ChannelOperations) args[0];
                    final InetSocketAddress inetSocketAddress = (InetSocketAddress) channelOperations.channel().remoteAddress();
                    if (inetSocketAddress != null) {
                        final String hostName = SocketAddressUtils.getHostNameFirst(inetSocketAddress);
                        if (hostName != null) {
                            recorder.recordAttribute(AnnotationKey.HTTP_INTERNAL_DISPLAY, HostAndPort.toHostAndPortString(hostName, inetSocketAddress.getPort()));
                        }
                    }
                } catch (Exception ignored) {
                }
            }
        }
    }

    @Override
    public void doInBeforeTrace(SpanEventRecorder recorder, AsyncContext asyncContext, Object target, int apiId, Object[] args) {
    }

    @Override
    public void afterTrace(AsyncContext asyncContext, Trace trace, SpanEventRecorder recorder, Object target, int apiId, Object[] args, Object result, Throwable throwable) {
        if (trace.canSampled()) {
            recorder.recordApiId(apiId);
            recorder.recordException(throwable);
            recorder.recordServiceType(ReactorNettyConstants.REACTOR_NETTY_CLIENT_INTERNAL);
        }
    }

    @Override
    public void doInAfterTrace(SpanEventRecorder recorder, Object target, int apiId, Object[] args, Object result, Throwable throwable) {
    }
}