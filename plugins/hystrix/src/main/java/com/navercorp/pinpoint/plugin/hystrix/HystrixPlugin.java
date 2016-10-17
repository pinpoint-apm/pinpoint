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
import com.navercorp.pinpoint.plugin.hystrix.interceptor.HystrixCommandTransformer;

import java.security.ProtectionDomain;

/**
 * Any Pinpoint profiler plugin must implement ProfilerPlugin interface.
 * ProfilerPlugin declares only one method {@link #setup(ProfilerPluginSetupContext)}.
 * You should implement the method to do whatever you need to setup your plugin with the passed ProfilerPluginSetupContext object.
 *
 * @author Jiaqi Feng
 */
public class HystrixPlugin implements ProfilerPlugin, TransformTemplateAware {
    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private TransformTemplate transformTemplate;

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        addTransformers();
    }

    private void addTransformers() {
        transformTemplate.transform("com.netflix.hystrix.HystrixCommand", new HystrixCommandTransformer());

        /*
         * After com.netflix.hystrix:hystrix-core:1.4.1 the api changed. The run() in subclass will be called
         * by com.netflix.hystrix.HystrixCommand$1.call() which is an inner class of HystrixCommand.
         * The HystrixCommand$1's type is Observable from rxjava package.
         * I choose HystrixCommand$1 to interceptor just because I found it in the call stack trace and it works.
         * Maybe there is a better way to do it.
         *
         * There is one drawback to intercept HystrixCommand$1, when the caller get the result, this call method
         * maybe still running some cleanup. This will cause HystrixCommand_1_4_x_IT integration test failure for the async trace
         * was not generated when verify. Though I think this doesn't matter for real app.
         */
        transformTemplate.transform("com.netflix.hystrix.HystrixCommand$1", new TransformCallback() {
            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

                InstrumentMethod executeCommand = target.getDeclaredMethod("call", "rx.Subscriber");
                if (executeCommand == null) {
                    // after hystrix-core 1.5.3, there are no arguments of call method
                    executeCommand = target.getDeclaredMethod("call");
                }
                if (executeCommand != null) {
                    executeCommand.addInterceptor("com.navercorp.pinpoint.plugin.hystrix.interceptor.HystrixObservableCallInterceptor");
                }

                return target.toBytecode();
            }
        });
    }

    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }
}
