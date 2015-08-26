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
public class EqualsPathMatcher implements PathMatcher {

    private final String pattern;

    public EqualsPathMatcher(String pattern) {
        if (pattern == null) {
            throw new NullPointerException("pattern must not be null");
        }
        this.pattern = pattern;
    }

    @Override
    public boolean isMatched(String path) {
        return pattern.equals(path);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("EqualsPathMatcher{");
        sb.append("pattern='").append(pattern).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
