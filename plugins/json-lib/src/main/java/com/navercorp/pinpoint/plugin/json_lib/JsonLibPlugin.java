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
package com.navercorp.pinpoint.plugin.json_lib;

import static com.navercorp.pinpoint.bootstrap.instrument.MethodFilters.*;

import java.lang.instrument.ClassFileTransformer;
import java.lang.reflect.Modifier;

import com.navercorp.pinpoint.bootstrap.interceptor.BasicMethodInterceptor;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginContext;
import com.navercorp.pinpoint.bootstrap.plugin.transformer.ClassFileTransformerBuilder;

/**
 * @author Sangyoon Lee
 *
 */
public class JsonLibPlugin implements ProfilerPlugin {
    private static final String BASIC_INTERCEPTOR = BasicMethodInterceptor.class.getName();
    private static final String PARSING_INTERCEPTOR = "com.navercorp.pinpoint.plugin.json_lib.interceptor.ParsingInterceptor";
    private static final String TO_STRING_INTERCEPTOR = "com.navercorp.pinpoint.plugin.json_lib.interceptor.ToStringInterceptor";
    private static final String GROUP = "json-lib";
  
    @Override
    public void setup(ProfilerPluginContext context) {
        addJSONSerializerInterceptor(context);
        addJSONObjectInterceptor(context);
        addJSONArrayInterceptor(context);
    }
    
    private void addJSONSerializerInterceptor(ProfilerPluginContext context) {
        ClassFileTransformerBuilder builder = context.getClassFileTransformerBuilder("net.sf.json.JSONSerializer");
        
        builder.editMethods(name("toJSON"), modifier(Modifier.PUBLIC)).injectInterceptor(PARSING_INTERCEPTOR).group(GROUP);
        builder.editMethods(name("toJava"), modifier(Modifier.PUBLIC)).injectInterceptor(BASIC_INTERCEPTOR, JsonLibConstants.SERVICE_TYPE).group(GROUP);
        
        ClassFileTransformer transformer = builder.build();
        context.addClassFileTransformer(transformer);
    }

    private void addJSONObjectInterceptor(ProfilerPluginContext context) {
        ClassFileTransformerBuilder builder = context.getClassFileTransformerBuilder("net.sf.json.JSONObject");
        
        builder.editMethods(name("fromObject"), modifier(Modifier.PUBLIC)).injectInterceptor(PARSING_INTERCEPTOR).group(GROUP);
        builder.editMethods(name("toBean"), modifier(Modifier.PUBLIC)).injectInterceptor(BASIC_INTERCEPTOR, JsonLibConstants.SERVICE_TYPE).group(GROUP);
        builder.editMethods(name("toString"), modifier(Modifier.PUBLIC)).injectInterceptor(TO_STRING_INTERCEPTOR).group(GROUP);
        
        ClassFileTransformer transformer = builder.build();
        context.addClassFileTransformer(transformer);
    }

    private void addJSONArrayInterceptor(ProfilerPluginContext context) {
        ClassFileTransformerBuilder builder = context.getClassFileTransformerBuilder("net.sf.json.JSONArray");
        
        builder.editMethods(name("fromObject"), modifier(Modifier.PUBLIC)).injectInterceptor(PARSING_INTERCEPTOR).group(GROUP);
        builder.editMethods(name("toArray"), modifier(Modifier.PUBLIC)).injectInterceptor(BASIC_INTERCEPTOR, JsonLibConstants.SERVICE_TYPE).group(GROUP);
        builder.editMethods(name("toList"), modifier(Modifier.PUBLIC)).injectInterceptor(BASIC_INTERCEPTOR, JsonLibConstants.SERVICE_TYPE).group(GROUP);
        builder.editMethods(name("toCollection"), modifier(Modifier.PUBLIC)).injectInterceptor(BASIC_INTERCEPTOR, JsonLibConstants.SERVICE_TYPE).group(GROUP);
        builder.editMethods(name("toString"), modifier(Modifier.PUBLIC)).injectInterceptor(TO_STRING_INTERCEPTOR).group(GROUP);
                      
        ClassFileTransformer transformer = builder.build();
        context.addClassFileTransformer(transformer);
    } 
}
