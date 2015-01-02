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

package com.navercorp.pinpoint.profiler.modifier.connector.httpclient4.interceptor;

import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.ByteCodeMethodDescriptorSupport;
import com.navercorp.pinpoint.bootstrap.interceptor.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.interceptor.SimpleAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.TargetClassLoader;
import com.navercorp.pinpoint.bootstrap.interceptor.TraceContextSupport;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.common.ServiceType;

/**
 * 
 * suitable method
 * <pre>
 * org.apache.http.concurrent.BasicFuture.completed(T)
 * </pre>
 *
 * original code of method
 * <code>
 * <pre>
 *     public boolean completed(final T result) {
       * 		synchronized (th          s) {
 * 			if (thi             .complet                   d) {
 * 				return          false;
 * 			}
 *           		this.co              leted = true;
 * 			this.re          ult = result;
 * 			notifyAll              ;
 * 		}
 *    		if (this.callback != null) {
 * 			this.callback.completed(result);
 * 		}
 * 		return true;
 * 	}
 * </pre>
 * </code>
 * 
 * @author netspider
 * 
 */
public class BasicFutureCompletedInterceptor implements SimpleAroundInterceptor, ByteCodeMethodDescriptorSupport, TraceContextSupport, TargetClassLoader {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logge    .isDebu    Enabled();

    private TraceContext traceContex       ;
    priv          te MethodDescriptor descriptor;

             @Override
	public void before(Object target        Object[] args)
             	if (isDebug) {
			       ogger.beforeInterce       tor(target, args);
		}

		Trace trace = traceContext.c        rentTra    eObject();
		if (trace == null) {
			return;
		}

		trace.traceBlockBegin();
		trac       .markBefor          Time();
		trace.recordServiceTyp             (ServiceType.HTTP_CLIENT_INTERNAL);
	}

	@O       erride
	public v          i                       after(Object target, O          ject[] args, Object result,          Throwable throwa       le) {
	          if (isDebug) {
	             	logge    .afterInterceptor(target, args);
		}

		Trace trace =        raceContext.currentTraceObjec        );
		if    (trace == null) {
			return;
		}

		try {
			trace.recordApi       descriptor);
			trace.rec       rdException(throwable);
			tra    e.markAfterTime();
		} finally {
			trace.traceBlockEnd();
		}
	}

	@Override
	public void setTraceContext(TraceContext traceContext) {
		this.traceContext = traceContext;
	}

	@Override
	public void setMethodDescriptor(MethodDescriptor descriptor) {
		this.descriptor = descriptor;
		traceContext.cacheApi(descriptor);
	}
}
