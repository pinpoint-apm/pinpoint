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

import com.navercorp.pinpoint.bootstrap.FieldAccessor;
import com.navercorp.pinpoint.bootstrap.MetadataAccessor;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.instrument.ByteCodeInstrumentor;

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
     * @return {@link TraceContext} of current transction
     */
    public TraceContext getTraceContext();
    
    /**
     * Get {@link ByteCodeInstrumentor}
     * 
     * @return {@link ByteCodeInstrumentor}
     */
    public ByteCodeInstrumentor getByteCodeInstrumentor();
}
