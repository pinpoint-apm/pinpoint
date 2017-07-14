/*
 * Copyright 2016 NAVER Corp.
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
 *
 */

package com.navercorp.pinpoint.profiler.util;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.jar.JarFile;

/**
 * @author Woonduk Kang(emeroad)
 */
public class JarReaderTest {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Test
    public void read() throws Exception {
        URL location = Logger.class.getProtectionDomain().getCodeSource().getLocation();
        JarFile jarFile = new JarFile(location.getPath());

        logger.debug("jarFile:{}", jarFile.getName());

        JarReader jarReader = new JarReader(jarFile);
        List<FileBinary> fileBinaries = jarReader.read(ExtensionFilter.CLASS_FILTER);
        logger.debug("file:{}", fileBinaries);

        for (FileBinary fileBinary : fileBinaries) {
            Assert.assertThat(fileBinary.getFileName(), Matchers.endsWith(".class"));
        }
    }

    @Test
    public void getInputStream() throws Exception {
        URL location = Logger.class.getProtectionDomain().getCodeSource().getLocation();
        JarFile jarFile = new JarFile(location.getPath());
        JarReader jarReader = new JarReader(jarFile);
        Assert.assertNotNull(jarReader.getInputStream("org/slf4j/Logger.class"));
        Assert.assertNull(jarReader.getInputStream("org/slf4j/NotFound.class"));
    }
}