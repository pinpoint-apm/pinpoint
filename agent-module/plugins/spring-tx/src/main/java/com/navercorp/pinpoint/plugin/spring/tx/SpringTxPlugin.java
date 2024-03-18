/*
 * Copyright 2022 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.spring.tx;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.MatchableTransformTemplate;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.MatchableTransformTemplateAware;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.plugin.spring.tx.interceptor.ReactiveTransactionSupportInterceptor;
import com.navercorp.pinpoint.plugin.spring.tx.interceptor.TransactionAspectSupportInterceptor;

import java.security.ProtectionDomain;

public class SpringTxPlugin implements ProfilerPlugin, MatchableTransformTemplateAware {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());

    private MatchableTransformTemplate transformTemplate;

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        SpringTxConfig config = new SpringTxConfig(context.getConfig());
        if (Boolean.FALSE == config.isEnabled()) {
            logger.info("{} disabled", this.getClass().getSimpleName());
            return;
        }
        logger.info("{} config:{}", this.getClass().getSimpleName(), config);

        transformTemplate.transform("org.springframework.transaction.interceptor.TransactionAspectSupport", TransactionAspectSupportTransform.class);
        transformTemplate.transform("org.springframework.transaction.interceptor.TransactionAspectSupport$ReactiveTransactionSupport", ReactiveTransactionSupportTransform.class);
    }

    @Override
    public void setTransformTemplate(MatchableTransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }


    public static class TransactionAspectSupportTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);

            final InstrumentMethod invokeWithinTransactionMethod = target.getDeclaredMethod("invokeWithinTransaction", "java.lang.reflect.Method", "java.lang.Class", "org.springframework.transaction.interceptor.TransactionAspectSupport$InvocationCallback");
            if (invokeWithinTransactionMethod != null) {
                invokeWithinTransactionMethod.addInterceptor(TransactionAspectSupportInterceptor.class);
            }

            return target.toBytecode();
        }
    }

    public static class ReactiveTransactionSupportTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);

            final InstrumentMethod invokeWithinTransactionMethod = target.getDeclaredMethod("invokeWithinTransaction", "java.lang.reflect.Method", "java.lang.Class", "org.springframework.transaction.interceptor.TransactionAspectSupport$InvocationCallback", "org.springframework.transaction.interceptor.TransactionAttribute", "org.springframework.transaction.ReactiveTransactionManager");
            if (invokeWithinTransactionMethod != null) {
                invokeWithinTransactionMethod.addInterceptor(ReactiveTransactionSupportInterceptor.class);
            }

            return target.toBytecode();
        }
    }
}