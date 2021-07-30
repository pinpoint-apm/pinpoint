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

import java.util.Objects;
import com.navercorp.pinpoint.common.util.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author Woonduk Kang(emeroad)
 */
public class JarReader {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final int BUFFER_SIZE = 1024 * 4;

    private final JarFile jarFile;

    public JarReader(JarFile jarFile) {
        this.jarFile = Objects.requireNonNull(jarFile, "jarFile");
    }

    public InputStream getInputStream(String name) throws IOException {
        final JarEntry jarEntry = this.jarFile.getJarEntry(name);
        if (jarEntry != null) {
            return this.jarFile.getInputStream(jarEntry);
        }

        return null;
    }

    public List<FileBinary> read(JarEntryFilter jarEntryFilter) throws IOException{
        Objects.requireNonNull(jarEntryFilter, "jarEntryFilter");

        final BufferedContext bufferedContext = new BufferedContext();

        Enumeration<JarEntry> entries = jarFile.entries();
        List<FileBinary> fileBinaryList = new ArrayList<>();
        while (entries.hasMoreElements()) {
            final JarEntry jarEntry = entries.nextElement();
            if (jarEntryFilter.filter(jarEntry)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("filter fileName:{}, JarFile:{}", jarEntry, jarFile.getName());
                }
                FileBinary fileBinary = newFileBinary(bufferedContext, jarEntry);
                fileBinaryList.add(fileBinary);
            }
        }
        return fileBinaryList;
    }

    private FileBinary newFileBinary(BufferedContext bufferedContext, JarEntry jarEntry) throws IOException {
        byte[] binary = bufferedContext.read(jarEntry);
        FileBinary fileBinary = new FileBinary(jarEntry.getName(), binary);
        return fileBinary;
    }

    private class BufferedContext {

        private final byte[] buffer = new byte[BUFFER_SIZE];
        private final ByteArrayOutputStream output = new ByteArrayOutputStream(BUFFER_SIZE);

        private BufferedContext() {
        }

        private byte[] read(JarEntry jarEntry) throws IOException{
            InputStream inputStream = null;
            try {
                inputStream = jarFile.getInputStream(jarEntry);
                if (inputStream == null) {
                    logger.warn("jarEntry not found. jarFile:{} jarEntry{}", jarFile.getName(), jarEntry);
                    return null;
                }
                return read(inputStream);
            } catch (IOException ioe) {
                logger.warn("jarFile read error jarFile:{} jarEntry{} {}", jarFile.getName(), jarEntry, ioe.getMessage(), ioe);
                throw ioe;
            } finally {
                IOUtils.closeQuietly(inputStream);
            }
        }

        public byte[] read(InputStream input) throws IOException {
            this.output.reset();
            IOUtils.copy(input, output, buffer);
            return output.toByteArray();
        }

    }
}
