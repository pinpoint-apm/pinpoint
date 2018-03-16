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
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Taejin Koo
 */
public class ModuleFactoryResolverTest {

    @Test
    public void test() {
        ModuleFactoryResolver provider = new DefaultModuleFactoryResolver(TestModuleFactory.class.getName());
        ModuleFactory moduleFactory = provider.resolve();

        Assert.assertEquals(TestModuleFactory.class, moduleFactory.getClass());
    }

    @Test
    public void test2() {
        ModuleFactoryResolver provider = new DefaultModuleFactoryResolver();
        ModuleFactory moduleFactory = provider.resolve();

        Assert.assertEquals(ApplicationContextModuleFactory.class, moduleFactory.getClass());
    }

    @Test(expected = Exception.class)
    public void test3() {
        ModuleFactoryResolver provider = new DefaultModuleFactoryResolver("abcde");
        provider.resolve();
    }

    public static class TestModuleFactory implements ModuleFactory {

        @Override
        public Module newModule(AgentOption agentOption) {
            return null;
        }

    }

}
