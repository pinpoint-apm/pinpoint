/*
 * Copyright 2017 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.profiler.context.module;

import com.google.inject.Binding;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Scope;
import com.google.inject.Scopes;
import com.google.inject.TypeLiteral;
import com.google.inject.internal.BindingImpl;
import com.google.inject.internal.Scoping;
import com.navercorp.pinpoint.bootstrap.AgentOption;
import com.navercorp.pinpoint.bootstrap.DefaultAgentOption;
import com.navercorp.pinpoint.bootstrap.config.DefaultProfilerConfig;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.common.service.DefaultAnnotationKeyRegistryService;
import com.navercorp.pinpoint.common.service.DefaultServiceTypeRegistryService;
import com.navercorp.pinpoint.profiler.AgentInfoSender;
import com.navercorp.pinpoint.profiler.interceptor.registry.InterceptorRegistryBinder;
import com.navercorp.pinpoint.profiler.util.TestInterceptorRegistryBinder;
import org.junit.Assert;
import org.junit.Test;

import java.lang.annotation.Annotation;
import java.lang.instrument.Instrumentation;
import java.net.URL;
import java.util.Map;

import static org.mockito.Mockito.mock;

/**
 * @author Woonduk Kang(emeroad)
 */
public class DefaultApplicationContextTest {

    private static final String PINPOINT_PACKAGE_PREFIX = "com.navercorp.pinpoint.";

    @Test
    public void test() {
        DefaultApplicationContext applicationContext = newApplicationContext();
        try {
            Injector injector = applicationContext.getInjector();
            Map<Key<?>, Binding<?>> bindings = injector.getBindings();
            for (Map.Entry<Key<?>, Binding<?>> e : bindings.entrySet()) {
                Key<?> key = e.getKey();
                Binding<?> binding = e.getValue();

                if (isPinpointBinding(key)) {
                    boolean isSingletonScoped = Scopes.isSingleton(binding);
                    Assert.assertTrue("Binding " + key + " is not Singleton scoped", isSingletonScoped);
                }
            }
            AgentInfoSender instance1 = injector.getInstance(AgentInfoSender.class);
            AgentInfoSender instance2 = injector.getInstance(AgentInfoSender.class);
            Assert.assertSame(instance1, instance2);
        } finally {
            applicationContext.close();
        }
    }

    private boolean isPinpointBinding(Key<?> key) {
        TypeLiteral<?> typeLiteral = key.getTypeLiteral();
        if (typeLiteral != null && typeLiteral.toString().startsWith(PINPOINT_PACKAGE_PREFIX)) {
            return true;
        }
        Class<? extends Annotation> annotationType = key.getAnnotationType();
        if (annotationType != null) {
            return annotationType.getName().startsWith(PINPOINT_PACKAGE_PREFIX);
        }
        return false;
    }

    private DefaultApplicationContext newApplicationContext() {
        ProfilerConfig profilerConfig = new DefaultProfilerConfig();
        InterceptorRegistryBinder binder = new TestInterceptorRegistryBinder();
        Instrumentation instrumentation = mock(Instrumentation.class);
        AgentOption agentOption = new DefaultAgentOption(instrumentation, "mockAgent", "mockApplicationName", profilerConfig, new URL[0],
                null, new DefaultServiceTypeRegistryService(), new DefaultAnnotationKeyRegistryService());

        return new DefaultApplicationContext(agentOption, binder);
    }

}