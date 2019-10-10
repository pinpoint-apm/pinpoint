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

import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.trace.AnnotationKeyMatcher;
import com.navercorp.pinpoint.common.trace.AnnotationKeyMatchers;
import com.navercorp.pinpoint.common.trace.ServiceType;

/**
 * @author emeroad
 */
public class DefaultDisplayArgument {

    public static final DisplayArgumentMatcher UNKNOWN_DB_MATCHER = createArgumentMatcher(ServiceType.UNKNOWN, AnnotationKey.ARGS0);
    public static final DisplayArgumentMatcher UNKNOWN_DB_EXECUTE_QUERY_MATCHER = createArgumentMatcher(ServiceType.UNKNOWN_DB_EXECUTE_QUERY, AnnotationKey.ARGS0);

    // FIXME replaced with IBATIS_SPRING under IBatis Plugin - kept for backwards compatibility
    public static final DisplayArgumentMatcher SPRING_ORM_IBATIS_MATCHER = createArgumentMatcher(ServiceType.SPRING_ORM_IBATIS, AnnotationKey.ARGS0);
    
    public static final DisplayArgumentMatcher COLLECTOR_MATCHER = createArgumentMatcher(ServiceType.COLLECTOR, AnnotationKey.ARGS0);


    private static AnnotationKeyMatcher createMatcher(AnnotationKey displayArgument) {
        return AnnotationKeyMatchers.exact(displayArgument);
    }

    private static DisplayArgumentMatcher createArgumentMatcher(ServiceType serviceType, AnnotationKey annotationKey) {
        return createArgumentMatcher(serviceType, createMatcher(annotationKey));
    }

    private static DisplayArgumentMatcher createArgumentMatcher(ServiceType serviceType, AnnotationKeyMatcher annotationKeyMatcher) {
        return new DisplayArgumentMatcher(serviceType, annotationKeyMatcher);
    }
}
