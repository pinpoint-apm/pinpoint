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

import java.security.ProtectionDomain;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.MethodFilters;
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.PinpointClassFileTransformer;
import com.navercorp.pinpoint.bootstrap.interceptor.group.InterceptorGroup;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;

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

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        addObjectMapperEditor(context, "com.fasterxml.jackson.databind.ObjectMapper");
        addObjectReaderEditor(context, "com.fasterxml.jackson.databind.ObjectReader");
        addObjectWriterEditor(context, "com.fasterxml.jackson.databind.ObjectWriter");

        addObjectMapper_1_X_Editor(context, "org.codehaus.jackson.map.ObjectMapper");
        addObjectReaderEditor(context, "org.codehaus.jackson.map.ObjectReader");
        addObjectWriterEditor(context, "org.codehaus.jackson.map.ObjectWriter");
    }

    private void addObjectMapperEditor(ProfilerPluginSetupContext context, String clazzName) {
        context.addClassFileTransformer(clazzName, new PinpointClassFileTransformer() {

            @Override
            public byte[] transform(Instrumentor instrumentContext, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentContext.getInstrumentClass(classLoader, className, classfileBuffer);
                InterceptorGroup group = instrumentContext.getInterceptorGroup(GROUP);

                final InstrumentMethod constructor1 = target.getConstructor();
                addInterceptor(constructor1, BASIC_METHOD_INTERCEPTOR, group, SERVICE_TYPE);

                final InstrumentMethod constructor2 = target.getConstructor("com.fasterxml.jackson.core.JsonFactory");
                addInterceptor(constructor2, BASIC_METHOD_INTERCEPTOR, group, SERVICE_TYPE);

                final InstrumentMethod constructor3 = target.getConstructor("com.fasterxml.jackson.core.JsonFactory", "com.fasterxml.jackson.databind.ser.DefaultSerializerProvider", "com.fasterxml.jackson.databind.deser.DefaultDeserializationContext");
                addInterceptor(constructor3, BASIC_METHOD_INTERCEPTOR, group, SERVICE_TYPE);

                for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.name("writeValue"))) {
                    addInterceptor(method, BASIC_METHOD_INTERCEPTOR, group, SERVICE_TYPE);
                }

                for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.name("writeValueAsString"))) {
                    addInterceptor(method, WRITE_VALUE_AS_STRING_INTERCEPTOR, group);
                }

                for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.name("writeValueAsBytes"))) {
                    addInterceptor(method, WRITE_VALUE_AS_BYTES_INTERCEPTOR, group);
                }

                for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.name("readValue"))) {
                    addInterceptor(method, READ_VALUE_INTERCEPTOR, group);
                }

                return target.toBytecode();
            }

        });
    }

    private void addObjectMapper_1_X_Editor(ProfilerPluginSetupContext context, String clazzName) {
        context.addClassFileTransformer(clazzName, new PinpointClassFileTransformer() {

            @Override
            public byte[] transform(Instrumentor instrumentContext, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentContext.getInstrumentClass(classLoader, className, classfileBuffer);
                InterceptorGroup group = instrumentContext.getInterceptorGroup(GROUP);

                final InstrumentMethod constructor1 = target.getConstructor();
                addInterceptor(constructor1, BASIC_METHOD_INTERCEPTOR, group, SERVICE_TYPE);

                final InstrumentMethod constructor2 = target.getConstructor("org.codehaus.jackson.JsonFactory");
                addInterceptor(constructor2, BASIC_METHOD_INTERCEPTOR, group, SERVICE_TYPE);

                final InstrumentMethod constructor3 = target.getConstructor("org.codehaus.jackson.JsonFactory", "org.codehaus.jackson.map.SerializerProvider", "org.codehaus.jackson.map.DeserializerProvider");
                addInterceptor(constructor3, BASIC_METHOD_INTERCEPTOR, group, SERVICE_TYPE);

                final InstrumentMethod constructor4 = target.getConstructor("org.codehaus.jackson.map.SerializerFactory");
                addInterceptor(constructor4, BASIC_METHOD_INTERCEPTOR, group, SERVICE_TYPE);

                final InstrumentMethod constructor5 = target.getConstructor("org.codehaus.jackson.JsonFactory", "org.codehaus.jackson.map.SerializerProvider", "org.codehaus.jackson.map.DeserializerProvider", "org.codehaus.jackson.map.SerializationConfig", "org.codehaus.jackson.map.DeserializationConfig");
                addInterceptor(constructor5, BASIC_METHOD_INTERCEPTOR, group, SERVICE_TYPE);


                for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.name("writeValue"))) {
                    addInterceptor(method, BASIC_METHOD_INTERCEPTOR, group, SERVICE_TYPE);
                }

                for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.name("writeValueAsString"))) {
                    addInterceptor(method, WRITE_VALUE_AS_STRING_INTERCEPTOR, group);
                }

                for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.name("writeValueAsBytes"))) {
                    addInterceptor(method, WRITE_VALUE_AS_BYTES_INTERCEPTOR, group);
                }

                for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.name("readValue"))) {
                    addInterceptor(method, READ_VALUE_INTERCEPTOR, group);
                }

                return target.toBytecode();
            }

        });
    }


    private void addObjectReaderEditor(ProfilerPluginSetupContext context, String clazzName) {
        context.addClassFileTransformer(clazzName, new PinpointClassFileTransformer() {

            @Override
            public byte[] transform(Instrumentor instrumentContext, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentContext.getInstrumentClass(classLoader, className, classfileBuffer);
                InterceptorGroup group = instrumentContext.getInterceptorGroup(GROUP);

                for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.name("readValue", "readValues"))) {
                    addInterceptor(method, READ_VALUE_INTERCEPTOR, group);
                }

                return target.toBytecode();
            }

        });
    }

    private void addObjectWriterEditor(ProfilerPluginSetupContext context, String clazzName) {
        context.addClassFileTransformer(clazzName, new PinpointClassFileTransformer() {

            @Override
            public byte[] transform(Instrumentor instrumentContext, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentContext.getInstrumentClass(classLoader, className, classfileBuffer);
                InterceptorGroup group = instrumentContext.getInterceptorGroup(GROUP);

                for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.name("writeValue"))) {
                    addInterceptor(method, BASIC_METHOD_INTERCEPTOR, group, SERVICE_TYPE);
                }

                for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.name("writeValueAsString"))) {
                    addInterceptor(method, WRITE_VALUE_AS_STRING_INTERCEPTOR, group);
                }

                for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.name("writeValueAsBytes"))) {
                    addInterceptor(method, WRITE_VALUE_AS_BYTES_INTERCEPTOR, group);
                }

                return target.toBytecode();
            }

        });
    }

    private boolean addInterceptor(InstrumentMethod method, String interceptorClassName, InterceptorGroup group, Object... constructorArgs) {
        if (method != null) {
            try {
                method.addGroupedInterceptor(interceptorClassName, group, constructorArgs);
                return true;
            } catch (InstrumentException e) {
                if (logger.isWarnEnabled()) {
                    logger.warn("Unsupported method " + method, e);
                }
            }
        }
        return false;
    }

}
