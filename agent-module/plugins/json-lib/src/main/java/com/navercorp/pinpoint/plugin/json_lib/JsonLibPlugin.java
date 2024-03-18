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
import com.navercorp.pinpoint.plugin.json_lib.interceptor.ParsingInterceptor;
import com.navercorp.pinpoint.plugin.json_lib.interceptor.ToStringInterceptor;

import java.security.ProtectionDomain;

/**
 * @author Sangyoon Lee
 *
 */
public class JsonLibPlugin implements ProfilerPlugin, TransformTemplateAware {

    private static final String JSON_LIB_SCOPE = "json-lib";

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private TransformTemplate transformTemplate;

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        JsonLibConfig config = new JsonLibConfig(context.getConfig());
        if (!config.isProfile()) {
            logger.info("{} disabled", this.getClass().getSimpleName());
            return;
        }
        logger.info("{} config:{}", this.getClass().getSimpleName(), config);
        addJSONSerializerInterceptor("net.sf.json.JSONSerializer");
        addJSONObjectInterceptor("net.sf.json.JSONObject");
        addJSONArrayInterceptor("net.sf.json.JSONArray");
    }
    
    private void addJSONSerializerInterceptor(String clazzName) {
        transformTemplate.transform(clazzName, JSONSerializerTransform.class);
    }
    public static class JSONSerializerTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);

            for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.name("toJSON"))) {
                JsonUtils.addInterceptor(method, ParsingInterceptor.class);
            }

            for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.name("toJava"))) {
                JsonUtils.addInterceptor(method, BasicMethodInterceptor.class, JsonLibConstants.SERVICE_TYPE);
            }

            return target.toBytecode();
        }

    }


    private void addJSONObjectInterceptor(String clazzName) {
        transformTemplate.transform(clazzName, JSONObjectTransform.class);
    }

    public static class JSONObjectTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);

            for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.name("fromObject"))) {
                JsonUtils.addInterceptor(method, ParsingInterceptor.class);
            }

            for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.name("toBean"))) {
                JsonUtils.addInterceptor(method, BasicMethodInterceptor.class, JsonLibConstants.SERVICE_TYPE);
            }

            for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.name("toString"))) {
                JsonUtils.addInterceptor(method, ToStringInterceptor.class);
            }

            return target.toBytecode();
        }

    }

    private void addJSONArrayInterceptor(String clazzName) {
        transformTemplate.transform(clazzName, JSONArrayTransform.class);

    }

    public static class JSONArrayTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);

            for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.name("fromObject"))) {
                JsonUtils.addInterceptor(method, ParsingInterceptor.class);
            }

            for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.name("toArray"))) {
                JsonUtils.addInterceptor(method, BasicMethodInterceptor.class, JsonLibConstants.SERVICE_TYPE);
            }

            for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.name("toList"))) {
                JsonUtils.addInterceptor(method, BasicMethodInterceptor.class, JsonLibConstants.SERVICE_TYPE);
            }

            for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.name("toCollection"))) {
                JsonUtils.addInterceptor(method, BasicMethodInterceptor.class, JsonLibConstants.SERVICE_TYPE);
            }

            for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.name("toString"))) {
                JsonUtils.addInterceptor(method, ToStringInterceptor.class);
            }

            return target.toBytecode();
        }

    }



    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }

}
