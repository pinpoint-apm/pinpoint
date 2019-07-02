/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.common.trace;

import java.util.List;

/**
 * @author HyunGil Jeong
 */
public class ParsedTraceMetadataProvider implements TraceMetadataProvider {

    private final String id;
    private final List<ServiceTypeInfo> serviceTypeInfos;
    private final List<AnnotationKey> annotationKeys;

    public ParsedTraceMetadataProvider(String id, List<ServiceTypeInfo> serviceTypeInfos, List<AnnotationKey> annotationKeys) {
        this.id = id;
        this.serviceTypeInfos = serviceTypeInfos;
        this.annotationKeys = annotationKeys;
    }

    public String getId() {
        return id;
    }

    @Override
    public void setup(TraceMetadataSetupContext context) {
        for (ServiceTypeInfo serviceTypeInfo : serviceTypeInfos) {
            ServiceType serviceType = serviceTypeInfo.getServiceType();
            AnnotationKeyMatcher annotationKeyMatcher = serviceTypeInfo.getPrimaryAnnotationKeyMatcher();
            if (annotationKeyMatcher != null) {
                context.addServiceType(serviceType, annotationKeyMatcher);
            } else {
                context.addServiceType(serviceType);
            }
        }
        for (AnnotationKey annotationKey : annotationKeys) {
            context.addAnnotationKey(annotationKey);
        }
    }

    @Override
    public String toString() {
        return id;
    }
}
