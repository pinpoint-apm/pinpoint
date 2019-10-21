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

package com.navercorp.pinpoint.common.profiler.trace;

import com.navercorp.pinpoint.common.trace.AnnotationKeyMatcher;
import com.navercorp.pinpoint.common.trace.AnnotationKeyMatcherLocator;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.common.util.apache.IntHashMap;
import com.navercorp.pinpoint.common.util.apache.IntHashMapUtils;

import java.util.HashMap;

/**
 * @author emeroad
 * @author HyunGil Jeong
 */
public class AnnotationKeyMatcherRegistry implements AnnotationKeyMatcherLocator {

    private final IntHashMap<AnnotationKeyMatcher> annotationMatcherMap;

    private AnnotationKeyMatcherRegistry(IntHashMap<AnnotationKeyMatcher> annotationMatcherMap) {
        this.annotationMatcherMap = Assert.requireNonNull(annotationMatcherMap, "annotationMatcherMap");
    }

    public AnnotationKeyMatcher findAnnotationKeyMatcher(short serviceType) {
        return annotationMatcherMap.get(serviceType);
    }

    static class Builder {

        private final HashMap<Integer, AnnotationKeyMatcher> buildMap = new HashMap<Integer, AnnotationKeyMatcher>();

        AnnotationKeyMatcher addAnnotationKeyMatcher(ServiceType serviceType, AnnotationKeyMatcher annotationKeyMatcher) {
            if (serviceType == null) {
                throw new NullPointerException("serviceType");
            }
            if (annotationKeyMatcher == null) {
                throw new NullPointerException("annotationKeyMatcher");
            }
            int code = serviceType.getCode();
            return this.buildMap.put(code, annotationKeyMatcher);
        }

        AnnotationKeyMatcherRegistry build() {
            IntHashMap<AnnotationKeyMatcher> copy = IntHashMapUtils.copy(buildMap);
            return new AnnotationKeyMatcherRegistry(copy);
        }
    }
}
