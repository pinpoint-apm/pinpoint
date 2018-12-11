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

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.plugin.RequestRecorderFactory;
import org.eclipse.jetty.server.HttpConnection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Taejin Koo
 * @author jaehong.kim
 * <p>
 * jetty-8.0
 */
public class Jetty80ServerHandleInterceptor extends AbstractServerHandleInterceptor {

    public Jetty80ServerHandleInterceptor(TraceContext traceContext, MethodDescriptor descriptor, RequestRecorderFactory requestRecorderFactory) {
        super(traceContext, descriptor, requestRecorderFactory);
    }

    @Override
    HttpServletRequest toHttpServletRequest(Object[] args) {
        if (args == null || args.length < 1) {
            return null;
        }

        if (args[0] instanceof HttpConnection) {
            try {
                HttpConnection connection = (HttpConnection) args[0];
                return connection.getRequest();
            } catch (Throwable ignored) {
            }
        }
        return null;
    }

    @Override
    HttpServletResponse toHttpServletResponse(Object[] args) {
        if (args == null || args.length < 1) {
            return null;
        }

        if (args[0] instanceof HttpConnection) {
            try {
                HttpConnection connection = (HttpConnection) args[0];
                return connection.getResponse();
            } catch (Throwable ignored) {
            }
        }
        return null;

    }
}
