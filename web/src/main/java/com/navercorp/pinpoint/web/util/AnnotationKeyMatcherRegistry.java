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

package com.navercorp.pinpoint.web.util;

import com.navercorp.pinpoint.common.trace.AnnotationKeyMatcher;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.util.apache.IntHashMap;
import com.navercorp.pinpoint.common.util.apache.IntHashMapUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author emeroad
 */
public class AnnotationKeyMatcherRegistry {

    private final IntHashMap<AnnotationKeyMatcher> annotationMatcherMap;

    public AnnotationKeyMatcherRegistry() {
        this.annotationMatcherMap = new IntHashMap<>();
    }

    AnnotationKeyMatcherRegistry(IntHashMap<AnnotationKeyMatcher> annotationMatcherMap) {
        if (annotationMatcherMap == null) {
            throw new NullPointerException("annotationMatcherMap must not be null");
        }
        this.annotationMatcherMap = annotationMatcherMap;
    }

    private IntHashMap<AnnotationKeyMatcher> copy(HashMap<Short, AnnotationKeyMatcher> annotationMatcherMap) {
        final IntHashMap<AnnotationKeyMatcher> copy = new IntHashMap<>();
        for (Map.Entry<Short, AnnotationKeyMatcher> entry : annotationMatcherMap.entrySet()) {
            copy.put(entry.getKey(), entry.getValue());
        }
        return copy;
    }


    public AnnotationKeyMatcher findAnnotationKeyMatcher(short serviceType) {
        return annotationMatcherMap.get(serviceType);
    }


    public static class Builder {

        private final HashMap<Integer, AnnotationKeyMatcher> buildMap = new HashMap<>();

        public AnnotationKeyMatcher addAnnotationMatcher(ServiceType serviceType, AnnotationKeyMatcher annotationKeyMatcher) {
            if (serviceType == null) {
                throw new NullPointerException("serviceType must not be null");
            }
            if (annotationKeyMatcher == null) {
                throw new NullPointerException("annotationKeyMatcher must not be null");
            }
            int code = serviceType.getCode();
            return this.buildMap.put(code, annotationKeyMatcher);
        }


        public AnnotationKeyMatcherRegistry build() {
            IntHashMap<AnnotationKeyMatcher> copy = IntHashMapUtils.copy(buildMap);
            return new AnnotationKeyMatcherRegistry(copy);
        }
    }
}
