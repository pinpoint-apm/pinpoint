/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.loader.plugins.trace.yaml;

import java.util.List;

/**
 * @author HyunGil Jeong
 */
public class ParsedTraceMetadata {

    private List<ParsedServiceType> serviceTypes;
    private List<ParsedAnnotationKey> annotationKeys;

    public List<ParsedServiceType> getServiceTypes() {
        return serviceTypes;
    }

    public void setServiceTypes(List<ParsedServiceType> serviceTypes) {
        this.serviceTypes = serviceTypes;
    }

    public List<ParsedAnnotationKey> getAnnotationKeys() {
        return annotationKeys;
    }

    public void setAnnotationKeys(List<ParsedAnnotationKey> annotationKeys) {
        this.annotationKeys = annotationKeys;
    }
}
