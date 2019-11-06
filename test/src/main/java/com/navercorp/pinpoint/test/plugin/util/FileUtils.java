/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.test.plugin.util;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;

/**
 * @author Woonduk Kang(emeroad)
 */
public final class FileUtils {
    private FileUtils() {
    }

    public static URL toURL(final File file) throws IOException {
        requireNonNull(file, "file");
        return toURL(file, new FileFunction());
    }

    public static URL toURL(final String filePath) throws IOException {
        requireNonNull(filePath, "filePath");
        return toURL(filePath, new FilePathFunction());
    }

    public static URL[] toURLs(final File[] files) throws IOException {
        requireNonNull(files, "files");
        return toURLs(files, new FileFunction());
    }

    public static URL[] toURLs(final String[] filePaths) throws IOException {
        requireNonNull(filePaths, "filePaths");
        return toURLs(filePaths, new FilePathFunction());
    }

    private static <T> URL toURL(final T source, final Function<T, URI> function) throws IOException {
        URI uri = function.apply(source);
        return uri.toURL();
    }

    private static <T> URL[] toURLs(final T[] source, final Function<T, URI> function) throws IOException {
        final URL[] urls = new URL[source.length];
        for (int i = 0; i < source.length; i++) {
            T t = source[i];
            urls[i] = toURL(t, function);
        }
        return urls;
    }

    private interface Function<T, R> {
        R apply(T t);
    }


    private static class FileFunction implements Function<File, URI> {
        public URI apply(File file) {
            return file.toURI();
        }
    }

    private static class FilePathFunction implements Function<String, URI> {
        public URI apply(String filePath) {
            final File file = new File(filePath);
            return file.toURI();
        }
    }

    private static <T> T requireNonNull(T object, String message) {
        if (object == null) {
            throw new NullPointerException(message);
        }
        return object;
    }

}
