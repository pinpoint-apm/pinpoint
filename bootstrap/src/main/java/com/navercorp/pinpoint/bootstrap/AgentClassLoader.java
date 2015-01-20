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

package com.navercorp.pinpoint.bootstrap;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.concurrent.Callable;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.common.plugin.Plugins;

/**
 * @author emeroad
 */
public class AgentClassLoader {

    private static final SecurityManager SECURITY_MANAGER = System.getSecurityManager();

    private final URLClassLoader classLoader;

    private String bootClass;

    private Agent agentBootStrap;
    private final ContextClassLoaderExecuteTemplate<Object> executeTemplate;

    public AgentClassLoader(URL[] urls) {
        if (urls == null) {
            throw new NullPointerException("urls");
        }

        ClassLoader bootStrapClassLoader = AgentClassLoader.class.getClassLoader();
        this.classLoader = createClassLoader(urls, bootStrapClassLoader);

        this.executeTemplate = new ContextClassLoaderExecuteTemplate<Object>(classLoader);
    }

    private PinpointURLClassLoader createClassLoader(final URL[] urls, final ClassLoader bootStrapClassLoader) {
        if (SECURITY_MANAGER != null) {
            return AccessController.doPrivileged(new PrivilegedAction<PinpointURLClassLoader>() {
                public PinpointURLClassLoader run() {
                    return new PinpointURLClassLoader(urls, bootStrapClassLoader);
                }
            });
        } else {
            return new PinpointURLClassLoader(urls, bootStrapClassLoader);
        }
    }

    public void setBootClass(String bootClass) {
        this.bootClass = bootClass;
    }

    public void boot(final String agentArgs, final Instrumentation instrumentation, final ProfilerConfig profilerConfig, final Plugins<ProfilerPlugin> plugins) {

        final Class<?> bootStrapClazz = getBootStrapClass();

        final Object agent = executeTemplate.execute(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                try {
                    Constructor<?> constructor = bootStrapClazz.getConstructor(String.class, Instrumentation.class, ProfilerConfig.class, Plugins.class);
                    return constructor.newInstance(agentArgs, instrumentation, profilerConfig, plugins);
                } catch (InstantiationException e) {
                    throw new BootStrapException("boot create failed. Error:" + e.getMessage(), e);
                } catch (IllegalAccessException e) {
                    throw new BootStrapException("boot method invoke failed. Error:" + e.getMessage(), e);
                }
            }
        });

        if (agent instanceof Agent) {
            this.agentBootStrap = (Agent) agent;
        } else {
            String agentClassName;
            if (agent == null) {
                agentClassName = "Agent is null";
            } else {
                agentClassName = agent.getClass().getName();
            }
            throw new BootStrapException("Invalid AgentType. boot failed. AgentClass:" + agentClassName);
        }
    }


    private Class<?> getBootStrapClass() {
        try {
            return this.classLoader.loadClass(bootClass);
        } catch (ClassNotFoundException e) {
            throw new BootStrapException("boot class not found. bootClass:" + bootClass + " Error:" + e.getMessage(), e);
        }
    }

    @Deprecated
    public Object initializeLoggerBinder() {
        if (agentBootStrap != null) {
            return reflectionInvoke(this.agentBootStrap, "initializeLogger", null, null);
        }
        return null;
    }

    private Object reflectionInvoke(Object target, String method, Class[] type, final Object[] args) {
        final Method findMethod = findMethod(target.getClass(), method, type);
        return executeTemplate.execute(new Callable<Object>() {
            @Override
            public Object call() {
                try {
                    return findMethod.invoke(agentBootStrap, args);
                } catch (InvocationTargetException e) {
                    throw new BootStrapException(findMethod.getName() + "() failed. Error:" + e.getMessage(), e);
                } catch (IllegalAccessException e) {
                    throw new BootStrapException("boot method invoke failed. Error:" + e.getMessage(), e);
                }
            }
        });

    }

    private Method findMethod(Class<?> clazz, String method, Class[] type) {
        try {
            return clazz.getDeclaredMethod(method, type);
        } catch (NoSuchMethodException e) {
            throw new BootStrapException("(" + method + ") boot method not found. Error:" + e.getMessage(), e);
        }
    }

}
