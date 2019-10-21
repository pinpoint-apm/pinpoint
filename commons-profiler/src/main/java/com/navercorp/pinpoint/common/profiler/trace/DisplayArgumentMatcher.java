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
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.util.Assert;

/**
 * @author emeroad
 */
public class DisplayArgumentMatcher {
    private final ServiceType serviceType;
    private final AnnotationKeyMatcher annotationKeyMatcher;

    public DisplayArgumentMatcher(ServiceType serviceType, AnnotationKeyMatcher annotationKeyMatcher) {
        this.serviceType = Assert.requireNonNull(serviceType, "serviceType");
        this.annotationKeyMatcher = Assert.requireNonNull(annotationKeyMatcher, "annotationKeyMatcher");
    }

    public AnnotationKeyMatcher getAnnotationKeyMatcher() {
        return annotationKeyMatcher;
    }

    public ServiceType getServiceType() {
        return serviceType;
    }
}
