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
package com.navercorp.pinpoint.plugin.json_lib;

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

import java.lang.reflect.Modifier;
import java.security.ProtectionDomain;

/**
 * @author Sangyoon Lee
 *
 */
public class JsonLibPlugin implements ProfilerPlugin, TransformTemplateAware {
    private static final String BASIC_INTERCEPTOR = BasicMethodInterceptor.class.getName();
    private static final String PARSING_INTERCEPTOR = "com.navercorp.pinpoint.plugin.json_lib.interceptor.ParsingInterceptor";
    private static final String TO_STRING_INTERCEPTOR = "com.navercorp.pinpoint.plugin.json_lib.interceptor.ToStringInterceptor";

    private static final String JSON_LIB_SCOPE = "json-lib";

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private TransformTemplate transformTemplate;

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        JsonLibConfig config = new JsonLibConfig(context.getConfig());
        logger.debug("[JsonLib] Initialized config={}", config);

        if (config.isProfile()) {
            addJSONSerializerInterceptor("net.sf.json.JSONSerializer");
            addJSONObjectInterceptor("net.sf.json.JSONObject");
            addJSONArrayInterceptor("net.sf.json.JSONArray");
        }
    }
    
    private void addJSONSerializerInterceptor(String clazzName) {
        transformTemplate.transform(clazzName, new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);

                for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.name("toJSON"))) {
                    addInterceptor(method, PARSING_INTERCEPTOR);
                }

                for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.name("toJava"))) {
                    addInterceptor(method, BASIC_INTERCEPTOR, JsonLibConstants.SERVICE_TYPE);
                }

                return target.toBytecode();
            }

        });

    }

    private void addJSONObjectInterceptor(String clazzName) {
        transformTemplate.transform(clazzName, new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);

                for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.name("fromObject"))) {
                    addInterceptor(method, PARSING_INTERCEPTOR);
                }

                for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.name("toBean"))) {
                    addInterceptor(method, BASIC_INTERCEPTOR, JsonLibConstants.SERVICE_TYPE);
                }

                for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.name("toString"))) {
                    addInterceptor(method, TO_STRING_INTERCEPTOR);
                }

                return target.toBytecode();
            }

        });
    }

    private void addJSONArrayInterceptor(String clazzName) {
        transformTemplate.transform(clazzName, new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);

                for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.name("fromObject"))) {
                    addInterceptor(method, PARSING_INTERCEPTOR);
                }

                for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.name("toArray"))) {
                    addInterceptor(method, BASIC_INTERCEPTOR, JsonLibConstants.SERVICE_TYPE);
                }

                for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.name("toList"))) {
                    addInterceptor(method, BASIC_INTERCEPTOR, JsonLibConstants.SERVICE_TYPE);
                }

                for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.name("toCollection"))) {
                    addInterceptor(method, BASIC_INTERCEPTOR, JsonLibConstants.SERVICE_TYPE);
                }

                for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.name("toString"))) {
                    addInterceptor(method, TO_STRING_INTERCEPTOR);
                }

                return target.toBytecode();
            }

        });

    }

    private boolean addInterceptor(InstrumentMethod method, String interceptorClassName, Object... constructorArgs) {
        if (method != null && isPublicMethod(method)) {
            try {
                method.addScopedInterceptor(interceptorClassName, constructorArgs, JSON_LIB_SCOPE);
                return true;
            } catch (InstrumentException e) {
                if (logger.isWarnEnabled()) {
                    logger.warn("Unsupported method " + method, e);
                }
            }
        }
        return false;
    }


    private boolean isPublicMethod(InstrumentMethod method) {
        int modifier = method.getModifiers();
        return Modifier.isPublic(modifier);
    }

    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }

}
