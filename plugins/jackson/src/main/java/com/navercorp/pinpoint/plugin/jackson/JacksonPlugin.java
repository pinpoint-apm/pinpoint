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

import com.navercorp.pinpoint.bootstrap.interceptor.group.ExecutionPolicy;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginContext;
import com.navercorp.pinpoint.bootstrap.plugin.transformer.ClassFileTransformerBuilder;
import com.navercorp.pinpoint.plugin.jackson.filter.MethodNameFilter;

/**
 * @author Sungkook Kim
 *
 */
public class JacksonPlugin implements ProfilerPlugin, JacksonConstants {
    private static final String JACKSON_OBJECTMAPPER_GROUP = "JACKSON_OBJECTMAPPER_GROUP";

    @Override
    public void setup(ProfilerPluginContext context) {
        intercept_ObjectMapper(context);
    }

    private void intercept_ObjectMapper(ProfilerPluginContext context) {
        ClassFileTransformerBuilder builder = context.getClassFileTransformerBuilder("com.fasterxml.jackson.databind.ObjectMapper"); 

        /* constructor */
        builder.editConstructor().injectInterceptor("com.navercorp.pinpoint.bootstrap.interceptor.BasicMethodInterceptor", SERVICE_TYPE).group(JACKSON_OBJECTMAPPER_GROUP);
        builder.editConstructor("com.fasterxml.jackson.core.JsonFactory").injectInterceptor("com.navercorp.pinpoint.bootstrap.interceptor.BasicMethodInterceptor", SERVICE_TYPE).group(JACKSON_OBJECTMAPPER_GROUP);
        builder.editConstructor("com.fasterxml.jackson.core.JsonFactory", "com.fasterxml.jackson.databind.ser.DefaultSerializerProvider", "com.fasterxml.jackson.databind.deser.DefaultDeserializationContext").injectInterceptor("com.navercorp.pinpoint.bootstrap.interceptor.BasicMethodInterceptor", SERVICE_TYPE).group(JACKSON_OBJECTMAPPER_GROUP, ExecutionPolicy.INTERNAL);

        /* serialization */
        builder.editMethod("writeValueAsString", "java.lang.Object").injectInterceptor("com.navercorp.pinpoint.plugin.jackson.interceptor.ObjectMapperWriteValueInterceptor");
        builder.editMethod("writeValueAsBytes", "java.lang.Object").injectInterceptor("com.navercorp.pinpoint.plugin.jackson.interceptor.ObjectMapperWriteValueInterceptor");
        builder.editMethods(new MethodNameFilter("writeValue")).injectInterceptor("com.navercorp.pinpoint.plugin.jackson.interceptor.ObjectMapperWriteValueInterceptor");

        /* deserialization */
        builder.editMethods(new MethodNameFilter("readValue")).injectInterceptor("com.navercorp.pinpoint.plugin.jackson.interceptor.ObjectMapperReadValueInterceptor");

        ClassFileTransformer transformer = builder.build();
        context.addClassFileTransformer(transformer);
    }
}
