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


import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Properties;

/**
 * @author emeroad
 */
public final class PropertyUtils {
    public static final Charset UTF_8 = StandardCharsets.UTF_8;

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

    public static Properties loadProperty(final InputStream inputStream) throws IOException {
        Objects.requireNonNull(inputStream, "inputStream");

        final InputStreamFactory inputStreamFactory = new InputStreamFactory() {
            @Override
            public InputStream openInputStream() throws IOException {
                return inputStream;
            }
        };
        return loadProperty(new Properties(), inputStreamFactory, UTF_8);
    }

    public static Properties loadProperty(Properties properties, final Path filePath) throws IOException {
        Objects.requireNonNull(filePath, "filePath");

        InputStreamFactory streamFactory = new PathFactory(filePath);
        return loadProperty(properties, streamFactory, UTF_8);
    }


    public static Properties loadPropertyFromClassPath(final String classPath) throws IOException {
        Objects.requireNonNull(classPath, "classPath");

        final InputStreamFactory inputStreamFactory = new InputStreamFactory() {
            @Override
            public InputStream openInputStream() throws IOException {
                return ClassLoaderUtils.getDefaultClassLoader(CLASS_LOADER_CALLABLE).getResourceAsStream(classPath);
            }
        };
        return loadProperty(new Properties(), inputStreamFactory, UTF_8);
    }

    public static Properties loadPropertyFromClassLoader(final ClassLoader classLoader, final String classPath) throws IOException {
        Objects.requireNonNull(classLoader, "classLoader");

        final InputStreamFactory inputStreamFactory = new InputStreamFactory() {
            @Override
            public InputStream openInputStream() throws IOException {
                return classLoader.getResourceAsStream(classPath);
            }
        };
        return loadProperty(new Properties(), inputStreamFactory, UTF_8);
    }


    public static Properties loadProperty(Properties properties, InputStreamFactory inputStreamFactory, Charset encoding) throws IOException {
        Objects.requireNonNull(properties, "properties");
        Objects.requireNonNull(inputStreamFactory, "inputStreamFactory");
        Objects.requireNonNull(encoding, "encoding");

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

    public static class PathFactory implements InputStreamFactory {
        private final Path filePath;

        public PathFactory(Path filePath) {
            this.filePath = Objects.requireNonNull(filePath, "filePath");
        }

        @Override
        public InputStream openInputStream() throws IOException {
            return Files.newInputStream(filePath);
        }
    }
}
