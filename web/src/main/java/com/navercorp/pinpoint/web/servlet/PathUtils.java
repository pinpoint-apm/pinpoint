/*
 * Copyright 2026 NAVER Corp.
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

package com.navercorp.pinpoint.web.servlet;

public final class PathUtils {

    private PathUtils() {
    }

    public static boolean matchesSegment(String path, String pathSegment) {
        return matchesSegment(path, pathSegment, 0, false);
    }

    public static boolean matchesSegment(String path, String pathSegment, int startOffset) {
        return matchesSegment(path, pathSegment, startOffset, false);
    }

    public static boolean matchesSegment(String path, String pathSegment, int startOffset, boolean prefixMatch) {
        if (!path.startsWith(pathSegment, startOffset)) {
            return false;
        }
        if (prefixMatch) {
            return true;
        }
        final int next = startOffset + pathSegment.length();
        if (next == path.length()) {
            return true;
        }
        return path.charAt(next) == '/';
    }
}