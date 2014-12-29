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

package com.navercorp.pinpoint.profiler.modifier.arcus.interceptor;

import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.SimpleAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.TraceContextSupport;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;


/**
 * @author emeroad
 */
@Deprecated
public class BaseOperationConstructInterceptor implements SimpleAroundInterceptor, TraceContextSupport {

	private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

//	private final MetaObject<Object> setAsyncTrace = new MetaObject<Object>("__setAsyncTrace", Object.class);

    private TraceContext traceContext;

    @Override
    public void before(Object target, Object[] args) {
    }

    @Override
	public void after(Object target, Object[] args, Object result, Throwable throwable) {
		if (isDebug) {
            logger.afterInterceptor(target, args, result, throwable);
		}

		Trace trace = traceContext.currentTraceObject();

		if (trace == null) {
			return;
		}

		// Assuming no events are missed, do not process timeout.
//		AsyncTrace asyncTrace = trace.createAsyncTrace();
//		asyncTrace.markBeforeTime();
//
//		asyncTrace.setAttachObject(new TimeObject());
//
//		setAsyncTrace.invoke(target, asyncTrace);
	}

    @Override
    public void setTraceContext(TraceContext traceContext) {

        this.traceContext = traceContext;
    }
}
