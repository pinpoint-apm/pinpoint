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
package com.navercorp.pinpoint.profiler.plugin.objectfactory;

import java.lang.annotation.Annotation;

import com.navercorp.pinpoint.bootstrap.FieldAccessor;
import com.navercorp.pinpoint.bootstrap.MetadataAccessor;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.instrument.ByteCodeInstrumentor;
import com.navercorp.pinpoint.bootstrap.interceptor.group.InterceptorGroup;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginContext;
import com.navercorp.pinpoint.bootstrap.plugin.annotation.Name;
import com.navercorp.pinpoint.exception.PinpointException;
import com.navercorp.pinpoint.profiler.plugin.TypeUtils;

/**
 * @author Jongho Moon
 *
 */
public class ProfilerPluginArgumentProvider implements ArgumentProvider {
    private final ProfilerPluginContext pluginContext;

    public ProfilerPluginArgumentProvider(ProfilerPluginContext pluginContext) {
        this.pluginContext = pluginContext;
    }

    @Override
    public Option get(int index, Class<?> type, Annotation[] annotations) {
        if (type == Trace.class) {
            return Option.withValue(pluginContext.getTraceContext().currentTraceObject());
        } else if (type == TraceContext.class) {
            return Option.withValue(pluginContext.getTraceContext());
        } else if (type == ProfilerPluginContext.class) {
            return Option.withValue(pluginContext);
        } else if (type == ByteCodeInstrumentor.class) {
            return Option.withValue(pluginContext.getByteCodeInstrumentor());
        } else if (type == MetadataAccessor.class) {
            Name annotation = TypeUtils.findAnnotation(annotations, Name.class);
            
            if (annotation == null) {
                throw new PinpointException("MetadataAccessor parameter must be annotated with @Name");
            }
            
            MetadataAccessor accessor = pluginContext.getMetadataAccessor(annotation.value());
            
            if (accessor == null) {
                throw new PinpointException("No such MetadataAccessor: " + annotation.value());
            }
            
            return Option.withValue(accessor);
        } else if (type == FieldAccessor.class) {
            Name annotation = TypeUtils.findAnnotation(annotations, Name.class);
            
            if (annotation == null) {
                throw new PinpointException("FieldAccessor parameter must be annotated with @Name");
            }
            
            FieldAccessor accessor = pluginContext.getFieldAccessor(annotation.value());
            
            if (accessor == null) {
                throw new PinpointException("No such FieldAccessor: " + annotation.value());
            }
            
            return Option.withValue(accessor);
        } else if (type == InterceptorGroup.class) {
            Name annotation = TypeUtils.findAnnotation(annotations, Name.class);
            
            if (annotation == null) {
                return Option.empty();
            }
            
            InterceptorGroup group = pluginContext.getInterceptorGroup(annotation.value());
            
            if (group == null) {
                throw new PinpointException("No such Group: " + annotation.value());
            }
            
            return Option.withValue(group);
        }
        
        return Option.empty();
    }
}
