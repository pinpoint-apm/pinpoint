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

import com.navercorp.pinpoint.bootstrap.module.Providers;
import com.navercorp.pinpoint.common.util.CodeSourceUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Woonduk Kang(emeroad)
 */
public class JarFileAnalyzerTest {
    private final Logger logger = LogManager.getLogger(this.getClass());

    private final Set<String> COMMONS_IO_PACKAGE = getCommonsLangPackage();

    private Set<String> getCommonsLangPackage() {
        String packagePrefix = "org.apache.commons.io";
        Set<String> set = new HashSet<>();
        set.add(packagePrefix);
        set.add(packagePrefix + ".comparator");
        set.add(packagePrefix + ".filefilter");
        set.add(packagePrefix + ".input");
        set.add(packagePrefix + ".monitor");
        set.add(packagePrefix + ".output");
        set.add(packagePrefix + ".serialization");
        return set;
    }

    @Test
    public void packageAnalyzer() throws IOException {
        URL url = CodeSourceUtils.getCodeLocation(IOUtils.class);

        JarFile jarFile = new JarFile(url.getFile());
        logger.debug("jarFile:{}", jarFile.getName());

        PackageAnalyzer packageAnalyzer = new JarFileAnalyzer(jarFile);
        PackageInfo packageInfo = packageAnalyzer.analyze();
        Set<String> packageSet = packageInfo.getPackage();

        logger.debug("package:{}", packageSet);

        Assert.assertEquals(packageSet, COMMONS_IO_PACKAGE);
    }

    @Test
    public void jarFileToURI() throws IOException {
        URL url = CodeSourceUtils.getCodeLocation(Logger.class);
        logger.debug("url:{}", url);


        JarFile jarFile = new JarFile(url.getFile());
        logger.debug("jarFile:{}", jarFile.getName());

        Path file = Paths.get(jarFile.getName());
        logger.debug("url1:{}", file.toUri());
    }


    @Test
    public void filter_emptyPackage() {
        JarFileAnalyzer.JarEntryFilter filter = new JarFileAnalyzer.PackageFilter();
        JarEntry jarEntry = mock(JarEntry.class);
        when(jarEntry.getName()).thenReturn("test.class");

        String empty = filter.filter(jarEntry);
        Assert.assertNull(empty);
    }

    @Test
    public void providers() throws IOException {
        // Jar
        URL url = CodeSourceUtils.getCodeLocation(com.mysql.jdbc.Driver.class);

        JarFile jarFile = new JarFile(url.getFile());
        PackageAnalyzer analyzer = new JarFileAnalyzer(jarFile);
        PackageInfo analyze = analyzer.analyze();
        List<Providers> providers = analyze.getProviders();
        Providers first = providers.get(0);
        Assert.assertEquals(first.getService(), "java.sql.Driver");
        Assert.assertTrue(first.getProviders().contains("com.mysql.cj.jdbc.Driver"));


    }
}