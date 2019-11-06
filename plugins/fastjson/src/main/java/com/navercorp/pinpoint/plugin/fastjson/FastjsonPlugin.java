/*
 *  Copyright 2018 NAVER Corp.
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.navercorp.pinpoint.plugin.fastjson;

import com.navercorp.pinpoint.bootstrap.instrument.*;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplate;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplateAware;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.plugin.fastjson.interceptor.ParseArrayInterceptor;
import com.navercorp.pinpoint.plugin.fastjson.interceptor.ParseInterceptor;
import com.navercorp.pinpoint.plugin.fastjson.interceptor.ParseObjectInterceptor;
import com.navercorp.pinpoint.plugin.fastjson.interceptor.ToJavaObjectInterceptor;
import com.navercorp.pinpoint.plugin.fastjson.interceptor.ToJsonBytesInterceptor;
import com.navercorp.pinpoint.plugin.fastjson.interceptor.ToJsonInterceptor;
import com.navercorp.pinpoint.plugin.fastjson.interceptor.ToJsonStringInterceptor;
import com.navercorp.pinpoint.plugin.fastjson.interceptor.WriteJsonStringInterceptor;

import java.security.ProtectionDomain;

/**
 * The type Fastjson plugin.
 *
 * @author Victor.Zxy
 * @version 1.8.1
 * @since 2017/07/17
 */
public class FastjsonPlugin implements ProfilerPlugin, TransformTemplateAware {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());

    private TransformTemplate transformTemplate;

    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }

    @Override
    public void setup(ProfilerPluginSetupContext context) {

        FastjsonConfig config = new FastjsonConfig(context.getConfig());
        if (!config.isProfile()) {
            logger.info("{} disabled", this.getClass().getSimpleName());
            return;
        }
        logger.info("{} config:{}", this.getClass().getSimpleName(), config);
        transformTemplate.transform("com.alibaba.fastjson.JSON", JSONTransformer.class);

    }

    public static class JSONTransformer implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

            for (InstrumentMethod m : target.getDeclaredMethods(MethodFilters.name("parse"))) {
                m.addScopedInterceptor(ParseInterceptor.class, FastjsonConstants.SCOPE);
            }

            for (InstrumentMethod m : target.getDeclaredMethods(MethodFilters.name("parseObject"))) {
                m.addScopedInterceptor(ParseObjectInterceptor.class, FastjsonConstants.SCOPE);
            }

            for (InstrumentMethod m : target.getDeclaredMethods(MethodFilters.name("parseArray"))) {
                m.addScopedInterceptor(ParseArrayInterceptor.class, FastjsonConstants.SCOPE);
            }

            for (InstrumentMethod m : target.getDeclaredMethods(MethodFilters.name("toJSON"))) {
                m.addScopedInterceptor(ToJsonInterceptor.class, FastjsonConstants.SCOPE);
            }

            for (InstrumentMethod m : target.getDeclaredMethods(MethodFilters.name("toJavaObject"))) {
                m.addScopedInterceptor(ToJavaObjectInterceptor.class, FastjsonConstants.SCOPE);
            }

            for (InstrumentMethod m : target.getDeclaredMethods(MethodFilters.name("toJSONString"))) {
                m.addScopedInterceptor(ToJsonStringInterceptor.class, FastjsonConstants.SCOPE);
            }

            for (InstrumentMethod m : target.getDeclaredMethods(MethodFilters.name("toJSONBytes"))) {
                m.addScopedInterceptor(ToJsonBytesInterceptor.class, FastjsonConstants.SCOPE);
            }

            for (InstrumentMethod m : target.getDeclaredMethods(MethodFilters.name("writeJSONString"))) {
                m.addScopedInterceptor(WriteJsonStringInterceptor.class, FastjsonConstants.SCOPE);
            }

            return target.toBytecode();
        }
    }
}
