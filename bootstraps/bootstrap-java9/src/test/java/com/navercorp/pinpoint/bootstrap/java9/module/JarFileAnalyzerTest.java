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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
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

    private static final String PACKAGE_PREFIX = "org.apache.commons.io.";

    @Test
    public void packageAnalyzer() throws IOException {
        URL url = CodeSourceUtils.getCodeLocation(IOUtils.class);

        JarFile jarFile = new JarFile(url.getFile());
        logger.debug("jarFile:{}", jarFile.getName());

        PackageAnalyzer packageAnalyzer = new JarFileAnalyzer(jarFile);
        PackageInfo packageInfo = packageAnalyzer.analyze();
        Set<String> packageSet = packageInfo.getPackage();

        logger.debug("package:{}", packageSet);

        long packageCount = jarFile.stream()
                .filter(this::packageFilter)
                .count();

        Assertions.assertEquals(packageCount, packageSet.size());
    }

    private boolean packageFilter(JarEntry jarEntry) {
        String directoryPrefix = PACKAGE_PREFIX.replace(".", "/");
        if (jarEntry.isDirectory()) {
            if (jarEntry.getName().startsWith(directoryPrefix)) {
                return true;
            }
        }
        return false;
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
        Assertions.assertNull(empty);
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
        Assertions.assertEquals("java.sql.Driver", first.getService());
        Assertions.assertTrue(first.getProviders().contains("com.mysql.cj.jdbc.Driver"));


    }
}