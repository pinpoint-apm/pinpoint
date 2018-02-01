/*
 * Copyright 2018 NAVER Corp.
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

import com.google.inject.Module;
import com.navercorp.pinpoint.bootstrap.AgentOption;
import com.navercorp.pinpoint.profiler.interceptor.registry.InterceptorRegistryBinder;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Taejin Koo
 */
public class ModuleFactoryProviderTest {

    @Test
    public void test() {
        ModuleFactoryProvider provider = new DefaultModuleFactoryProvider(TestModuleFactory.class.getName());
        ModuleFactory moduleFactory = provider.get();

        Assert.assertEquals(TestModuleFactory.class, moduleFactory.getClass());
    }

    @Test
    public void test2() {
        ModuleFactoryProvider provider = new DefaultModuleFactoryProvider("");
        ModuleFactory moduleFactory = provider.get();

        Assert.assertEquals(ApplicationContextModuleFactory.class, moduleFactory.getClass());
    }

    @Test
    public void test3() {
        ModuleFactoryProvider provider = new DefaultModuleFactoryProvider("abcde");
        ModuleFactory moduleFactory = provider.get();

        Assert.assertNull(moduleFactory);
    }

    public static class TestModuleFactory implements ModuleFactory {

        @Override
        public Module newModule(AgentOption agentOption, InterceptorRegistryBinder interceptorRegistryBinder) {
            return null;
        }

    }

}
