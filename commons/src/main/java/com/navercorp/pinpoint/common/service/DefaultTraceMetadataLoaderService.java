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

package com.navercorp.pinpoint.common.service;

import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.trace.ServiceTypeInfo;
import com.navercorp.pinpoint.common.trace.TraceMetadataLoader;
import com.navercorp.pinpoint.common.trace.TraceMetadataProvider;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.common.util.logger.CommonLoggerFactory;

import java.util.List;

/**
 * @author emeroad
 */
public class DefaultTraceMetadataLoaderService implements TraceMetadataLoaderService {

    private final TraceMetadataLoader loader;

    public DefaultTraceMetadataLoaderService(List<TraceMetadataProvider> providers, CommonLoggerFactory commonLoggerFactory) {
        Assert.requireNonNull(commonLoggerFactory, "commonLoggerFactory must not be null");
        this.loader = new TraceMetadataLoader(commonLoggerFactory);

        Assert.requireNonNull(providers, "providers must not be null");
        loader.load(providers);

    }


    @Override
    public List<ServiceTypeInfo> getServiceTypeInfos() {
        return loader.getServiceTypeInfos();
    }

    @Override
    public List<AnnotationKey> getAnnotationKeys() {
        return loader.getAnnotationKeys();
    }


}
