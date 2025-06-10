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

package com.navercorp.pinpoint.bootstrap;

import com.navercorp.pinpoint.bootstrap.config.SystemPropertyManager;
import com.navercorp.pinpoint.bootstrap.util.StringUtils;

import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;


/**
 * @author emeroad
 */
public class AgentBootLoader {

    private final ClassLoader classLoader;

    private final String bootClass;

    private final ContextClassLoaderExecuteTemplate<Object> executeTemplate;

    public AgentBootLoader(String bootClass, ClassLoader agentClassLoader) {
        this.bootClass = Objects.requireNonNull(bootClass, "bootClass");
        this.classLoader = Objects.requireNonNull(agentClassLoader, "agentClassLoader");
        this.executeTemplate = new ContextClassLoaderExecuteTemplate<>(agentClassLoader);
    }

    public Object boot(final AgentOption agentOption) {
        final Class<?> agentClazz = getBootStrapClass("com.navercorp.pinpoint.profiler.Agent");
        final Class<?> bootStrapClazz = getBootStrapClass(bootClass);
        if (!agentClazz.isAssignableFrom(bootStrapClazz)) {
            throw new IllegalStateException("Invalid AgentClass:" + bootStrapClazz);
        }
        final SystemPropertyManager systemPropertyManager = new SystemPropertyManager();

        return executeTemplate.execute(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                try {
                    systemPropertyManager.backup(agentOption);
                    Constructor<?> constructor = bootStrapClazz.getDeclaredConstructor(Map.class);
                    return constructor.newInstance(agentOption.toMap());
                } catch (InstantiationException e) {
                    throw new BootStrapException("boot create failed. Error:" + e.getMessage(), e);
                } catch (IllegalAccessException e) {
                    throw new BootStrapException("boot method invoke failed. Error:" + e.getMessage(), e);
                } finally {
                    systemPropertyManager.restore();
                }
            }
        });
    }

    private Class<?> getBootStrapClass(String clazzName) {
        try {
            return this.classLoader.loadClass(clazzName);
        } catch (ClassNotFoundException e) {
            throw new BootStrapException("boot class not found. bootClass:" + bootClass + " Error:" + e.getMessage(), e);
        }
    }
}
