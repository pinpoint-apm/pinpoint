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

import com.navercorp.pinpoint.common.util.CodeSourceUtils;
import org.apache.commons.lang3.CharUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.util.List;
import java.util.jar.JarFile;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Woonduk Kang(emeroad)
 */
public class JarReaderTest {
    private final Logger logger = LogManager.getLogger(this.getClass());

    @Test
    public void read() throws Exception {
        URL location = CodeSourceUtils.getCodeLocation(CharUtils.class);

        JarFile jarFile = new JarFile(location.getPath());

        logger.debug("jarFile:{}", jarFile.getName());

        JarReader jarReader = new JarReader(jarFile);
        List<FileBinary> fileBinaries = jarReader.read(ExtensionFilter.CLASS_FILTER);
        logger.debug("file:{}", fileBinaries);

        for (FileBinary fileBinary : fileBinaries) {
            assertThat(fileBinary.getFileName()).endsWith(".class");
        }
    }

    @Test
    public void getInputStream() throws Exception {
        URL location = CodeSourceUtils.getCodeLocation(CharUtils.class);

        JarFile jarFile = new JarFile(location.getPath());
        JarReader jarReader = new JarReader(jarFile);
        Assertions.assertNotNull(jarReader.getInputStream("org/apache/commons/lang3/CharUtils.class"));
        Assertions.assertNull(jarReader.getInputStream("org/apache/commons/lang3/NotFound.class"));
    }
}