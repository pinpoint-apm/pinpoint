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
package com.navercorp.pinpoint.plugin.jetty.interceptor;

import com.navercorp.pinpoint.bootstrap.config.Filter;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.plugin.request.ServerRequestTrace;
import com.navercorp.pinpoint.plugin.jetty.JettyServerRequestTrace;
import org.eclipse.jetty.server.HttpChannel;
import org.eclipse.jetty.server.Request;

/**
 * @author Taejin Koo
 * @author jaehong.kim
 */
public class ServerHandleInterceptor extends AbstractServerHandleInterceptor {
    // jetty-9.x
    public ServerHandleInterceptor(TraceContext traceContext, MethodDescriptor descriptor, Filter<String> excludeFilter) {
        super(traceContext, descriptor, excludeFilter);
    }

    @Override
    protected Request getRequest(final Object[] args) {
        if (args == null || args.length < 1) {
            return null;
        }

        if (args[0] instanceof HttpChannel) {
            final HttpChannel<?> channel = (HttpChannel<?>) args[0];
            return channel.getRequest();
        }
        return null;
    }

    @Override
    ServerRequestTrace getServerRequestTrace(final Request request) {
        return new JettyServerRequestTrace(request) {
            public String _getHeader(String name) {
                if (request != null && request.getHttpFields() != null) {
                    return request.getHttpFields().get(name);
                }
                return null;
            }
        };
    }
}