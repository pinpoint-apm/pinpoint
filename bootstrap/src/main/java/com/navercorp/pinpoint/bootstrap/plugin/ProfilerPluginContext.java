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
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.instrument.ByteCodeInstrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.interceptor.group.InterceptorGroup;
import com.navercorp.pinpoint.bootstrap.plugin.transformer.ClassFileTransformerBuilder;

/**
 *  Provides attributes and objects to interceptors.
 * 
 *  Only interceptors can acquire an instance of this class as a constructor argument.
 * 
 * @author Jongho Moon
 *
 */
public interface ProfilerPluginContext {
    /**
     * Set an attribute. Only objects within same plug-in can see this attribute.
     *   
     * @param name attribute name
     * @param value attribute value
     * @return Previous value if the name was associated with other value. null otherwise.
     */
    public Object setAttribute(String name, Object value);
    
    /**
     * Get an attribute set within a plug-in.
     * 
     * You can get attributes set by {@link ProfilerPluginSetupContext#setAttribute(String, Object)} too.
     * 
     * @param name attribute name
     * @return value value associated with given name. null if no value is set.
     */
    public Object getAttribute(String name);

    /**
     * Get the {@link MetadataAccessor} with given name.
     * 
     * @param name 
     * @return {@link MetadataAccessor} with given name. null if there is no {@link MetadataAccessor} with the name.  
     */
    public MetadataAccessor getMetadataAccessor(String name);
    
    /**
     * Get the {@link MetadataAccessor} with given name.
     * 
     * @param name
     * @return {@link MetadataAccessor} with given name. null if there is no {@link MetadataAccessor} with the name.  
     */
    public FieldAccessor getFieldAccessor(String name);
    
    /**
     * Get {@link TraceContext}
     * 
     * @return {@link TraceContext} of current transaction
     */
    public TraceContext getTraceContext();
    
    /**
     * Get the {@link ProfilerConfig}
     * 
     * @return {@link ProfilerConfig}
     */
    public ProfilerConfig getConfig();
    
    public InstrumentClass getInstrumentClass(ClassLoader classLoader, String className, byte[] classFileBuffer);
    
    public InterceptorGroup getInterceptorGroup(String name);
    
    /**
     * Add a {@link ApplicationTypeDetector} to Pinpoint agent.
     * 
     * @param detectors
     */
    public void addApplicationTypeDetector(ApplicationTypeDetector... detectors);

    public void addClassFileTransformer(String targetClassName, ClassFileTransformer transformer);
    
    public void retransform(Class<?> target, ClassFileTransformer classEditor);
    
    
    
    
    /**
     * Add a {@link ClassEditor} to Pinpoint agent.
     * 
     * @param classEditor
     */
    @Deprecated

    public void addClassFileTransformer(ClassFileTransformer transformer);
    /**
     * Get {@link ByteCodeInstrumentor}
     * 
     * @return {@link ByteCodeInstrumentor}
     */
    @Deprecated
    public ByteCodeInstrumentor getByteCodeInstrumentor();
    
    /**
     * Get a {@link ClassFileTransformerBuilder}.
     * 
     * By using returned {@link ClassFileTransformerBuilder} you can create a {@link ClassFileTransformer} easily.
     * You have to register resulting {@link ClassFileTransformer} by {@link #addClassFileTransformer(ClassFileTransformer)} to make it works.
     *
     * @param targetClassName target class name
     * @return {@link ClassFileTransformerBuilder}
     */
    @Deprecated
    public ClassFileTransformerBuilder getClassFileTransformerBuilder(String targetClassName);
}
