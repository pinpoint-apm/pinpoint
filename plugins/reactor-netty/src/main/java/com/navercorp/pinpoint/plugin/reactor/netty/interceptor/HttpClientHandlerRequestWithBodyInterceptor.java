/*
 * Copyright 2020 NAVER Corp.
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
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AsyncContextSpanEventSimpleAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.plugin.util.SocketAddressUtils;
import com.navercorp.pinpoint.common.plugin.util.HostAndPort;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.plugin.reactor.netty.ReactorNettyConstants;
import reactor.netty.channel.ChannelOperations;

import java.net.InetSocketAddress;

/**
 * @author jaehong.kim
 */
public class HttpClientHandlerRequestWithBodyInterceptor extends AsyncContextSpanEventSimpleAroundInterceptor {

    public HttpClientHandlerRequestWithBodyInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor) {
        super(traceContext, methodDescriptor);
    }

    @Override
    public void doInBeforeTrace(SpanEventRecorder recorder, AsyncContext asyncContext, Object target, Object[] args) {
        if (args == null || args.length < 1) {
            // Skip
            return;
        }

        // Set HttpClientOptions
        if (args[0] instanceof AsyncContextAccessor) {
            ((AsyncContextAccessor) args[0])._$PINPOINT$_setAsyncContext(asyncContext);
        }
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

    @Override
    public void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) {
        recorder.recordApi(methodDescriptor);
        recorder.recordException(throwable);
        recorder.recordServiceType(ReactorNettyConstants.REACTOR_NETTY_CLIENT_INTERNAL);
    }
}
