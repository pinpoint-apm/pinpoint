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
package com.navercorp.pinpoint.plugin.json.lib;

import static com.navercorp.pinpoint.common.trace.HistogramSchema.*;

import java.lang.instrument.ClassFileTransformer;

import com.navercorp.pinpoint.bootstrap.interceptor.group.ExecutionPolicy;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginContext;
import com.navercorp.pinpoint.bootstrap.plugin.transformer.ClassFileTransformerBuilder;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.plugin.json.lib.JsonLibConstants;
import com.navercorp.pinpoint.bootstrap.plugin.transformer.MethodTransformerBuilder;
import com.navercorp.pinpoint.bootstrap.plugin.transformer.MethodTransformerProperty;

/**
 * @author Sangyoon Lee
 *
 */
public class JsonLibPlugin implements ProfilerPlugin {
    private static final String GROUP = "json-lib";
  
    @Override
    public void setup(ProfilerPluginContext context) {
        addJSONSerializerInterceptor(context);
        addJSONObjectInterceptor(context);
        addJSONArrayInterceptor(context);
    }
    
    private void addJSONSerializerInterceptor(ProfilerPluginContext context) {
        ClassFileTransformerBuilder builder = context.getClassFileTransformerBuilder("net.sf.json.JSONSerializer");
        builder.editMethod("toJSON", "java.lang.Object").injectInterceptor("com.navercorp.pinpoint.plugin.json.lib.interceptor.JsonLibMethodInterceptor").group(GROUP);
        
        builder.editMethod("toJava", "net.sf.json.JSON").injectInterceptor("com.navercorp.pinpoint.plugin.json.lib.interceptor.JsonLibMethodInterceptor").group(GROUP);
        ClassFileTransformer transformer = builder.build();
        context.addClassFileTransformer(transformer);
    }

    private void addJSONObjectInterceptor(ProfilerPluginContext context) {
        ClassFileTransformerBuilder builder = context.getClassFileTransformerBuilder("net.sf.json.JSONObject");
        
        builder.editMethod("fromObject", "java.lang.Object").injectInterceptor("com.navercorp.pinpoint.plugin.json.lib.interceptor.JsonLibMethodInterceptor").group(GROUP);
        
        builder.editMethod("toBean", "net.sf.json.JSONObject").injectInterceptor("com.navercorp.pinpoint.plugin.json.lib.interceptor.JsonLibMethodInterceptor").group(GROUP);
        
        builder.editMethod("toString").injectInterceptor("com.navercorp.pinpoint.plugin.json.lib.interceptor.JsonLibMethodInterceptor").group(GROUP);
        
        ClassFileTransformer transformer = builder.build();
        context.addClassFileTransformer(transformer);
    }

    private void addJSONArrayInterceptor(ProfilerPluginContext context) {
        ClassFileTransformerBuilder builder = context.getClassFileTransformerBuilder("net.sf.json.JSONArray");
        builder.editMethod("fromObject", "java.lang.Object").injectInterceptor("com.navercorp.pinpoint.plugin.json.lib.interceptor.JsonLibMethodInterceptor").group(GROUP);
        
        builder.editMethod("toArray", "net.sf.json.JSONArray").injectInterceptor("com.navercorp.pinpoint.plugin.json.lib.interceptor.JsonLibMethodInterceptor").group(GROUP);	
        
        builder.editMethod("toString").injectInterceptor("com.navercorp.pinpoint.plugin.json.lib.interceptor.JsonLibMethodInterceptor").group(GROUP);
        
        MethodTransformerBuilder toCollection = builder.editMethod("toCollection", "net.sf.json.JSONArray");
        toCollection.injectInterceptor("com.navercorp.pinpoint.plugin.json.lib.interceptor.JsonLibMethodInterceptor").group(GROUP);
        toCollection.property(MethodTransformerProperty.IGNORE_IF_NOT_EXIST);

        builder.editMethod("toList", "net.sf.json.JSONArray").injectInterceptor("com.navercorp.pinpoint.plugin.json.lib.interceptor.JsonLibMethodInterceptor").group(GROUP);
                      
        ClassFileTransformer transformer = builder.build();
        context.addClassFileTransformer(transformer);
    } 
}
