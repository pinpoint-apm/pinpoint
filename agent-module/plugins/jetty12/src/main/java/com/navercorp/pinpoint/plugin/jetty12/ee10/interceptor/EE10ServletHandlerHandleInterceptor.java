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
package com.navercorp.pinpoint.plugin.jetty12.ee10.interceptor;

import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.plugin.RequestRecorderFactory;
import com.navercorp.pinpoint.plugin.jetty12.interceptor.AbstractServletHandlerHandleInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.jetty.ee10.servlet.ServletContextRequest;
import org.eclipse.jetty.ee10.servlet.ServletContextResponse;
import org.eclipse.jetty.server.Request;

/**
 * Entry point interceptor for Jetty 12 EE10. Hooks
 * {@code org.eclipse.jetty.ee10.servlet.ServletHandler#handle(Request, Response, Callback)}
 * and extracts the request/response by unwrapping the core {@code Request}
 * argument to the EE10 {@code ServletContextRequest}.
 */
public class EE10ServletHandlerHandleInterceptor extends AbstractServletHandlerHandleInterceptor {

    public EE10ServletHandlerHandleInterceptor(TraceContext traceContext, RequestRecorderFactory<HttpServletRequest> requestRecorderFactory) {
        super(traceContext, requestRecorderFactory);
    }

    private ServletContextRequest toServletContextRequest(Object[] args) {
        if (args == null || args.length < 1 || !(args[0] instanceof Request)) {
            return null;
        }
        return Request.as((Request) args[0], ServletContextRequest.class);
    }

    @Override
    protected HttpServletRequest toHttpServletRequest(Object[] args) {
        final ServletContextRequest contextRequest = toServletContextRequest(args);
        return contextRequest != null ? contextRequest.getServletApiRequest() : null;
    }

    @Override
    protected HttpServletResponse toHttpServletResponse(Object[] args) {
        final ServletContextRequest contextRequest = toServletContextRequest(args);
        if (contextRequest == null) {
            return null;
        }
        final ServletContextResponse contextResponse = contextRequest.getServletContextResponse();
        return contextResponse != null ? contextResponse.getServletApiResponse() : null;
    }
}
