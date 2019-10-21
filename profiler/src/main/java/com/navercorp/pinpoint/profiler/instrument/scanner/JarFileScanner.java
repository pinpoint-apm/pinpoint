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

package com.navercorp.pinpoint.profiler.instrument.scanner;

import com.navercorp.pinpoint.common.util.Assert;

import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author Woonduk Kang(emeroad)
 */
public class JarFileScanner implements Scanner {

    private final JarFile jarFile;

    public JarFileScanner(String path) {
        Assert.requireNonNull(path, "path");
        try {
            this.jarFile = new JarFile(path);
        } catch (IOException e) {
            throw new IllegalStateException(path + " create fail");
        }
    }

    @Override
    public boolean exist(String fileName) {
        final JarEntry jarEntry = jarFile.getJarEntry(fileName);
        if (jarEntry == null) {
            return false;
        }
        return true;
    }


    @Override
    public InputStream openStream(String fileName) {
        final JarEntry jarEntry = jarFile.getJarEntry(fileName);
        if (jarEntry == null) {
            return null;
        }
        try {
            return jarFile.getInputStream(jarEntry);
        } catch (IOException e) {
            return null;
        }
    }

    public void close() {
        if (jarFile != null) {
            try {
                jarFile.close();
            } catch (IOException ignore) {
                ;
            }
        }
    }

    @Override
    public String toString() {
        return "JarFileScanner{" +
                "jarFile=" + jarFile.getName() +
                '}';
    }
}
