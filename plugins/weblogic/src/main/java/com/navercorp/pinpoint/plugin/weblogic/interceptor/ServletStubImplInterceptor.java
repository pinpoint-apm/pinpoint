/*
 * Copyright 2016 Pinpoint contributors and NAVER Corp.
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
package com.navercorp.pinpoint.plugin.weblogic.interceptor;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;

import javax.servlet.http.HttpServletRequest;

/**
 * 
 * @author andyspan
 *
 */
public class ServletStubImplInterceptor extends AbstractServerHandleInterceptor {

    public ServletStubImplInterceptor(TraceContext traceContext, MethodDescriptor descriptor) {
        super(traceContext, descriptor);
    }

    @Override
    protected HttpServletRequest getRequest(Object[] args) {
        final Object iRequestObject = args[0];
        if (!(iRequestObject instanceof HttpServletRequest)) {
           return null;
        }
        return (HttpServletRequest) iRequestObject;
    }

}
