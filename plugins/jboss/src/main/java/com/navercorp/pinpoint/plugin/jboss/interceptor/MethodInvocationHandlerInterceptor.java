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

package com.navercorp.pinpoint.plugin.jboss.interceptor;

import java.lang.reflect.Method;

import org.jboss.as.security.remoting.RemotingContext;
import org.jboss.remoting3.Connection;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.SpanRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.plugin.jboss.JbossConstants;
import com.navercorp.pinpoint.plugin.jboss.MethodInvocationHandlerMethodDescriptor;
import com.navercorp.pinpoint.plugin.jboss.util.JbossUtility;

/**
 * The Class MethodInvocationHandlerInterceptor.
 *
 * @author <a href="mailto:suraj.raturi89@gmail.com">Suraj Raturi</a>
 */
public class MethodInvocationHandlerInterceptor implements AroundInterceptor {

    /** The Constant METHOD_INVOCATION_HANDLER_API_TAG. */
    public static final MethodInvocationHandlerMethodDescriptor METHOD_INVOCATION_HANDLER_API_TAG = new MethodInvocationHandlerMethodDescriptor();

    /** The logger. */
    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());

    /** The is debug. */
    private final boolean isDebug = logger.isDebugEnabled();

    /** The method descriptor. */
    private final MethodDescriptor methodDescriptor;

    /** The trace context. */
    private final TraceContext traceContext;

    /**
     * Instantiates a new invoke context interceptor.
     *
     * @param traceContext the trace context
     * @param descriptor the descriptor
     */
    public MethodInvocationHandlerInterceptor(final TraceContext traceContext, final MethodDescriptor descriptor) {
        this.traceContext = traceContext;
        this.methodDescriptor = descriptor;
        traceContext.cacheApi(METHOD_INVOCATION_HANDLER_API_TAG);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor#before(java.lang.Object, java.lang.Object[])
     */
    @Override
    public void before(final Object target, final Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }
        try {
            final Trace trace = createTrace(target, args);
            if (trace == null) {
                return;
            }

            if (!trace.canSampled()) {
                return;
            }
            final SpanEventRecorder recorder = trace.traceBlockBegin();
            recorder.recordServiceType(JbossConstants.JBOSS_METHOD);
        } catch (final Throwable th) {
            if (logger.isWarnEnabled()) {
                logger.warn("BEFORE. Caused:{}", th.getMessage(), th);
            }
        }
    }

    /**
     * Creates the trace.
     *
     * @param target the target
     * @param args the args
     * @return the trace
     */
    private Trace createTrace(final Object target, final Object[] args) {
        final Method methodInvoked = (Method) args[2];
        final StringBuilder methodNameBuilder = new StringBuilder();
        if (methodInvoked != null) {
            try {
                final Class<?> declaringClass = methodInvoked.getDeclaringClass();
                if (declaringClass != null) {
                    methodNameBuilder.append(declaringClass.getCanonicalName());
                    methodNameBuilder.append('.');
                }
                methodNameBuilder.append(methodInvoked.getName());
            } catch (final Exception exception) {
                logger.error("An error occurred while fetching method details", exception);
            }
        }
        final Trace trace = traceContext.newTraceObject();
        final Connection connection = RemotingContext.getConnection();
        final String remoteAddress = JbossUtility.fetchRemoteAddress(connection);
        if (trace.canSampled()) {
            final SpanRecorder recorder = trace.getSpanRecorder();
            recordRootSpan(recorder, methodNameBuilder.toString(), remoteAddress);
            if (isDebug) {
                logger.debug("Trace sampling is true, Recording trace. methodInvoked:{}, remoteAddress:{}", methodNameBuilder.toString(), remoteAddress);
            }
        } else {
            if (isDebug) {
                logger.debug("Trace sampling is false, Skip recording trace. methodInvoked:{}, remoteAddress:{}", methodNameBuilder.toString(), remoteAddress);
            }
        }
        return trace;
    }

    /**
     * Record root span.
     *
     * @param recorder the recorder
     * @param rpcName the rpc name
     * @param remoteAddress
     */
    private void recordRootSpan(final SpanRecorder recorder, final String rpcName, final String remoteAddress) {
        recorder.recordServiceType(JbossConstants.JBOSS);

        recorder.recordRpcName(rpcName);

        final String serverHostName = System.getProperty("jboss.host.name", "");
        recorder.recordEndPoint(serverHostName);

        recorder.recordRemoteAddress(remoteAddress);

        recorder.recordApi(METHOD_INVOCATION_HANDLER_API_TAG);

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor#after(java.lang.Object, java.lang.Object[],
     * java.lang.Object, java.lang.Throwable)
     */
    @Override
    public void after(final Object target, final Object[] args, final Object result, final Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args, result, throwable);
        }
        final Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }

        if (!trace.canSampled()) {
            traceContext.removeTraceObject();
            return;
        }
        try {
            final SpanEventRecorder recorder = trace.currentSpanEventRecorder();
            recorder.recordApi(methodDescriptor);
            recorder.recordException(throwable);
        } catch (final Throwable th) {
            if (logger.isWarnEnabled()) {
                logger.warn("AFTER. Caused:{}", th.getMessage(), th);
            }
        } finally {
            trace.traceBlockEnd();
            trace.close();
            traceContext.removeTraceObject();
        }
    }

}
