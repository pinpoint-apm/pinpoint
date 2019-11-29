/*
 * Copyright 2014 NAVER Corp.
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
 */

package com.navercorp.pinpoint.common.util;

import com.navercorp.pinpoint.common.Charsets;

import java.io.*;
import java.util.Properties;

/**
 * @author emeroad
 */
public final class PropertyUtils {
    public static final String DEFAULT_ENCODING = Charsets.UTF_8_NAME;

    private static final ClassLoaderUtils.ClassLoaderCallable CLASS_LOADER_CALLABLE = new ClassLoaderUtils.ClassLoaderCallable() {
        @Override
        public ClassLoader getClassLoader() {
            return PropertyUtils.class.getClassLoader();
        }
    };

    public interface InputStreamFactory {
        InputStream openInputStream() throws IOException;
    }

    private PropertyUtils() {
    }

    public static Properties loadProperty(final String filePath) throws IOException {
        if (filePath == null) {
            throw new NullPointerException("filePath");
        }
        final InputStreamFactory inputStreamFactory = new FileInputStreamFactory(filePath);
        return loadProperty(new Properties(), inputStreamFactory, DEFAULT_ENCODING);
    }

    public static Properties loadPropertyFromClassPath(final String classPath) throws IOException {
        if (classPath == null) {
            throw new NullPointerException("classPath");
        }
        final InputStreamFactory inputStreamFactory = new InputStreamFactory() {
            @Override
            public InputStream openInputStream() throws IOException {
                return ClassLoaderUtils.getDefaultClassLoader(CLASS_LOADER_CALLABLE).getResourceAsStream(classPath);
            }
        };
        return loadProperty(new Properties(), inputStreamFactory, DEFAULT_ENCODING);
    }

    public static Properties loadPropertyFromClassLoader(final ClassLoader classLoader, final String classPath) throws IOException {
        if (classLoader == null) {
            throw new NullPointerException("classLoader");
        }
        final InputStreamFactory inputStreamFactory = new InputStreamFactory() {
            @Override
            public InputStream openInputStream() throws IOException {
                return classLoader.getResourceAsStream(classPath);
            }
        };
        return loadProperty(new Properties(), inputStreamFactory, DEFAULT_ENCODING);
    }


    public static Properties loadProperty(Properties properties, InputStreamFactory inputStreamFactory, String encoding) throws IOException {
        if (properties == null) {
            throw new NullPointerException("properties");
        }
        if (inputStreamFactory == null) {
            throw new NullPointerException("inputStreamFactory");
        }
        if (encoding == null) {
            throw new NullPointerException("encoding");
        }
        InputStream in = null;
        Reader reader = null;
        try {
            in = inputStreamFactory.openInputStream();
            reader = new InputStreamReader(in, encoding);
            properties.load(reader);
        } finally {
            IOUtils.closeQuietly(reader);
            IOUtils.closeQuietly(in);
        }
        return properties;
    }

    public static class FileInputStreamFactory implements  InputStreamFactory {
        private final String filePath;

        public FileInputStreamFactory(String filePath) {
            if (filePath == null) {
                throw new NullPointerException("filePath");
            }
            this.filePath = filePath;
        }

        @Override
        public InputStream openInputStream() throws IOException {
            return new FileInputStream(filePath);
        }
    };

}
