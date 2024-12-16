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
import java.util.Objects;
import java.util.Properties;

/**
 * @author emeroad
 */
public final class PropertyUtils {
    public static final Charset UTF_8 = StandardCharsets.UTF_8;

    private PropertyUtils() {
    }

    public static Properties loadProperty(final InputStream inputStream) throws IOException {
        Objects.requireNonNull(inputStream, "inputStream");

        return loadProperty(new Properties(), inputStream, UTF_8);
    }



    public static Properties loadPropertyFromClassPath(final String classPath) throws IOException {
        Objects.requireNonNull(classPath, "classPath");

        ClassLoader cl = ClassLoaderUtils.getDefaultClassLoader(PropertyUtils.class::getClassLoader);
        InputStream inputStream = cl.getResourceAsStream(classPath);
        if (inputStream == null) {
            throw new IOException("classPath not found. classPath:" + classPath);
        }
        return loadProperty(new Properties(), inputStream, UTF_8);
    }


    public static Properties loadProperty(Properties properties, InputStream inputStream, Charset encoding) throws IOException {
        Objects.requireNonNull(properties, "properties");
        Objects.requireNonNull(inputStream, "inputStream");
        Objects.requireNonNull(encoding, "encoding");

        Reader reader = null;
        try {
            reader = new InputStreamReader(inputStream, encoding);
            properties.load(reader);
        } finally {
            IOUtils.closeQuietly(reader);
            IOUtils.closeQuietly(inputStream);
        }
        return properties;
    }

}
