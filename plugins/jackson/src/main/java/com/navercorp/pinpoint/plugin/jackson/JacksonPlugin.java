/*
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

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.MethodFilters;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplate;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplateAware;
import com.navercorp.pinpoint.bootstrap.interceptor.BasicMethodInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.plugin.jackson.interceptor.ReadValueInterceptor;
import com.navercorp.pinpoint.plugin.jackson.interceptor.WriteValueAsBytesInterceptor;
import com.navercorp.pinpoint.plugin.jackson.interceptor.WriteValueAsStringInterceptor;

import static com.navercorp.pinpoint.common.util.VarArgs.va;

import java.security.ProtectionDomain;

/**
 * @author Sungkook Kim
 *
 */
public class JacksonPlugin implements ProfilerPlugin, TransformTemplateAware {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private TransformTemplate transformTemplate;

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        JacksonConfig config = new JacksonConfig(context.getConfig());
        if (!config.isProfile()) {
            logger.info("{} disabled", this.getClass().getSimpleName());
            return;
        }
        logger.info("{} config:{}", this.getClass().getSimpleName(), config);
        addObjectMapperEditor("com.fasterxml.jackson.databind.ObjectMapper");
        addObjectReaderEditor("com.fasterxml.jackson.databind.ObjectReader");
        addObjectWriterEditor("com.fasterxml.jackson.databind.ObjectWriter");

        addObjectMapper_1_X_Editor("org.codehaus.jackson.map.ObjectMapper");
        addObjectReaderEditor("org.codehaus.jackson.map.ObjectReader");
        addObjectWriterEditor("org.codehaus.jackson.map.ObjectWriter");
    }

    private void addObjectMapperEditor(String clazzName) {
        transformTemplate.transform(clazzName, ObjectMapperTransform.class);
    }

    public static class ObjectMapperTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);

            final InstrumentMethod constructor1 = target.getConstructor();
            JacksonUtils.addInterceptor(constructor1, BasicMethodInterceptor.class, va(JacksonConstants.SERVICE_TYPE));

            final InstrumentMethod constructor2 = target.getConstructor("com.fasterxml.jackson.core.JsonFactory");
            JacksonUtils.addInterceptor(constructor2, BasicMethodInterceptor.class, va(JacksonConstants.SERVICE_TYPE));

            final InstrumentMethod constructor3 = target.getConstructor("com.fasterxml.jackson.core.JsonFactory", "com.fasterxml.jackson.databind.ser.DefaultSerializerProvider", "com.fasterxml.jackson.databind.deser.DefaultDeserializationContext");
            JacksonUtils.addInterceptor(constructor3, BasicMethodInterceptor.class, va(JacksonConstants.SERVICE_TYPE));

            for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.name("writeValue"))) {
                JacksonUtils.addInterceptor(method, BasicMethodInterceptor.class, va(JacksonConstants.SERVICE_TYPE));
            }

            for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.name("writeValueAsString"))) {
                JacksonUtils.addInterceptor(method, WriteValueAsStringInterceptor.class);
            }

            for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.name("writeValueAsBytes"))) {
                JacksonUtils.addInterceptor(method, WriteValueAsBytesInterceptor.class);
            }

            for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.name("readValue"))) {
                JacksonUtils.addInterceptor(method, ReadValueInterceptor.class);
            }

            return target.toBytecode();
        }

    }

    private void addObjectMapper_1_X_Editor(String clazzName) {
        transformTemplate.transform(clazzName, ObjectMapperV1Transform.class);
    }

    public static class ObjectMapperV1Transform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);

            final InstrumentMethod constructor1 = target.getConstructor();
            JacksonUtils.addInterceptor(constructor1, BasicMethodInterceptor.class, va(JacksonConstants.SERVICE_TYPE));

            final InstrumentMethod constructor2 = target.getConstructor("org.codehaus.jackson.JsonFactory");
            JacksonUtils.addInterceptor(constructor2, BasicMethodInterceptor.class, va(JacksonConstants.SERVICE_TYPE));

            final InstrumentMethod constructor3 = target.getConstructor("org.codehaus.jackson.JsonFactory", "org.codehaus.jackson.map.SerializerProvider", "org.codehaus.jackson.map.DeserializerProvider");
            JacksonUtils.addInterceptor(constructor3, BasicMethodInterceptor.class, va(JacksonConstants.SERVICE_TYPE));

            final InstrumentMethod constructor4 = target.getConstructor("org.codehaus.jackson.map.SerializerFactory");
            JacksonUtils.addInterceptor(constructor4, BasicMethodInterceptor.class, va(JacksonConstants.SERVICE_TYPE));

            final InstrumentMethod constructor5 = target.getConstructor("org.codehaus.jackson.JsonFactory", "org.codehaus.jackson.map.SerializerProvider", "org.codehaus.jackson.map.DeserializerProvider", "org.codehaus.jackson.map.SerializationConfig", "org.codehaus.jackson.map.DeserializationConfig");
            JacksonUtils.addInterceptor(constructor5, BasicMethodInterceptor.class, va(JacksonConstants.SERVICE_TYPE));


            for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.name("writeValue"))) {
                JacksonUtils.addInterceptor(method, BasicMethodInterceptor.class, va(JacksonConstants.SERVICE_TYPE));
            }

            for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.name("writeValueAsString"))) {
                JacksonUtils.addInterceptor(method, WriteValueAsStringInterceptor.class);
            }

            for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.name("writeValueAsBytes"))) {
                JacksonUtils.addInterceptor(method, WriteValueAsBytesInterceptor.class);
            }

            for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.name("readValue"))) {
                JacksonUtils.addInterceptor(method, ReadValueInterceptor.class);
            }

            return target.toBytecode();
        }

    }


    private void addObjectReaderEditor(String clazzName) {
        transformTemplate.transform(clazzName, ObjectReaderTransform.class);
    }

    public static class ObjectReaderTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);

            for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.name("readValue", "readValues"))) {
                JacksonUtils.addInterceptor(method, ReadValueInterceptor.class);
            }

            return target.toBytecode();
        }

    }

    private void addObjectWriterEditor(String clazzName) {
        transformTemplate.transform(clazzName, ObjectWriterTransform.class);
    }

    public static class ObjectWriterTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
//                InterceptorScope scope = instrumentor.getInterceptorScope(JACKSON_SCOPE);

            for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.name("writeValue"))) {
                JacksonUtils.addInterceptor(method, BasicMethodInterceptor.class, va(JacksonConstants.SERVICE_TYPE));
            }

            for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.name("writeValueAsString"))) {
                JacksonUtils.addInterceptor(method, WriteValueAsStringInterceptor.class);
            }

            for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.name("writeValueAsBytes"))) {
                JacksonUtils.addInterceptor(method, WriteValueAsBytesInterceptor.class);
            }

            return target.toBytecode();
        }

    }



    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }
}
