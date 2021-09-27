/*
 * Copyright 2021 NAVER Corp.
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

package com.navercorp.pinpoint.test.plugin;

import java.io.File;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.Objects;

class LibraryFilter {

    public final LibraryMatcher[] matchers;

    public LibraryFilter(LibraryMatcher... matchers) {
        this.matchers = Objects.requireNonNull(matchers, "matchers");
    }

    public boolean filter(URL url) {
        for (LibraryMatcher matcher : matchers) {
            if (matcher.include(url.getFile())) {
                return true;
            }
        }
        return false;
    }

    static interface LibraryMatcher {
        boolean include(String filePath);
    }

    static LibraryMatcher createContainsMatcher(String[] paths) {
        return new ContainsMatcher(paths);
    }

    static LibraryMatcher createGlobMatcher(String[] paths) {
        return new GlobMatcher(paths);
    }

    private static class ContainsMatcher implements LibraryMatcher {

        private final String[] paths;

        private ContainsMatcher(String[] paths) {
            this.paths = Objects.requireNonNull(paths, "paths");
        }

        @Override
        public boolean include(String filePath) {
            for (String required : paths) {
                if (filePath.contains(required)) {
                    return true;
                }
            }
            return false;
        }

    }

    private static class GlobMatcher implements LibraryMatcher {

        private static final String GLOB_MATCHER_PREFIX = "glob:";

        private final PathMatcher[] pathMatchers;

        private GlobMatcher(String[] paths) {
            Objects.requireNonNull(paths);

            FileSystem fileSystem = FileSystems.getDefault();

            this.pathMatchers = new PathMatcher[paths.length];
            for (int i = 0; i < paths.length; i++) {
                String syntaxAndPattern = GLOB_MATCHER_PREFIX + paths[i];
                this.pathMatchers[i] = fileSystem.getPathMatcher(syntaxAndPattern);
            }
        }

        @Override
        public boolean include(String filePath) {
            Path path = new File(filePath).toPath();


            for (PathMatcher pathMatcher : pathMatchers) {
                if (pathMatcher.matches(path)) {
                    return true;
                }
            }
            return false;
        }

    }

}
