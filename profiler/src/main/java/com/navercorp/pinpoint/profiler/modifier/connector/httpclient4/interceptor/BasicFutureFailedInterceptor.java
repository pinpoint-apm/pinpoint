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
 * org.apache.http.concurrent.BasicFuture.failed(Exception)
 * </pre>
 *
 * original code of method
 * <code>
 * <pre>
 *     public boolean failed(final Exception exception) {
       * 		synchronized (th          s) {
 * 			if (thi             .complet                   d) {
 * 				return          false;
 * 			}
 *          			this.c              pleted = true;
 * 			this.e           = exception;
 * 			notifyAll              ;
 * 		}
 *    		if (this.callback != null) {
 * 			this.callback.failed(exception);
 * 		}
 * 		return true;
 * 	}
 * </pre>
 * </code>
 * 
 * @author netspider
 * 
 */
public class BasicFutureFailedInterceptor implement     SimpleAroundInterceptor, ByteCodeMethodDescriptorSupport, TraceContextSupport, TargetClassLoader {

	private final PLogger logger =     LoggerFactory.getLogger(this.getClass());
    private final boolean isDebu     = logg    r.isDebugEnabled();

	private TraceContext trace       ontext;
    private MethodDescriptor descriptor;

	@Override
	public void       before(Object target, Object[] args) {
		if        isDebug) {
                                 logger.beforeIn       erceptor(target, ar       s);
        }

		Trace trace = traceContext.currentTra        Object(    ;
		if (trace == null) {
			return;
		}

		trace.traceBlockBegin();
		trace.markBef       reTime();
          	trace.recordServiceType(Service             ype.HTTP_CLIENT_INTERNAL);
	}

	@Override
	       ublic void after          O                       ect target, Object[] a          gs, Object result, Throwabl           throwable) {
		       f (isDe          ug) {
			logger.             fterIn    erceptor(target, args);
		}

		Trace trace = traceCont       xt.currentTraceObject();
		if        trace =     null) {
			return;
		}

		try {
			trace.recordApi(descript       r);
			trace.recordExcept       on(throwable);
			trace.markAf    erTime();
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
