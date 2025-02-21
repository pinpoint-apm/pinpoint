/*
 * Copyright 2019 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.plugin.spring.cloud.sleuth;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.Matcher;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.Matchers;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.operand.InterfaceInternalNameMatcherOperand;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.MatchableTransformTemplate;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.MatchableTransformTemplateAware;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogManager;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogger;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.bootstrap.plugin.reactor.CoreSubscriberOnSubscribeInterceptor;
import com.navercorp.pinpoint.bootstrap.plugin.reactor.CoreSubscriberConstructorInterceptor;
import com.navercorp.pinpoint.bootstrap.plugin.reactor.CoreSubscriberOnNextInterceptor;
import com.navercorp.pinpoint.bootstrap.plugin.reactor.ReactorActualAccessor;
import com.navercorp.pinpoint.bootstrap.plugin.reactor.ReactorSubscriberAccessor;
import com.navercorp.pinpoint.common.util.ArrayUtils;

import java.security.ProtectionDomain;

import static com.navercorp.pinpoint.common.util.VarArgs.va;

public class SpringCloudSleuthPlugin implements ProfilerPlugin, MatchableTransformTemplateAware {
    private final PluginLogger logger = PluginLogManager.getLogger(this.getClass());
    private MatchableTransformTemplate transformTemplate;

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        final SpringCloudSleuthPluginConfig config = new SpringCloudSleuthPluginConfig(context.getConfig());
        if (!config.isEnable()) {
            logger.info("Disable {}", this.getClass().getSimpleName());
            return;
        }

        logger.info("{} config:{}", this.getClass().getSimpleName(), config);

        // for reactor
        addCoreSubscriber();
    }

    @Override
    public void setTransformTemplate(MatchableTransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }

    private void addCoreSubscriber() {
        final Matcher coreSubscriberMatcher = Matchers.newPackageBasedMatcher("org.springframework.cloud.sleuth.instrument.reactor", new InterfaceInternalNameMatcherOperand("reactor.core.CoreSubscriber", true));
        transformTemplate.transform(coreSubscriberMatcher, CoreSubscriberTransform.class);
    }

    public static class CoreSubscriberTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            target.addField(AsyncContextAccessor.class);
            target.addField(ReactorActualAccessor.class);
            target.addField(ReactorSubscriberAccessor.class);

            for (InstrumentMethod constructorMethod : target.getDeclaredConstructors()) {
                final String[] parameterTypes = constructorMethod.getParameterTypes();
                if (ArrayUtils.hasLength(parameterTypes)) {
                    constructorMethod.addInterceptor(CoreSubscriberConstructorInterceptor.class);
                }
            }

            final InstrumentMethod onSubscribeMethod = target.getDeclaredMethod("onSubscribe", "org.reactivestreams.Subscription");
            if (onSubscribeMethod != null) {
                onSubscribeMethod.addInterceptor(CoreSubscriberOnSubscribeInterceptor.class);
            }
            final InstrumentMethod onNextMethod = target.getDeclaredMethod("onNext", "java.lang.Object");
            if (onNextMethod != null) {
                onNextMethod.addInterceptor(CoreSubscriberOnNextInterceptor.class, va(SpringCloudSleuthConstants.SPRING_CLOUD_SLEUTH));
            }

            return target.toBytecode();
        }
    }
}