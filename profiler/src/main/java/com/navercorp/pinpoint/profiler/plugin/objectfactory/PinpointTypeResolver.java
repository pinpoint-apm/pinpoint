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
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.MethodInfo;
import com.navercorp.pinpoint.bootstrap.interceptor.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.plugin.Cached;
import com.navercorp.pinpoint.bootstrap.plugin.Name;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginContext;
import com.navercorp.pinpoint.exception.PinpointException;
import com.navercorp.pinpoint.profiler.plugin.TypeUtils;

/**
 * @author Jongho Moon
 *
 */
public class PinpointTypeResolver implements ParameterResolver {
    private final ProfilerPluginContext pluginContext;
    private final InstrumentClass targetClass;
    private final MethodInfo targetMethod;

    public PinpointTypeResolver(ProfilerPluginContext pluginContext, InstrumentClass targetClass, MethodInfo targetMethod) {
        this.pluginContext = pluginContext;
        this.targetClass = targetClass;
        this.targetMethod = targetMethod;
    }

    /* (non-Javadoc)
     * @see com.navercorp.pinpoint.profiler.plugin.ParameterResolver#resolve(int, java.lang.Class, java.lang.annotation.Annotation[])
     */
    @Override
    public Option<Object> resolve(int index, Class<?> type, Annotation[] annotations) {
        if (type == Trace.class) {
            return Option.<Object>withValue(pluginContext.getTraceContext().currentTraceObject());
        } else if (type == TraceContext.class) {
            return Option.<Object>withValue(pluginContext.getTraceContext());
        } else if (type == ProfilerPluginContext.class) {
            return Option.<Object>withValue(pluginContext);
        } else if (type == ByteCodeInstrumentor.class) {
            return Option.<Object>withValue(pluginContext.getByteCodeInstrumentor());
        } else if (type == InstrumentClass.class) {
            return Option.<Object>withValue(targetClass);
        } else if (type == MethodDescriptor.class) {
            MethodDescriptor descriptor = targetMethod.getDescriptor();
            cacheApiIfAnnotationPresent(annotations, descriptor);
            
            return Option.<Object>withValue(descriptor);
        } else if (type == MethodInfo.class) {
            cacheApiIfAnnotationPresent(annotations, targetMethod.getDescriptor());

            return Option.<Object>withValue(targetMethod);
        } else if (type == MetadataAccessor.class) {
            Annotation annotation = TypeUtils.findAnnotation(annotations, Name.class);
            
            if (annotation == null) {
                throw new PinpointException("MetadataAccessor parameter must be annotated with @Name");
            }
            
            MetadataAccessor accessor = pluginContext.getMetadataAccessor(((Name)annotation).value());
            return Option.<Object>withValue(accessor);
        } else if (type == FieldAccessor.class) {
            Annotation annotation = TypeUtils.findAnnotation(annotations, Name.class);
            
            if (annotation == null) {
                throw new PinpointException("FieldAccessor parameter must be annotated with @Name");
            }
            
            FieldAccessor accessor = pluginContext.getFieldAccessor(((Name)annotation).value());
            return Option.<Object>withValue(accessor);
        }
        
        return Option.<Object>empty();
    }

    private void cacheApiIfAnnotationPresent(Annotation[] annotations, MethodDescriptor descriptor) {
        Annotation annotation = TypeUtils.findAnnotation(annotations, Cached.class);
        if (annotation != null) {
            pluginContext.getTraceContext().cacheApi(descriptor);
        }
    }
}
