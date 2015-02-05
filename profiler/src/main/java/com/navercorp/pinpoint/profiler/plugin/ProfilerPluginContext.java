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

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.instrument.ByteCodeInstrumentor;
import com.navercorp.pinpoint.bootstrap.plugin.FieldSnooper;
import com.navercorp.pinpoint.bootstrap.plugin.MetadataAccessor;
import com.navercorp.pinpoint.bootstrap.plugin.PluginContext;
import com.navercorp.pinpoint.bootstrap.plugin.PluginSetupContext;
import com.navercorp.pinpoint.bootstrap.plugin.ServerTypeDetector;
import com.navercorp.pinpoint.bootstrap.plugin.editor.ClassEditor;
import com.navercorp.pinpoint.bootstrap.plugin.editor.ClassEditorBuilder;

public class ProfilerPluginContext implements PluginSetupContext, PluginContext {
    private final ProfilerConfig config;
    
    private final List<ServerTypeDetector> serverTypeDetectors = new ArrayList<ServerTypeDetector>();
    private final List<DefaultClassEditorBuilder> classEditorBuilders = new ArrayList<DefaultClassEditorBuilder>();
    
    private final ConcurrentMap<String, Object> attributeMap = new ConcurrentHashMap<String, Object>();
    private final Map<String, MetadataAccessor> metadataAccessorMap = new HashMap<String, MetadataAccessor>();
    private final Map<String, FieldSnooper> fieldSnooperMap = new HashMap<String, FieldSnooper>();
    
    private int metadataAccessorIndex = 0;
    private int fieldSnooperIndex = 0;
    
    public ProfilerPluginContext(ProfilerConfig config) {
        this.config = config;
    }

    @Override
    public ClassEditorBuilder newClassEditorBuilder() {
        DefaultClassEditorBuilder builder = new DefaultClassEditorBuilder(this);
        classEditorBuilders.add(builder);
        return builder;
    }
    
    @Override
    public ProfilerConfig getConfig() {
        return config;
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

    public FieldSnooper allocateFieldSnooper(String name) {
        FieldSnooper snooper = fieldSnooperMap.get(name);
        
        if (snooper != null) {
            return snooper;
        }
        
        try {
            snooper = FieldSnooper.get(fieldSnooperIndex);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new IllegalStateException("Cannot allocate FieldSnooper. Exceeded max:" + fieldSnooperIndex);
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
    public FieldSnooper getFieldSnooper(String name) {
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
    public void addServerTypeDetector(ServerTypeDetector... detectors) {
        for (ServerTypeDetector detector : detectors) {
            serverTypeDetectors.add(detector);
        }
    }

    public List<ClassEditor> getClassEditors(TraceContext context, ByteCodeInstrumentor instrumentor) {
        List<ClassEditor> editors = new ArrayList<ClassEditor>(classEditorBuilders.size());
        
        for (DefaultClassEditorBuilder builder : classEditorBuilders) {
            editors.add(builder.build(context, instrumentor));
        }
        
        return editors;
    }

    public List<ServerTypeDetector> getServerTypeDetectors() {
        return serverTypeDetectors;
    }
}
