/*
 * Copyright 2022 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.sampler;

import com.navercorp.pinpoint.bootstrap.sampler.TraceSampler;
import com.navercorp.pinpoint.bootstrap.util.AntPathMatcher;
import com.navercorp.pinpoint.bootstrap.util.EqualsPathMatcher;
import com.navercorp.pinpoint.bootstrap.util.PathMatcher;
import com.navercorp.pinpoint.common.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class UrlTraceSampler implements TraceSampler {
    private final List<UrlPathMatcher> urlPathMatcherList;
    private final TraceSampler defaultTraceSampler;

    public UrlTraceSampler(Map<String, TraceSampler> urlMap, TraceSampler defaultTraceSampler) {
        Objects.requireNonNull(urlMap, "urlMap");
        this.defaultTraceSampler = Objects.requireNonNull(defaultTraceSampler, "defaultSampler");

        final List<UrlPathMatcher> list = new ArrayList<>();
        for (Map.Entry<String, TraceSampler> entry : urlMap.entrySet()) {
            final String urlPath = entry.getKey();
            final TraceSampler traceSampler = entry.getValue();
            if (StringUtils.isEmpty(urlPath) || traceSampler == null) {
                continue;
            }
            list.add(new UrlPathMatcher(urlPath, traceSampler));
        }
        this.urlPathMatcherList = list;
    }

    @Override
    public State isNewSampled() {
        return this.defaultTraceSampler.isNewSampled();
    }

    @Override
    public State isNewSampled(String urlPath) {
        if (Boolean.FALSE == StringUtils.isEmpty(urlPath)) {
            TraceSampler traceSampler = getSampler(urlPath);
            if (traceSampler != null) {
                return traceSampler.isNewSampled();
            }
        }
        return this.defaultTraceSampler.isNewSampled();
    }

    @Override
    public State isContinueSampled() {
        return this.defaultTraceSampler.isContinueSampled();
    }

    @Override
    public State getContinueDisableState() {
        return this.defaultTraceSampler.getContinueDisableState();
    }

    TraceSampler getSampler(String urlPath) {
        for (UrlPathMatcher urlPathMatcher : this.urlPathMatcherList) {
            if (urlPathMatcher != null && urlPathMatcher.isMatched(urlPath)) {
                if (urlPathMatcher.getTraceSampler() != null) {
                    return urlPathMatcher.getTraceSampler();
                }
            }
        }
        return this.defaultTraceSampler;
    }

    private class UrlPathMatcher implements PathMatcher {
        private PathMatcher pathMatcher;
        private TraceSampler traceSampler;

        public UrlPathMatcher(String urlPath, TraceSampler traceSampler) {
            if (AntPathMatcher.isAntStylePattern(urlPath)) {
                this.pathMatcher = new AntPathMatcher(urlPath);
            } else {
                this.pathMatcher = new EqualsPathMatcher(urlPath);
            }
            this.traceSampler = traceSampler;
        }

        @Override
        public boolean isMatched(String urlPath) {
            return this.pathMatcher.isMatched(urlPath);
        }

        public TraceSampler getTraceSampler() {
            return traceSampler;
        }
    }
}
