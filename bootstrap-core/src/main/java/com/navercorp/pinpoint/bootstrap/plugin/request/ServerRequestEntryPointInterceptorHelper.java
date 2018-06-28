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

package com.navercorp.pinpoint.bootstrap.plugin.request;

import com.navercorp.pinpoint.bootstrap.config.Filter;
import com.navercorp.pinpoint.bootstrap.config.SkipFilter;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.proxy.ProxyHttpHeaderRecorder;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.util.Assert;

public class ServerRequestEntryPointInterceptorHelper {
    private PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();
    private final boolean isTrace = logger.isTraceEnabled();

    private final TraceContext traceContext;
    private final Filter<String> excludeUrlFilter;
    private final ProxyHttpHeaderRecorder proxyHttpHeaderRecorder;
    private final ServerRequestRecorder serverRequestRecorder = new ServerRequestRecorder();
    private final RequestTraceReader requestTraceReader;

    public ServerRequestEntryPointInterceptorHelper(final TraceContext traceContext, final Filter<String> excludeUrlFilter) {
        this.traceContext = traceContext;
        if (excludeUrlFilter != null) {
            this.excludeUrlFilter = excludeUrlFilter;
        } else {
            this.excludeUrlFilter = new SkipFilter<String>();
        }
        this.proxyHttpHeaderRecorder = new ProxyHttpHeaderRecorder(traceContext.getProfilerConfig().isProxyHttpHeaderEnable());
        this.requestTraceReader = new RequestTraceReader(traceContext, true);
    }

    public Trace accept(final ServerRequestWrapper serverRequestWrapper, final ServiceType entryPointServiceType, final MethodDescriptor entryPointMethodDescriptor) {
        Assert.requireNonNull(serverRequestWrapper, "serverRequestWrapper must not be null");
        Assert.requireNonNull(entryPointServiceType, "entryPointServiceType must not be null");
        Assert.requireNonNull(entryPointMethodDescriptor, "entryPointMethodDescriptor must not be null");

        if (isDebug) {
            logger.debug("Accept serverRequestWrapper={}, entryPointServiceType={}, entryPointMethodDescriptor={}", serverRequestWrapper, entryPointServiceType, entryPointMethodDescriptor);
        }

        final String requestURI = serverRequestWrapper.getRpcName();
        if (this.excludeUrlFilter.filter(requestURI)) {
            if (isTrace) {
                logger.trace("Filter requestURI={}", requestURI);
            }
            return null;
        }

        final Trace trace = this.requestTraceReader.read(serverRequestWrapper);
        if (trace.canSampled()) {
            final SpanRecorder recorder = trace.getSpanRecorder();
            // record root span
            recorder.recordServiceType(entryPointServiceType);
            recorder.recordApi(entryPointMethodDescriptor);
            this.serverRequestRecorder.record(recorder, serverRequestWrapper);
            // record proxy HTTP header.
            this.proxyHttpHeaderRecorder.record(recorder, serverRequestWrapper.getProxyHeaderMap());
        }
        return trace;
    }
}