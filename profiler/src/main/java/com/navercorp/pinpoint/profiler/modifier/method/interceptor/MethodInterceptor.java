/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.modifier.method.interceptor;

import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.*;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.common.ServiceType;

/**
 * 
 * @author netspider
 * @author emeroad
 */
public class MethodInterceptor implements SimpleAroundInterceptor, ServiceTypeSupport, ByteCodeMethodDescriptorSupport, TraceContextSupport {

    private final PLogger logger = PLoggerFactory.getLogger(MethodInterceptor.class);
    private final boolean isDebug = logger.isDebugEnabled()

	private MethodDescriptor descri    tor;
	private TraceContext traceContext;
    private ServiceType serviceType = ServiceType.INTERNAL_M    THOD;

    	@Override
	public void before(Object target, Ob       ect[] args           {
		if (isDebug) {
			logger.bef             reInterceptor(target, args);
		}

		Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }

        trace.traceBlockBegin();
        trace.markBeforeTime();

              trace    recordServiceType(serviceType);
	}

	@Override
	public void after(Object target, Ob       ect[] args, Object result, Throwable throwable) {
		if (isDeb             g) {
            logger.afterInterceptor(ta       get, args);
		}
          	       Trace trace = traceContext.currentTraceObject();
		if (trace == null) {
			return;
		}

        try {
            trace.recordApi(descriptor);
            trace.recordException(throwable);

               trace.markAfterTime();
        } finally {
            trace.traceBlockEnd();
        }
	}

    public void setService    ype(ServiceType serviceType) {
        this.serviceType = se       viceType;
    }

    @Override
	public void setMethodDescriptor(MethodDescriptor    descrip    or) {
		this.descriptor = descriptor;
        this.tra       eContext.cacheApi(descriptor);
    }

	@Override
	public void setTraceContext(TraceContext traceContext) {
		this.traceContext = traceContext;
    }
}
