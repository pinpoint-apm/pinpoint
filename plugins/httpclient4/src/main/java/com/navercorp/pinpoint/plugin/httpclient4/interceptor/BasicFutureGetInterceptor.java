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

package com.navercorp.pinpoint.plugin.httpclient4.interceptor;

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
 *         while (!this.completed) {
 *             wait();
 *         }
 *         return getResult();
 *     }
 * 
 *     public synchronized T get(final long timeout, final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
 *         Args.notNull(unit, "Time unit");
 *         final long msecs = unit.toMillis(timeout);
 *         final long startTime = (msecs <= 0) ? 0 : System.currentTimeMillis();
 *         long waitTime = msecs;
 *         if (this.completed) {
 *             return getResult();
 *         } else if (waitTime <= 0) {
 *             throw new TimeoutException();
 *         } else {
 *             for (;;) {
 *                 wait(waitTime);
 *                 if (this.completed) {
 *                     return getResult();
 *                 } else {
 *                     waitTime = msecs - (System.currentTimeMillis() - startTime);
 *                     if (waitTime <= 0) {
 *                         throw new TimeoutException();
 *                     }
 *                 }
 *             }
 *         }
 *     }
 * </pre>
 * </code>
 * 
 * @author netspider
 * 
 */
public class BasicFutureGetInterceptor implements SimpleAroundInterceptor, ByteCodeMethodDescriptorSupport, TraceContextSupport, TargetClassLoader {

    protected final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    protected final boolean isDebug = logger.isDebugEnabled();

    protected TraceContext traceContext;
    protected MethodDescriptor descriptor;

    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }

        Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }

        trace.traceBlockBegin();
        trace.markBeforeTime();
        trace.recordServiceType(ServiceType.HTTP_CLIENT_INTERNAL);
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args);
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
