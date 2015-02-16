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

import com.navercorp.pinpoint.common.AnnotationKeyMatcher;
import com.navercorp.pinpoint.common.ServiceType;
import com.navercorp.pinpoint.common.util.apache.IntHashMap;

/**
 * @author emeroad
 */
public class AnnotationKeyMatcherRegistry {

    private final IntHashMap<AnnotationKeyMatcher> annotationMatcherMap;

    public AnnotationKeyMatcherRegistry() {
        this.annotationMatcherMap = new IntHashMap<AnnotationKeyMatcher>();
    }

    AnnotationKeyMatcherRegistry(IntHashMap<AnnotationKeyMatcher> annotationMatcherMap) {
        if (annotationMatcherMap == null) {
            throw new NullPointerException("annotationMatcherMap must not be null");
        }
        this.annotationMatcherMap = annotationMatcherMap;
    }


    public AnnotationKeyMatcher findAnnotationKeyMatcher(short serviceType) {
        return annotationMatcherMap.get(serviceType);
    }


    public static class Builder {

        private IntHashMap<AnnotationKeyMatcher> buildMap = new IntHashMap<AnnotationKeyMatcher>();

        public AnnotationKeyMatcher addAnnotationMatcher(ServiceType serviceType, AnnotationKeyMatcher annotationKeyMatcher) {
            if (serviceType == null) {
                throw new NullPointerException("serviceType must not be null");
            }
            if (annotationKeyMatcher == null) {
                throw new NullPointerException("annotationKeyMatcher must not be null");
            }
            return this.buildMap.put(serviceType.getCode(), annotationKeyMatcher);
        }


        public AnnotationKeyMatcherRegistry build() {
            AnnotationKeyMatcherRegistry annotationKeyMatcherRegistry = new AnnotationKeyMatcherRegistry(buildMap);
            buildMap = new IntHashMap<AnnotationKeyMatcher>();
            return annotationKeyMatcherRegistry;
        }
    }
}
