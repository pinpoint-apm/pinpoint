/*
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
package com.navercorp.pinpoint.profiler.objectfactory;

import java.lang.annotation.Annotation;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentContext;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentorDelegate;
import com.navercorp.pinpoint.bootstrap.interceptor.annotation.Name;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScope;
import com.navercorp.pinpoint.exception.PinpointException;
import com.navercorp.pinpoint.profiler.util.TypeUtils;

/**
 * @author Jongho Moon
 *
 */
public class ProfilerPluginArgumentProvider implements ArgumentProvider {
    private final ProfilerConfig profilerConfig;
    private final TraceContext traceContext;
    private final InstrumentContext pluginContext;

    public ProfilerPluginArgumentProvider(ProfilerConfig profilerConfig, TraceContext traceContext, InstrumentContext pluginContext) {
        if (profilerConfig == null) {
            throw new NullPointerException("profilerConfig");
        }
        if (traceContext == null) {
            throw new NullPointerException("traceContext");
        }
        if (pluginContext == null) {
            throw new NullPointerException("pluginContext");
        }
        this.profilerConfig = profilerConfig;
        this.traceContext = traceContext;
        this.pluginContext = pluginContext;

    }

    @Override
    public Option get(int index, Class<?> type, Annotation[] annotations) {
        if (type == Trace.class) {
            return Option.withValue(traceContext.currentTraceObject());
        } else if (type == TraceContext.class) {
            return Option.withValue(traceContext);
        } else if (type == Instrumentor.class) {
            final InstrumentorDelegate delegate = new InstrumentorDelegate(profilerConfig, pluginContext);
            return Option.withValue(delegate);
        } else if (type == InterceptorScope.class) {
            Name annotation = TypeUtils.findAnnotation(annotations, Name.class);
            
            if (annotation == null) {
                return Option.empty();
            }
            
            InterceptorScope scope = pluginContext.getInterceptorScope(annotation.value());
            
            if (scope == null) {
                throw new PinpointException("No such Scope: " + annotation.value());
            }
            
            return Option.withValue(scope);
        }
        
        return Option.empty();
    }
}
