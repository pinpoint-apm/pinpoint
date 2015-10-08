/**
 * Copyright 2014 NAVER Corp.
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
package com.navercorp.pinpoint.plugin.spring.beans.interceptor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.interceptor.StaticAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.plugin.spring.beans.SpringBeansConstants;

/**
 * @author Jongho Moon
 *
 */
public class BeanMethodInterceptor implements StaticAroundInterceptor {
    private final PLogger logger = PLoggerFactory.getLogger(getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final ConcurrentMap<String, MethodDescriptor> descriptorMap = new ConcurrentHashMap<String, MethodDescriptor>();
    private final TraceContext traceContext;
    private final Instrumentor instrumentor;
    
    public BeanMethodInterceptor(TraceContext traceContext, Instrumentor instrumentor) {
        this.traceContext = traceContext;
        this.instrumentor = instrumentor;
    }

    @Override
    public void before(Object target, String className, String methodName, String parameterDescription, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, className, methodName, parameterDescription, args);
        }

        Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }

        final SpanEventRecorder recorder = trace.traceBlockBegin();
        recorder.recordServiceType(SpringBeansConstants.SERVICE_TYPE);
    }

    @Override
    public void after(Object target, String className, String methodName, String parameterDescription, Object result, Throwable throwable, Object[] args) {
        if (isDebug) {
            logger.afterInterceptor(target, className, methodName, parameterDescription, args);
        }

        Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }

        try {
            String fullName = className + "." + methodName + parameterDescription;
            MethodDescriptor descriptor = descriptorMap.get(fullName);
            
            if (descriptor == null) {
                InstrumentClass targetClass = instrumentor.getInstrumentClass(target.getClass().getClassLoader(), className, null);
                InstrumentMethod targetMethod = targetClass.getDeclaredMethod(methodName, divideParamTypes(parameterDescription));
                
                descriptor = targetMethod.getDescriptor();
                traceContext.cacheApi(descriptor);
                descriptorMap.putIfAbsent(fullName, descriptor);
            }
            
            final SpanEventRecorder recorder = trace.currentSpanEventRecorder();
            recorder.recordApi(descriptor);
            recorder.recordException(throwable);
        } finally {
            trace.traceBlockEnd();
        }
    }
    
    private String[] divideParamTypes(String parameterDescription) {
        if (parameterDescription == null || parameterDescription.charAt(0) != '(' || parameterDescription.charAt(parameterDescription.length() - 1) != ')') {
            throw new IllegalArgumentException(parameterDescription);
        }
        
        List<String> types = new ArrayList<String>();
        String removeParenthesis = parameterDescription.substring(1, parameterDescription.length() - 1);
        
        for (String token : removeParenthesis.split(",")) {
            String trimmed = token.trim();
            if (!trimmed.isEmpty()) {
                types.add(trimmed);
            }
        }
        
        return types.toArray(new String[types.size()]);
    }
}
