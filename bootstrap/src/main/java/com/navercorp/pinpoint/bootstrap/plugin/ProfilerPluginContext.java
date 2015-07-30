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
package com.navercorp.pinpoint.bootstrap.plugin;

import java.lang.instrument.ClassFileTransformer;

import com.navercorp.pinpoint.bootstrap.FieldAccessor;
import com.navercorp.pinpoint.bootstrap.MetadataAccessor;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentableClass;
import com.navercorp.pinpoint.bootstrap.interceptor.group.InterceptorGroup;

/**
 * @author Jongho Moon
 *
 */
public interface ProfilerPluginContext {
    public TraceContext getTraceContext();
    
    public InstrumentableClass getInstrumentableClass(ClassLoader classLoader, String className, byte[] classFileBuffer);
    
    public InterceptorGroup getInterceptorGroup(String name);
        
    public <T> Class<? extends T> injectClass(ClassLoader targetClassLoader, String className);
    
    public void retransform(Class<?> target, ClassFileTransformer classEditor);
    
    @Deprecated
    public MetadataAccessor getMetadataAccessor(String name);
    
    @Deprecated
    public FieldAccessor getFieldAccessor(String name);
}
