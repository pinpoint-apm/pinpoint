/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.navercorp.pinpoint.bootstrap.FieldAccessor;
import com.navercorp.pinpoint.bootstrap.MetadataAccessor;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.instrument.ByteCodeInstrumentor;
import com.navercorp.pinpoint.bootstrap.plugin.ApplicationTypeDetector;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginContext;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.bootstrap.plugin.editor.ClassEditor;
import com.navercorp.pinpoint.bootstrap.plugin.editor.ClassEditorBuilder;
import com.navercorp.pinpoint.profiler.DefaultAgent;
import com.navercorp.pinpoint.profiler.plugin.editor.DefaultClassEditorBuilder;

public class DefaultProfilerPluginContext implements ProfilerPluginSetupContext, ProfilerPluginContext {
    private final DefaultAgent agent;
    
    private final List<ApplicationTypeDetector> serverTypeDetectors = new ArrayList<ApplicationTypeDetector>();
    private final List<ClassEditor> classEditors = new ArrayList<ClassEditor>();
    
    private final ConcurrentMap<String, Object> attributeMap = new ConcurrentHashMap<String, Object>();
    private final Map<String, MetadataAccessor> metadataAccessorMap = new HashMap<String, MetadataAccessor>();
    private final Map<String, FieldAccessor> fieldSnooperMap = new HashMap<String, FieldAccessor>();
    
    private int metadataAccessorIndex = 0;
    private int fieldSnooperIndex = 0;
    
    public DefaultProfilerPluginContext(DefaultAgent agent) {
        this.agent = agent;
    }

    @Override
    public ClassEditorBuilder getClassEditorBuilder(String targetClassName) {
        return new DefaultClassEditorBuilder(this, targetClassName);
    }
    
    @Override
    public void addClassEditor(ClassEditor classEditor) {
        classEditors.add(classEditor);
    }

    @Override
    public ProfilerConfig getConfig() {
        return agent.getProfilerConfig();
    }

    @Override
    public TraceContext getTraceContext() {
        return agent.getTraceContext();
    }

    @Override
    public ByteCodeInstrumentor getByteCodeInstrumentor() {
        return agent.getByteCodeInstrumentor();
    }

    public MetadataAccessor allocateMetadataAccessor(String name) {
        MetadataAccessor accessor = metadataAccessorMap.get(name);
        
        if (accessor != null) {
            return accessor;
        }
        
        try {
            accessor = MetadataAccessor.get(metadataAccessorIndex);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new IllegalStateException("Cannot allocate MetadataAccessor. Exceeded max:" + metadataAccessorIndex);
        }

        metadataAccessorMap.put(name, accessor);
        metadataAccessorIndex++;
        
        return accessor;
    }

    public FieldAccessor allocateFieldSnooper(String name) {
        FieldAccessor snooper = fieldSnooperMap.get(name);
        
        if (snooper != null) {
            return snooper;
        }
        
        try {
            snooper = FieldAccessor.get(fieldSnooperIndex);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new IllegalStateException("Cannot allocate FieldAccessor. Exceeded max:" + fieldSnooperIndex);
        }
        
        fieldSnooperMap.put(name, snooper);
        fieldSnooperIndex++;
        
        return snooper;
    }

    @Override
    public MetadataAccessor getMetadataAccessor(String name) {
        return metadataAccessorMap.get(name);
    }

    @Override
    public FieldAccessor getFieldSnooper(String name) {
        return fieldSnooperMap.get(name);
    }

    @Override
    public Object setAttribute(String key, Object value) {
        return attributeMap.put(key, value);
    }

    @Override
    public Object getAttribute(String key) {
        return attributeMap.get(key);
    }
    
    @Override
    public void addApplicationTypeDetector(ApplicationTypeDetector... detectors) {
        for (ApplicationTypeDetector detector : detectors) {
            serverTypeDetectors.add(detector);
        }
    }

    public List<ClassEditor> getClassEditors() {
        return classEditors;
    }

    public List<ApplicationTypeDetector> getApplicationTypeDetectors() {
        return serverTypeDetectors;
    }
}
