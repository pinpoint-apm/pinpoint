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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
        if (jarFile == null) {
            throw new NullPointerException("jarFile must not be null");
        }
        this.jarFile = jarFile;
    }

    public InputStream getInputStream(String name) throws IOException {
        final JarEntry jarEntry = this.jarFile.getJarEntry(name);
        if (jarEntry != null) {
            return this.jarFile.getInputStream(jarEntry);
        }

        return null;
    }

    public List<FileBinary> read(JarEntryFilter jarEntryFilter) throws IOException{
        if (jarEntryFilter == null) {
            throw new NullPointerException("jarEntryFilter must not be null");
        }

        final BufferedContext bufferedContext = new BufferedContext();

        String jarFileName = jarFile.getName();
        Enumeration<JarEntry> entries = jarFile.entries();
        List<FileBinary> fileBinaryList = new ArrayList<FileBinary>();
        while (entries.hasMoreElements()) {
            final JarEntry jarEntry = entries.nextElement();
            if (jarEntryFilter.filter(jarEntry)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("filter fileName:{}, JarFile:{}", jarEntry.getName(), jarFileName);
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
                    logger.warn("jarEntry not found. jarFile:{} jarEntry{}", jarFile, jarEntry);
                    return null;
                }
                return read(inputStream);
            } catch (IOException ioe) {
                logger.warn("jarFile read error jarFile:{} jarEntry{} {}", jarFile, jarEntry, ioe.getMessage(), ioe);
                throw ioe;
            } finally {
                close(inputStream);
            }
        }

        public byte[] read(InputStream input) throws IOException {
            this.output.reset();
            read(input, output);
            return output.toByteArray();
        }

        public void read(InputStream input, OutputStream output) throws IOException {
            int readIndex;
            while ((readIndex = input.read(buffer)) != -1) {
                output.write(buffer, 0, readIndex);
            }
        }

        private void close(Closeable closeable) {
            if (closeable != null) {
                try {
                    closeable.close();
                } catch (IOException ignore) {
                }
            }
        }
    }
}
