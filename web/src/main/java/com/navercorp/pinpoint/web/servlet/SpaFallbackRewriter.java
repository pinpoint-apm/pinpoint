/*
 * Copyright 2023 NAVER Corp.
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
 *
 */

package com.navercorp.pinpoint.web.servlet;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class SpaFallbackRewriter {
    static final String DEFAULT_MAIN_PATH = "/index.html";
    static final List<String> DEFAULT_RESOURCE_PATHS = List.of("/assets", "/fonts", "/img");

    private static final String API_PREFIX = "/api";

    private final String page;

    private final String[] specialPaths;
    private final String[] resourcePaths;


    public SpaFallbackRewriter() {
        this(DEFAULT_MAIN_PATH, Collections.emptyList(), DEFAULT_RESOURCE_PATHS);
    }

    public SpaFallbackRewriter(String page, List<String> specialPaths, List<String> resourcePaths) {
        this.page = Objects.requireNonNull(page, "page");
        this.specialPaths = ensureLeadingSlash(Objects.requireNonNull(specialPaths, "specialPaths"));
        this.resourcePaths = ensureLeadingSlash(Objects.requireNonNull(resourcePaths, "resourcePaths"));
    }

    private static String[] ensureLeadingSlash(List<String> paths) {
        String[] array = new String[paths.size()];
        for (int i = 0; i < array.length; i++) {
            String path = paths.get(i);
            if (path.startsWith("/")) {
                array[i] = path;
            } else {
                array[i] = "/" + path;
            }
        }
        return array;
    }

    public String rewrite(String path) {
        if (path.equals("/")) {
            return page;
        }

        // 1. backend API: forward as-is
        if (isBackendApi(path)) {
            return null;
        }
        // 2. static resource: serve URL as-is
        if (isStaticResource(path)) {
            return null;
        }
        // 3. SPA fallback: rewrite to index.html
        return page;
    }

    private boolean isBackendApi(String path) {
        if (PathUtils.matchesSegment(path, API_PREFIX, 0, true)) {
            return true;
        }

        for (String sPath : specialPaths) {
            if (PathUtils.matchesSegment(path, sPath)) {
                return true;
            }
        }
        return false;
    }

    private boolean isStaticResource(String path) {
        for (String resource : resourcePaths) {
            if (PathUtils.matchesSegment(path, resource)) {
                return true;
            }
        }
        return false;
    }

}
