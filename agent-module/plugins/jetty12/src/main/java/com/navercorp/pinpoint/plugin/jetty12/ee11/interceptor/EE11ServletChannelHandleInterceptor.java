/*
 * Copyright 2026 NAVER Corp.
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
package com.navercorp.pinpoint.plugin.jetty12.ee11.interceptor;

import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.plugin.RequestRecorderFactory;
import com.navercorp.pinpoint.plugin.jetty12.interceptor.AbstractServletChannelHandleInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.jetty.ee11.servlet.ServletChannel;
import org.eclipse.jetty.ee11.servlet.ServletContextRequest;
import org.eclipse.jetty.ee11.servlet.ServletContextResponse;

/**
 * Entry point interceptor for Jetty 12 EE11. Hooks
 * {@code org.eclipse.jetty.ee11.servlet.ServletChannel#handle()} and extracts
 * the request/response via direct typed access on the EE11 servlet API.
 */
public class EE11ServletChannelHandleInterceptor extends AbstractServletChannelHandleInterceptor {

    public EE11ServletChannelHandleInterceptor(TraceContext traceContext, RequestRecorderFactory<HttpServletRequest> requestRecorderFactory) {
        super(traceContext, requestRecorderFactory);
    }

    @Override
    protected HttpServletRequest toHttpServletRequest(Object target) {
        if (!(target instanceof ServletChannel)) {
            return null;
        }
        final ServletContextRequest contextRequest = ((ServletChannel) target).getServletContextRequest();
        return contextRequest != null ? contextRequest.getServletApiRequest() : null;
    }

    @Override
    protected HttpServletResponse toHttpServletResponse(Object target) {
        if (!(target instanceof ServletChannel)) {
            return null;
        }
        final ServletContextResponse contextResponse = ((ServletChannel) target).getServletContextResponse();
        return contextResponse != null ? contextResponse.getServletApiResponse() : null;
    }
}
