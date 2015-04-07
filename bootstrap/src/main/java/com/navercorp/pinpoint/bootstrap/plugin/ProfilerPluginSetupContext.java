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

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.instrument.ByteCodeInstrumentor;
import com.navercorp.pinpoint.bootstrap.plugin.transformer.ClassFileTransformerBuilder;

/**
 * {@link ProfilerPlugin} uses this class to setup itself.
 * 
 * 
 * @author Jongho Moon
 */
public interface ProfilerPluginSetupContext {
    /**
     * Get the {@link ProfilerConfig}
     * 
     * @return {@link ProfilerConfig}
     */
    public ProfilerConfig getConfig();

    /**
     * Set an attribute.
     * 
     * Interceptors of same plug-in can get attributes set by this method by {@link ProfilerPluginContext#getAttribute(String)}
     * 
     * @param name attribute name
     * @param value attribute value
     * 
     * @return Previous value if the key was associated with other value. null otherwise. 
     */
    public Object setAttribute(String name, Object value);
    
    /**
     * Get an attribute value with given name.
     * 
     * @param name attribute name
     * @return value value associated with given name. null if no value is set.
     */
    public Object getAttribute(String name);

    /**
     * Get a {@link ClassFileTransformerBuilder}.
     * 
     * By using returned {@link ClassFileTransformerBuilder} you can create a {@link ClassEditor} easily.
     * You have to register resulting {@link ClasEditor} by {@link #addClassFileTransformer(ClassEditor)} to make it works.
     *
     * @param targetClassName target class name
     * @return {@link ClassFileTransformerBuilder}
     */
    public ClassFileTransformerBuilder getClassEditorBuilder(String targetClassName);
    
    /**
     * Add a {@link ClassEditor} to Pinpoint agent.
     * 
     * @param classEditor
     */
    public void addClassFileTransformer(ClassFileTransformer classEditor);
    
    /**
     * Add a {@link ApplicationTypeDetector} to Pinpoint agent.
     * 
     * @param detectors
     */
    public void addApplicationTypeDetector(ApplicationTypeDetector... detectors);
    
    public ByteCodeInstrumentor getByteCodeInstrumentor();
    
    public PluginClassLoaderFactory getClassLoaderFactory();
}
