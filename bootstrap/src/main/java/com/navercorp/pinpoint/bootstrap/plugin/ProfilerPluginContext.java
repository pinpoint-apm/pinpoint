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

package com.navercorp.pinpoint.bootstrap.plugin;

import java.util.HashMap;
import java.util.Map;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.instrument.ByteCodeInstrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.MethodInfo;
import com.navercorp.pinpoint.bootstrap.interceptor.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.plugin.editor.ClassEditorBuilder;

public class ProfilerPluginContext {
    private final ByteCodeInstrumentor instrumentor;
    private final TraceContext traceContext;
    
    private final Map<String, Integer> cachedApiTable = new HashMap<String, Integer>();
    
    public ProfilerPluginContext(ByteCodeInstrumentor instrumentor, TraceContext traceContext) {
        this.instrumentor = instrumentor;
        this.traceContext = traceContext;
    }

    public ClassEditorBuilder newClassEditorBuilder() {
        return new ClassEditorBuilder(this, instrumentor, traceContext); 
    }
    
    public ProfilerConfig getConfig() {
        return traceContext.getProfilerConfig();
    }
    
    public int cacheApi(MethodInfo methodInfo) {
        MethodDescriptor descriptor = methodInfo.getDescriptor();
        String apiDescriptor = descriptor.getApiDescriptor();
        
        Integer inTable = cachedApiTable.get(apiDescriptor);
        
        if (inTable != null) {
            return inTable;
        }
        
        int id = traceContext.cacheApi(descriptor);
        cachedApiTable.put(apiDescriptor, id);
        
        return id;
    }
}
