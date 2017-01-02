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
package com.navercorp.pinpoint.plugin.hystrix;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplate;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplateAware;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.plugin.hystrix.transformer.HystrixCommandTransformer;

import java.security.ProtectionDomain;

/**
 * Any Pinpoint profiler plugin must implement ProfilerPlugin interface.
 * ProfilerPlugin declares only one method {@link #setup(ProfilerPluginSetupContext)}.
 * You should implement the method to do whatever you need to setup your plugin with the passed ProfilerPluginSetupContext object.
 *
 * @author Jiaqi Feng
 * @author HyunGil Jeong
 */
public class HystrixPlugin implements ProfilerPlugin, TransformTemplateAware {
    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private TransformTemplate transformTemplate;

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        HystrixPluginConfig config = new HystrixPluginConfig(context.getConfig());
        if (config.isTraceHystrix()) {
            addTransformers(config.getNumHystrixCommandAnonymousInnerClass());

        }
    }

    private void addTransformers(int numHystrixCommandAnonymousLocalClass) {
        transformTemplate.transform("com.netflix.hystrix.HystrixCommand", new HystrixCommandTransformer());

        /*
         * After com.netflix.hystrix:hystrix-core:1.4.0 the api changed.
         * The run() and getFallback() methods of the subclass will be called by HystrixCommand's
         * anonymous inner classes.
         *
         * Safest way (although ugly) is to predefine the anonymous inner class names and check each of them to inject
         * their appropriate interceptors as they are loaded.
         *
         * The anonymous inner classes that should be modified may differ according to hystrix-core version. This is
         * simply something that we'll have to keep updating if any changes occur.
         * (Any breaking changes can be detected through integration tests.)
         */

        // number of anonymous inner classes to look for.
        // We start with 3 only because the most recent version requires this many. May be better to make this
        // configurable but for now let's just hard-code it.
        final int numAnonymousInnerClassesToTest = numHystrixCommandAnonymousLocalClass;
        for (int i = 0; i < numAnonymousInnerClassesToTest; ++i) {
            String anonymousInnerClassName = "com.netflix.hystrix.HystrixCommand$" + (i + 1);
            logger.debug("Registering transformer for {}", anonymousInnerClassName);

            transformTemplate.transform(anonymousInnerClassName, new TransformCallback() {
                @Override
                public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                    InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

                    if (target.hasEnclosingMethod("getExecutionObservable")) {
                        // 1.4.0 ~ 1.5.2 - void call(Subscriber<? super R> s)
                        InstrumentMethod method = target.getDeclaredMethod("call", "rx.Subscriber");
                        // 1.5.3+ - Observable<R> call()
                        if (method == null) {
                            method = target.getDeclaredMethod("call");
                            // 1.5.4+ - May be another anonymous class inside getExecutionObservable()
                            if (!method.getReturnType().equals("rx.Observable")) {
                                return null;
                            }
                        }
                        if (method != null) {
                            // Add getter for the enclosing instance
                            target.addGetter("com.navercorp.pinpoint.plugin.hystrix.field.EnclosingInstanceFieldGetter", "this$0");
                            method.addInterceptor("com.navercorp.pinpoint.plugin.hystrix.interceptor.ExecutionObservableCallInterceptor");
                            return target.toBytecode();
                        } else {
                            logger.warn("Unknown version of HystrixCommand.getExecutionObservable() detected");
                            return null;
                        }
                    } else if (target.hasEnclosingMethod("getFallbackObservable")) {
                        // 1.4.0 ~ 1.5.2 - void call(Subscriber<? super R> s)
                        InstrumentMethod method = target.getDeclaredMethod("call", "rx.Subscriber");
                        // 1.5.3+ - Observable<R> call()
                        if (method == null) {
                            method = target.getDeclaredMethod("call");
                        }
                        if (method != null) {
                            // Add getter for the enclosing instance
                            target.addGetter("com.navercorp.pinpoint.plugin.hystrix.field.EnclosingInstanceFieldGetter", "this$0");
                            method.addInterceptor("com.navercorp.pinpoint.plugin.hystrix.interceptor.FallbackObservableCallInterceptor");
                            return target.toBytecode();
                        } else {
                            logger.warn("Unknown version of HystrixCommand.getFallbackObservable detected");
                            return null;
                        }
                    } else {
                        return null;
                    }
                }
            });
        }
    }

    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }
}
