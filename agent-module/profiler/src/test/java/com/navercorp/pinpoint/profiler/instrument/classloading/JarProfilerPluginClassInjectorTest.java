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

package com.navercorp.pinpoint.profiler.instrument.classloading;

import com.navercorp.pinpoint.bootstrap.classloader.PinpointClassLoaderFactory;
import com.navercorp.pinpoint.common.util.ClassLoaderUtils;
import com.navercorp.pinpoint.common.util.CodeSourceUtils;
import com.navercorp.pinpoint.profiler.plugin.JarPlugin;
import com.navercorp.pinpoint.profiler.plugin.Plugin;
import com.navercorp.pinpoint.profiler.plugin.PluginConfig;
import com.navercorp.pinpoint.profiler.plugin.PluginJar;
import com.navercorp.pinpoint.profiler.plugin.PluginPackageClassRequirementFilter;
import com.navercorp.pinpoint.profiler.plugin.PluginPackageFilter;
import org.apache.commons.lang3.CharUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.Collections;
import java.util.List;

/**
 * @author Woonduk Kang(emeroad)
 */
public class JarProfilerPluginClassInjectorTest {

    public static final String CONTEXT_TYPE_MATCH_CLASS_LOADER = "org.springframework.context.support.ContextTypeMatchClassLoader";

    private final Logger logger = LogManager.getLogger(this.getClass());

    private static final List<String> LOG4_IMPL = Collections.singletonList(CharUtils.class.getPackage().getName());

    @Test
    public void testInjectClass() throws Exception {
        String className = CharUtils.class.getName();
        final Plugin<?> plugin = getMockPlugin(className);

        final ClassLoader contextTypeMatchClassLoader = createContextTypeMatchClassLoader(new URL[]{plugin.getURL()});

        final PluginPackageFilter pluginPackageFilter = new PluginPackageFilter(LOG4_IMPL);
        final PluginPackageClassRequirementFilter pluginPackageRequirementFilter = new PluginPackageClassRequirementFilter(Collections.emptyList());
        PluginConfig pluginConfig = new PluginConfig(plugin, pluginPackageFilter, pluginPackageRequirementFilter);
        logger.debug("pluginConfig:{}", pluginConfig);

        ClassInjector injector = new PlainClassLoaderHandler(pluginConfig);
        final Class<?> commonsLangClass = injector.injectClass(contextTypeMatchClassLoader, className);

        logger.debug("ClassLoader{}", commonsLangClass.getClassLoader());
        Assertions.assertEquals(commonsLangClass.getName(), className, "check className");
        Assertions.assertEquals(commonsLangClass.getClassLoader().getClass().getName(), CONTEXT_TYPE_MATCH_CLASS_LOADER, "check ClassLoader");

    }

    private ClassLoader createContextTypeMatchClassLoader(URL[] urlArray) {
        try {
            final ClassLoader classLoader = this.getClass().getClassLoader();
            final Class<ClassLoader> aClass = (Class<ClassLoader>) classLoader.loadClass(CONTEXT_TYPE_MATCH_CLASS_LOADER);
            final Constructor<ClassLoader> constructor = aClass.getConstructor(ClassLoader.class);
            constructor.setAccessible(true);

            List<String> lib = LOG4_IMPL;

            ClassLoader testClassLoader = PinpointClassLoaderFactory.createClassLoader(this.getClass().getName(), urlArray, ClassLoader.getSystemClassLoader(), lib);
            final ClassLoader contextTypeMatchClassLoader = constructor.newInstance(testClassLoader);

            logger.debug("cl:{}", contextTypeMatchClassLoader);

//        final Method excludePackage = aClass.getMethod("excludePackage", String.class);
//        ReflectionUtils.invokeMethod(excludePackage, contextTypeMatchClassLoader, "org.slf4j");


            return contextTypeMatchClassLoader;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }


    private Plugin<?> getMockPlugin(String className) throws Exception {
        ClassLoader cl = ClassLoaderUtils.getDefaultClassLoader();
        Class<?> clazz = null;
        try {
            clazz = cl.loadClass(className);
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException(className + " class not found. Caused by:" + ex.getMessage(), ex);
        }
        return getMockPlugin(clazz);
    }

    private Plugin<?> getMockPlugin(Class<?> clazz) throws Exception {

        final URL location = CodeSourceUtils.getCodeLocation(clazz);

        logger.debug("url:{}", location);
        PluginJar pluginJar = PluginJar.fromFilePath(location.getFile());
        return new JarPlugin<>(pluginJar, Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
    }

}