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

package com.navercorp.pinpoint.plugin.jetty.jakarta.interceptor;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.plugin.RequestRecorderFactory;
import com.navercorp.pinpoint.common.util.ArrayArgumentUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.HttpChannel;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;

/**
 * @author Taejin Koo
 * @author jaehong.kim
 * <p>
 * For jetty >= v11
 */
public class Jetty11xServerHandleInterceptor extends AbstractServerHandleInterceptor {

    public Jetty11xServerHandleInterceptor(TraceContext traceContext, MethodDescriptor descriptor, RequestRecorderFactory requestRecorderFactory) {
        super(traceContext, descriptor, requestRecorderFactory);
    }

    @Override
    HttpServletRequest toHttpServletRequest(Object[] args) {
        HttpChannel<?> channel = getArgument(args);
        if (channel != null) {
            Request request = channel.getRequest();
            if (request instanceof HttpServletRequest) {
                return (HttpServletRequest) request;
            }
        }
        return null;
    }

    @Override
    HttpServletResponse toHttpServletResponse(Object[] args) {
        HttpChannel<?> channel = getArgument(args);
        if (channel != null) {
            Response response = channel.getResponse();
            if (response instanceof HttpServletResponse) {
                return (HttpServletResponse) channel.getResponse();
            }
        }
        return null;
    }

    private HttpChannel getArgument(Object[] args) {
        return ArrayArgumentUtils.getArgument(args, 0, HttpChannel.class);
    }
}
