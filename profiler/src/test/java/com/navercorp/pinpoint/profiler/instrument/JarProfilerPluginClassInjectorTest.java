/*
 * Copyright 2016 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.instrument;

import com.navercorp.pinpoint.bootstrap.LibClass;
import com.navercorp.pinpoint.bootstrap.PinpointURLClassLoader;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClassPool;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.profiler.plugin.PluginConfig;
import com.navercorp.pinpoint.profiler.plugin.PluginPackageFilter;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.security.CodeSource;
import java.util.Arrays;

/**
 * @author Woonduk Kang(emeroad)
 */
public class JarProfilerPluginClassInjectorTest {

    public static final String CONTEXT_TYPE_MATCH_CLASS_LOADER = "org.springframework.context.support.ContextTypeMatchClassLoader";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Test
    public void testInjectClass() throws Exception {
        final URL sampleJar = getSampleJar(Logger.class);

        final ClassLoader contextTypeMatchClassLoader = createContextTypeMatchClassLoader(new URL[]{sampleJar});

        InstrumentClassPool pool = Mockito.mock(InstrumentClassPool.class);
        final Instrumentation instrumentation = Mockito.mock(Instrumentation.class);
        final ProfilerPlugin profilerPlugin = Mockito.mock(ProfilerPlugin.class);


//        final PluginPackageFilter filter = new PluginPackageFilter(Arrays.asList("test"));
        final String packageName = logger.getClass().getPackage().getName();
        final PluginPackageFilter filter = new PluginPackageFilter(Arrays.asList(packageName));
        PluginConfig pluginConfig = new PluginConfig(sampleJar, profilerPlugin, instrumentation, pool, sampleJar.getPath(), filter);

        PlainClassLoaderHandler injector = new PlainClassLoaderHandler(pluginConfig);
        final Class<?> loggerClass = injector.injectClass(contextTypeMatchClassLoader, logger.getClass().getName());

        logger.debug("ClassLoader{}", loggerClass.getClassLoader());
        Assert.assertEquals("check className", loggerClass.getName(), "org.slf4j.impl.Log4jLoggerAdapter");
        Assert.assertEquals("check ClassLoader", loggerClass.getClassLoader().getClass().getName(), CONTEXT_TYPE_MATCH_CLASS_LOADER);

    }

    private ClassLoader createContextTypeMatchClassLoader(URL[] urlArray) throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, java.lang.reflect.InvocationTargetException {
        final ClassLoader classLoader = this.getClass().getClassLoader();
        final Class<ClassLoader> aClass = (Class<ClassLoader>) classLoader.loadClass(CONTEXT_TYPE_MATCH_CLASS_LOADER);
        final Constructor<ClassLoader> constructor = aClass.getConstructor(ClassLoader.class);
        ReflectionUtils.makeAccessible(constructor);

        final LibClass libClassFilter = new LibClass() {
            @Override
            public boolean onLoadClass(String clazzName) {
                if (clazzName.startsWith("org.slf4j")) {
                    logger.debug("Loading {}", clazzName);
                    return ON_LOAD_CLASS;
                }
                return DELEGATE_PARENT;
            }
        };
        PinpointURLClassLoader testClassLoader = new PinpointURLClassLoader(urlArray, ClassLoader.getSystemClassLoader(), libClassFilter);

        final ClassLoader contextTypeMatchClassLoader = constructor.newInstance(testClassLoader);

        logger.debug("cl:{}",contextTypeMatchClassLoader);

//        final Method excludePackage = aClass.getMethod("excludePackage", String.class);
//        ReflectionUtils.invokeMethod(excludePackage, contextTypeMatchClassLoader, "org.slf4j");


        return contextTypeMatchClassLoader;
    }


    private URL getSampleJar(Class clazz) {
        final CodeSource codeSource = clazz.getProtectionDomain().getCodeSource();
        final URL location = codeSource.getLocation();

        logger.debug("url:{}", location);

        return location;
    }

}