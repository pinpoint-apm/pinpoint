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

package com.navercorp.pinpoint.bootstrap.config;

import com.navercorp.pinpoint.bootstrap.util.AntPathMatcher;
import com.navercorp.pinpoint.bootstrap.util.PathMatcher;
import com.navercorp.pinpoint.bootstrap.util.EqualsPathMatcher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author emeroad
 */
public class ExcludeUrlFilter implements Filter<String> {

    private final List<PathMatcher> excludeMatcherList;

    public ExcludeUrlFilter(String excludeFormat) {
        this(excludeFormat, ",");
    }

    public ExcludeUrlFilter(String excludeFormat, String separator) {
        if (isEmpty(excludeFormat)) {
            this.excludeMatcherList = Collections.emptyList();
            return;
        }
        final String[] split = excludeFormat.split(separator);
        final List<PathMatcher> buildList = new ArrayList<PathMatcher>();
        for (String path : split) {
            if (isEmpty(path)) {
                continue;
            }
            path = path.trim();
            if (path.isEmpty()) {
                continue;
            }
            final PathMatcher pathMatcher = createPathMatcher(path);
            buildList.add(pathMatcher);
        }

        this.excludeMatcherList = buildList;
    }

    protected PathMatcher createPathMatcher(String pattern) {
        if (AntPathMatcher.isAntStylePattern(pattern)) {
            return new AntPathMatcher(pattern);
        }
        return new EqualsPathMatcher(pattern);
    }

    private boolean isEmpty(String string) {
        return string == null || string.isEmpty();
    }

    @Override
    public boolean filter(String requestURI) {
        for (PathMatcher excludePathMatcher : this.excludeMatcherList) {
            if (excludePathMatcher.isMatched(requestURI)) {
                return FILTERED;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ExcludeUrlFilter{");
        sb.append("excludeMatcherList=").append(excludeMatcherList);
        sb.append('}');
        return sb.toString();
    }
}

