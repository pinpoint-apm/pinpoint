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

package com.navercorp.pinpoint.bootstrap.util;

/**
 * @author emeroad
 */
public class AntPathMatcher implements PathMatcher {

    private final com.navercorp.pinpoint.bootstrap.util.spring.AntPathMatcher springAntMatcher = new com.navercorp.pinpoint.bootstrap.util.spring.AntPathMatcher();
    private final String pattern;

    public AntPathMatcher(String pattern) {
        if (pattern == null) {
            throw new NullPointerException("pattern must not be null");
        }
        this.pattern = pattern;
        preCreatePatternCache();
    }

    private void preCreatePatternCache() {
        // dummy call
        this.springAntMatcher.match(pattern, "/");
    }

    @Override
    public boolean isMatched(String path) {
        return this.springAntMatcher.match(pattern, path);
    }

    public static boolean isAntStylePattern(String pattern) {
        // copy AntPathMatcher.isPattern(String path);
        return (pattern.indexOf('*') != -1 || pattern.indexOf('?') != -1);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AntPathMatcher{");
        sb.append("pattern='").append(pattern).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
