/*
 * Copyright 2020 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.plugin.process.interceptor;

import com.navercorp.pinpoint.bootstrap.context.*;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.plugin.process.ProcessPluginConstants;

import javax.lang.model.SourceVersion;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

public class ProcessExecuteInterceptor implements AroundInterceptor {
    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();
    public final long PID_INACCESSIBLE = -1L;

    private static final Method pidMethod = getPidMethod();
    private static Field pidField = null;

    private TraceContext traceContext;
    private MethodDescriptor descriptor;

    public ProcessExecuteInterceptor(TraceContext traceContext, MethodDescriptor descriptor) {
        this.traceContext = traceContext;
        this.descriptor = descriptor;
    }

    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }

        final Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }

        final SpanEventRecorder recorder = trace.traceBlockBegin();
        recorder.recordServiceType(ProcessPluginConstants.SERVICE_TYPE);
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args, result, throwable);
        }

        final Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }

        try {
            final SpanEventRecorder recorder = trace.currentSpanEventRecorder();
            recorder.recordException(throwable);
            recorder.recordApi(descriptor);

            List<String> commands = ((ProcessBuilder)target).command();
            recorder.recordAttribute(ProcessPluginConstants.PROCESS_COMMAND, commands.toString());

            long pid = getPid(result);
            if(pid != -1L) {
                recorder.recordAttribute(ProcessPluginConstants.PROCESS_ID, pid);
            } else {
                recorder.recordAttribute(ProcessPluginConstants.PROCESS_ID, "PID not supported");
            }
        } finally {
            trace.traceBlockEnd();
        }
    }

    private static Method getPidMethod() {
        if (SourceVersion.latest().toString().compareTo("RELEASE_9") >= 0) {
            /* Use pid() when it is available (JDK 9 or above) */
            try {
                return Process.class.getDeclaredMethod("pid");
            } catch (Exception ignore) {}
        }
        return null;
    }

    private void initializePidField(Object process) {
        if(process.getClass().getName().equals("java.lang.UNIXProcess")) {
            /* Assuming it is not on Windows */
            try {
                pidField = process.getClass().getDeclaredField("pid");
                pidField.setAccessible(true);
            } catch (Exception ignore) {}
        } else {
            try {
                pidField = this.getClass().getDeclaredField("PID_INACCESSIBLE");
            } catch (Exception ignore) {}
        }
    }

    private long getPid(Object process) {
        if (pidMethod != null) {
            try {
                return (Long) pidMethod.invoke(process);
            } catch (Exception ignore) {}
        } else {
            if (pidField == null) {
                initializePidField(process);
            }

            try {
                Object value = pidField.get(process);
                logger.debug("Pid of the forked process: {}", value);
                return ((Integer)value).longValue();
            } catch (Exception ignore) {}
        }
        return -1L;
    }
}
