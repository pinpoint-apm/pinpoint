/*
 * Copyright 2014 NAVER Corp.
 *
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
package com.navercorp.pinpoint.plugin.gson;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.MethodFilters;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplate;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplateAware;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.plugin.gson.interceptor.FromJsonInterceptor;
import com.navercorp.pinpoint.plugin.gson.interceptor.ToJsonInterceptor;

import java.security.ProtectionDomain;

/**
 * @author ChaYoung You
 */
public class GsonPlugin implements ProfilerPlugin, TransformTemplateAware {

    private static final String GSON_SCOPE = "GSON_SCOPE";

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());

    private TransformTemplate transformTemplate;

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        GsonConfig config = new GsonConfig(context.getConfig());
        if (!config.isProfile()) {
            logger.info("{} disabled", this.getClass().getSimpleName());
        }
        logger.info("{} config:{}", this.getClass().getSimpleName(), config);
        transformTemplate.transform("com.google.gson.Gson", GsonTransform.class);

    }

    public static class GsonTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

            for (InstrumentMethod m : target.getDeclaredMethods(MethodFilters.name("fromJson"))) {
                m.addScopedInterceptor(FromJsonInterceptor.class, GSON_SCOPE);
            }

            for (InstrumentMethod m : target.getDeclaredMethods(MethodFilters.name("toJson"))) {
                m.addScopedInterceptor(ToJsonInterceptor.class, GSON_SCOPE);
            }

            return target.toBytecode();
        }
    }


    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }
}
