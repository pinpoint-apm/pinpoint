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

package com.navercorp.pinpoint.test;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.util.Modules;
import com.navercorp.pinpoint.bootstrap.AgentOption;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.interceptor.registry.InterceptorRegistry;
import com.navercorp.pinpoint.bootstrap.interceptor.registry.InterceptorRegistryAdaptor;
import com.navercorp.pinpoint.profiler.AgentInformation;
import com.navercorp.pinpoint.profiler.context.module.ApplicationContextModule;
import com.navercorp.pinpoint.profiler.context.module.ModuleFactory;
import com.navercorp.pinpoint.profiler.context.storage.LogStorageFactory;
import com.navercorp.pinpoint.profiler.context.storage.StorageFactory;
import com.navercorp.pinpoint.profiler.interceptor.registry.InterceptorRegistryBinder;
import com.navercorp.pinpoint.profiler.sender.EnhancedDataSender;
import com.navercorp.pinpoint.profiler.sender.LoggingDataSender;



/**
 * @author emeroad
 */
public class MockTraceContextFactory {


    public static MockApplicationContext newMockApplicationContext(ProfilerConfig profilerConfig) {

        ModuleFactory moduleFactory = new ModuleFactory() {
            @Override
            public Module newModule(AgentOption agentOption, InterceptorRegistryBinder interceptorRegistryBinder) {
                Module module = new ApplicationContextModule(agentOption, interceptorRegistryBinder);

                LoggingModule loggingModule = new LoggingModule();
                return Modules.override(module).with(loggingModule);
            }
        };

        MockApplicationContextFactory factory = new MockApplicationContextFactory();
        InterceptorRegistryBinder binder = new InterceptorRegistryBinder() {
            @Override
            public void bind() {

            }

            @Override
            public void unbind() {

            }

            @Override
            public InterceptorRegistryAdaptor getInterceptorRegistryAdaptor() {
                return null;
            }

            @Override
            public String getInterceptorRegistryClassName() {
                return null;
            }
        };
        return factory.of(profilerConfig, binder, moduleFactory);
    }

    public static class LoggingModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(AgentInformation.class).toInstance(new TestAgentInformation());
            bind(StorageFactory.class).toInstance(new LogStorageFactory());
            bind(EnhancedDataSender.class).toInstance(new LoggingDataSender());
        }
    }

}
