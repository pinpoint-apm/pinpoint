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

package com.navercorp.pinpoint.loader.service;

import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.trace.AnnotationKeyLocator;
import com.navercorp.pinpoint.common.util.Assert;

/**
 * @author emeroad
 * @author HyunGil Jeong
 */
public class DefaultAnnotationKeyRegistryService implements AnnotationKeyRegistryService {

    private final AnnotationKeyLocator annotationKeyLocator;

    public DefaultAnnotationKeyRegistryService(TraceMetadataLoaderService typeLoaderService) {
        Assert.requireNonNull(typeLoaderService, "typeLoaderService");
        this.annotationKeyLocator = typeLoaderService.getAnnotationKeyLocator();
    }

    @Override
    public AnnotationKey findAnnotationKey(int annotationCode) {
        return this.annotationKeyLocator.findAnnotationKey(annotationCode);
    }

    @Override
    public AnnotationKey findAnnotationKeyByName(String keyName) {
        return this.annotationKeyLocator.findAnnotationKeyByName(keyName);
    }

    @Override
    public AnnotationKey findApiErrorCode(int annotationCode) {
        return this.annotationKeyLocator.findApiErrorCode(annotationCode);
    }
}
