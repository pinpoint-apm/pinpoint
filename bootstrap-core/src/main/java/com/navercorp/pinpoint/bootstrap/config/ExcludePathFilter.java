/*
 * Copyright 2016 Naver Corp.
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
import com.navercorp.pinpoint.bootstrap.util.EqualsPathMatcher;
import com.navercorp.pinpoint.bootstrap.util.PathMatcher;
import com.navercorp.pinpoint.common.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @author emeroad
 * @author HyunGil Jeong
 */
public class ExcludePathFilter implements Filter<String> {

    public static final String DEFAULT_PATH_SEAPARATOR = "/";
    public static final String DEFAULT_FORMAT_SEPARATOR = ",";

    protected final PathMatcher[] excludePathMatchers;

    public ExcludePathFilter(String excludePathFormatString) {
        this(excludePathFormatString, DEFAULT_PATH_SEAPARATOR);
    }

    public ExcludePathFilter(String excludePathFormatString, String pathSeparator) {
        this(excludePathFormatString, pathSeparator, DEFAULT_FORMAT_SEPARATOR);
    }

    public ExcludePathFilter(String excludePathFormatString, String pathSeparator, String formatSeparator) {
        if (StringUtils.isEmpty(pathSeparator)) {
            throw new IllegalArgumentException("pathSeparator must not be empty");
        }
        if (StringUtils.isEmpty(excludePathFormatString)) {
            this.excludePathMatchers = new PathMatcher[0];
            return;
        }
        final List<String> excludePathFormats = StringUtils.tokenizeToStringList(excludePathFormatString, formatSeparator);
        final List<PathMatcher> excludePathMatchers = new ArrayList<PathMatcher>(excludePathFormats.size());
        for (String excludePathFormat : excludePathFormats) {
            final PathMatcher pathMatcher = createPathMatcher(excludePathFormat, pathSeparator);
            excludePathMatchers.add(pathMatcher);
        }
        this.excludePathMatchers = toArray(excludePathMatchers);
    }

    public PathMatcher[] toArray(Collection<PathMatcher> collection) {
        if (collection == null) {
            throw new NullPointerException("collection");
        }
        return collection.toArray(new PathMatcher[0]);
    }

    protected PathMatcher createPathMatcher(String pattern, String pathSeparator) {
        if (AntPathMatcher.isAntStylePattern(pattern)) {
            return new AntPathMatcher(pattern, pathSeparator);
        }
        return new EqualsPathMatcher(pattern);
    }

    @Override
    public boolean filter(String value) {
        for (PathMatcher excludePathMatcher : this.excludePathMatchers) {
            if (excludePathMatcher.isMatched(value)) {
                return FILTERED;
            }
        }
        return NOT_FILTERED;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ExcludePathFilter{");
        sb.append("excludePathMatchers=").append(Arrays.toString(excludePathMatchers));
        sb.append('}');
        return sb.toString();
    }
}
