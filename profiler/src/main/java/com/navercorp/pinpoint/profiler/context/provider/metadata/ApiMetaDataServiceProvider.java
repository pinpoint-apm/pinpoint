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

package com.navercorp.pinpoint.profiler.context.provider.metadata;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.profiler.context.module.MetadataDataSender;
import com.navercorp.pinpoint.profiler.metadata.DefaultApiMetaDataService;
import com.navercorp.pinpoint.profiler.metadata.ApiMetaDataService;
import com.navercorp.pinpoint.profiler.metadata.SimpleCache;
import com.navercorp.pinpoint.profiler.sender.EnhancedDataSender;

/**
 * @author Woonduk Kang(emeroad)
 */
public class ApiMetaDataServiceProvider implements Provider<ApiMetaDataService> {

    private final Provider<EnhancedDataSender<Object>> enhancedDataSenderProvider;
    private final SimpleCacheFactory simpleCacheFactory;

    @Inject
    public ApiMetaDataServiceProvider(@MetadataDataSender Provider<EnhancedDataSender<Object>> enhancedDataSenderProvider, SimpleCacheFactory simpleCacheFactory) {
        this.enhancedDataSenderProvider = Assert.requireNonNull(enhancedDataSenderProvider, "enhancedDataSenderProvider");
        this.simpleCacheFactory = Assert.requireNonNull(simpleCacheFactory, "simpleCacheFactory");

    }

    @Override
    public ApiMetaDataService get() {
        final EnhancedDataSender<Object> enhancedDataSender = this.enhancedDataSenderProvider.get();
        final SimpleCache<String> simpleCache = simpleCacheFactory.newSimpleCache();
        return new DefaultApiMetaDataService(enhancedDataSender, simpleCache);
    }
}
