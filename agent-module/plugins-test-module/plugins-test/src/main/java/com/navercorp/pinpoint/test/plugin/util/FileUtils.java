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

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Woonduk Kang(emeroad)
 */
public final class FileUtils {
    private FileUtils() {
    }

    public static URL[] toURLs(final String[] filePaths) throws MalformedURLException {
        Objects.requireNonNull(filePaths, "filePaths");
        final URL[] urls = new URL[filePaths.length];
        for (int i = 0; i < filePaths.length; i++) {
            String path = filePaths[i];
            urls[i] = Paths.get(path).toUri().toURL();
        }
        return urls;
    }

    public static List<Path> toAbsolutePath(List<Path> files) {
        Objects.requireNonNull(files, "files");

        List<Path> libs = new ArrayList<>(files.size());
        for (Path lib : files) {
            libs.add(lib.toAbsolutePath());
        }
        return libs;
    }

    public static List<Path> toPaths(List<String> filePaths) {
        Objects.requireNonNull(filePaths, "filePaths");

        List<Path> result = new ArrayList<>(filePaths.size());
        for (String filePath : filePaths) {
            final Path file = Paths.get(filePath);
            result.add(file);
        }
        return result;
    }

    public static List<String> toString(List<Path> filePaths) {
        Objects.requireNonNull(filePaths, "filePaths");

        List<String> result = new ArrayList<>(filePaths.size());
        for (Path filePath : filePaths) {
            result.add(filePath.toString());
        }
        return result;
    }

}
