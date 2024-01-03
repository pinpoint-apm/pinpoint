/*
 * Copyright 2023 NAVER Corp.
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

package com.navercorp.pinpoint.test.plugin.agent;

import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
import com.navercorp.pinpoint.profiler.context.module.DefaultApplicationContext;
import com.navercorp.pinpoint.profiler.interceptor.registry.InterceptorRegistryBinder;
import com.navercorp.pinpoint.profiler.test.MockApplicationContextFactory;
import com.navercorp.pinpoint.profiler.test.PluginVerifierExternalAdaptor;
import com.navercorp.pinpoint.test.plugin.PluginTestInstanceCallback;
import com.navercorp.pinpoint.test.plugin.agent.classloader.MockInstrumentor;

public class PluginTestAgentStarter {

    private MockInstrumentor mockInstrumentor;
    private InterceptorRegistryBinder interceptorRegistryBinder;
    private PluginTestVerifier pluginTestVerifier;

    private DefaultApplicationContext applicationContext;

    public PluginTestAgentStarter(String configFile, ClassLoader classLoader) {
        final com.navercorp.pinpoint.profiler.test.MockApplicationContextFactory factory = new MockApplicationContextFactory();
        this.applicationContext = factory.build(configFile);
        this.mockInstrumentor = new MockInstrumentor(classLoader, applicationContext.getClassFileTransformer());
        this.interceptorRegistryBinder = applicationContext.getInterceptorRegistryBinder();
        this.pluginTestVerifier = new PluginVerifierExternalAdaptor(applicationContext);
    }

    public PluginTestInstanceCallback getCallback() {
        return new PluginTestInstanceCallback() {
            @Override
            public byte[] transform(ClassLoader classLoader, String name, byte[] classfileBuffer) throws ClassNotFoundException {
                return mockInstrumentor.transform(classLoader, name, classfileBuffer);
            }

            @Override
            public void before(boolean verify, boolean manageTraceObject) {
                interceptorRegistryBinder.bind();
                if (verify) {
                    PluginTestVerifierHolder.setInstance(pluginTestVerifier);
                    pluginTestVerifier.initialize(manageTraceObject);
                }
            }

            @Override
            public void after(boolean verify, boolean manageTraceObject) {
                interceptorRegistryBinder.unbind();
                if (verify) {
                    pluginTestVerifier.cleanUp(manageTraceObject);
                    PluginTestVerifierHolder.setInstance(null);
                }
            }

            @Override
            public void clear() {
                if (applicationContext != null) {
                    applicationContext.close();
                    applicationContext = null;
                }
                if (interceptorRegistryBinder.getInterceptorRegistryAdaptor() != null) {
                    interceptorRegistryBinder.getInterceptorRegistryAdaptor().clear();
                    interceptorRegistryBinder = null;
                }
                if (pluginTestVerifier != null) {
                    pluginTestVerifier = null;
                }
                if (mockInstrumentor != null) {
                    mockInstrumentor = null;
                }
            }
        };
    }
}
