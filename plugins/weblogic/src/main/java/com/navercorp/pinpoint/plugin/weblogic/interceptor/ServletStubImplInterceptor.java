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



import com.navercorp.pinpoint.bootstrap.config.Filter;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.annotation.TargetMethod;

import weblogic.servlet.internal.ServletRequestImpl;

/**
 * 
 * @author andyspan
 *
 */
@TargetMethod(name = "execute", paramTypes = { "weblogic.servlet.internal.ServletRequestImpl" , "weblogic.servlet.internal.ServletResponseImpl" })
public class ServletStubImplInterceptor extends AbstractServerHandleInterceptor {

    public ServletStubImplInterceptor(TraceContext traceContext, MethodDescriptor descriptor, Filter<String> excludeFilter) {
        super(traceContext, descriptor, excludeFilter);
    }

    @Override
    protected ServletRequestImpl getRequest(Object[] args) {
        final Object iRequestObject = args[0];
        if (!(iRequestObject instanceof weblogic.servlet.internal.ServletRequestImpl)) {
           return null;
        }
        return (ServletRequestImpl) iRequestObject;
    }

}
