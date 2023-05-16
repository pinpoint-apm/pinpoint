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

package com.navercorp.pinpoint.bootstrap.java9.module;

import com.navercorp.pinpoint.bootstrap.java9.classloader.Java9ClassLoader;
import com.navercorp.pinpoint.common.util.CodeSourceUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.module.ModuleDescriptor;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Set;
import java.util.jar.JarFile;

/**
 * @author Woonduk Kang(emeroad)
 */
public class ModuleBuilderTest {
    private final Logger logger = LogManager.getLogger(this.getClass());

    private final Class<?> clazz = org.apache.logging.log4j.LogManager.class;

    @Test
    public void defineModuleTest() throws IOException, URISyntaxException, ClassNotFoundException {
        URL testClassJar = CodeSourceUtils.getCodeLocation(clazz);
        URL[] urls = {testClassJar};

        String moduleName = "log4j.test";


        JarFile jarFile = new JarFile(testClassJar.getFile());
        PackageAnalyzer packageAnalyzer = new JarFileAnalyzer(jarFile);
        PackageInfo packageInfo = packageAnalyzer.analyze();
        Set<String> slf4j = packageInfo.getPackage();
        logger.debug("log4j packages:{}", slf4j);

        Java9ClassLoader classLoader = new Java9ClassLoader(moduleName, urls, this.getClass().getClassLoader(), new ArrayList<>(slf4j));

        ModuleDescriptor.Builder builder = ModuleDescriptor.newModule(moduleName);
        builder.packages(slf4j);
        ModuleDescriptor descriptor = builder.build();


        Module module = InternalModules.defineModule(classLoader, descriptor, testClassJar.toURI());
        logger.debug("module:{}", module);

        Class<?> slf4jModule = classLoader.loadClass(clazz.getName());
        logger.debug("slf4j:{}", slf4jModule);

        Assertions.assertSame(module, slf4jModule.getModule());
        Assertions.assertEquals(module.getName(), slf4jModule.getModule().getName());

        classLoader.close();

    }

    @Test
    public void moduleBuilderTest() throws IOException {
        URL testClassJar = CodeSourceUtils.getCodeLocation(clazz);
        URL[] urls = {testClassJar};

        String moduleName = "log4j.test";


        JarFile jarFile = new JarFile(testClassJar.getFile());
        PackageAnalyzer packageAnalyzer = new JarFileAnalyzer(jarFile);
        PackageInfo analyze = packageAnalyzer.analyze();
        Set<String> slf4j = analyze.getPackage();
        logger.debug("log4j packages:{}", slf4j);

        Java9ClassLoader classLoader = new Java9ClassLoader(moduleName, urls, this.getClass().getClassLoader(), new ArrayList<>(slf4j));


        ModuleBuilder moduleBuilder = new ModuleBuilder();
        Module module = moduleBuilder.defineModule(ModuleBuilderTest.class.getSimpleName(), classLoader, urls);

        classLoader.close();

    }

}