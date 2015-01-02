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
 * suitable method
 * 
 * <pre>
 * org.apache.http.concurrent.BasicFuture.get()
 * org.apache.http.concurrent.BasicFuture.get(long, TimeUnit)
 * </pre>
 * 
 * <code>
 * <pre>
 *     public synchronized T get() throws InterruptedException, ExecutionException {
       * 		while (!this.complet          d) {              * 			wait();
 *
 * 		    eturn getResult();
 * 	}
 * 
 * 	public synchronized T get(final long timeout, final TimeUnit unit) throws InterruptedException, Execut       onException, TimeoutException {        * 		Args.notNull(unit, "Time unit");
 *        	final long msecs = unit.toMillis(timeout);
 * 		final long startTim        = (msecs <= 0) ? 0 :       System.currentTimeMi          lis();
 * 		long       waitTime = msecs;
 * 		if           this.completed) {
 * 			re       urn get          esult()
 * 		} el             e if (waitTime <                 0) {
 * 			             hro                 new TimeoutException();
 * 		} else {
 * 			for (;;)                {
 * 				wait                   waitTime);
 * 				if                                              (this.completed) {
 * 					return getResult();
 * 				} else {
 * 					waitTime = msecs - (System.currentTimeMillis() - startTime);
 * 					if (waitTime <= 0) {
 * 						throw new TimeoutException();
 * 					}
 * 				}
 * 			}
 * 		}
 * 	}
 * </pre>
 * </code>
 * 
 * @author netspider
 * 
 */
public class BasicFutureGetInterceptor implem    nts SimpleAroundInterceptor, ByteC    deMethodDescriptorSupport, TraceConte    tSuppor    , TargetClassLoader {

    protected final PLogg       r logger =          PLoggerFactory.getLogger(this.get             lass());
    protected final boolean isDebu        = logger.isDebu          E             abled();

	protecte        TraceContext trace       ontext;
	protected MethodDescriptor descriptor;

	@Ove        ide
	pu    lic void before(Object target, Object[] args) {
		if (isDebug) {
			logger.beforeIn       erceptor(t          rget, args);
		}

		Trace trace               traceContext.currentTraceObject();
		if (t       ace == null) {
	          	                       turn;
		}

		trace.tra          eBlockBegin();
		trace.mark          eforeTime();
		t       ace.rec          rdServiceType(Se             viceTy    e.HTTP_CLIENT_INTERNAL);
	}

	@Override
	public void a       ter(Object target, Object[] a        s, Obje    t result, Throwable throwable) {
		if (isDebug) {
			logger.       fterInterceptor(target, a       gs);
		}

		Trace trace = trac    Context.currentTraceObject();
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
