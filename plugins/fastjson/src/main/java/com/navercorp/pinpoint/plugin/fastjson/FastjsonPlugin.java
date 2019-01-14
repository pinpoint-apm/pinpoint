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

        logger.debug("[Fastjson] Initialized config={}", config);

        if (config.isProfile()) {

            transformTemplate.transform("com.alibaba.fastjson.JSON", new TransformCallback() {

                @Override
                public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                    InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

                    for (InstrumentMethod m : target.getDeclaredMethods(MethodFilters.name("parse"))) {
                        m.addScopedInterceptor("com.navercorp.pinpoint.plugin.fastjson.interceptor.ParseInterceptor", FastjsonConstants.SCOPE);
                    }

                    for (InstrumentMethod m : target.getDeclaredMethods(MethodFilters.name("parseObject"))) {
                        m.addScopedInterceptor("com.navercorp.pinpoint.plugin.fastjson.interceptor.ParseObjectInterceptor", FastjsonConstants.SCOPE);
                    }

                    for (InstrumentMethod m : target.getDeclaredMethods(MethodFilters.name("parseArray"))) {
                        m.addScopedInterceptor("com.navercorp.pinpoint.plugin.fastjson.interceptor.ParseArrayInterceptor", FastjsonConstants.SCOPE);
                    }

                    for (InstrumentMethod m : target.getDeclaredMethods(MethodFilters.name("toJSON"))) {
                        m.addScopedInterceptor("com.navercorp.pinpoint.plugin.fastjson.interceptor.ToJsonInterceptor", FastjsonConstants.SCOPE);
                    }

                    for (InstrumentMethod m : target.getDeclaredMethods(MethodFilters.name("toJavaObject"))) {
                        m.addScopedInterceptor("com.navercorp.pinpoint.plugin.fastjson.interceptor.ToJavaObjectInterceptor", FastjsonConstants.SCOPE);
                    }

                    for (InstrumentMethod m : target.getDeclaredMethods(MethodFilters.name("toJSONString"))) {
                        m.addScopedInterceptor("com.navercorp.pinpoint.plugin.fastjson.interceptor.ToJsonStringInterceptor", FastjsonConstants.SCOPE);
                    }

                    for (InstrumentMethod m : target.getDeclaredMethods(MethodFilters.name("toJSONBytes"))) {
                        m.addScopedInterceptor("com.navercorp.pinpoint.plugin.fastjson.interceptor.ToJsonBytesInterceptor", FastjsonConstants.SCOPE);
                    }

                    for (InstrumentMethod m : target.getDeclaredMethods(MethodFilters.name("writeJSONString"))) {
                        m.addScopedInterceptor("com.navercorp.pinpoint.plugin.fastjson.interceptor.WriteJsonStringInterceptor", FastjsonConstants.SCOPE);
                    }

                    return target.toBytecode();
                }
            });
        }
    }
}
