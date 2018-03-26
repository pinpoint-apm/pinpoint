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

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.common.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;

/**
 * @author Taejin Koo
 */
public class DefaultModuleFactoryProvider implements ModuleFactoryProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultModuleFactoryProvider.class);

    private final String moduleFactoryClazzName;

    public DefaultModuleFactoryProvider(String moduleFactoryClazzName) {
        this.moduleFactoryClazzName = moduleFactoryClazzName;
    }

    public DefaultModuleFactoryProvider(ProfilerConfig profilerConfig) {
        Assert.requireNonNull(profilerConfig, "profilerConfig must not be null");
        this.moduleFactoryClazzName = profilerConfig.getInjectionModuleFactoryClazzName();
    }

    @Override
    public ModuleFactory get() {
        if (StringUtils.isEmpty(moduleFactoryClazzName) || ApplicationContextModuleFactory.class.getName().equals(moduleFactoryClazzName)) {
            return new ApplicationContextModuleFactory();
        } else {
            ClassLoader classLoader = getClassLoader(DefaultModuleFactoryProvider.class.getClassLoader());
            try {
                final Class<? extends ModuleFactory> moduleFactoryClass =
                        (Class<? extends ModuleFactory>) Class.forName(moduleFactoryClazzName, true, classLoader);
                Constructor<? extends ModuleFactory> constructor = moduleFactoryClass.getConstructor();
                return constructor.newInstance();
            } catch (Exception e) {
                LOGGER.warn("{} clazz not found", moduleFactoryClazzName);
            }
        }
        return null;
    }

    private ClassLoader getClassLoader(ClassLoader classLoader) {
        Assert.requireNonNull(classLoader, "can't find classLoader");
        return classLoader;
    }

}
