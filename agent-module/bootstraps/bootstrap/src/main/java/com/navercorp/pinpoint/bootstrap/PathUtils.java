/*
 * Copyright 2021 NAVER Corp.
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

package com.navercorp.pinpoint.bootstrap;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

final class PathUtils {
    private PathUtils() {
    }

    public static URL toURL(Path path) {
        Objects.requireNonNull(path, "path");

        try {
            return path.toUri().toURL();
        } catch (MalformedURLException e) {
            throw new RuntimeException("MalformedURL error", e);
        }
    }

    public static URL[] toURLs(List<Path> pathList) {
        Objects.requireNonNull(pathList, "pathList");

        List<URL> list = new ArrayList<>(pathList.size());
        for (Path path : pathList) {
            list.add(PathUtils.toURL(path));
        }
        return list.toArray(new URL[0]);
    }
}
