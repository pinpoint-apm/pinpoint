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
package com.navercorp.pinpoint.plugin.jackson;

import java.lang.instrument.ClassFileTransformer;

import com.navercorp.pinpoint.bootstrap.instrument.MethodFilters;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginContext;
import com.navercorp.pinpoint.bootstrap.plugin.transformer.ClassFileTransformerBuilder;
import com.navercorp.pinpoint.bootstrap.plugin.transformer.ConstructorTransformerBuilder;
import com.navercorp.pinpoint.bootstrap.plugin.transformer.MethodTransformerProperty;

/**
 * @author Sungkook Kim
 *
 */
public class JacksonPlugin implements ProfilerPlugin, JacksonConstants {
    private static final String GROUP = "JACKSON_OBJECTMAPPER_GROUP";

    private static final String BASIC_METHOD_INTERCEPTOR = "com.navercorp.pinpoint.bootstrap.interceptor.BasicMethodInterceptor";
    private static final String READ_VALUE_INTERCEPTOR = "com.navercorp.pinpoint.plugin.jackson.interceptor.ReadValueInterceptor";
    private static final String WRITE_VALUE_AS_BYTES_INTERCEPTOR = "com.navercorp.pinpoint.plugin.jackson.interceptor.WriteValueAsBytesInterceptor";
    private static final String WRITE_VALUE_AS_STRING_INTERCEPTOR = "com.navercorp.pinpoint.plugin.jackson.interceptor.WriteValueAsStringInterceptor";

    @Override
    public void setup(ProfilerPluginContext context) {
        intercept_ObjectMapper(context);
        intercept_ObjectReader(context, "com.fasterxml.jackson.databind.ObjectReader");
        intercept_ObjectWriter(context, "com.fasterxml.jackson.databind.ObjectWriter");
        
        intercept_ObjectMapper_1_x(context);
        intercept_ObjectReader(context, "org.codehaus.jackson.map.ObjectReader");
        intercept_ObjectWriter(context, "org.codehaus.jackson.map.ObjectWriter");
    }

    private void intercept_ObjectMapper(ProfilerPluginContext context) {
        ClassFileTransformerBuilder builder = context.getClassFileTransformerBuilder("com.fasterxml.jackson.databind.ObjectMapper"); 

        /* constructor */
        builder.editConstructor().injectInterceptor(BASIC_METHOD_INTERCEPTOR, SERVICE_TYPE).group(GROUP);
        builder.editConstructor("com.fasterxml.jackson.core.JsonFactory").injectInterceptor(BASIC_METHOD_INTERCEPTOR, SERVICE_TYPE).group(GROUP);
        builder.editConstructor("com.fasterxml.jackson.core.JsonFactory", "com.fasterxml.jackson.databind.ser.DefaultSerializerProvider", "com.fasterxml.jackson.databind.deser.DefaultDeserializationContext").injectInterceptor(BASIC_METHOD_INTERCEPTOR, SERVICE_TYPE).group(GROUP);

        /* serialization */
        builder.editMethods(MethodFilters.name("writeValue")).injectInterceptor(BASIC_METHOD_INTERCEPTOR, SERVICE_TYPE).group(GROUP);
        builder.editMethods(MethodFilters.name("writeValueAsString")).injectInterceptor(WRITE_VALUE_AS_STRING_INTERCEPTOR).group(GROUP);
        builder.editMethods(MethodFilters.name("writeValueAsBytes")).injectInterceptor(WRITE_VALUE_AS_BYTES_INTERCEPTOR).group(GROUP);

        /* deserialization */
        builder.editMethods(MethodFilters.name("readValue")).injectInterceptor(READ_VALUE_INTERCEPTOR).group(GROUP);

        ClassFileTransformer transformer = builder.build();
        context.addClassFileTransformer(transformer);
    }
    
    private void intercept_ObjectReader(ProfilerPluginContext context, String className) {
        ClassFileTransformerBuilder builder = context.getClassFileTransformerBuilder(className); 

        /* deserialization */
        builder.editMethods(MethodFilters.name("readValue", "readValues")).injectInterceptor(READ_VALUE_INTERCEPTOR).group(GROUP);

        ClassFileTransformer transformer = builder.build();
        context.addClassFileTransformer(transformer);
    }
    
    private void intercept_ObjectWriter(ProfilerPluginContext context, String className) {
        ClassFileTransformerBuilder builder = context.getClassFileTransformerBuilder(className); 

        /* deserialization */
        builder.editMethods(MethodFilters.name("writeValue")).injectInterceptor(BASIC_METHOD_INTERCEPTOR, SERVICE_TYPE).group(GROUP);
        builder.editMethods(MethodFilters.name("writeValueAsString")).injectInterceptor(WRITE_VALUE_AS_STRING_INTERCEPTOR).group(GROUP);
        builder.editMethods(MethodFilters.name("writeValueAsBytes")).injectInterceptor(WRITE_VALUE_AS_BYTES_INTERCEPTOR).group(GROUP);

        ClassFileTransformer transformer = builder.build();
        context.addClassFileTransformer(transformer);
    }

    
    private void intercept_ObjectMapper_1_x(ProfilerPluginContext context) {
        ClassFileTransformerBuilder builder = context.getClassFileTransformerBuilder("org.codehaus.jackson.map.ObjectMapper"); 

        /* constructor */
        builder.editConstructor().injectInterceptor(BASIC_METHOD_INTERCEPTOR, SERVICE_TYPE).group(GROUP);
        builder.editConstructor("org.codehaus.jackson.JsonFactory").injectInterceptor(BASIC_METHOD_INTERCEPTOR, SERVICE_TYPE).group(GROUP);
        builder.editConstructor("org.codehaus.jackson.JsonFactory", "org.codehaus.jackson.map.SerializerProvider", "org.codehaus.jackson.map.DeserializerProvider").injectInterceptor(BASIC_METHOD_INTERCEPTOR, SERVICE_TYPE).group(GROUP);

        ConstructorTransformerBuilder cb0 = builder.editConstructor("org.codehaus.jackson.map.SerializerFactory");
        cb0.property(MethodTransformerProperty.IGNORE_IF_NOT_EXIST);
        cb0.injectInterceptor(BASIC_METHOD_INTERCEPTOR, SERVICE_TYPE).group(GROUP);
        
        ConstructorTransformerBuilder cb1 = builder.editConstructor("org.codehaus.jackson.JsonFactory", "org.codehaus.jackson.map.SerializerProvider", "org.codehaus.jackson.map.DeserializerProvider", "org.codehaus.jackson.map.SerializationConfig", "org.codehaus.jackson.map.DeserializationConfig");
        cb1.property(MethodTransformerProperty.IGNORE_IF_NOT_EXIST);
        cb1.injectInterceptor(BASIC_METHOD_INTERCEPTOR, SERVICE_TYPE).group(GROUP);

        /* serialization */
        builder.editMethods(MethodFilters.name("writeValue")).injectInterceptor(BASIC_METHOD_INTERCEPTOR, SERVICE_TYPE).group(GROUP);
        builder.editMethods(MethodFilters.name("writeValueAsString")).injectInterceptor(WRITE_VALUE_AS_STRING_INTERCEPTOR).group(GROUP);
        builder.editMethods(MethodFilters.name("writeValueAsBytes")).injectInterceptor(WRITE_VALUE_AS_BYTES_INTERCEPTOR).group(GROUP);

        /* deserialization */
        builder.editMethods(MethodFilters.name("readValue")).injectInterceptor(READ_VALUE_INTERCEPTOR).group(GROUP);

        ClassFileTransformer transformer = builder.build();
        context.addClassFileTransformer(transformer);
    }
}
