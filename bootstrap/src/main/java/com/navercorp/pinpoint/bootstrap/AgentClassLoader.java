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

import com.navercorp.pinpoint.bootstrap.classloader.PinpointClassLoaderFactory;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.common.service.AnnotationKeyRegistryService;
import com.navercorp.pinpoint.common.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.common.util.ClassLoaderType;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.concurrent.Callable;


/**
 * @author emeroad
 */
public class AgentClassLoader {

    private final BootLogger logger = BootLogger.getLogger(AgentClassLoader.class.getName());

    private static final SecurityManager SECURITY_MANAGER = System.getSecurityManager();

    private final URLClassLoader classLoader;

    private String bootClass;
    private String bootClassParamClass;

    private final ContextClassLoaderExecuteTemplate<Object> executeTemplate;

    public AgentClassLoader(URL[] urls) {
        this(urls, ClassLoaderType.SYSTEM);
    }

    public AgentClassLoader(URL[] urls, ClassLoaderType parentClassLoaderType) {
        if (urls == null) {
            throw new NullPointerException("urls");
        }
        if (parentClassLoaderType == null) {
            throw new NullPointerException("parentClassLoaderType must not be null");
        }

        ClassLoader parentClassLoader = parentClassLoaderType.getClassLoader();
        if (parentClassLoader == null) {
            parentClassLoader = AgentClassLoader.class.getClassLoader();
        }

        logger.info("had set parent classloader:" + parentClassLoader.toString() + ", type:" + parentClassLoaderType);
        this.classLoader = createClassLoader(urls, parentClassLoader);

        this.executeTemplate = new ContextClassLoaderExecuteTemplate<Object>(classLoader);
    }

    private URLClassLoader createClassLoader(final URL[] urls, final ClassLoader parentClassLoader) {
        if (SECURITY_MANAGER != null) {
            return AccessController.doPrivileged(new PrivilegedAction<URLClassLoader>() {
                public URLClassLoader run() {
                    return PinpointClassLoaderFactory.createClassLoader(urls, parentClassLoader);
                }
            });
        } else {
            return PinpointClassLoaderFactory.createClassLoader(urls, parentClassLoader);
        }
    }

    public void setBootClass(String bootClass) {
        this.bootClass = bootClass;
    }

    public void setBootClassParamClass(String bootClassParamClass) {
        this.bootClassParamClass = bootClassParamClass;
    }

    public Agent boot(final String agentId, final String applicationName, final ProfilerConfig profilerConfig,
                      final Instrumentation instrumentation,
                      final URL[] pluginJars,
                      final BootstrapJarFile bootstrapJarFile,
                      final ServiceTypeRegistryService serviceTypeRegistryService,
                      final AnnotationKeyRegistryService annotationKeyRegistryService) {

        final Class<?> bootStrapClazz = getBootStrapClass();
        final Class<?> bootStrapParamsClazz = getBootStrapParamsClass();

        final Object agent = executeTemplate.execute(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                try {
                    Constructor<?>[] bootClassParamClassConstructors = bootStrapParamsClazz.getConstructors();
                    if (bootClassParamClassConstructors.length != 1) {
                        throw new BootStrapException("bootClassParamClass create failed.");
                    }

                    Constructor<?> bootClassParamClassConstructor = bootClassParamClassConstructors[0];
                    Object params = bootClassParamClassConstructor.newInstance(instrumentation, agentId, applicationName, profilerConfig, pluginJars, bootstrapJarFile.getJarNameList(), serviceTypeRegistryService, annotationKeyRegistryService);
                    if (params == null) {
                        throw new NullPointerException("params must not be null");
                    }

                    Constructor<?> constructor = null;
                    Class<?> paramsClazz = params.getClass();

                    while (paramsClazz != null && !Object.class.equals(paramsClazz)) {
                        constructor = getBootStrapConstructor0(bootStrapClazz, paramsClazz);
                        if (constructor != null) {
                            break;
                        }

                        Class<?>[] interfaces = paramsClazz.getInterfaces();
                        for (Class eachInterface : interfaces) {
                            constructor = getBootStrapConstructor0(bootStrapClazz, eachInterface);
                            if (constructor != null) {
                                break;
                            }
                        }

                        if (constructor != null) {
                            break;
                        }

                        paramsClazz = paramsClazz.getSuperclass();
                    }

                    return constructor.newInstance(params);
                } catch (InstantiationException e) {
                    throw new BootStrapException("boot create failed. Error:" + e.getMessage(), e);
                } catch (IllegalAccessException e) {
                    throw new BootStrapException("boot method invoke failed. Error:" + e.getMessage(), e);
                }
            }
        });

        if (agent instanceof Agent) {
            return (Agent) agent;
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

    private Constructor<?> getBootStrapConstructor0(Class<?> bootStrapClazz, Class<?> paramsClazz) {
        try {
            return bootStrapClazz.getConstructor(paramsClazz);
        } catch (Exception e) {
            // ignore
        }
        return null;
    }

    private Class<?> getBootStrapClass() {
        try {
            return this.classLoader.loadClass(bootClass);
        } catch (ClassNotFoundException e) {
            throw new BootStrapException("boot class not found. bootClass:" + bootClass + " Error:" + e.getMessage(), e);
        }
    }

    private Class<?> getBootStrapParamsClass() {
        try {
            return this.classLoader.loadClass(bootClassParamClass);
        } catch (ClassNotFoundException e) {
            throw new BootStrapException("bootClassParamClass not found. bootClassParamClass:" + bootClassParamClass + " Error:" + e.getMessage(), e);
        }
    }

}
